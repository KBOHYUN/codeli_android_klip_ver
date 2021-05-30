package com.example.codeli_klip;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText address_et;
    private Button search_button;
    private Button ok_button;

    private TextView result_address;

    private Geocoder geocoder;

    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        address_et=findViewById(R.id.map_et);
        search_button=findViewById(R.id.map_search_button);
        ok_button=findViewById(R.id.map_ok_button);
        result_address=findViewById(R.id.map_result_address);

        geocoder = new Geocoder(this);

        //결과 주소 보내
        ok_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear(); //지도 초기

                String str=address_et.getText().toString();
                List<Address> addressList = null;
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(str, // 주소
                            10); // 최대 검색 결과 개수
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(addressList.get(0).toString());
                // 콤마를 기준으로 split
                String []splitStr = addressList.get(0).toString().split(",");
                address = splitStr[0].substring(splitStr[0].indexOf("\"") + 6,splitStr[0].length() - 2); // 주소
                System.out.println(address);

                RoomAddActivity.meeting_address=address;

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                //System.out.println(latitude);
                //System.out.println(longitude);

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                RoomAddActivity.meeting_latitude=Double.parseDouble(latitude);
                RoomAddActivity.meeting_longtitude=Double.parseDouble(longitude);

                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("검색 결과");
                mOptions2.snippet(address);
                result_address.setText(address);
                mOptions2.position(point);
                // 마커 추가
                mMap.addMarker(mOptions2);
                // 해당 좌표로 화면 줌
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //현재 위치로 받아오기
        LatLng cau = new LatLng(MainActivity.latitude, MainActivity.longtitude);
        mMap.addMarker(new MarkerOptions().position(cau).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cau));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cau,16));

    }
}