package com.example.codeli_klip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class RoomChatFragment extends Fragment {

    private ImageButton room_chat_button; //채팅 전송 버튼
    private EditText room_chat_text; //채팅 메세지 입력

    private int pos=0;

    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatItem> chatItemArrayList = new ArrayList<ChatItem>();

    //Firebase Database 관리 객체참조변수
    private FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    private DatabaseReference chatRef;

    private DatabaseReference chat_user_Ref;

    private String room_id = "";


    public RoomChatFragment(int pos){
        this.pos=pos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_roomchat_fragment, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance(); //파이어베이스 설정
        //채팅 리스트
        final ListView chatListView = root.findViewById(R.id.room_chat_list);
        chatListAdapter = new ChatListAdapter(getContext(), chatItemArrayList);
        chatListView.setAdapter(chatListAdapter);

        //Firebase DB관리 객체와 'caht'노드 참조객체 얻어오기
        chatRef = firebaseDatabase.getReference("/Chat/" + pos + "/chat"); //ref 맞는지 확인!
        //firebaseDB에서 채팅 메세지들 실시간 읽어오기..
        //'chat'노드에 저장되어 있는 데이터들을 읽어오기
        //chatRef에 데이터가 변경되는 것으 듣는 리스너 추가
        chatItemArrayList.clear();
        chatRef.addChildEventListener(new ChildEventListener() {
            //새로 추가된 것만 줌 ValueListener는 하나의 값만 바뀌어도 처음부터 다시 값을 줌
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //새로 추가된 데이터(값 : MessageItem객체) 가져오기
                ChatItem messageItem = dataSnapshot.getValue(ChatItem.class);

                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                chatItemArrayList.add(messageItem);


                //리스트뷰를 갱신
                chatListAdapter.notifyDataSetChanged();
                chatListView.setSelection(chatItemArrayList.size() - 1); //리스트뷰의 마지막 위치로 스크롤 위치 이동
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //chat message send
        room_chat_text = root.findViewById(R.id.room_chat_text);
        room_chat_button = root.findViewById(R.id.room_chat_button);
        room_chat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅 전송
                //Toast.makeText(getApplicationContext(), "메세지 전송", Toast.LENGTH_SHORT).show();

                String text = room_chat_text.getText().toString();

//                Calendar calendar= Calendar.getInstance(); //현재 시간
//                String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

                Date today = new Date();
                SimpleDateFormat format1 = new SimpleDateFormat("HHmmss");
                SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
                String id_time = format1.format(today);
                String time = format2.format(today);

                ChatItem messageItem = new ChatItem(LoginActivity.nickname, text, time);
                //'char'노드에 MessageItem객체를 통해
                chatRef.child(id_time).setValue(messageItem);

                //EditText에 있는 글씨 지우기
                room_chat_text.setText("");

                //소프트키패드를 안보이도록..
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                chatListAdapter.notifyDataSetChanged();

            }
        });

        return root;
    }

}