package com.example.codeli_klip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.DoubleNode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

    private int people_size=1;

    private int pos;

    private int cur_people;
    private int price_per_person;

    private MyItem my_menu_item;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatItem> chatItemArrayList=new ArrayList<ChatItem>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chatRef;
    //'partition'노드 참조객체 변수
    private DatabaseReference partitionRef;
    private DatabaseReference chat_user_Ref;


    private DatabaseReference klay_Ref; //클레이 시세 받아오는 참조변수
    private ArrayList<KlayData> klayDataArrayList=new ArrayList<KlayData>();
    private double klay_flow;

    private String room_id="";

    private FirebaseFirestore firestore;

    private Map<String, Object> roomValue = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설정
        firestore = FirebaseFirestore.getInstance(); //파이어스토어 설정

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

        //방 정보 받기
        Intent data=getIntent();
        pos=data.getIntExtra("position",0);

        //방 데이터 설정
        room_name.setText(MainActivity.roomItemArrayList.get(pos).getName());
        room_platform.setText("사용플랫폼: "+MainActivity.roomItemArrayList.get(pos).getPlatform());
        room_order_price.setText("최소주문금액: "+MainActivity.roomItemArrayList.get(pos).getOrderPrice()+"원");
        price_per_person=MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()/MainActivity.roomItemArrayList.get(pos).getCurrentPeople();
        room_delivery_price.setText("배달팁: "+MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()+"원 (1인당 : "+price_per_person+")");
        room_delivery_place.setText("배달장소: "+MainActivity.roomItemArrayList.get(pos).getAddress()+" "+MainActivity.roomItemArrayList.get(pos).getSpecificAddress());

        room_my_status=findViewById(R.id.room_my_status);
        room_my_nickname=findViewById(R.id.room_my_nickname);
        room_my_menu=findViewById(R.id.room_my_menu);
        room_my_price=findViewById(R.id.room_my_price);

        room_my_nickname.setText(LoginActivity.nickname);
        room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강

        //나의 주문 목록 초기화
        my_menu_item=new MyItem(LoginActivity.nickname,false, "",0,0,"","",false);

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+pos+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        klay_Ref=firebaseDatabase.getReference("/klay_value/"); //클레이 reference

        //결제 버튼
        Intent intent=new Intent(getApplicationContext(),PayActivity.class);
        room_pay_button=findViewById(R.id.room_pay_button);
        room_pay_button.setVisibility(View.INVISIBLE);
        room_pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KlayData trigger=new KlayData(true, "");
                klay_Ref.setValue(trigger);
                //클레이 시세 확인

                klay_Ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                        //데이터가 쌓이기 때문에  clear()

                            //String value=ds.getValue(KlayData.class).getValue();
                            KlayData klay=dataSnapshot.getValue(KlayData.class);
                            if(klay.getTrigger()==false){
                                klayDataArrayList.add(klay);


                                    klay_flow= Double.parseDouble(klayDataArrayList.get(0).getValue());

                                    // Toast.makeText(getApplicationContext(), "클레이 시세"+klay_flow, Toast.LENGTH_SHORT).show();

                                    int total=price_per_person+my_menu_item.getMenu_price();
                                    double klay_price=total/klay_flow;
                                    double total_klay_6=Double.parseDouble(String.format("%.6f",klay_price));

                                    intent.putExtra("menu_price",my_menu_item.getMenu_price());
                                    intent.putExtra("delivery_price",price_per_person);
                                    intent.putExtra("room_id",room_id); //방 번호
                                    intent.putExtra("klay_flow",klay_flow); //클레이 시세
                                    intent.putExtra("klay_total",total_klay_6);
                                    intent.putExtra("my_menu_item",my_menu_item); //메뉴 데이터
                                    intent.putExtra("position",pos);
                                    startActivity(intent);
                                    finish();


                        }


