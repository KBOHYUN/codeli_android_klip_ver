package com.example.codeli_klip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RoomOwnerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private Button verfity_trigger_button;//검증 버튼

    private ImageButton room_chat_button; //채팅 전송 버튼
    private ImageButton room_time_edit_button; //약속시간 변경 버튼

    private Calendar calendar;

    private TextView room_name; //가게 이름
    private TextView room_order_price; //최소주문금액
    private TextView room_delivery_price;//배달금액
    private TextView room_platform; //배달 플랫폼
    private TextView room_delivery_place; //배달 장소
    private TextView room_delivery_time; //배달 시간


    private EditText room_chat_text; //채팅 메세지 입력

    private boolean sendingStatusCheck=false;

    private String name;
    private int order_price;
    private int delivery_price;
    private String platform;
    private String delivery_place;
    private String specific_address;
    private String delivery_time;
    private int cur_people;
    private int price_per_person;

    private int pos;

    PeopleItem my_menu_item;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatItem> chatItemArrayList=new ArrayList<ChatItem>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chatRef;
    //'partition'노드 참조객체 변
    private DatabaseReference partitionRef;
    private DatabaseReference chat_user_Ref;

    private DatabaseReference verification_ref;

    private String room_id="";

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_owner);

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설정
        firestore=FirebaseFirestore.getInstance();

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
        name=data.getStringExtra("name");
        platform=data.getStringExtra("platform");
        order_price=data.getIntExtra("order_price",0);
        delivery_price=data.getIntExtra("delivery_price",0);
        delivery_place=data.getStringExtra("address");
        specific_address=data.getStringExtra("specific_address");
        cur_people=data.getIntExtra("cur_people",1);
        room_id=data.getStringExtra("room_id");
        specific_address=data.getStringExtra("specific_address");
        pos=data.getIntExtra("position",0);

        //방 데이터 설정
        room_name.setText(MainActivity.roomItemArrayList.get(pos).getName());
        room_platform.setText("사용플랫폼: "+MainActivity.roomItemArrayList.get(pos).getPlatform());
        room_order_price.setText("최소주문금액: "+MainActivity.roomItemArrayList.get(pos).getOrderPrice()+"원");
        price_per_person=MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()/MainActivity.roomItemArrayList.get(pos).getCurrentPeople();
        room_delivery_price.setText("배달팁: "+MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()+"원 (1인당 : "+price_per_person+")");
        room_delivery_place.setText("배달장소: "+MainActivity.roomItemArrayList.get(pos).getAddress()+" "+MainActivity.roomItemArrayList.get(pos).getSpecificAddress());


        //약속 시간 설정
        calendar=Calendar.getInstance();
        room_time_edit_button=findViewById(R.id.room_time_modify_button);
        room_time_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog timePickerDialog=new TimePickerDialog(RoomOwnerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        room_delivery_time.setText("약속시간: "+hourOfDay+"시 "+minute+"분  ");

                        //firestore에 올리기

                    }
                },calendar.HOUR_OF_DAY,calendar.MINUTE,false);
                timePickerDialog.show();
            }
        });

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+pos+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        //송금 요청 버튼
        verfity_trigger_button=findViewById(R.id.verify_trigger_button);
        verfity_trigger_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verification_ref= firebaseDatabase.getReference("/Chat/"+pos+"/verification/"); //방장 송금 요청 verification reference

                VerificationData verification=new VerificationData(LoginActivity.klip_address,true);
                verification_ref.setValue(verification);

                Toast.makeText(getApplicationContext(), "송금이 요청되었습니다", Toast.LENGTH_SHORT).show();


            }
        });

        partitionRef= firebaseDatabase.getReference("/Chat/"+pos+"/partitions"); //ref 맞는지 확인!
        //'partitions'노드에 저장되어 있는 데이터들을 읽어오기
        peopleItemArrayList.clear();
        partitionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                peopleItemArrayList.clear();
                sendingStatusCheck=false;
                for(DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
                {
                    PeopleItem partition = ds.getValue(PeopleItem.class);
                    order_price+=partition.getMenu_price(); //***** 현재 금액 파이어스토어 정보 업데이트하기!!!

                    if(partition.getId()!=null&&!partition.getId().equals(LoginActivity.nickname)) {

                        //새로운 참여자 정보를 리스뷰에 추가하기 위해 ArrayList에 추가
                        peopleItemArrayList.add(partition);

                        //결제하지 않은 인원이 있는 경우 check=false
                        if(partition.getSendingStatus()!=null&&partition.getSendingStatus().equals("success")){
                            sendingStatusCheck=true;
                        }
                        else{
                            sendingStatusCheck=false;
                        }

                        cur_people = peopleItemArrayList.size()+1;
                        price_per_person = MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() / cur_people;
                        room_delivery_price.setText("배달팁: " + MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() + "원 (1인당 : " + price_per_person + ")");

                        //리스트뷰를 갱신
                       peopleListAdapter.notifyDataSetChanged();
                    }
                }
                if(sendingStatusCheck==true){
                    Toast.makeText(getApplicationContext(),"모든 인원이 결제를 완료하였습니다\n음식 수령 시 검증 절차 후 송금 요청을 진행해주세요",Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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
                        if(snapshot.getData().get("time")!=null){
                            roomItem = new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString(),snapshot.getData().get("x").toString(),snapshot.getData().get("y").toString(),snapshot.getData().get("time").toString());
                        }
                        else{
                            roomItem = new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString(),snapshot.getData().get("x").toString(),snapshot.getData().get("y").toString());
                        }

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