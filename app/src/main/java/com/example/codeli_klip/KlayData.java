package com.example.codeli_klip;

public class KlayData {
//클레이시세
    private String value;
    private boolean trigger;

    KlayData(){
    }


    KlayData(boolean trigger, String value){
        this.trigger=trigger;
        this.value=value;
    }

    public String getValue(){
        return value;
    }
    public boolean getTrigger(){
        return trigger;
    }

}