//                            if(klay.getTrigger()==false){
//
//                                klay_flow= Double.parseDouble(klay.getValue());
//
//                               // Toast.makeText(getApplicationContext(), "클레이 시세"+klay_flow, Toast.LENGTH_SHORT).show();
//
//                                int total=price_per_person+my_menu_item.getMenu_price();
//                                double klay_price=total/klay_flow;
//                                double total_klay_6=Double.parseDouble(String.format("%.6f",klay_price));
//
//                                intent.putExtra("menu_price",my_menu_item.getMenu_price());
//                                intent.putExtra("delivery_price",price_per_person);
//                                intent.putExtra("room_id",room_id); //방 번호
//                                intent.putExtra("klay_flow",klay_flow); //클레이 시세
//                                intent.putExtra("klay_total",total_klay_6);
//                                intent.putExtra("my_menu_item",my_menu_item); //메뉴 데이터
//                                startActivity(intent);
//                                finish();
//
//                            }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

            }
        });

        //준비 취소 버튼
        room_ready_cancel_button=findViewById(R.id.room_ready_cancel_button);
        room_ready_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //준비 취소하기
                room_ready_cancel_button.setVisibility(View.INVISIBLE); //준비취소버튼 안보이기
                room_ready_button.setVisibility(View.VISIBLE); //준비버튼 보이기
                room_pay_button.setVisibility(View.INVISIBLE); //결제버튼 안보이기

                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강
                //my_menu_item=new PeopleItem(LoginActivity.nickname,false, my_menu_item.getMenu_name(), my_menu_item.getMenu_price(),my_menu_item.getExpiration_time(),my_menu_item.getSendingStatus());
                my_menu_item=new MyItem(LoginActivity.nickname,false, my_menu_item.getMenu_name(), my_menu_item.getMenu_price(),0,"","",false);
                chat_user_Ref.setValue(my_menu_item);

                //****파이어스토어에 데이터 업데이트 하기
                //메뉴 가격, 현재 인원 감소 -> 1인당 배달비 업데이트

                int price=Integer.parseInt(room_my_price.getText().toString().trim());
                int current_price=MainActivity.roomItemArrayList.get(pos).getCurrentOrderPrice()-price;
                int current_people=MainActivity.roomItemArrayList.get(pos).getCurrentPeople()-1;

                RoomItem roomItem= new RoomItem(MainActivity.roomItemArrayList.get(pos).getName(),MainActivity.roomItemArrayList.get(pos).getPlatform(),current_price,MainActivity.roomItemArrayList.get(pos).getOrderPrice(),MainActivity.roomItemArrayList.get(pos).getDeliveryPrice(),MainActivity.roomItemArrayList.get(pos).getAddress(),MainActivity.roomItemArrayList.get(pos).getSpecificAddress(),current_people,MainActivity.roomItemArrayList.get(pos).getTotalPeople(),MainActivity.roomItemArrayList.get(pos).getOwner(),MainActivity.roomItemArrayList.get(pos).getX(),MainActivity.roomItemArrayList.get(pos).getY());
                roomValue=roomItem.toMap();

                int delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

                MainActivity.roomItemArrayList.set(pos, roomItem);
                MainActivity.roomLIstAdapter.notifyDataSetChanged();

                room_delivery_price.setText("배달팁: "+roomItem.getDeliveryPrice()+"원 (1인당 : "+delivery_price_per_person+")");

                firestore.collection("Rooms")
                        .document(""+pos)
                        .set(roomValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                //Toast.makeText(getApplicationContext(), "방 등록이 성공하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, "Error adding document", e);
                                //Toast.makeText(getApplicationContext(), "방 등록이 실패하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        //준비하기 버튼
        room_ready_button=findViewById(R.id.room_ready_button);
        room_ready_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //준비 완료 확인 -> 데이터 db 전송!


                //내 메뉴 db에 전송
                String menu=room_my_menu.getText().toString().trim();
                int price=Integer.parseInt(room_my_price.getText().toString().trim());
                my_menu_item=new MyItem(LoginActivity.nickname,true, menu, price);
                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //준비돰 - 파랑
                chat_user_Ref.setValue(my_menu_item);

                //****파이어스토어에 데이터 업데이트 하기
                //메뉴 가격, 현재 인원 증가 -> 1인당 배달비 업데이트

                int current_price=MainActivity.roomItemArrayList.get(pos).getCurrentOrderPrice()+price;
                int current_people=MainActivity.roomItemArrayList.get(pos).getCurrentPeople()+1;

                RoomItem roomItem= new RoomItem(MainActivity.roomItemArrayList.get(pos).getName(),MainActivity.roomItemArrayList.get(pos).getPlatform(),current_price,MainActivity.roomItemArrayList.get(pos).getOrderPrice(),MainActivity.roomItemArrayList.get(pos).getDeliveryPrice(),MainActivity.roomItemArrayList.get(pos).getAddress(),MainActivity.roomItemArrayList.get(pos).getSpecificAddress(),current_people,MainActivity.roomItemArrayList.get(pos).getTotalPeople(),MainActivity.roomItemArrayList.get(pos).getOwner(),MainActivity.roomItemArrayList.get(pos).getX(),MainActivity.roomItemArrayList.get(pos).getY());
                roomValue=roomItem.toMap();

                int delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

                MainActivity.roomItemArrayList.set(pos, roomItem);
                MainActivity.roomLIstAdapter.notifyDataSetChanged();

                room_delivery_price.setText("배달팁: "+roomItem.getDeliveryPrice()+"원 (1인당 : "+delivery_price_per_person+")");

                firestore.collection("Rooms")
                        .document(""+pos)
                        .set(roomValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                //Toast.makeText(getApplicationContext(), "방 등록이 성공하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, "Error adding document", e);
                                //Toast.makeText(getApplicationContext(), "방 등록이 실패하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });

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
                //my_menu_item.setVerification_status(true);

                //MyItem data=new MyItem(my_menu_item.getId(),my_menu_item.getStatus(),my_menu_item.getMenu_name(),my_menu_item.getMenu_price(),0,"","",true);
                my_menu_item.setVerification_status(true);
                chat_user_Ref.setValue(my_menu_item);
                Toast.makeText(getApplicationContext(), "배달 주문 검증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                //한 번 더 확인하는 alert창 띄우기
                //room_verfity_button.setVisibility(View.INVISIBLE);
            }
        });

        ReadFirestoreData();

        //***** 실시간 데이터베이스 정보 읽기*****
        partitionRef= firebaseDatabase.getReference("/Chat/"+pos+"/partitions"); //ref 맞는지 확인!
        //'partitions'노드에 저장되어 있는 데이터들을 읽어오기
        partitionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                peopleItemArrayList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
                {
                    PeopleItem partition = ds.getValue(PeopleItem.class);
                    //order_price+=partition.getMenu_price(); //***** 현재 금액 파이어스토어 정보 업데이트하기!!!

                    if(partition.getId()!=null&&!partition.getId().equals(LoginActivity.nickname)) {

                        //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                        peopleItemArrayList.add(partition);

                        cur_people = peopleItemArrayList.size()+1;
                        people_size=cur_people;

                        price_per_person = MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() / people_size;
                        room_delivery_price.setText("배달팁: " + MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() + "원 (1인당 : " + price_per_person + ")");

                        //리스트뷰를 갱신
                        peopleListAdapter.notifyDataSetChanged();
                    }

                    //***** 추가: 준비가 되어있는 경우는 나갔다 들어오더라도 준비버튼 안보이도록!!!

                    if(partition.getId()!=null&&partition.getId().equals(LoginActivity.nickname)){ //nickname이 자신일 경
                        my_menu_item=new MyItem(partition.getId(),partition.getStatus(),partition.getMenu_name(),partition.getMenu_price(),partition.getExpiration_time(),partition.getTx_hash(),partition.getSendingStatus(),partition.getVerification_status());
                        if(my_menu_item.getId()!=null){
                            if(my_menu_item.getStatus()==false){
                                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강
                            }
                            else{
                                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //준비됨 -파란우
                                room_verfity_button.setVisibility(View.INVISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                                room_pay_button.setVisibility(View.VISIBLE);
                                room_ready_cancel_button.setVisibility(View.VISIBLE);
                            }
                            //Toast.makeText(getApplicationContext(), "** sending status: "+my_menu_item.getSendingStatus(), Toast.LENGTH_SHORT).show();
                            //결제 성공 시
                            if(my_menu_item.getSendingStatus()!=null&&my_menu_item.getSendingStatus().equals("success")){

                                room_verfity_button.setVisibility(View.VISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                                room_pay_button.setVisibility(View.INVISIBLE);
                                room_ready_cancel_button.setVisibility(View.INVISIBLE);
                            }
//                            if(my_menu_item.getVerification_status()==true){
//                                room_verfity_button.setVisibility(View.VISIBLE);
//                            }
                            room_my_menu.setText(my_menu_item.getMenu_name());
                            room_my_price.setText(""+my_menu_item.getMenu_price());

                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Firebase DB관리 객체와 'caht'노드 참조객체 얻어오기
        chatRef= firebaseDatabase.getReference("/Chat/"+pos+"/chat"); //ref 맞는지 확인!
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

                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                chatItemArrayList.add(messageItem);


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
                //Toast.makeText(getApplicationContext(), "메세지 전송", Toast.LENGTH_SHORT).show();

                String text=room_chat_text.getText().toString();

//                Calendar calendar= Calendar.getInstance(); //현재 시간
//                String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

                Date today = new Date();
                SimpleDateFormat format1 = new SimpleDateFormat("HHmmss");
                SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
                String id_time=format1.format(today);
                String time=format2.format(today);

                ChatItem messageItem= new ChatItem(LoginActivity.nickname,text,time);
                //'char'노드에 MessageItem객체를 통해
                chatRef.child(id_time).setValue(messageItem);

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


    private void ReadFirestoreData(){
        //*****Firestore 실시간 정보 읽기******
        final DocumentReference docRef = firestore.collection("Rooms").document(""+pos);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    System.out.println("Current data: " + snapshot.getData());

                    RoomItem roomItem=null;
                    if(snapshot.getData().get("x")!=null) {
                        roomItem = new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString(),Double.parseDouble(snapshot.getData().get("x").toString()),Double.parseDouble(snapshot.getData().get("y").toString()));
                    }else{
                        roomItem= new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString());
                    }

                    int delivery_price_per_person=roomItem.getDeliveryPrice() / roomItem.getCurrentPeople();
                    room_delivery_price.setText("배달팁: "+roomItem.getDeliveryPrice()+"원 (1인당 : "+delivery_price_per_person+")");

                    MainActivity.roomItemArrayList.set(pos, roomItem);
                    MainActivity.roomLIstAdapter.notifyDataSetChanged();

                } else { }
            }
        });
    }


}