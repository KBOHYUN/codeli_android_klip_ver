package com.example.codeli_klip;

import java.util.HashMap;
import java.util.Map;

public class RoomItem {
    private String r_name; //가게 이름
    private String r_platform=""; //플랫폼
    private int r_cur_order_price; //현재 채워진 금액
    private int r_order_price; //최소 주문 금액
    private int r_delivery_price; //배달팁
    private String r_address; //배달 주소
    private String r_specific_address; //세부 주소
    private int r_cur_people; //현재 인원
    private int r_tot_people; //목표 인원
    private String r_owner; //방장

    //약속 시간 추가

    private Double x; //위도
    private Double y; //경도

    public RoomItem(){

    }

    public RoomItem(String name, String platform, int cur_order_price, int order_price, int deliver_price, String address, String specific_address, int cur_people, int tot_people){
        this.r_name=name;
        this.r_platform=platform;
        this.r_cur_order_price=cur_order_price;
        this.r_order_price=order_price;
        this.r_delivery_price=deliver_price;
        this.r_address=address;
        this.r_specific_address=specific_address;
        this.r_cur_people=cur_people;
        this.r_tot_people=tot_people;
    }


    public RoomItem(String name, String platform, int cur_order_price, int order_price, int deliver_price, String address, String specific_address, int cur_people, int tot_people,String owner){
        this.r_name=name;
        this.r_platform=platform;
        this.r_cur_order_price=cur_order_price;
        this.r_order_price=order_price;
        this.r_delivery_price=deliver_price;
        this.r_address=address;
        this.r_specific_address=specific_address;
        this.r_cur_people=cur_people;
        this.r_tot_people=tot_people;
        this.r_owner=owner;
    }

    //위도 경도 추가
    public RoomItem(String name, String platform, int cur_order_price, int order_price, int deliver_price, String address, String specific_address, int cur_people, int tot_people,String owner, Double x, Double y){
        this.r_name=name;
        this.r_platform=platform;
        this.r_cur_order_price=cur_order_price;
        this.r_order_price=order_price;
        this.r_delivery_price=deliver_price;
        this.r_address=address;
        this.r_specific_address=specific_address;
        this.r_cur_people=cur_people;
        this.r_tot_people=tot_people;
        this.r_owner=owner;
        this.x=x;
        this.y=y;
    }

    //-> 약속 시간 추가하기

    public String getName(){
        return this.r_name;
    }
    public String getPlatform(){return this.r_platform;}
    public int getCurrentOrderPrice(){
        return this.r_cur_order_price;
    }
    public int getOrderPrice(){
        return this.r_order_price;
    }
    public int getDeliveryPrice(){
        return this.r_delivery_price;
    }
    public String getAddress(){
        return this.r_address;
    }
    public String getSpecificAddress(){
        return this.r_specific_address;
    }
    public int getCurrentPeople(){
        return this.r_cur_people;
    }
    public int getTotalPeople(){
        return this.r_tot_people;
    }
    public String getOwner(){
        return this.r_owner;
    }
    public Double getX(){
        return this.x;
    }
    public Double getY(){
        return this.y;
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("restaurant", r_name);
        result.put("deliveryApp", r_platform);
        result.put("currentValue", r_cur_order_price);
        result.put("minOrderAmount", r_order_price);
        result.put("deliveryCost", r_delivery_price);
        result.put("deliveryAddress",r_address);
        result.put("deliveryDetailAddress",r_specific_address);
        result.put("participantsNum", r_cur_people);
        result.put("participantsMax", r_tot_people);
        result.put("owner",r_owner);
        //방장 정보, 참여 인원 id 정보도 추가

        //위도 경도 추가
        result.put("x",x);
        result.put("y",y);

        return result;
    }



}
