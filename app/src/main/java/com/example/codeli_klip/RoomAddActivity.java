package com.example.codeli_klip;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RoomAddActivity extends AppCompatActivity {

    private EditText name;
    private Spinner platform;
    private EditText address;
    private EditText specific_address;
    private EditText minimum_price;
    private EditText delivery_price;

    private TextView people;

    private Button addr_search_bt; //주소 검색 버튼
    private Button add_cancel_bt; //방 추가 취소 버튼
    private Button add_room_bt; //방 추가하기 버튼
    private Button people_up; //인원 증가
    private Button people_down; //인원 감소

    private int people_num=1;

    //realtime db
    DatabaseReference mDBReference = null;
    HashMap<String, Object> childUpdates = null;
    Map<String, Object> roomValue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room2);

        name=findViewById(R.id.add_name_et);
        platform=findViewById(R.id.add_platform_spinner);
        address=findViewById(R.id.add_addr_et);
        specific_address=findViewById(R.id.add_specific_addr_et);
        minimum_price=findViewById(R.id.add_min_price_et);
        delivery_price=findViewById(R.id.add_delivery_price_et);

        addr_search_bt=findViewById(R.id.add_addr_search_bt);
        addr_search_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("주소 검색 시작");
            }
        });

        people=findViewById(R.id.add_people_et);
        people_up=findViewById(R.id.add_people_plus_bt);
        people_down=findViewById(R.id.add_people_minus_bt);
        people_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                people_num++;
                people.setText(Integer.toString(people_num));
            }
        });
        people_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0 이하로는 될 수 없게 -> 에러메세지
                
                people_num--;
                people.setText(Integer.toString(people_num));
            }
        });

        add_cancel_bt=findViewById(R.id.add_cancel_bt);
        add_cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //값 넣어서 새로운 room item 생성 후 추가
                Toast.makeText(getApplicationContext(), "방이 생성이 취소되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(RoomAddActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        add_room_bt=findViewById(R.id.add_room_bt);
        add_room_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //값 넣어서 새로운 room item 생성 후 추가
                RoomItem item=null;

                if(name.getText().toString()!=null){
                    item=new RoomItem(name.getText().toString(), platform.getSelectedItem().toString(),0,Integer.parseInt(minimum_price.getText().toString()),Integer.parseInt(delivery_price.getText().toString()),address.getText().toString(),specific_address.getText().toString(),1,people_num,LoginActivity.nickname);
                    roomValue=item.toMap();
                }

                //---데이터베이스로 추가---
                //firestore db
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                firestore.collection("Rooms")
                        .document(""+MainActivity.roomItemArrayList.size())
                        .set(roomValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "방 등록이 성공하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, "Error adding document", e);
                                Toast.makeText(getApplicationContext(), "방 등록이 실패하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });

//                mDBReference = FirebaseDatabase.getInstance().getReference();
//                childUpdates = new HashMap<>();
//                if(name.getText().toString()!=null){
//                    item=new RoomItem(name.getText().toString(), platform.getSelectedItem().toString(),0,Integer.parseInt(minimum_price.getText().toString()),Integer.parseInt(delivery_price.getText().toString()),address.getText().toString()+" "+specific_address.getText().toString(),1,people_num);
//                    roomValue=item.toMap();
//                }
//
//                //String room_id="room" + (int)((Math.random()*10000)%10);
//                String room_id="room";
//                System.out.println("id: "+ LoginActivity.email);
//                System.out.println("roominfo: "+name.getText().toString());
//                childUpdates.put("/Room_info/" + room_id, roomValue); //id값에 이메일 안됨
//                mDBReference.updateChildren(childUpdates);

                //Toast.makeText(getApplicationContext(), "방이 생성되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}