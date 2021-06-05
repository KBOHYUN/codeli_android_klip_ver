package com.example.codeli_klip;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BackgroundGPS extends Service implements LocationListener {
    private String TAG = "BackgroundGPS";
    private String sPackageName = "sundosoft.co.eco";

    private final IBinder mBinder = new LocalBinder();
    int iLoopValue = 0;

    int iThreadInterval = 600000;    // 쓰레드 루프 간격 5초
    boolean bThreadGo = true;        // 루프로직을 태운다.


    public static LocationManager locationMgr;
    String sBestGpsProvider = "";

    private String getTime; //현재 시간을 format에 맞춰 가져오기
    private long now; //현재 시간
    private Timestamp mDate;
    private SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //저장할 날짜, 시간 형식

    private double longtitude=0, latitude=0; //latitude-위도, longtitude-경도

    public static ArrayList<LocationInfo> location_list=new ArrayList<LocationInfo>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chat_user_Ref;
    //'partition'노드 참조객체 변수
    private DatabaseReference partitionRef;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private MyItem item;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "onStart : 서비스 시작");
        super.onStart(intent, startId);

        //***********   pos 변화시키기!!!!!!!!

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설정
        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+PayActivity.roomPosition+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        bThreadGo = true;

        locationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        sBestGpsProvider = LocationManager.NETWORK_PROVIDER; // 강제로 위성으로 지정 ->네트워크로 지정

        setGpsPosition(); // 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationMgr.requestLocationUpdates(sBestGpsProvider, 5000, 0, this);// 10초마다체크(10000),0미터,리스너위치
        setGpsPosition(); //기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정

        //서비스 시작시 현재 위치 저장
        now = System.currentTimeMillis();
        mDate = new Timestamp(now);
        getTime = simpleDate.format(mDate);

        if(latitude!=0&&longtitude!=0){
            location_list.add(new LocationInfo(getTime, latitude,longtitude));
        }




        //쓰레드 실행
        new Thread(mRun).start();
    }

    //서비스 종료
    @Override
    public void onDestroy() {
            try {
                Log.i(TAG, "onDestroy : 서비스 종료");

                bThreadGo = false;

                if (this != null && locationMgr != null) {
                    locationMgr.removeUpdates(this);
                }

                //종료 시에도 위경도 업데이트 후 종료

                item=RoomInfoFragment.my_menu_item;

                //item=RoomInfoFragment.my_menu_item;
                item.setX(latitude);
                item.setY(longtitude);
                chat_user_Ref.setValue(item);

                TAG = null;
                sBestGpsProvider = null;
                locationMgr = null;
                mRun = null;
            } catch (Exception e) {
                Log.i(TAG, ">onDestroy : " + e.toString());
            }

            super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //강제종료시

    }

    /*
     * @위치가 변경될때마다 호출
     */
    public void onLocationChanged(Location location) {
        try {
            //Log.i(TAG, ">onLocationChanged : 위치가 변경되었을 경우");

            // 위치를 저장
            positionSaveProc();

        } catch (Exception e) {
            Log.i(TAG, ">onLocationChanged : " + e.toString());
        }
    }

    public void onProviderDisabled(String provider) {
        ;
    }

    public void onProviderEnabled(String provider) {
        ;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        ;
    }

    //액티비티와 통신하기 위한 부분 START

    public class LocalBinder extends Binder {
        BackgroundGPS getService() {
            return BackgroundGPS.this;
        }
    }

    public interface ICallback {
        ;
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    //액티비티와 통신하기 위한 부분 END
    Runnable mRun = new Runnable() {
        public void run() {
            try {
                while (bThreadGo) {
                    Log.i(TAG, ">mRun");

                    iLoopValue++;
                    Thread.sleep(iThreadInterval);
                    if (iLoopValue > 600000)  //900000 = 15분
                        iLoopValue = 0;

                    //위경도 전송
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 위치를 저장
                            positionSaveProc();


                            //realtime db에 실시간 gps 전송
                            //payactivity에서 호출할 경우
                            MyItem item=PayActivity.item;

                            if(RoomInfoFragment.my_menu_item.getVerification_status()==true){
                                item=RoomInfoFragment.my_menu_item;
                            }

                            //roomactivity에서 호출할 경우
                            //item=RoomInfoFragment.my_menu_item;
                            item.setX(latitude);
                            item.setY(longtitude);
                            chat_user_Ref.setValue(item);

                        }

                    }, 0);

                    //'partitions'노드에 저장되어 있는 데이터들을 읽어오기
                    chat_user_Ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                            //데이터가 쌓이기 때문에  clear()
                            peopleItemArrayList.clear();


                            item = dataSnapshot.getValue(MyItem.class);

                            if(item!=null){
                                //System.out.println("***백그라운드 my item 읽기: "+item.getId()+" "+item.getVerification_status());
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });



                    //변경된 위치 내부저장소 파일에 저장
                    //현재 시간
                    if(bThreadGo==true){
                        //서비스 시작시 gps 파일 생성 후 현재 위치 저장
                        now = System.currentTimeMillis();
                        mDate = new Timestamp(now);
                        getTime = simpleDate.format(mDate);
                        System.out.println("*****백그라운드 서비스 : "+getTime+ " "+latitude+" " +longtitude);
                        location_list.add(new LocationInfo(getTime, latitude,longtitude));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * 변경된 위치 저장
     */
    public synchronized void positionSaveProc() {
        try {
            //Log.i(TAG, ">positionSaveProc : 변경된 위치 저장");

            double dLatitude = 0;
            double dLongitude = 0;
            if (sBestGpsProvider != null && locationMgr != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lcPosition = locationMgr.getLastKnownLocation(sBestGpsProvider);
                if (lcPosition != null) {
                    dLatitude = lcPosition.getLatitude(); //위도
                    dLongitude = lcPosition.getLongitude(); //경도
                    //Log.i(TAG, ">positionSaveProc : lat(" + dLatitude + "), lot(" + dLongitude + ")");
                    latitude=dLatitude;
                    longtitude=dLongitude;

                    //Log.i(TAG, ">positionSaveProc : "+latitude +" "+longtitude);

                    if (dLatitude != 0 && dLongitude != 0) {
                        setSharePreferenceFloatValue("dUserContactLatitude", (float) dLatitude);
                        setSharePreferenceFloatValue("dUserContactLongitude", (float) dLongitude);

                        setLocationProvider("NETWORK");        //값을 가져오니 위성으로 설정 -> network
                    } else {
                        setLocationProvider("GPS");    //실내이어서 위성으로 못가져올 가능성이 커서 네트워크로 설정 -> gps
                    }
                } else {
                    //Log.i(TAG, ">positionSaveProc : 널이어서 위치값이 없는 경우");
                    setLocationProvider("GPS");        //실내이어서 위성으로 못가져올 가능성이 커서 네트워크로 설정 -> gps
                }
            }
        } catch (Exception e) {
            Log.i(TAG, ">positionSaveProc : " + e.toString());
        }
    }

    /*
     * 위치가져가는 방법을 변경
     */
    public synchronized void setLocationProvider(String parmOption) {
        if (locationMgr == null)
            return;
        if (parmOption.equals("NETWORK")) {
            //Log.i(TAG, ">setLocationProvider sBestGpsProvider : " + sBestGpsProvider);
            setGpsPosition(); // 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정
            sBestGpsProvider = LocationManager.NETWORK_PROVIDER; // 강제로 네트워크로 지정
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationMgr.requestLocationUpdates(sBestGpsProvider, 10000, 0, this);// 10초마다체크(1000),0미터,리스너위치
            setGpsPosition(); // 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정
        } else if (parmOption.equals("GPS")) {
            //Log.i(TAG, ">setLocationProvider sBestGpsProvider : " + sBestGpsProvider);
            sBestGpsProvider = LocationManager.GPS_PROVIDER; // 강제로 위성으로 지정
            locationMgr.requestLocationUpdates(sBestGpsProvider, 10000, 0, this);// 10초마다체크(1000),0미터,리스너위치
        }
    }

    /*
     * GPS 위치를 셋팅 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정
     */
    public synchronized void setGpsPosition() {
        try {
            //Log.i(TAG, ">setGpsPosition : 위치 셋팅");
            if (locationMgr == null)
                return;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location lcPosition = locationMgr.getLastKnownLocation(sBestGpsProvider);
            if (lcPosition != null) {
                //Log.i(TAG, ">setGpsPosition : lat(" + lcPosition.getLatitude() + "), lot(" + lcPosition.getLongitude() + ")");
                setSharePreferenceFloatValue("dUserContactLatitude", (float) lcPosition.getLatitude());
                setSharePreferenceFloatValue("dUserContactLongitude",(float) lcPosition.getLongitude());

                latitude=lcPosition.getLatitude();
                longtitude=lcPosition.getLongitude();
                //Log.i(TAG, ">setGpsPosition : "+latitude +" "+longtitude);

            } else {
                Log.i(TAG, ">setGpsPosition : 널이어서 위치값이 없는 경우");
                sBestGpsProvider=LocationManager.GPS_PROVIDER;
                lcPosition = locationMgr.getLastKnownLocation(sBestGpsProvider);
                latitude=lcPosition.getLatitude();
                longtitude=lcPosition.getLongitude();
                //Log.i(TAG, ">setGpsPosition - gps_provider: "+latitude +" "+longtitude);
            }
        } catch (Exception e) {
            Log.i(TAG, ">setGpsPosition : error : " + e.toString());
        }
    }

    /*
     * @xml정보저장(float)
     * @parmName : 저장키
     * @parmValue : 저장키의 값
     */
    public synchronized void setSharePreferenceFloatValue(String parmName, float parmValue) {
        try {
            SharedPreferences spSvc = getApplicationContext().getSharedPreferences(sPackageName, MODE_PRIVATE);
            Editor ed = spSvc.edit();
            ed.putFloat(parmName, parmValue);
            ed.commit();
            spSvc = null;
        } catch (Exception e) {
            Log.i(TAG, ">setSharePreferenceStringValue error : " + e.toString());
        }
    }

    //텍스트에 저장한 gps값 한줄씩 읽어서 시간, 위도, 경도로 나누기
    private void splitGpsFile(String line){
        String[] splitLine=line.split(",");
        location_list.add(new LocationInfo(splitLine[0], Double.parseDouble(splitLine[1]),Double.parseDouble(splitLine[2])));
        System.out.println("split line: "+splitLine[0]+" "+splitLine[1]+" "+splitLine[2]);
        return;
    }

    public double getLongtitude(){
        return longtitude;
    }

    public double getLatitude(){
        return latitude;
    }

}

class LocationInfo{
    private String location_date="";
    private double location_lat=0;
    private double location_lon=0;

    LocationInfo(String date, double lat, double lon){
        this.location_date=date;
        this.location_lat=lat;
        this.location_lon=lon;
    }

    String getLocation_date(){
        return location_date;
    }
    double getLocation_lat(){
        return location_lat;
    }
    double getLocation_lon(){
        return location_lon;
    }

}