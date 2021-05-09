package com.example.codeli_klip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class RoomActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private Button room_ready_button; //준비 버튼
    private Button room_ready_cancel_button; //준비 취소 버튼
    private Button room_pay_button; //결제 버튼
    private Button room_verfity_button;//검증 버튼

    private ImageButton room_chat_button; //채팅 전송 버튼

    private TextView room_name; //가게 이름
    private TextView room_order_price; //최소주문금액
    private TextView room_delivery_price;//배달금액
    private TextView room_platform; //배달 플랫폼
    private TextView room_delivery_place; //배달 장소
    private TextView room_delivery_time; //배달 시간

    private ImageView room_my_status;
    private TextView room_my_nickname;
    private TextView room_my_menu;
    private TextView room_my_price;

    private EditText room_chat_text; //채팅 메세지 입력

    private String name;
    private int order_price;
    private int delivery_price;
    private String platform;
    private String delivery_place;
    private String specific_address;
    private String delivery_time;
    private int cur_people;
    private int price_per_person;

    PeopleItem my_menu_item;
    public static PeopleListAdapter peopleListAdapter;
    public static ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatItem> chatItemArrayList=new ArrayList<ChatItem>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chatRef;
    //'partition'노드 참조객체 변
    private DatabaseReference partitionRef;
    private DatabaseReference chat_user_Ref;

    private String room_id="";

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설

        //개인별 주문 읍식 나타날 리스트
        ListView listView=findViewById(R.id.room_people_list);
        peopleListAdapter=new PeopleListAdapter(this, peopleItemArrayList);
        listView.setAdapter(peopleListAdapter);

        //채팅 리스트
        final ListView chatListView=findViewById(R.id.room_chat_list);
        chatListAdapter=new ChatListAdapter(this, chatItemArrayList);
        chatListView.setAdapter(chatListAdapter);

        //각 텍스트에 필요한 값 붙이기
        room_name=findViewById(R.id.room_name);
        room_order_price=findViewById(R.id.room_minimum_price);
        room_delivery_price=findViewById(R.id.room_delivery_price);
        room_platform=findViewById(R.id.room_platform);
        room_delivery_place=findViewById(R.id.room_delivery_place);
        room_delivery_time=findViewById(R.id.room_delivery_time);

        //방 정보 받
        Intent data=getIntent();
        name=data.getStringExtra("name");
        platform=data.getStringExtra("platform");
        order_price=data.getIntExtra("order_price",0);
        delivery_price=data.getIntExtra("delivery_price",0);
        delivery_place=data.getStringExtra("address");
        specific_address=data.getStringExtra("specific_address");
        cur_people=data.getIntExtra("cur_people",1);
        room_id=data.getStringExtra("room_id");
        specific_address=data.getStringExtra("specific_address");

        //방 데이터 설정
        room_name.setText(name);
        room_platform.setText("사용플랫폼: "+platform);
        room_order_price.setText("최소주문금액: "+order_price+"원");
        price_per_person=delivery_price/cur_people;
        room_delivery_price.setText("배달팁: "+delivery_price+"원 (1인당 : "+price_per_person+")");
        room_delivery_place.setText("배달장소: "+delivery_place+" "+specific_address);

        room_my_status=findViewById(R.id.room_my_status);
        room_my_nickname=findViewById(R.id.room_my_nickname);
        room_my_menu=findViewById(R.id.room_my_menu);
        room_my_price=findViewById(R.id.room_my_price);

        room_my_nickname.setText(LoginActivity.nickname);
        room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강

        //나의 주문 목록 초기화
        my_menu_item=new PeopleItem(LoginActivity.nickname,false, "", 0,"", 0,0,false,0,false);

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+room_id+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        //결제 버튼
        room_pay_button=findViewById(R.id.room_pay_button);
        room_pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),PayActivity.class);
                intent.putExtra("menu_price",my_menu_item.getMenu_price());
                intent.putExtra("delivery_price",price_per_person);
                intent.putExtra("room_id",room_id); //방 번호
                intent.putExtra("my_menu_item",my_menu_item); //메뉴 데이터
                startActivity(intent);
                finish();
            }
        });

        room_ready_cancel_button=findViewById(R.id.room_ready_cancel_button);
        room_ready_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //준비 취소하기
                room_ready_cancel_button.setVisibility(View.INVISIBLE); //준비취소버튼 안보이기
                room_ready_button.setVisibility(View.VISIBLE); //준비버튼 보이기
                room_pay_button.setVisibility(View.INVISIBLE); //결제버튼 안보이기

                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강

                my_menu_item=new PeopleItem(LoginActivity.nickname,false, my_menu_item.getMenu_name(), my_menu_item.getMenu_price(),my_menu_item.getVerification(), my_menu_item.getUid(),my_menu_item.getExpiration_time(),my_menu_item.getSendingStatus(),my_menu_item.getTx_hash(),my_menu_item.getVerification_status());
                chat_user_Ref.setValue(my_menu_item);
            }
        });

        //준비하기 버튼
        room_ready_button=findViewById(R.id.room_ready_button);
        room_ready_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //준비 완료 확인 -> 데이터 db 전송!
                //Toast.makeText(getApplicationContext(), "준비 버튼", Toast.LENGTH_SHORT).show();
                String menu=room_my_menu.getText().toString().trim();
                int price=Integer.parseInt(room_my_price.getText().toString().trim());

                my_menu_item=new PeopleItem(LoginActivity.nickname,true, menu, price,my_menu_item.getVerification(), my_menu_item.getUid(),my_menu_item.getExpiration_time(),my_menu_item.getSendingStatus(),my_menu_item.getTx_hash(),my_menu_item.getVerification_status());

                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //준비돰 - 파랑

                chat_user_Ref.setValue(my_menu_item);

                //버튼 보이기 유무
                room_ready_button.setVisibility(View.INVISIBLE);
                room_ready_cancel_button.setVisibility(View.VISIBLE);
                room_pay_button.setVisibility(View.VISIBLE);
            }
        });

        room_verfity_button=findViewById(R.id.verify_button);
        room_verfity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my_menu_item.setVerfication_status(true);
                chat_user_Ref.setValue(my_menu_item);
            }
        });

        partitionRef= firebaseDatabase.getReference("/Chat/"+room_id+"/partitions"); //ref 맞는지 확인!
        //'partitions'노드에 저장되어 있는 데이터들을 읽어오기
        peopleItemArrayList.clear();
        partitionRef.addChildEventListener(new ChildEventListener() {
            //새로 추가된 것만 줌 ValueListener는 하나의 값만 바뀌어도 처음부터 다시 값을 줌
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    int my_data_check=0;
                    //새로 추가된 데이터 모두 가져오기
                    PeopleItem partition= dataSnapshot.getValue(PeopleItem.class);
                    order_price+=partition.getMenu_price(); //***** 현재 금액 파이어스토어 정보 업데이트하기!!!

                    if(partition.getId()!=null&&!partition.getId().equals(LoginActivity.nickname)) {

                        //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                        peopleItemArrayList.add(partition);

                        cur_people = peopleItemArrayList.size()+1;
                        price_per_person = delivery_price / cur_people;
                        room_delivery_price.setText("배달팁: " + delivery_price + "원 (1인당 : " + price_per_person + ")");

                        //리스트뷰를 갱신
                        peopleListAdapter.notifyDataSetChanged();
                    }

                    if(partition.getId()!=null&&partition.getId().equals(LoginActivity.nickname)){
                        my_menu_item=partition;
                        if(my_menu_item.getId()!=null){
                            if(my_menu_item.getStatus()==false){
                                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강
                            }
                            else{
                                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //준비됨 -파란
                            }

                            if(my_menu_item.getSendingStatus()==true){
                                room_verfity_button.setVisibility(View.VISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                            }
                            room_my_menu.setText(my_menu_item.getMenu_name());
                            room_my_price.setText(""+my_menu_item.getMenu_price());

                        }
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //Firebase DB관리 객체와 'caht'노드 참조객체 얻어오기
        chatRef= firebaseDatabase.getReference("/Chat/"+room_id+"/chat"); //ref 맞는지 확인!
        //firebaseDB에서 채팅 메세지들 실시간 읽어오기..
        //'chat'노드에 저장되어 있는 데이터들을 읽어오기
        //chatRef에 데이터가 변경되는 것으 듣는 리스너 추가
        chatItemArrayList.clear();
        chatRef.addChildEventListener(new ChildEventListener() {
            //새로 추가된 것만 줌 ValueListener는 하나의 값만 바뀌어도 처음부터 다시 값을 줌
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //새로 추가된 데이터(값 : MessageItem객체) 가져오기
                ChatItem messageItem= dataSnapshot.getValue(ChatItem.class);

                System.out.println("message item: "+messageItem.getMessage());

                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                chatItemArrayList.add(messageItem);

                System.out.println("chat item size: "+chatItemArrayList.size());

                //리스트뷰를 갱신
                chatListAdapter.notifyDataSetChanged();
                chatListView.setSelection(chatItemArrayList.size()-1); //리스트뷰의 마지막 위치로 스크롤 위치 이동
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //chat message send
        room_chat_text=findViewById(R.id.room_chat_text);
        room_chat_button=findViewById(R.id.room_chat_button);
        room_chat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅 전송
                Toast.makeText(getApplicationContext(), "메세지 전송", Toast.LENGTH_SHORT).show();

                String text=room_chat_text.getText().toString();

                Calendar calendar= Calendar.getInstance(); //현재 시간
                String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

                String id_time=""+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+calendar.get(Calendar.SECOND);

                ChatItem messageItem= new ChatItem(LoginActivity.nickname,text,time);
                //'char'노드에 MessageItem객체를 통해
                chatRef.child(LoginActivity.nickname+id_time).setValue(messageItem);

                //EditText에 있는 글씨 지우기
                room_chat_text.setText("");

                //소프트키패드를 안보이도록..
                InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

                chatListAdapter.notifyDataSetChanged();

            }
        });

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.chat);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        finish();
                        return true;
                    case R.id.chat:
                        //방목록으로 이동하기
                        return true;
                    case R.id.my:
                        Intent my_intent=new Intent(getApplicationContext(), MypageActivity.class);
                        startActivity(my_intent);
                        finish();
                        //Toast.makeText(getApplicationContext(), "마이 페이지로 이동", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

    }



}