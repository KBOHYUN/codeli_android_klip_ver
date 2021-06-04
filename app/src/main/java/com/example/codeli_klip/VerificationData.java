package com.example.codeli_klip;

public class VerificationData {
    private String room_manager_wallet;
    private boolean trigger;
    private String state; //검증 결과
    private String comment; //노쇼 인원 이름

    VerificationData(){ }

    VerificationData(boolean trigger){
        this.trigger=trigger;

    }

    VerificationData(String klip_address, boolean trigger){
        this.room_manager_wallet=klip_address;
        this.trigger=trigger;
    }


    VerificationData(String klip_address, boolean trigger, String state){
        this.room_manager_wallet=klip_address;
        this.trigger=trigger;
        this.state=state;
    }

    VerificationData(String klip_address, boolean trigger, String state, String comment){
        this.room_manager_wallet=klip_address;
        this.trigger=trigger;
        this.state=state;
        this.comment=comment;
    }


    public String getRoom_manager_wallet(){
        return room_manager_wallet;
    }
    public boolean getTrigger(){
        return trigger;
    }

    public String getState(){
        return state;
    }
    public String getComment(){
        return comment;
    }



    public void setState(String state){
        this.state=state;
    }

    public void setComment(String comment){
        this.comment=comment;
    }
}
