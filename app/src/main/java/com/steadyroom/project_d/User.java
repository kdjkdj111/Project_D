package com.steadyroom.project_d;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String user_id;
    public String nickname;
    public long userPoint = 0;
    public List<CharacterInstance> characters = new ArrayList<>();  // 빈 리스트로 초기화
    public List<Item> items = new ArrayList<>();

    public User() {}  // Firebase에서 사용

    public User(String user_id, String nickname) {
        this.user_id = user_id;
        this.nickname = nickname;
        // characters, items는 이미 빈 리스트 상태
    }
}
