package com.example.codeli_klip;

import java.io.Serializable;

public class MyItem implements Serializable {

    private int expiration_time;
    private String id;
    private boolean status=false;
    private String menu_name;
    private int menu_price;
    private String tx_hash;
    private String sendingStatus;
    private boolean verification_status=false;

    //위 경도 추가
    private Double latitude;
    private Double longitude;


    MyItem(){

    }

    public MyItem(String id, boolean status, String name, int price){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
    }


    public MyItem(String id, boolean status, String name, int price, boolean verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.verification_status=verification_status;
    }

    public MyItem(String id, boolean status, String name, int price, boolean verification_status, double latitude, double longitude){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.verification_status=verification_status;
        this.latitude=latitude;
        this.longitude=longitude;
    }


    public MyItem(String id, boolean status, String name, int price, int expiration_time, String tx_hash, String sending_status, boolean verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
    }


    public MyItem(String id, boolean status, String name, int price, int expiration_time,String tx_hash, String sending_status, boolean verification_status, double latitude, double longitude){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
        this.latitude=latitude;
        this.longitude=longitude;
    }


    public String getId(){return id;}
    public boolean getStatus(){return status;}
    public String getMenu_name(){return menu_name;}
    public int getMenu_price(){return menu_price;};
    public int getExpiration_time(){
        return expiration_time;
    }
    public String getSendingStatus(){
        return sendingStatus;
    }
    public boolean getVerification_status(){
        return verification_status;
    }
    public String getTx_hash(){
        return this.tx_hash;
    }
    public Double getLatitude(){
        return this.latitude;
    }
    public Double getLongitude(){
        return this.longitude;
    }


    public void setMenu_name(String menu_name){
        this.menu_name=menu_name;
    }
    public void setMenu_price(int menu_price){
        this.menu_price=menu_price;
    }
    public void setExpiration_time(int time){
        this.expiration_time=time;
    }
    public void setTx_hash(String tx_hash){
        this.tx_hash=tx_hash;
    }
    public void setSending_status(String status){
        this.sendingStatus=status;
    }
    public void setVerification_status(boolean status){
        this.verification_status=status;
    }
    public void setLatitude(double latitude){
        this.latitude=latitude;
    }
    public void setLongitude(double longitude){
        this.longitude=longitude;
    }
}
