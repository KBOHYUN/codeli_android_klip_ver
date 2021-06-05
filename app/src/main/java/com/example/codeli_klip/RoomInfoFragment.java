package com.example.codeli_klip;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomInfoFragment extends Fragment {

    public static int pos=0;

    private RecyclerView recyclerview;

    private Button room_ready_button; //준비 버튼
    private Button room_ready_cancel_button; //준비 취소 버튼
    private Button room_pay_button; //결제 버튼
    private Button room_verfity_button;//검증 버튼

    private TextView room_order_price; //최소주문금액
    private TextView room_delivery_price;//배달금액
    private TextView room_platform; //배달 플랫폼
    private TextView room_delivery_place; //배달 장소
    private TextView room_delivery_time; //배달 시간

    private ImageView room_my_status;
    private TextView room_my_nickname;
    private TextView room_my_menu;
    private TextView room_my_price;

    private int people_size=1;

    private int cur_people;
    private int price_per_person;

    public static MyItem my_menu_item;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chatRef;
    //'partition'노드 참조객체 변수
    private DatabaseReference partitionRef;
    private DatabaseReference chat_user_Ref;

    private DatabaseReference verification_ref;


    private DatabaseReference klay_Ref; //클레이 시세 받아오는 참조변수
    private ArrayList<KlayData> klayDataArrayList=new ArrayList<KlayData>();
    private double klay_flow;

    private String room_id="";

    private FirebaseFirestore firestore;

    private Map<String, Object> roomValue = null;

    private int delivery_price_per_person;


    public RoomInfoFragment(int pos){
        this.pos=pos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup root=(ViewGroup) inflater.inflate(R.layout.activity_roominfo_fragment,container,false);

        firebaseDatabase= FirebaseDatabase.getInstance(); //파이어베이스 설정
        firestore = FirebaseFirestore.getInstance(); //파이어스토어 설정

        //개인별 주문 읍식 나타날 리스트
        ListView listView=root.findViewById(R.id.room_people_list);
        peopleListAdapter=new PeopleListAdapter(getContext(), peopleItemArrayList);
        listView.setAdapter(peopleListAdapter);

        room_order_price=root.findViewById(R.id.room_minimum_price);
        room_delivery_price=root.findViewById(R.id.room_delivery_price);
        room_platform=root.findViewById(R.id.room_platform);
        room_delivery_place=root.findViewById(R.id.room_delivery_place);
        room_delivery_time=root.findViewById(R.id.room_delivery_time);

        room_platform.setText("사용플랫폼: "+MainActivity.roomItemArrayList.get(pos).getPlatform());
        room_order_price.setText("최소주문금액: "+MainActivity.roomItemArrayList.get(pos).getOrderPrice()+"원");
        price_per_person=MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()/MainActivity.roomItemArrayList.get(pos).getCurrentPeople();
        room_delivery_price.setText("배달팁: "+MainActivity.roomItemArrayList.get(pos).getDeliveryPrice()+"원 (1인당 : "+price_per_person+")");
        room_delivery_place.setText("배달장소: "+MainActivity.roomItemArrayList.get(pos).getAddress()+" "+MainActivity.roomItemArrayList.get(pos).getSpecificAddress());
        //약속시간 텍스트 설정
        if(MainActivity.roomItemArrayList.get(pos).getTime()!=null){
            String replaceTime=MainActivity.roomItemArrayList.get(pos).getTime();
            room_delivery_time.setText("약속시간: "+replaceTime+"   ");
        }

        room_my_status=root.findViewById(R.id.room_my_status);
        room_my_nickname=root.findViewById(R.id.room_my_nickname);
        room_my_menu=root.findViewById(R.id.room_my_menu);
        room_my_price=root.findViewById(R.id.room_my_price);

        room_my_nickname.setText(LoginActivity.nickname);
        room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강

        //나의 주문 목록 초기화
        my_menu_item=new MyItem(LoginActivity.nickname,false, "",0,0,"","",false);

        chat_user_Ref= firebaseDatabase.getReference("/Chat/"+pos+"/partitions/"+ LoginActivity.nickname); //채팅 reference

        klay_Ref=firebaseDatabase.getReference("/klay_value/"); //클레이 reference

        //결제 버튼
        Intent intent=new Intent(getActivity(),PayActivity.class);
        room_pay_button=root.findViewById(R.id.room_pay_button);
        room_pay_button.setVisibility(View.INVISIBLE);
        room_pay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                //******** 임시로 결제 화면 넘어가기 *******
//                intent.putExtra("menu_price",my_menu_item.getMenu_price());
//                intent.putExtra("delivery_price",delivery_price_per_person);
//                intent.putExtra("room_id",""+pos); //방 번호
//                intent.putExtra("klay_flow",123); //클레이 시세
//                intent.putExtra("klay_total",0);
//                intent.putExtra("my_menu_item",my_menu_item); //메뉴 데이터
//                intent.putExtra("position",pos);
//                startActivity(intent);
//                getActivity().finish();


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

                            int total=my_menu_item.getMenu_price();
                            double klay_price=total/klay_flow;
                            double total_klay_6=Double.parseDouble(String.format("%.6f",klay_price));

                            if(getActivity()!=null){
                                intent.putExtra("menu_price",my_menu_item.getMenu_price());
                                intent.putExtra("delivery_price",delivery_price_per_person);
                                intent.putExtra("room_id",room_id); //방 번호
                                intent.putExtra("klay_flow",klay_flow); //클레이 시세
                                intent.putExtra("klay_total",total_klay_6);
                                intent.putExtra("my_menu_item",my_menu_item); //메뉴 데이터
                                intent.putExtra("position",pos);
                                startActivity(intent);
                                getActivity().finish();
                            }



                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

            }
        });

        //준비 취소 버튼
        room_ready_cancel_button=root.findViewById(R.id.room_ready_cancel_button);
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

                delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

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
        room_ready_button=root.findViewById(R.id.room_ready_button);
        room_ready_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //준비 완료 확인 -> 데이터 db 전송!


                //내 메뉴 db에 전송
                String menu=room_my_menu.getText().toString().trim();
                int price=Integer.parseInt(room_my_price.getText().toString().trim());
                my_menu_item=new MyItem(LoginActivity.nickname,true, menu, price);
                room_my_status.setColorFilter(Color.parseColor("#a7ca5d")); //준비돰 - 초록
                chat_user_Ref.setValue(my_menu_item);

                //****파이어스토어에 데이터 업데이트 하기
                //메뉴 가격, 현재 인원 증가 -> 1인당 배달비 업데이트

                int current_price=MainActivity.roomItemArrayList.get(pos).getCurrentOrderPrice()+price;
                int current_people=MainActivity.roomItemArrayList.get(pos).getCurrentPeople()+1;

                RoomItem roomItem= new RoomItem(MainActivity.roomItemArrayList.get(pos).getName(),MainActivity.roomItemArrayList.get(pos).getPlatform(),current_price,MainActivity.roomItemArrayList.get(pos).getOrderPrice(),MainActivity.roomItemArrayList.get(pos).getDeliveryPrice(),MainActivity.roomItemArrayList.get(pos).getAddress(),MainActivity.roomItemArrayList.get(pos).getSpecificAddress(),current_people,MainActivity.roomItemArrayList.get(pos).getTotalPeople(),MainActivity.roomItemArrayList.get(pos).getOwner(),MainActivity.roomItemArrayList.get(pos).getX(),MainActivity.roomItemArrayList.get(pos).getY());
                roomValue=roomItem.toMap();

                delivery_price_per_person=roomItem.getDeliveryPrice() / current_people;

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


        room_verfity_button=root.findViewById(R.id.verify_button);
        room_verfity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //my_menu_item.setVerification_status(true);

                //MyItem data=new MyItem(my_menu_item.getId(),my_menu_item.getStatus(),my_menu_item.getMenu_name(),my_menu_item.getMenu_price(),0,"","",true);
                my_menu_item.setVerification_status(true);
                chat_user_Ref.setValue(my_menu_item);
                //Toast.makeText(getActivity(), "배달 주문 검증이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                // timer trigger 변경 시 동작하도록 변경하기!!!!!!!!!
                //백그라운드 서비스 종료
