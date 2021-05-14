package com.example.codeli_klip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeli_klip.actions.KlipAction;
import com.example.codeli_klip.util.JsonHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klipwallet.app2app.api.Klip;
import com.klipwallet.app2app.api.KlipCallback;
import com.klipwallet.app2app.api.request.AuthRequest;
import com.klipwallet.app2app.api.request.KlayTxRequest;
import com.klipwallet.app2app.api.request.model.BAppInfo;
import com.klipwallet.app2app.api.response.KlipErrorResponse;
import com.klipwallet.app2app.api.response.KlipResponse;
import com.klipwallet.app2app.api.response.model.KlipResult;
import com.klipwallet.app2app.exception.KlipRequestException;

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
    private EditText sign_klip; //클립 주

    private Button sign_requst_button;
    private Button sign_result_button;

    private String google_email;
    private String google_name;
    private String name_result;
    private String nickname_result;
    private String pw_result;
    private String email_result;

    private Context ctx;
    private KlipAction klipAction;

    private Klip klip;

    private String requestKey;
    private String userAddress;
    private String expiration_time;
    private String txHash;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        name=findViewById(R.id.sign_name);
        nickname=findViewById(R.id.sign_nickname);
        email=findViewById(R.id.sign_email);
        sign_klip=findViewById(R.id.sign_klip);
        sign_requst_button=findViewById(R.id.sign_requst_button);
        sign_result_button=findViewById(R.id.sign_result_button);

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
                //LoginActivity.klip_address=buf.readLine();
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

        ctx = this;
        klipAction = new KlipAction(ctx, Klip.getInstance(ctx));

        klip = Klip.getInstance(this);

        // BApp 정보
        BAppInfo bAppInfo = new BAppInfo("Codeli");
        // Auth 정보
        AuthRequest req = new AuthRequest();
        try {
            klip.prepare(req, bAppInfo, klipCallback);
        } catch (KlipRequestException e) {
            e.printStackTrace();
        }

        sign_requst_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클립주소 요청
                klipAction.request(requestKey);
            }
        });
        sign_result_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클립주소 결과
                klipAction.getResult(requestKey,klipCallback); //결과 요청
            }
        });

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
                //LoginActivity.klip_address=userAddress;

                try{
                    //파일에 쓰기
                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "/user.txt",true));
                    bw.write(name_result);
                    bw.newLine();
                    bw.write(nickname_result);
                    bw.newLine();
                    //bw.write(userAddress);
                    bw.close();
                    System.out.println("파일쓰기 완료");
                }catch(IOException e){
                    e.printStackTrace();
                }


//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference mDBReference=database.getReference();
//
//                HashMap<String,Object> childUpdates =new HashMap<>();
//                Map<String, Object> userValue = null;
//
//                UserInfo userinfo=new UserInfo(name_result,nickname_result,email_result);
//                userValue=userinfo.toMap();
//
//                childUpdates.put("/User_info/" + nickname_result, userValue); //id값에 이메일 안됨
//                mDBReference.updateChildren(childUpdates);

                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }
    private KlipCallback klipCallback = new KlipCallback<KlipResponse>() {
        @Override
        public void onSuccess(final KlipResponse res) {
            String out = JsonHelper.toPrettyFormat(res.toString());
            result=out;
            //resView.setText(out);

            // save request key
            String resultKey = res.getRequestKey();
            if (resultKey != null && userAddress==null){
                System.out.println("******request key: "+resultKey);
                requestKey = resultKey;
            }

            // save user address
            KlipResult result = res.getResult();
            if (result != null && res.getStatus().equals("completed")) {
                userAddress = result.getKlaytnAddress();
                sign_klip.setText(userAddress);
                System.out.println("*****result 성공 - user klip address : "+userAddress);

            }

        }
        @Override
        public void onFail(final KlipErrorResponse res) {
            //resView.setText(res.toString());
            Toast.makeText(getApplicationContext(), "결제에 실패하였습니다 - "+res.getErrorMsg(), Toast.LENGTH_SHORT).show();
            // reset request key
            requestKey = null;
        }
    };

}