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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    private FirebaseFirestore firestore;

    public static Integer[] owner_check_verification_num=new Integer[100];

    public static int select_room_num=0;
    public static boolean is_payment=false;

    public static String email="";

    public static RoomLIstAdapter roomLIstAdapter;
    public static ArrayList<RoomItem> roomItemArrayList = new ArrayList<RoomItem>();

    public static ArrayList<String> roomIdArrayList=new ArrayList<String>();

    public static BottomNavigationView bottomNavigationView;

    private ImageButton add_room_bt; //??? ?????? ??????

    private TextView cur_location; //?????? ?????? ??????

    public static boolean gpsCheckStart=false;

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

        firestore = FirebaseFirestore.getInstance();

        Intent loginIntent=getIntent();
        email=loginIntent.getStringExtra("email");

        ListView listView=findViewById(R.id.room_list_item);
        roomLIstAdapter=new RoomLIstAdapter(this, roomItemArrayList);
        listView.setAdapter(roomLIstAdapter);

        //room data ??????
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

                                RoomItem roomItem=null;
                                if(document.getData().get("x")!=null) {
                                    if(document.getData().get("time")!=null){//?????? ?????? ??????
                                        roomItem = new RoomItem(document.getData().get("restaurant").toString(), document.getData().get("deliveryApp").toString(), Integer.parseInt(document.getData().get("currentValue").toString()), Integer.parseInt(document.getData().get("minOrderAmount").toString()), Integer.parseInt(document.getData().get("deliveryCost").toString()), document.getData().get("deliveryAddress").toString(), document.getData().get("deliveryDetailAddress").toString(), Integer.parseInt(document.getData().get("participantsNum").toString()), Integer.parseInt(document.getData().get("participantsMax").toString()), document.getData().get("owner").toString(), document.getData().get("x").toString(), document.getData().get("y").toString(),document.getData().get("time").toString());
                                    }
                                    else{//????????? ?????? ??????
                                        roomItem = new RoomItem(document.getData().get("restaurant").toString(), document.getData().get("deliveryApp").toString(), Integer.parseInt(document.getData().get("currentValue").toString()), Integer.parseInt(document.getData().get("minOrderAmount").toString()), Integer.parseInt(document.getData().get("deliveryCost").toString()), document.getData().get("deliveryAddress").toString(), document.getData().get("deliveryDetailAddress").toString(), Integer.parseInt(document.getData().get("participantsNum").toString()), Integer.parseInt(document.getData().get("participantsMax").toString()), document.getData().get("owner").toString(), document.getData().get("x").toString(), document.getData().get("y").toString());
                                    }
                                }else{//?????? ??????
                                    roomItem= new RoomItem(document.getData().get("restaurant").toString(),document.getData().get("deliveryApp").toString(),Integer.parseInt(document.getData().get("currentValue").toString()),Integer.parseInt(document.getData().get("minOrderAmount").toString()),Integer.parseInt(document.getData().get("deliveryCost").toString()),document.getData().get("deliveryAddress").toString(),document.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(document.getData().get("participantsNum").toString()),Integer.parseInt(document.getData().get("participantsMax").toString()),document.getData().get("owner").toString());
                                }
                                roomItemArrayList.add(roomItem);
                                roomLIstAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w("Error getting documents.", task.getException());
                        }
                    }
                });

        ReadFirestoreData(); //????????? ??? ?????? ??????

        //????????? ????????? ????????? -> ????????? ??????
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), roomLIstAdapter.getItem(position).getName(),Toast.LENGTH_SHORT).show();
                //????????? ????????? ??????
                String room_id=roomIdArrayList.get(position);

                if(roomLIstAdapter.getItem(position).getOwner()!=null&&roomLIstAdapter.getItem(position).getOwner().equals(LoginActivity.nickname)) {
                    //?????? ???????????? ??????
                    select_room_num=position;
                    Intent ownerintent=new Intent(getApplicationContext(),RoomOwnerPagerActivity.class);
                    ownerintent.putExtra("position",position);
                    startActivity(ownerintent);
                }
                else{
                    //Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                    Intent intent=new Intent(getApplicationContext(),RoomPagerActivity.class);
                    intent.putExtra("position",position);
                    //owner - ?????? ?????? ??????
                    startActivity(intent);

                    //????????? ????????? ??? ?????? ??? ?????? ??????
//                    if(roomLIstAdapter.getItem(position).getCurrentPeople()<roomLIstAdapter.getItem(position).getTotalPeople()){
//                        Intent intent=new Intent(getApplicationContext(),RoomPagerActivity.class);
//                        intent.putExtra("position",position);
//                        //owner - ?????? ?????? ??????
//                        startActivity(intent);
//                    }else{
//                        Toast.makeText(getApplicationContext(), "????????? ?????????????????????", Toast.LENGTH_SHORT).show();
//                    }
                }
            }
        });

        add_room_bt=findViewById(R.id.add_room_bt);
        add_room_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??? ??????????????? ????????????
                Intent intent = new Intent(getApplicationContext(), RoomAddActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //?????? ??????(??????, ??????) ?????? ????????? ???????????? ????????? ??????
        cur_location=findViewById(R.id.location_tv);

        //??????????????? ?????? ??????
        if(checkRunTimePermission()){
            gpsTracker = new GpsTracker(MainActivity.this);

            latitude = gpsTracker.getLatitude(); // ??????
            longtitude = gpsTracker.getLongitude(); //??????
            String addr=getCurrentAddress(latitude,longtitude);
            cur_location.setText(addr);
        }


        //background service ??????
        //if(gpsCheckStart==true){
//            Handler mHandler = new Handler(Looper.getMainLooper());
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        Intent intent =new Intent(MainActivity.this, BackgroundGPS.class);
//                        System.out.println("service start");
//                        stopService(intent);
//
//                        //stopService??? ??????????????? ?????? ???????????? ??? ?????? gps ??????????????? ??????
//                        startService(intent);
//                    }catch (Exception e){
//                        Log.i("main service error", e.toString());
//                    }
//                }
//            }, 2000);
        //}



        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        //???????????? ????????????
                        Intent refresh=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(refresh);
                        roomLIstAdapter.notifyDataSetChanged();
                        finish();
                        return true;
                    case R.id.chat:
                        //??????????????? ????????????
                        //default room num=0

                        if(roomLIstAdapter.getItem(select_room_num).getOwner()!=null&&roomLIstAdapter.getItem(select_room_num).getOwner().equals(LoginActivity.nickname)) {
                            //?????? ???????????? ??????
                            Intent ownerintent=new Intent(getApplicationContext(),RoomOwnerPagerActivity.class);
                            ownerintent.putExtra("position",select_room_num);
                            startActivity(ownerintent);
                            return true;
                        }
                        else{
                            Intent intent=new Intent(getApplicationContext(),RoomPagerActivity.class);
                            intent.putExtra("position",select_room_num);
                            //owner - ?????? ?????? ??????
                            startActivity(intent);
                            return true;
                        }

                    case R.id.my:
                        Intent my_intent=new Intent(getApplicationContext(), MypageActivity.class);
                        my_intent.putExtra("email",email);
                        startActivity(my_intent);
                        //Toast.makeText(getApplicationContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }


    public String getCurrentAddress( double latitude, double longitude) {

        //????????????
        // GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(latitude, longitude, 7);

        } catch (IOException ioException) {
            //???????????? ??????
            //Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            //Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            //Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }

        Address address = addresses.get(0);
        String addr=address.getAddressLine(0);
        addr=addr.substring(5);
        //return address.getAddressLine(0).toString()+"\n";
        return addr;
    }
    //??????????????? GPS ???????????? ?????? ????????????

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkRunTimePermission(){
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            // 3.  ?????? ?????? ????????? ??? ??????
            return true;
        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(MainActivity.this, "?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return true;
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) { case GPS_ENABLE_REQUEST_CODE:
            //???????????? GPS ?????? ???????????? ??????
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {

                    Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                    return;
                }
            }
            break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;
            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(!check_result){
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            else{
                gpsTracker = new GpsTracker(MainActivity.this);
                latitude = gpsTracker.getLatitude(); // ??????
                longtitude = gpsTracker.getLongitude(); //??????
                
                String addr=getCurrentAddress(latitude,longtitude);
                cur_location.setText(addr);
            }

        }
    }


    private void ReadFirestoreData(){
        //*****Firestore  ??? ?????? ????????? ??????
        firestore.collection("Rooms")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        roomItemArrayList.clear();
                        roomIdArrayList.clear();


                        for (QueryDocumentSnapshot document : value) {
                            roomIdArrayList.add(document.getId());

                            RoomItem roomItem=null;
                            if(document.getData().get("x")!=null) {
                                    if(document.getData().get("time")!=null){//?????? ?????? ??????
                                        roomItem = new RoomItem(document.getData().get("restaurant").toString(), document.getData().get("deliveryApp").toString(), Integer.parseInt(document.getData().get("currentValue").toString()), Integer.parseInt(document.getData().get("minOrderAmount").toString()), Integer.parseInt(document.getData().get("deliveryCost").toString()), document.getData().get("deliveryAddress").toString(), document.getData().get("deliveryDetailAddress").toString(), Integer.parseInt(document.getData().get("participantsNum").toString()), Integer.parseInt(document.getData().get("participantsMax").toString()), document.getData().get("owner").toString(), document.getData().get("x").toString(), document.getData().get("y").toString(),document.getData().get("time").toString());
                                    }
                                    else{//????????? ?????? ??????
                                        roomItem = new RoomItem(document.getData().get("restaurant").toString(), document.getData().get("deliveryApp").toString(), Integer.parseInt(document.getData().get("currentValue").toString()), Integer.parseInt(document.getData().get("minOrderAmount").toString()), Integer.parseInt(document.getData().get("deliveryCost").toString()), document.getData().get("deliveryAddress").toString(), document.getData().get("deliveryDetailAddress").toString(), Integer.parseInt(document.getData().get("participantsNum").toString()), Integer.parseInt(document.getData().get("participantsMax").toString()), document.getData().get("owner").toString(), document.getData().get("x").toString(), document.getData().get("y").toString());
                                    }
                                }else{//?????? ??????
                                    roomItem= new RoomItem(document.getData().get("restaurant").toString(),document.getData().get("deliveryApp").toString(),Integer.parseInt(document.getData().get("currentValue").toString()),Integer.parseInt(document.getData().get("minOrderAmount").toString()),Integer.parseInt(document.getData().get("deliveryCost").toString()),document.getData().get("deliveryAddress").toString(),document.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(document.getData().get("participantsNum").toString()),Integer.parseInt(document.getData().get("participantsMax").toString()),document.getData().get("owner").toString());
                                }
                                roomItemArrayList.add(roomItem);
                                roomLIstAdapter.notifyDataSetChanged();

                        }
                    }
                });




    }


}
