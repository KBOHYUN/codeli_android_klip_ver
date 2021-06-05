package com.example.codeli_klip;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RoomOwnerInfoFragment extends Fragment {

    private int pos=0;

    private RecyclerView recyclerview;

    private Calendar calendar;

    private int order_price;

    private Button verfity_trigger_button;//검증 버튼
    private Button room_time_edit_button; //약속시간 변경 버튼

    private TextView room_order_price; //최소주문금액
    private TextView room_delivery_price;//배달금액
    private TextView room_platform; //배달 플랫폼
    private TextView room_delivery_place; //배달 장소
    private TextView room_delivery_time; //배달 시간

    private int people_size=1;

    private int cur_people;
    private int price_per_person;

    private MyItem my_menu_item;
    private PeopleListAdapter peopleListAdapter;
    private ArrayList<PeopleItem> peopleItemArrayList = new ArrayList<PeopleItem>();

    private boolean sendingStatusCheck=false;

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'partition'노드 참조객체 변수
    private DatabaseReference partitionRef;
    private DatabaseReference verification_ref;
    private DatabaseReference verification_state_ref;

    private FirebaseFirestore firestore;

    Map<String, Object> roomValue = null;

    private VerificationData verification;
    private ArrayList<VerificationData> verificationDataArrayList=new ArrayList<VerificationData>();


    public RoomOwnerInfoFragment(int pos){
        this.pos=pos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup root=(ViewGroup) inflater.inflate(R.layout.activity_roomowner_info_fragment,container,false);

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

        if(MainActivity.roomItemArrayList.get(pos).getTime()!=null){
            String replaceTime=MainActivity.roomItemArrayList.get(pos).getTime();
            room_delivery_time.setText("약속시간: "+replaceTime+"   ");
        }

        ReadFirestoreData();  //firestore 데이터 실시간 읽기

        //약속 시간 설정
        Date today=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        SimpleDateFormat hourFormat = new SimpleDateFormat("hh");
        SimpleDateFormat minFormat = new SimpleDateFormat("mm");
        final String datetime = dateFormat.format(today);

        calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR);
        int min=calendar.get(Calendar.MINUTE);
        room_time_edit_button=root.findViewById(R.id.room_time_modify_button);
        room_time_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog timePickerDialog=new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time=" ";

                        if(hourOfDay<10){
                            time+="0"+hourOfDay;
                        }else{
                            time+=hourOfDay;
                        }
                        time+=":";
                        if(minute<10){
                            time+="0"+minute;
                        }else{
                            time+=minute;
                        }

                        room_delivery_time.setText("약속시간: "+ datetime+time);


                        //firestore db

                        RoomItem roomItem=MainActivity.roomItemArrayList.get(pos);
                        roomItem.setTime(datetime+time);
                        roomValue=roomItem.toMap_time();

                        firestore.collection("Rooms")
                                .document(""+pos)
                                .set(roomValue)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        MainActivity.roomLIstAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.w(TAG, "Error adding document", e);
                                    }
                                });



                    }
                },hour,min,false);
                timePickerDialog.show();
            }
        });


        verification_ref= firebaseDatabase.getReference("/Chat/"+pos+"/verification/"); //방장 송금 요청 verification reference
        //송금 요청 버튼
        verfity_trigger_button=root.findViewById(R.id.verify_trigger_button);
        //verfity_trigger_button.setVisibility(View.GONE);
        verfity_trigger_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verification=new VerificationData(LoginActivity.klip_address,true);
                verification_ref.setValue(verification);

                Toast.makeText(getContext(), "송금이 요청되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        verification_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()

                    verification=dataSnapshot.getValue(VerificationData.class);

                    if(verification!=null){
                        if(verification.getTrigger()==true && verification.getState()!=null){
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
                                else{
                                    Toast.makeText(getActivity(),"No-show 인원이 있습니다",Toast.LENGTH_LONG).show();Toast.makeText(getActivity(),"No-show 인원이 있습니다 : "+noshow,Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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

                    if(partition.getId()!=null&&!partition.getId().equals(LoginActivity.nickname)) {

                        //새로운 참여자 정보를 리스뷰에 추가하기 위해 ArrayList에 추가
                        peopleItemArrayList.add(partition);

                        //결제하지 않은 인원이 있는 경우 check=false
                        if(partition.getVerification_status()==true){
                            sendingStatusCheck=true;
                        }
                        else{
                            sendingStatusCheck=false;
                        }


                        //리스트뷰를 갱신
                        peopleListAdapter.notifyDataSetChanged();
                    }
                }

//                //송금하였는지 확인
//                if(sendingStatusCheck==true) {
//                    //Toast.makeText(getActivity().getApplicationContext(),"모든 인원이 결제를 완료하였습니다\n음식 수령 시 검증 절차 후 송금 요청을 진행해주세요",Toast.LENGTH_LONG).show();
//                    verfity_trigger_button.setVisibility(View.VISIBLE);
//
//                    AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getActivity())
//                            .setTitle("지급요청 알림")
//                            .setMessage("모든 인원이 위치 검증 절차 완료하였습니다\n지급 요청을 진행해주세요")
//                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    //finish;
//                                    dialogInterface.dismiss();
//                                }
//                            });
//                    AlertDialog msgDlg = msgBuilder.create();
//                    msgDlg.show();
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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
                            room_delivery_time.setText("약속시간: "+replaceTime+"  ");
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