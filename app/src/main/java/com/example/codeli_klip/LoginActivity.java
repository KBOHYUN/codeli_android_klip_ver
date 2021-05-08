package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button login_bt; //로그인
    Button signin_bt; //회원가입

    EditText id_et;
    EditText pw_et;

    public static String email;
    public static String nickname;
    public static String name;

    private String pw;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDBReference = null;
    private HashMap<String, Object> childUpdates = null;
    private Map<String, Object> userValue = null;
    UserInfo userInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth =  FirebaseAuth.getInstance();

        id_et=findViewById(R.id.login_id_et);
        pw_et=findViewById(R.id.login_pw_et);

        login_bt=findViewById(R.id.login_bt);
        login_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email=id_et.getText().toString().trim();
                pw=pw_et.getText().toString().trim();

                firebaseAuth.signInWithEmailAndPassword(email,pw)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("email",email);
                                    startActivity(intent);
                                    finish();

                                }else{
                                    Toast.makeText(LoginActivity.this,"로그인 오류",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signin_bt=findViewById(R.id.signin_bt);
        signin_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

    }
}