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
import java.util.Locale;

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


        //지도 검색 시 주소
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
                System.out.println("************latitude: "+latitude);
                System.out.println("************longitude: "+longitude);

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                RoomAddActivity.meeting_latitude=Double.parseDouble(latitude);
                RoomAddActivity.meeting_longtitude=Double.parseDouble(longitude);

                System.out.println("************meeting latitude: "+RoomAddActivity.meeting_latitude);
                System.out.println("************meeting longitude: "+RoomAddActivity.meeting_longtitude);

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

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("");
                mMap.clear(); //지도 초기화

                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude + ", " + longitude);
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                googleMap.addMarker(mOptions);

                String address_geocoder=getCurrentAddress(latitude,longitude);
                result_address.setText(address_geocoder);

                RoomAddActivity.meeting_address=address_geocoder;
                RoomAddActivity.meeting_latitude=latitude;
                RoomAddActivity.meeting_longtitude=longitude;

            }
        });

        // Add a marker in Sydney and move the camera
        //현재 위치로 받아오기
        LatLng cau = new LatLng(MainActivity.latitude, MainActivity.longtitude);
        mMap.addMarker(new MarkerOptions().position(cau).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cau));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cau,16));

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
}