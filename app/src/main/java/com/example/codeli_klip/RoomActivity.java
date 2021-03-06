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

    private Button room_ready_button; //?????? ??????
    private Button room_ready_cancel_button; //?????? ?????? ??????
    private Button room_pay_button; //?????? ??????
    private Button room_verfity_button;//?????? ??????

    private ImageButton room_chat_button; //?????? ?????? ??????

    private TextView room_name; //?????? ??????
    private TextView room_order_price; //??????????????????
    private TextView room_delivery_price;//????????????
    private TextView room_platform; //?????? ?????????
    private TextView room_delivery_place; //?????? ??????
    private TextView room_delivery_time; //?????? ??????

    private ImageView room_my_status;
    private TextView room_my_nickname;
    private TextView room_my_menu;
    private TextView room_my_price;

    private EditText room_chat_text; //?????? ????????? ??????

    private int people_size=1;

    private int pos;

    private int cur_people;
    private int price_per_person;

    private MyItem my_menu_item;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatItem> chatItemArrayList=new ArrayList<ChatItem>();

    //Firebase Database ?????? ??????????????????
    private FirebaseDatabase firebaseDatabase;

    //'chat'????????? ???????????? ????????????
    private DatabaseReference chatRef;
    //'partition'?????? ???????????? ??????
    private DatabaseReference partitionRef;
    private DatabaseReference chat_user_Ref;


    private DatabaseReference klay_Ref; //????????? ?????? ???????????? ????????????
    private ArrayList<KlayData> klayDataArrayList=new ArrayList<KlayData>();
    private double klay_flow;

    private String room_id="";

    private FirebaseFirestore firestore;

    private Map<String, Object> roomValue = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        firebaseDatabase= FirebaseDatabase.getInstance(); //?????????????????? ??????
        firestore = FirebaseFirestore.getInstance(); //?????????????????? ??????

        //????????? ?????? ?????? ????????? ?????????
        ListView listView=findViewById(R.id.room_people_list);
        peopleListAdapter=new PeopleListAdapter(this, peopleItemArrayList);
        listView.setAdapter(peopleListAdapter);

        //?????? ?????????
        final ListView chatListView=findViewById(R.id.room_chat_list);
        chatListAdapter=new ChatListAdapter(this, chatItemArrayList);
        chatListView.setAdapter(chatListAdapter);

        //??? ???????????? ????????? ??? ?????????
        room_name=findViewById(R.id.room_name);
        room_order_price=findViewById(R.id.room_minimum_price);
        room_delivery_price=findViewById(R.id.room_delivery_price);
        room_platform=findViewById(R.id.room_platform);
        room_delivery_place=findViewById(R.id.room_delivery_place);
        room_delivery_time=findViewById(R.id.room_delivery_time);

        //??? ?????? ??????
        Intent data=getIntent();
        pos=data.getIntExtra("position",0);

        //??? ????????? ??????
        room_name.setText(MainActivity.roomItemArrayList.get(pos).getName());
        room_platform.setText("???????????????: "+MainActivity.roomItemArrayList.get(pos).getPlatform());
        room_order_price.setText("??????????????????: "+MainActivity.roomItemArrayList.get(pos).getOrderPrice()+"???");
        price_per_person=MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()/MainActivity.roomItemArrayList.get(pos).getCurrentPeople();
        room_delivery_price.setText("?????????: "+MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()+"??? (1?????? : "+price_per_person+")");
        room_delivery_place.setText("????????????: "+MainActivity.roomItemArrayList.get(pos).getAddress()+" "+MainActivity.roomItemArrayList.get(pos).getSpecificAddress());

        room_my_status=findViewById(R.id.room_my_status);
        room_my_nickname=findViewById(R.id.room_my_nickname);
        room_my_menu=findViewById(R.id.room_my_menu);
        room_my_price=findViewById(R.id.room_my_price);

        room_my_nickname.setText(LoginActivity.nickname);
        room_my_status.setColorFilter(Color.parseColor("#FF0000")); //?????? ?????? - ??????

        //?????? ?????? ?????? ?????????
        my_menu_item=new MyItem(LoginActivity.nickname,false, "",0,0,"","",false);

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+pos+"/partitions/"+ LoginActivity.nickname); //?????? reference

        klay_Ref=firebaseDatabase.getReference("/klay_value/"); //????????? reference

        //?????? ??????
        Intent intent=new Intent(getApplicationContext(),PayActivity.class);
        room_pay_button=findViewById(R.id.room_pay_button);
        room_pay_button.setVisibility(View.INVISIBLE);
        room_pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KlayData trigger=new KlayData(true, "");
                klay_Ref.setValue(trigger);
                //????????? ?????? ??????

                klay_Ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //????????? ?????? DataSnapshot ?????? ????????????.
                        //???????????? ????????? ?????????  clear()

                            //String value=ds.getValue(KlayData.class).getValue();
                            KlayData klay=dataSnapshot.getValue(KlayData.class);
                            if(klay.getTrigger()==false){
                                klayDataArrayList.add(klay);


                                    klay_flow= Double.parseDouble(klayDataArrayList.get(0).getValue());

                                    // Toast.makeText(getApplicationContext(), "????????? ??????"+klay_flow, Toast.LENGTH_SHORT).show();

                                    int total=price_per_person+my_menu_item.getMenu_price();
                                    double klay_price=total/klay_flow;
                                    double total_klay_6=Double.parseDouble(String.format("%.6f",klay_price));

                                    intent.putExtra("menu_price",my_menu_item.getMenu_price());
                                    intent.putExtra("delivery_price",price_per_person);
                                    intent.putExtra("room_id",room_id); //??? ??????
                                    intent.putExtra("klay_flow",klay_flow); //????????? ??????
                                    intent.putExtra("klay_total",total_klay_6);
                                    intent.putExtra("my_menu_item",my_menu_item); //?????? ?????????
                                    intent.putExtra("position",pos);
                                    startActivity(intent);
                                    finish();


                        }


