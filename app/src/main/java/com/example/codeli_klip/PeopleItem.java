package com.example.codeli_klip;

import java.io.Serializable;

public class PeopleItem implements Serializable {
    private int UID;
    private String id;
    private boolean status=false;
    private String menu_name;
    private int menu_price;
    private String verification;
    private int expiration_time;
    private boolean sending_status;
    private int tx_hash;
    private boolean verfication_status;

//    public PeopleItem(String id, boolean status, String name, int price, String verification){
//        this.id=id;
//        this.status=status;
//        this.menu_name=name;
//        this.menu_price=price;
//        this.verification=verification;
//    }

    public PeopleItem(String id, boolean status, String name, int price, String verification, int uid, int expiration_time, boolean sending_status, int hash, boolean verification_status){
        this.id=id;
        this.status=status;
        this.menu_name=name;
        this.menu_price=price;
        this.verification=verification;
        this.UID=uid;
        this.expiration_time=expiration_time;
        this.sending_status=sending_status;
        this.tx_hash=hash;
        this.verfication_status=verification_status;
    }

    public PeopleItem(){

    }

    public String getId(){return id;}
    public boolean getStatus(){return status;}
    public String getMenu_name(){return menu_name;}
    public int getMenu_price(){return menu_price;};
    public String getVerification(){ return  verification; }
    public int getUid(){
        return UID;
    }
    public int getExpiration_time(){
        return expiration_time;
    }
    public boolean getSendingStatus(){
        return sending_status;
    }
    public int getTx_hash(){
        return tx_hash;
    }
    public boolean getVerification_status(){
        return verfication_status;
    }
    public void setMenu_name(String menu_name){
        this.menu_name=menu_name;
    }
    public void setMenu_price(int menu_price){
        this.menu_price=menu_price;
    }
    public void setSending_status(boolean status){
        this.sending_status=status;
    }
    public void setVerfication_status(boolean status){
        this.verfication_status=status;
    }
}
