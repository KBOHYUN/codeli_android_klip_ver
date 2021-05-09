package com.example.codeli_klip;

public class VerificationData {
    private String room_manager_wallet;
    private boolean trigger;

    VerificationData(){

    }

    VerificationData(String klip_address, boolean trigger){
        this.room_manager_wallet=klip_address;
        this.trigger=trigger;
    }

    public String getRoom_manager_wallet(){
        return room_manager_wallet;
    }
    public boolean getTrigger(){
        return trigger;
    }
}
