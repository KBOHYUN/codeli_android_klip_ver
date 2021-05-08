package com.example.codeli_klip;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    private String name;
    private String nickname;
    private String email;

    UserInfo(String name, String nickname, String email){
        this.name=name;
        this.nickname=nickname;
        this.email=email;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user_name", name);
        result.put("user_nickname", nickname);
        result.put("user_email", email);

        return result;
    }

}
