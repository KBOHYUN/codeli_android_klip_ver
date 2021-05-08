package com.example.codeli_klip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class RoomLIstAdapter extends BaseAdapter {

    Context mContext=null;
    LayoutInflater layoutInflater=null;
    ArrayList<RoomItem> roomItemArrayList;

    public RoomLIstAdapter(Context context, ArrayList<RoomItem> item){
        mContext=context;
        roomItemArrayList=item;
        layoutInflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return roomItemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RoomItem getItem(int position) {
        return roomItemArrayList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.activity_room_list, null);

        TextView id=view.findViewById(R.id.list_id);
        ProgressBar progressBar=view.findViewById(R.id.list_progress_bar);
        TextView price_progress=view.findViewById(R.id.list_price_progress);
        TextView price=view.findViewById(R.id.list_price);
        TextView price_delivery=view.findViewById(R.id.list_price_delivery);
        TextView location=view.findViewById(R.id.list_location);
        TextView person=view.findViewById(R.id.list_person);

        id.setText(roomItemArrayList.get(position).getName());
        double progress=((double)roomItemArrayList.get(position).getCurrentOrderPrice() / (double)roomItemArrayList.get(position).getOrderPrice())*100;
        progressBar.setProgress((int)progress);
        price_progress.setText("￦"+roomItemArrayList.get(position).getCurrentOrderPrice());
        price.setText(roomItemArrayList.get(position).getOrderPrice()+"원");
        price_delivery.setText("(배달팁: "+roomItemArrayList.get(position).getDeliveryPrice()+"원)");
        location.setText(roomItemArrayList.get(position).getAddress()+" "+roomItemArrayList.get(position).getSpecificAddress());
        person.setText(roomItemArrayList.get(position).getCurrentPeople() + " / "+ roomItemArrayList.get(position).getTotalPeople());

        return view;
    }
}
