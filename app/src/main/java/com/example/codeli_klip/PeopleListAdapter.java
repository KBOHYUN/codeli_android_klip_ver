package com.example.codeli_klip;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PeopleListAdapter extends BaseAdapter {

    private TextView nickname;
    private ImageView status;
    private TextView menu_name;
    private TextView menu_price;

    Context mContext=null;
    LayoutInflater layoutInflater=null;
    ArrayList<PeopleItem> peopleItemArrayList;

    public PeopleListAdapter(Context context, ArrayList<PeopleItem> item){
        mContext=context;
        peopleItemArrayList=item;
        layoutInflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return peopleItemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public PeopleItem getItem(int position) {
        return peopleItemArrayList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.activity_people_list, null);

        nickname=view.findViewById(R.id.people_nickname);
        status=view.findViewById(R.id.people_status);
        menu_name=view.findViewById(R.id.people_menu);
        menu_price=view.findViewById(R.id.people_price);

        nickname.setText(peopleItemArrayList.get(position).getId());
        boolean color_status=peopleItemArrayList.get(position).getStatus();
        if(color_status==false){
            status.setColorFilter(Color.parseColor("#FF0000")); //준비 안됨 - 빨강
        }
        else{
            //파란색 - FF028BBB
            status.setColorFilter(Color.parseColor("#a7ca5d")); //준비됨 -초록
        }


        if(peopleItemArrayList.get(position).getSendingStatus()!=null&&peopleItemArrayList.get(position).getSendingStatus().equals("success")){
            status.setColorFilter(Color.parseColor("#FFD869")); //결제 완료 - 노랑
        }

        if(peopleItemArrayList.get(position).getVerification_status()==true){
            status.setColorFilter(Color.parseColor("#a7ca5d")); //검증 완료 - 초록
        }

        menu_name.setText(peopleItemArrayList.get(position).getMenu_name());
        menu_price.setText(peopleItemArrayList.get(position).getMenu_price()+"");

        return view;
    }
}