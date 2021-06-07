package com.example.codeli_klip;

import java.io.Serializable;

public class PeopleItem implements Serializable {
    private int expiration_time;
    private String id;
    private boolean status=false;
    private String menu_name;
    private int menu_price;
    private String tx_hash;
    private String sendingStatus;
    private boolean verification_status=false;
    private boolean location_verification_status=false;

    //위 경도 추가
    private Double x;
    private Double y;

    private boolean sendToManager=false;

    public  PeopleItem(){

    }

    public PeopleItem(String id, boolean status, String name, int price){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
    }

    public PeopleItem(String id, boolean status, String name, int price, boolean location_verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.location_verification_status=location_verification_status;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time,String sending_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.sendingStatus=sending_status;
    }


    public PeopleItem(String id, boolean status, String name, int price, int expiration_time, String tx_hash, String sending_status, boolean verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time, String tx_hash, String sending_status, boolean verification_status, boolean location_verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
        this.location_verification_status=location_verification_status;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time,String sending_status, boolean verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time,String tx_hash, String sending_status, boolean verification_status, double x, double y){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
        this.x=x;
        this.y=y;
    }

    public PeopleItem(String id, boolean status, String name, int price, int expiration_time,String tx_hash, String sending_status, boolean verification_status, boolean location_verification_status,double x, double y){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
        this.location_verification_status=location_verification_status;
        this.x=x;
        this.y=y;
    }



    public PeopleItem(String id, boolean status, String name, int price, int expiration_time,String tx_hash, String sending_status, boolean verification_status, boolean location_verification_status,boolean sendToManager,double x, double y){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.expiration_time=expiration_time;
        this.tx_hash=tx_hash;
        this.sendingStatus=sending_status;
        this.verification_status=verification_status;
        this.location_verification_status=location_verification_status;
        this.x=x;
        this.y=y;
        this.sendToManager=sendToManager;
    }


    public String getId(){return id;}
    public boolean getStatus(){return status;}
    public String getMenu_name(){return menu_name;}
    public int getMenu_price(){return menu_price;};
    public int getExpiration_time(){
        return expiration_time;
    }
    public String getTx_hash(){
        return this.tx_hash;
    }
    public String getSendingStatus(){
        return sendingStatus;
    }
    public boolean getVerification_status(){
        return verification_status;
    }
    public boolean getLocation_verification_status(){
        return location_verification_status;
    }
    public Double getX(){return this.x;}
    public Double getY(){
        return this.y;
    }
    public boolean getSendToManager(){
        return this.sendToManager;
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
}