//                            if(klay.getTrigger()==false){
//
//                                klay_flow= Double.parseDouble(klay.getValue());
//
//                               // Toast.makeText(getApplicationContext(), "????????? ??????"+klay_flow, Toast.LENGTH_SHORT).show();
//
//                                int total=price_per_person+my_menu_item.getMenu_price();
//                                double klay_price=total/klay_flow;
//                                double total_klay_6=Double.parseDouble(String.format("%.6f",klay_price));
//
//                                intent.putExtra("menu_price",my_menu_item.getMenu_price());
//                                intent.putExtra("delivery_price",price_per_person);
//                                intent.putExtra("room_id",room_id); //??? ??????
//                                intent.putExtra("klay_flow",klay_flow); //????????? ??????
//                                intent.putExtra("klay_total",total_klay_6);
//                                intent.putExtra("my_menu_item",my_menu_item); //?????? ?????????
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

        //?????? ?????? ??????
        room_ready_cancel_button=findViewById(R.id.room_ready_cancel_button);
        room_ready_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ????????????
                room_ready_cancel_button.setVisibility(View.INVISIBLE); //?????????????????? ????????????
                room_ready_button.setVisibility(View.VISIBLE); //???????????? ?????????
                room_pay_button.setVisibility(View.INVISIBLE); //???????????? ????????????

                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //?????? ?????? - ??????
                //my_menu_item=new PeopleItem(LoginActivity.nickname,false, my_menu_item.getMenu_name(), my_menu_item.getMenu_price(),my_menu_item.getExpiration_time(),my_menu_item.getSendingStatus());
                my_menu_item=new MyItem(LoginActivity.nickname,false, my_menu_item.getMenu_name(), my_menu_item.getMenu_price(),0,"","",false);
                chat_user_Ref.setValue(my_menu_item);

                //****????????????????????? ????????? ???????????? ??????
                //?????? ??????, ?????? ?????? ?????? -> 1?????? ????????? ????????????

                int price=Integer.parseInt(room_my_price.getText().toString().trim());
                int current_price=MainActivity.roomItemArrayList.get(pos).getCurrentOrderPrice()-price;
                int current_people=MainActivity.roomItemArrayList.get(pos).getCurrentPeople()-1;

                RoomItem roomItem= new RoomItem(MainActivity.roomItemArrayList.get(pos).getName(),MainActivity.roomItemArrayList.get(pos).getPlatform(),current_price,MainActivity.roomItemArrayList.get(pos).getOrderPrice(),MainActivity.roomItemArrayList.get(pos).getDeliveryPrice(),MainActivity.roomItemArrayList.get(pos).getAddress(),MainActivity.roomItemArrayList.get(pos).getSpecificAddress(),current_people,MainActivity.roomItemArrayList.get(pos).getTotalPeople(),MainActivity.roomItemArrayList.get(pos).getOwner(),MainActivity.roomItemArrayList.get(pos).getX(),MainActivity.roomItemArrayList.get(pos).getY());
                roomValue=roomItem.toMap();

                int delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

                MainActivity.roomItemArrayList.set(pos, roomItem);
                MainActivity.roomLIstAdapter.notifyDataSetChanged();

                room_delivery_price.setText("?????????: "+roomItem.getDeliveryPrice()+"??? (1?????? : "+delivery_price_per_person+")");

                firestore.collection("Rooms")
                        .document(""+pos)
                        .set(roomValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                //Toast.makeText(getApplicationContext(), "??? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, "Error adding document", e);
                                //Toast.makeText(getApplicationContext(), "??? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        //???????????? ??????
        room_ready_button=findViewById(R.id.room_ready_button);
        room_ready_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ?????? ?????? -> ????????? db ??????!


                //??? ?????? db??? ??????
                String menu=room_my_menu.getText().toString().trim();
                int price=Integer.parseInt(room_my_price.getText().toString().trim());
                my_menu_item=new MyItem(LoginActivity.nickname,true, menu, price);
                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //????????? - ??????
                chat_user_Ref.setValue(my_menu_item);

                //****????????????????????? ????????? ???????????? ??????
                //?????? ??????, ?????? ?????? ?????? -> 1?????? ????????? ????????????

                int current_price=MainActivity.roomItemArrayList.get(pos).getCurrentOrderPrice()+price;
                int current_people=MainActivity.roomItemArrayList.get(pos).getCurrentPeople()+1;

                RoomItem roomItem= new RoomItem(MainActivity.roomItemArrayList.get(pos).getName(),MainActivity.roomItemArrayList.get(pos).getPlatform(),current_price,MainActivity.roomItemArrayList.get(pos).getOrderPrice(),MainActivity.roomItemArrayList.get(pos).getDeliveryPrice(),MainActivity.roomItemArrayList.get(pos).getAddress(),MainActivity.roomItemArrayList.get(pos).getSpecificAddress(),current_people,MainActivity.roomItemArrayList.get(pos).getTotalPeople(),MainActivity.roomItemArrayList.get(pos).getOwner(),MainActivity.roomItemArrayList.get(pos).getX(),MainActivity.roomItemArrayList.get(pos).getY());
                roomValue=roomItem.toMap();

                int delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

                MainActivity.roomItemArrayList.set(pos, roomItem);
                MainActivity.roomLIstAdapter.notifyDataSetChanged();

                room_delivery_price.setText("?????????: "+roomItem.getDeliveryPrice()+"??? (1?????? : "+delivery_price_per_person+")");

                firestore.collection("Rooms")
                        .document(""+pos)
                        .set(roomValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                //Toast.makeText(getApplicationContext(), "??? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w(TAG, "Error adding document", e);
                                //Toast.makeText(getApplicationContext(), "??? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        });

                //?????? ????????? ??????
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
                Toast.makeText(getApplicationContext(), "?????? ?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                //??? ??? ??? ???????????? alert??? ?????????
                //room_verfity_button.setVisibility(View.INVISIBLE);
            }
        });

        ReadFirestoreData();

        //***** ????????? ?????????????????? ?????? ??????*****
        partitionRef= firebaseDatabase.getReference("/Chat/"+pos+"/partitions"); //ref ????????? ??????!
        //'partitions'????????? ???????????? ?????? ??????????????? ????????????
        partitionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //????????? ?????? DataSnapshot ?????? ????????????.
                //???????????? ????????? ?????????  clear()
                peopleItemArrayList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())           //?????? ?????? ????????? ?????????
                {
                    PeopleItem partition = ds.getValue(PeopleItem.class);
                    //order_price+=partition.getMenu_price(); //***** ?????? ?????? ?????????????????? ?????? ??????????????????!!!

                    if(partition.getId()!=null&&!partition.getId().equals(LoginActivity.nickname)) {

                        //????????? ???????????? ???????????? ???????????? ?????? ArrayList??? ??????
                        peopleItemArrayList.add(partition);

                        cur_people = peopleItemArrayList.size()+1;
                        people_size=cur_people;

                        price_per_person = MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() / people_size;
                        room_delivery_price.setText("?????????: " + MainActivity.roomItemArrayList.get(pos).getDeliveryPrice() + "??? (1?????? : " + price_per_person + ")");

                        //??????????????? ??????
                        peopleListAdapter.notifyDataSetChanged();
                    }

                    //***** ??????: ????????? ???????????? ????????? ????????? ?????????????????? ???????????? ???????????????!!!

                    if(partition.getId()!=null&&partition.getId().equals(LoginActivity.nickname)){ //nickname??? ????????? ???
                        my_menu_item=new MyItem(partition.getId(),partition.getStatus(),partition.getMenu_name(),partition.getMenu_price(),partition.getExpiration_time(),partition.getTx_hash(),partition.getSendingStatus(),partition.getVerification_status());
                        if(my_menu_item.getId()!=null){
                            if(my_menu_item.getStatus()==false){
                                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //?????? ?????? - ??????
                            }
                            else{
                                room_my_status.setColorFilter(Color.parseColor("#FF028BBB")); //????????? -?????????
                                room_verfity_button.setVisibility(View.INVISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                                room_pay_button.setVisibility(View.VISIBLE);
                                room_ready_cancel_button.setVisibility(View.VISIBLE);
                            }
                            //Toast.makeText(getApplicationContext(), "** sending status: "+my_menu_item.getSendingStatus(), Toast.LENGTH_SHORT).show();
                            //?????? ?????? ???
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

        //Firebase DB?????? ????????? 'caht'?????? ???????????? ????????????
        chatRef= firebaseDatabase.getReference("/Chat/"+pos+"/chat"); //ref ????????? ??????!
        //firebaseDB?????? ?????? ???????????? ????????? ????????????..
        //'chat'????????? ???????????? ?????? ??????????????? ????????????
        //chatRef??? ???????????? ???????????? ?????? ?????? ????????? ??????
        chatItemArrayList.clear();
        chatRef.addChildEventListener(new ChildEventListener() {
            //?????? ????????? ?????? ??? ValueListener??? ????????? ?????? ???????????? ???????????? ?????? ?????? ???
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //?????? ????????? ?????????(??? : MessageItem??????) ????????????
                ChatItem messageItem= dataSnapshot.getValue(ChatItem.class);

                //????????? ???????????? ???????????? ???????????? ?????? ArrayList??? ??????
                chatItemArrayList.add(messageItem);


                //??????????????? ??????
                chatListAdapter.notifyDataSetChanged();
                chatListView.setSelection(chatItemArrayList.size()-1); //??????????????? ????????? ????????? ????????? ?????? ??????
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
                //?????? ??????
                //Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();

                String text=room_chat_text.getText().toString();

//                Calendar calendar= Calendar.getInstance(); //?????? ??????
//                String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

                Date today = new Date();
                SimpleDateFormat format1 = new SimpleDateFormat("HHmmss");
                SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
                String id_time=format1.format(today);
                String time=format2.format(today);

                ChatItem messageItem= new ChatItem(LoginActivity.nickname,text,time);
                //'char'????????? MessageItem????????? ??????
                chatRef.child(id_time).setValue(messageItem);

                //EditText??? ?????? ?????? ?????????
                room_chat_text.setText("");

                //????????????????????? ???????????????..
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
                        //??????????????? ????????????
                        return true;
                    case R.id.my:
                        Intent my_intent=new Intent(getApplicationContext(), MypageActivity.class);
                        startActivity(my_intent);
                        finish();
                        //Toast.makeText(getApplicationContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

    }


    private void ReadFirestoreData(){
        //*****Firestore ????????? ?????? ??????******
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
                        roomItem = new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString(),snapshot.getData().get("x").toString(),snapshot.getData().get("y").toString());
                    }else{
                        roomItem= new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString());
                    }

                    int delivery_price_per_person=roomItem.getDeliveryPrice() / roomItem.getCurrentPeople();
                    room_delivery_price.setText("?????????: "+roomItem.getDeliveryPrice()+"??? (1?????? : "+delivery_price_per_person+")");

                    MainActivity.roomItemArrayList.set(pos, roomItem);
                    MainActivity.roomLIstAdapter.notifyDataSetChanged();

                } else { }
            }
        });
    }


}