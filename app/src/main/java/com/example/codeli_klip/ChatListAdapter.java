package com.example.codeli_klip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {

    Context mContext=null;
    LayoutInflater layoutInflater=null;
    ArrayList<ChatItem> chatItemArrayList;

    public ChatListAdapter(Context context, ArrayList<ChatItem> item){
        mContext=context;
        chatItemArrayList=item;
        layoutInflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return chatItemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ChatItem getItem(int position) {
        return chatItemArrayList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //현재 보여줄 번째의(position)의 데이터로 뷰를 생성
        ChatItem item=chatItemArrayList.get(position);

        //재활용할 뷰는 사용하지 않음!!
        View itemView=null;

        //메세지가 내 메세지인지??
        if(item.getName().equals(LoginActivity.nickname)){
            itemView= layoutInflater.inflate(R.layout.activity_chat_mymsg,parent,false);
        }else{
            itemView= layoutInflater.inflate(R.layout.activity_chat_othermsg,parent,false);
        }

        //만들어진 itemView에 값들 설정
        //ImageView my_iamge= itemView.findViewById(R.id.mychat_user_image);
        TextView my_name= itemView.findViewById(R.id.user_name);
        TextView my_message= itemView.findViewById(R.id.user_message);
        TextView my_time= itemView.findViewById(R.id.current_time);

        my_name.setText(item.getName());
        my_message.setText(item.getMessage());
        my_time.setText(item.getTime());


        return itemView;

    }
}
