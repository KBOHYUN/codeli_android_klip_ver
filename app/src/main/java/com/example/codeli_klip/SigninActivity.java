package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private static String user_uid="";

    private FirebaseAuth firebaseAuth;

    private Button sign_bt; //회원가입 버튼
    private EditText name; //이름
    private EditText nickname; //닉네임
    private EditText pw; //비밀번호
    private EditText email; //이메

    private String google_email;
    private String google_name;
    private String name_result;
    private String nickname_result;
    private String pw_result;
    private String email_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        name=findViewById(R.id.sign_name);
        nickname=findViewById(R.id.sign_nickname);
        pw=findViewById(R.id.sign_password);
        email=findViewById(R.id.sign_email);

        firebaseAuth= FirebaseAuth.getInstance();

        Intent data=getIntent();
        google_name=data.getStringExtra("name");
        google_email=data.getStringExtra("email");
        name.setText(google_name);
        email.setText(google_email);

        LoginActivity.email=google_email;

        File saveFile = new File(getFilesDir() + "/user.txt"); // 저장 경로
        if(saveFile.exists()){
            //로그인 정보가 있는 경우 -> 자동로그인
            try {
                BufferedReader buf = new BufferedReader(new FileReader(saveFile));
                LoginActivity.name=buf.readLine();
                LoginActivity.nickname=buf.readLine();
                //LoginActivity.email=buf.readLine();
                buf.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        //회원가입 버튼 클릭
        sign_bt=findViewById(R.id.sign_bt);
        sign_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입 데이터 서버로 전송 - 널값인지 확인후 에러메세지 처리
                name_result=name.getText().toString();
                nickname_result=nickname.getText().toString();
                //pw_result=pw.getText().toString();
                email_result=email.getText().toString(); //이메일 형식 맞는지 확인

                LoginActivity.nickname=nickname_result;
                LoginActivity.email=google_email;

                try{
                    //파일에 쓰기
                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "/user.txt",true));
                    bw.write(name_result);
                    bw.newLine();
                    bw.write(nickname_result);
                    bw.newLine();
                    bw.write(email_result);
                    bw.close();
                    System.out.println("파일쓰기 완료");
                }catch(IOException e){
                    e.printStackTrace();
                }


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference mDBReference=database.getReference();

                HashMap<String,Object> childUpdates =new HashMap<>();
                Map<String, Object> userValue = null;

                UserInfo userinfo=new UserInfo(name_result,nickname_result,email_result);
                userValue=userinfo.toMap();

                childUpdates.put("/User_info/" + nickname_result, userValue); //id값에 이메일 안됨
                mDBReference.updateChildren(childUpdates);

                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();

            }
        });


    }
}