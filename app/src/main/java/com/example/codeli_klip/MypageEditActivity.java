package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MypageEditActivity extends AppCompatActivity {

    private Switch arrive_switch;
    private Switch chat_switch;

    private EditText my_name;
    private EditText my_email;
    private EditText my_klip;

    private Button cancel_bt;
    private Button ok_bt;

    private String name;
    private String email;
    private String klip;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage_edit);

        cancel_bt=findViewById(R.id.my_cancel_bt);
        ok_bt=findViewById(R.id.my_ok_bt);

        cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        my_name=findViewById(R.id.my_name);
        my_email=findViewById(R.id.my_email);
        my_klip=findViewById(R.id.my_klip);

        my_name.setText(LoginActivity.nickname);
        my_email.setText(LoginActivity.email);
        my_klip.setText(LoginActivity.klip_address);


        ok_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "수정이 완료되었습니다", Toast.LENGTH_SHORT).show();
                name=my_name.getText().toString();
                email=my_email.getText().toString();
                klip=my_klip.getText().toString();

                LoginActivity.nickname=name;
                LoginActivity.email=email;
                LoginActivity.klip_address=klip;

                String root=getFilesDir() + "/user.txt";
                File file=new File(root);
                if(file.exists()){
                    file.delete();

                    try{
                        //파일에 쓰기
                        BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "/user.txt",true));
                        bw.write(name);
                        bw.newLine();
                        bw.write(email);
                        bw.newLine();
                        //bw.write(klip);
                        bw.close();
                        System.out.println("파일쓰기 완료");
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                Intent intent=new Intent(getApplicationContext(), MypageActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }
}