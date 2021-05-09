package com.example.codeli_klip;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static int select_room_num=0;
    public static boolean is_payment=false;

    public static String email="";

    public static RoomLIstAdapter roomLIstAdapter;
    public static ArrayList<RoomItem> roomItemArrayList = new ArrayList<RoomItem>();

    public static ArrayList<String> roomIdArrayList=new ArrayList<String>();

    private BottomNavigationView bottomNavigationView;

    private ImageButton add_room_bt; //방 추가 버튼

    private TextView cur_location; //현재 위치 주소

    private GpsTracker gpsTracker;
    private double latitude;
    private double longtitude;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent loginIntent=getIntent();
        email=loginIntent.getStringExtra("email");

        ListView listView=findViewById(R.id.room_list_item);
        roomLIstAdapter=new RoomLIstAdapter(this, roomItemArrayList);
        listView.setAdapter(roomLIstAdapter);

        //room data 수신
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Rooms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            roomItemArrayList.clear();
                            roomIdArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                roomIdArrayList.add(document.getId());
                                RoomItem roomItem= new RoomItem(document.getData().get("restaurant").toString(),document.getData().get("deliveryApp").toString(),Integer.parseInt(document.getData().get("currentValue").toString()),Integer.parseInt(document.getData().get("minOrderAmount").toString()),Integer.parseInt(document.getData().get("deliveryCost").toString()),document.getData().get("deliveryAddress").toString(),document.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(document.getData().get("participantsNum").toString()),Integer.parseInt(document.getData().get("participantsMax").toString()));
                                roomItemArrayList.add(roomItem);
                                roomLIstAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w("Error getting documents.", task.getException());
                        }
                    }
                });


        //리스트 아이템 클릭스 -> 방으로 이동
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), roomLIstAdapter.getItem(position).getName(),Toast.LENGTH_SHORT).show();
                //선택한 방으로 이동
                String room_id=roomIdArrayList.get(position);
                Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                intent.putExtra("room_id",room_id); //room id
                intent.putExtra("name",roomLIstAdapter.getItem(position).getName()); //가게 이름
                intent.putExtra("platform",roomLIstAdapter.getItem(position).getPlatform()); //플랫폼
                intent.putExtra("order_price",roomLIstAdapter.getItem(position).getOrderPrice());//최소주문금액
                intent.putExtra("delivery_price",roomLIstAdapter.getItem(position).getDeliveryPrice());//배달 금액
                intent.putExtra("address",roomLIstAdapter.getItem(position).getAddress());//배달 주소
                intent.putExtra("specific_address",roomLIstAdapter.getItem(position).getSpecificAddress());//세부주소
                intent.putExtra("cur_people",roomLIstAdapter.getItem(position).getCurrentPeople()); //현재 인원

                startActivity(intent);
            }
        });

        add_room_bt=findViewById(R.id.add_room_bt);
        add_room_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방 추가하기로 넘어가기
                Intent intent = new Intent(getApplicationContext(), RoomAddActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //현재 위치(위도, 경도) 찾아 주소로 변환하여 텍스트 출력
        cur_location=findViewById(R.id.location_tv);
        gpsTracker = new GpsTracker(MainActivity.this);

        //위치서비스 상태 확인
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();

        }else {
            if(checkRunTimePermission()){
                latitude = gpsTracker.getLatitude(); // 위도
                longtitude = gpsTracker.getLongitude(); //경도
                String addr=getCurrentAddress(latitude,longtitude);
                cur_location.setText(addr);
            }
        }

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        //현재화면 새로고침
                        Intent refresh=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(refresh);
                        roomLIstAdapter.notifyDataSetChanged();
                        finish();
                        return true;
                    case R.id.chat:
                        //방목록으로 이동하기
                        //default room num=0
                        Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                        intent.putExtra("room_id",roomIdArrayList.get(select_room_num)); //room id
                        intent.putExtra("name",roomLIstAdapter.getItem(select_room_num).getName()); //가게 이름
                        intent.putExtra("platform",roomLIstAdapter.getItem(select_room_num).getPlatform()); //플랫폼
                        intent.putExtra("order_price",roomLIstAdapter.getItem(select_room_num).getOrderPrice());//최소주문금액
                        intent.putExtra("delivery_price",roomLIstAdapter.getItem(select_room_num).getDeliveryPrice());//배달 금액
                        intent.putExtra("address",roomLIstAdapter.getItem(select_room_num).getAddress());//배달 주소
                        intent.putExtra("cur_people",roomLIstAdapter.getItem(select_room_num).getCurrentPeople()); //현재 인원
                        startActivity(intent);
                        return true;
                    case R.id.my:
                        Intent my_intent=new Intent(getApplicationContext(), MypageActivity.class);
                        my_intent.putExtra("email",email);
                        startActivity(my_intent);
                        Toast.makeText(getApplicationContext(), "마이 페이지로 이동", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더
        // GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(latitude, longitude, 7);

        } catch (IOException ioException) {
            //네트워크 문제
            //Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        String addr=address.getAddressLine(0);
        addr=addr.substring(5);
        //return address.getAddressLine(0).toString()+"\n";
        return addr;
    }
    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스"); builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 확인해 주세요");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
            return true;
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) { case GPS_ENABLE_REQUEST_CODE:
            //사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {
                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                    return;
                }
            }
            break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(!check_result){
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }


}
