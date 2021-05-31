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
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
    public static double latitude;
    public static double longtitude;
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

                                RoomItem roomItem= new RoomItem(document.getData().get("restaurant").toString(),document.getData().get("deliveryApp").toString(),Integer.parseInt(document.getData().get("currentValue").toString()),Integer.parseInt(document.getData().get("minOrderAmount").toString()),Integer.parseInt(document.getData().get("deliveryCost").toString()),document.getData().get("deliveryAddress").toString(),document.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(document.getData().get("participantsNum").toString()),Integer.parseInt(document.getData().get("participantsMax").toString()),document.getData().get("owner").toString());
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

                if(roomLIstAdapter.getItem(position).getOwner()!=null&&roomLIstAdapter.getItem(position).getOwner().equals(LoginActivity.nickname)) {
                    //방장 페이지로 이동
                    select_room_num=position;
                    Intent ownerintent=new Intent(getApplicationContext(),RoomOwnerActivity.class);
                    ownerintent.putExtra("position",position);
//                    ownerintent.putExtra("room_id",room_id); //room id
//                    ownerintent.putExtra("name",roomLIstAdapter.getItem(position).getName()); //가게 이름
//                    ownerintent.putExtra("platform",roomLIstAdapter.getItem(position).getPlatform()); //플랫폼
//                    ownerintent.putExtra("order_price",roomLIstAdapter.getItem(position).getOrderPrice());//최소주문금액
//                    ownerintent.putExtra("delivery_price",roomLIstAdapter.getItem(position).getDeliveryPrice());//배달 금액
//                    ownerintent.putExtra("address",roomLIstAdapter.getItem(position).getAddress());//배달 주소
//                    ownerintent.putExtra("specific_address",roomLIstAdapter.getItem(position).getSpecificAddress());//세부주소
//                    ownerintent.putExtra("cur_people",roomLIstAdapter.getItem(position).getCurrentPeople()); //현재 인원
                    startActivity(ownerintent);
                }
                else{
                    Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                    intent.putExtra("position",position);
                    //owner - 방장 정보 추가
                    startActivity(intent);
                }
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

        //위치서비스 상태 확인
        if(checkRunTimePermission()){
            gpsTracker = new GpsTracker(MainActivity.this);

            latitude = gpsTracker.getLatitude(); // 위도
            longtitude = gpsTracker.getLongitude(); //경도
            String addr=getCurrentAddress(latitude,longtitude);
            cur_location.setText(addr);
        }


        //background service 시작
//        Handler mHandler = new Handler(Looper.getMainLooper());
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    Intent intent =new Intent(MainActivity.this, BackgroundGPS.class);
//                    System.out.println("service start");
//                    stopService(intent);
//
//                    //stopService를 주석처리할 경우 강제종료 할 때만 gps 백그라운드 종료
//                    startService(intent);
//                }catch (Exception e){
//                    Log.i("main service error", e.toString());
//                }
//            }
//        }, 2000);


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

                        if(roomLIstAdapter.getItem(select_room_num).getOwner()!=null&&roomLIstAdapter.getItem(select_room_num).getOwner().equals(LoginActivity.nickname)) {
                            //방장 페이지로 이동
                            Intent ownerintent=new Intent(getApplicationContext(),RoomOwnerActivity.class);
                            ownerintent.putExtra("position",select_room_num);
                            startActivity(ownerintent);
                            return true;
                        }
                        else{
                            Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                            intent.putExtra("position",select_room_num);
                            //owner - 방장 정보 추가
                            startActivity(intent);
                            return true;
                        }

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
                Toast.makeText(MainActivity.this, "앱을 사용하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
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
            else{
                gpsTracker = new GpsTracker(MainActivity.this);
                latitude = gpsTracker.getLatitude(); // 위도
                longtitude = gpsTracker.getLongitude(); //경도
                String addr=getCurrentAddress(latitude,longtitude);
                cur_location.setText(addr);
            }

        }
    }


}
