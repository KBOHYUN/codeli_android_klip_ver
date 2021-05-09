package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MypageActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private String my_id;
    private String my_email;
    private String my_klip;

    private TextView id;
    private TextView email;
    private TextView klip;

    private Button logout_button;

    private Switch arrive_switch;
    private Switch chat_switch;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        logout_button=findViewById(R.id.my_logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //로그아웃 후 text file 삭제
            }
        });

        id=findViewById(R.id.my_name);
        email=findViewById(R.id.my_email);
        klip=findViewById(R.id.my_klip);
        chat_switch=findViewById(R.id.my_chat_switch);
        arrive_switch=findViewById(R.id.my_arrive_switch);

        email.setText(LoginActivity.email);
        id.setText(LoginActivity.nickname);

        chat_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(getApplicationContext(), "on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "off ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        arrive_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(getApplicationContext(), "on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "off ", Toast.LENGTH_SHORT).show();
                }
            }
        });


        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.my);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        finish();
                        return true;
                    case R.id.chat:
                        //방목록으로 이동하기
                        Intent intent=new Intent(getApplicationContext(),RoomActivity.class);
                        intent.putExtra("room_id",MainActivity.roomIdArrayList.get(MainActivity.select_room_num)); //room id
                        intent.putExtra("name",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getName()); //가게 이름
                        intent.putExtra("platform",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getPlatform()); //플랫폼
                        intent.putExtra("order_price",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getOrderPrice());//최소주문금액
                        intent.putExtra("delivery_price",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getDeliveryPrice());//배달 금액
                        intent.putExtra("address",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getAddress());//배달 주소
                        intent.putExtra("cur_people",MainActivity.roomLIstAdapter.getItem(MainActivity.select_room_num).getCurrentPeople()); //현재 인원
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.my:
                        return true;
                }
                return false;
            }
        });
    }
}
