package com.example.codeli_klip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeli_klip.actions.KlipAction;
import com.example.codeli_klip.util.JsonHelper;
import com.klipwallet.app2app.api.Klip;
import com.klipwallet.app2app.api.KlipCallback;
import com.klipwallet.app2app.api.response.KlipErrorResponse;
import com.klipwallet.app2app.api.response.KlipResponse;
import com.klipwallet.app2app.api.response.model.KlipResult;


public class PayActivity extends AppCompatActivity {

    Button pay_button; //결제하기 버튼
    Button test_button;

    TextView pay_price;
    TextView pay_total_price;

    private int menu_price;
    private int delivery_price;
    private int total_price=0;

    private Context ctx;
    private KlipAction klipAction;

    private String requestKey;
    private String userAddress;

    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        pay_price=findViewById(R.id.pay_price);
        pay_total_price=findViewById(R.id.pay_total_price);

        Intent getIntent=getIntent();
        menu_price=getIntent.getIntExtra("menu_price",0);
        delivery_price=getIntent.getIntExtra("delivery_price",0);
        total_price=menu_price+delivery_price;

        pay_price.setText("음식가격 "+menu_price+" + 배달팁 "+delivery_price);
        pay_total_price.setText("총 금액 "+total_price);


        ctx = this;
        klipAction = new KlipAction(ctx, Klip.getInstance(ctx));

        //서버에서 값 받아서 각 텍스트에 넣기
        pay_button=findViewById(R.id.pay_button);
        pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //결제하기
                klipAction.prepareLink(klipCallback);
                //klipAction.prepareKlay(klipCallback);

                //Toast.makeText(getApplicationContext(), "결제하기", Toast.LENGTH_SHORT).show();
                //finish(); //종료
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
            if (resultKey != null){
                System.out.println("******request key: "+resultKey);
                requestKey = resultKey;

                klipAction.request(requestKey); //요청 시
            }

            // save user address
            KlipResult result = res.getResult();
            if (result != null) {
                if(result.getStatus().equals("prepared")){
                    klipAction.getResult(requestKey, klipCallback);
                }

                userAddress = result.getKlaytnAddress();
                System.out.println("*****result 성공 - user klip address : "+userAddress);

            }
        }
        @Override
        public void onFail(final KlipErrorResponse res) {
            //resView.setText(res.toString());

            // reset request key
            requestKey = null;
        }
    };

}

