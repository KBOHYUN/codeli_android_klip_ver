package com.example.codeli_klip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codeli_klip.actions.KlipAction;
import com.example.codeli_klip.util.JsonHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.snapshot.DoubleNode;
import com.klipwallet.app2app.api.Klip;
import com.klipwallet.app2app.api.KlipCallback;
import com.klipwallet.app2app.api.request.AuthRequest;
import com.klipwallet.app2app.api.request.KlayTxRequest;
import com.klipwallet.app2app.api.request.model.BAppInfo;
import com.klipwallet.app2app.api.response.KlipErrorResponse;
import com.klipwallet.app2app.api.response.KlipResponse;
import com.klipwallet.app2app.api.response.model.KlipResult;
import com.klipwallet.app2app.exception.KlipRequestException;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PayActivity extends AppCompatActivity {

    Button pay_button; //결제하기 버튼
    Button result_button; //결제요청 버

    TextView pay_price;
    TextView pay_total_price;
    TextView pay_total_price_klay;
    TextView pay_klay_unit;

    private int menu_price;
    private int delivery_price;
    private int total_price=0;
    private String room_id;
    private int room_position=0;
    private double klay_flow=2690.0;
    private double total_klay=0.0;
    private double total_klay_6=0.0001;

    private Context ctx;
    private KlipAction klipAction;

    private Klip klip;

    private String requestKey;
    private String userAddress;
    private String expiration_time;
    private String txHash;
    private String result;

    private MyItem my_data_peopleitem;

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chat_user_Ref;

    private int pos;

    public static int roomPosition=0;


    public static MyItem item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        pay_price=findViewById(R.id.pay_price);
        pay_total_price=findViewById(R.id.pay_total_price);
        pay_total_price_klay=findViewById(R.id.pay_total_price_klay);
        pay_klay_unit=findViewById(R.id.pay_klay_unit);

        Intent getIntent=getIntent();
        menu_price=getIntent.getIntExtra("menu_price",0);
        delivery_price=getIntent.getIntExtra("delivery_price",0);
        total_price=menu_price+delivery_price;

        pos=getIntent.getIntExtra("position",0);
        roomPosition=pos;

        //my_data_peopleitem=(MyItem) getIntent.getSerializableExtra("my_menu_item");
        item=(MyItem) getIntent.getSerializableExtra("my_menu_item");


        pay_price.setText("음식가격 "+menu_price+" + 배달팁 "+delivery_price);
        pay_total_price.setText("총 금액 "+total_price+"원");

        klay_flow=getIntent.getDoubleExtra("klay_flow",1.0);

        //total_klay=total_price/klay_flow;
        total_klay_6=getIntent.getDoubleExtra("klay_total",1.0);
        Date today = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String current_time=format1.format(today);

        pay_total_price_klay.setText("총 "+total_klay_6+" KLAY");
        pay_klay_unit.setText("(1KLAY=₩"+klay_flow+", "+current_time+" 기준)");

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설정

        ctx = this;
        klipAction = new KlipAction(ctx, Klip.getInstance(ctx));

        klip = Klip.getInstance(this);

        // BApp 정보
        BAppInfo bAppInfo = new BAppInfo("Codeli");
        // Auth 정보
        //AuthRequest req = new AuthRequest();

        KlayTxRequest req = new KlayTxRequest.Builder()
                .to("0x697e67f7767558dcc8ffee7999e05807da45002d") //서버 클립 주소..?
                .amount(""+total_klay_6)
                //.amount("0.00001")
                .build();
        try {
            klip.prepare(req, bAppInfo, klipCallback);
        } catch (KlipRequestException e) {
            e.printStackTrace();
        }

        //서버에서 값 받아서 각 텍스트에 넣기
        pay_button=findViewById(R.id.pay_button);
        pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //결제하기

                //klipAction.prepareLink(klipCallback);

                klipAction.request(requestKey);

                pay_button.setVisibility(View.INVISIBLE);
                result_button.setVisibility(View.VISIBLE);
            }
        });

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+pos+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        result_button=findViewById(R.id.result_button);
        result_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //결제하기 - 성공시
                klipAction.getResult(requestKey,klipCallback); //결과 요청

                Toast.makeText(getApplicationContext(), "결제가 완료되었습니다", Toast.LENGTH_SHORT).show();
                MainActivity.select_room_num=room_position;
                MainActivity.is_payment=true;

                //my_data_peopleitem.setExpiration_time(Integer.parseInt(expiration_time));
               // Toast.makeText(getApplicationContext(), "tx_hash"+txHash, Toast.LENGTH_SHORT).show();
//                if(txHash!=null){
//                    my_data_peopleitem.setTx_hash(txHash);
//                }
//                chat_user_Ref.setValue(my_data_peopleitem);


                finish(); //종료

            }
        });

    }

    private KlipCallback klipCallback = new KlipCallback<KlipResponse>() {
        @Override
        public void onSuccess(final KlipResponse res) {
            String out = JsonHelper.toPrettyFormat(res.toString());
            result=out;
            //resView.setText(out);

            expiration_time=res.getExpirationTime();
            if(expiration_time!=null){
                System.out.println("****** expiration time: "+expiration_time);
            }
            // save request key
            String resultKey = res.getRequestKey();
            if (resultKey != null && userAddress==null){
                System.out.println("******request key: "+resultKey);
                requestKey = resultKey;

                //klipAction.request(requestKey);
            }

            // save user address
            KlipResult result = res.getResult();
            if (result != null && res.getStatus().equals("completed")) {
                //userAddress = result.getKlaytnAddress(); ->auth 사용
                txHash=result.getTxHash();

                //my_data_peopleitem.setSending_status("prepared");
                //my_data_peopleitem.setTx_hash(txHash);


                //item=new MyItem(my_data_peopleitem.getId(),my_data_peopleitem.getStatus(),my_data_peopleitem.getMenu_name(),my_data_peopleitem.getMenu_price(),my_data_peopleitem.getExpiration_time(),txHash,"success",my_data_peopleitem.getVerification_status());
                
                item=new MyItem(item.getId(),item.getStatus(),item.getMenu_name(),item.getMenu_price(),item.getExpiration_time(),txHash,"success",item.getVerification_status(),item.getLocation_verification_status());
                chat_user_Ref.setValue(item);
                txHash="";


                Toast.makeText(getApplicationContext(), "결제가 완료되었습니다", Toast.LENGTH_SHORT).show();
                System.out.println("*****result 성공 - tx hash: "+txHash);
                //System.out.println("*****result 성공 - user klip address : "+userAddress);

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