//                Intent intent =new Intent(getActivity(), BackgroundGPS.class);
//                System.out.println("종료");
//                getActivity().stopService(intent);

                //->>>> 위치 확인 후 도착했다고 확인되면 도착 확인 메세지 출력

                //한 번 더 확인하는 alert창 띄우기
                //room_verfity_button.setVisibility(View.INVISIBLE);
            }
        });


        verification_ref= firebaseDatabase.getReference("/Chat/"+pos+"/verification/"); //방장 송금 요청 verification reference
        verification_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()

                VerificationData verification=dataSnapshot.getValue(VerificationData.class);

                if(verification!=null && verification.getTrigger()==true){
                    if(verification.getState()!=null){
                        if(verification.getState().equals("success")){
                            Toast.makeText(getActivity(),"방장 지갑으로 송금 완료되었습니다",Toast.LENGTH_LONG).show();
                        }
                        else if(verification.getState().equals("room_manager_not_here")){
                            Toast.makeText(getActivity(),"전부 환불되었습니다",Toast.LENGTH_LONG).show();
                        }
                        else{
                            String noshow="";
                            if(verification.getComment()!=null){
                                noshow=verification.getComment();
                                Toast.makeText(getActivity(),"No-show 인원이 있습니다 : "+noshow,Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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

                    if(partition.getId()!=null&&partition.getId().equals(LoginActivity.nickname)){ //nickname이 자신일 경우
                        my_menu_item=new MyItem(partition.getId(),partition.getStatus(),partition.getMenu_name(),partition.getMenu_price(),partition.getExpiration_time(),partition.getTx_hash(),partition.getSendingStatus(),partition.getVerification_status());
                        if(my_menu_item.getId()!=null){
                            if(my_menu_item.getStatus()==false){
                                room_my_status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강
                            }
                            else{
                                room_my_status.setColorFilter(Color.parseColor("#a7ca5d")); //준비됨 -파랑
                                room_verfity_button.setVisibility(View.INVISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                                room_pay_button.setVisibility(View.VISIBLE);
                                room_ready_cancel_button.setVisibility(View.VISIBLE);

                            }
                            //Toast.makeText(getApplicationContext(), "** sending status: "+my_menu_item.getSendingStatus(), Toast.LENGTH_SHORT).show();

                            //결제 성공 시
                            if(my_menu_item.getSendingStatus()!=null&&my_menu_item.getSendingStatus().equals("success")){
                                room_my_status.setColorFilter(Color.parseColor("#FFD869")); //결제 완료시 노랑
                                room_verfity_button.setVisibility(View.VISIBLE);
                                room_ready_button.setVisibility(View.INVISIBLE);
                                room_pay_button.setVisibility(View.INVISIBLE);
                                room_ready_cancel_button.setVisibility(View.INVISIBLE);

                            }
                            if(my_menu_item.getVerification_status()==true){
                                //room_verfity_button.setVisibility(View.VISIBLE);
                                room_my_status.setColorFilter(Color.parseColor("#a7ca5d")); //결제 완료시 노랑
                            }
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

        //상태 정보 알려주는 recycler view
        recyclerview = root.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        List<ExpandableListAdapter.Item> data = new ArrayList<>();

        ExpandableListAdapter.Item status = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "상태 색상 정보");
        status.invisibleChildren = new ArrayList<>();
        status.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "준비 안됨","#FF0000"));
        status.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "준비됨","#FF028BBB"));
        status.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "결제 완료","#FFD869"));
        status.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "위치 검증 완료","#a7ca5d"));

        data.add(status);

        recyclerview.setAdapter(new ExpandableListAdapter(data));



        return root;
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
                            //약속 시간 변경시 업데이트
                            String replaceTime=snapshot.getData().get("time").toString();
                            room_delivery_time.setText("약속시간: "+replaceTime+"   ");
                        }
                        else{
                            roomItem = new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString(),snapshot.getData().get("x").toString(),snapshot.getData().get("y").toString());
                        }

                    }else{
                        roomItem= new RoomItem(snapshot.getData().get("restaurant").toString(),snapshot.getData().get("deliveryApp").toString(),Integer.parseInt(snapshot.getData().get("currentValue").toString()),Integer.parseInt(snapshot.getData().get("minOrderAmount").toString()),Integer.parseInt(snapshot.getData().get("deliveryCost").toString()),snapshot.getData().get("deliveryAddress").toString(),snapshot.getData().get("deliveryDetailAddress").toString(),Integer.parseInt(snapshot.getData().get("participantsNum").toString()),Integer.parseInt(snapshot.getData().get("participantsMax").toString()),snapshot.getData().get("owner").toString());
                    }

                    delivery_price_per_person=roomItem.getDeliveryPrice() / roomItem.getCurrentPeople();
                    room_delivery_price.setText("배달팁: "+roomItem.getDeliveryPrice()+"원 (1인당 : "+delivery_price_per_person+")");

                    MainActivity.roomItemArrayList.set(pos, roomItem);
                    MainActivity.roomLIstAdapter.notifyDataSetChanged();

                } else { }
            }
        });
    }

}