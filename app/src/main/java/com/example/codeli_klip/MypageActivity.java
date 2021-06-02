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

    private TextView mypage_id;
    private static TextView mypage_email;
    private static TextView mypage_klip;

    private Button logout_button;
    private Button my_edit_bt;

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

        mypage_id=findViewById(R.id.my_name);
        mypage_email=findViewById(R.id.my_email);
        mypage_klip=findViewById(R.id.my_klip);
        chat_switch=findViewById(R.id.my_chat_switch);
        arrive_switch=findViewById(R.id.my_arrive_switch);

        mypage_email.setText(LoginActivity.email);
        mypage_id.setText(LoginActivity.nickname);
        mypage_klip.setText(LoginActivity.klip_address);

        my_edit_bt=findViewById(R.id.my_edit_bt);
        my_edit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //편집 화면 넘어가기
                Intent intent=new Intent(getApplicationContext(),MypageEditActivity.class);
                startActivity(intent);
                finish();
            }
        });


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
                        Intent intent=new Intent(getApplicationContext(),RoomPagerActivity.class);
                        intent.putExtra("position",MainActivity.select_room_num);
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
