package com.steadyroom.project_d;


import java.util.ArrayList;
import java.util.List;
public class CharacterList {
    public static final List<CharacterTemplate> BASE_POOL = new ArrayList<>();
    static {
        BASE_POOL.add(new CharacterTemplate("단데기", 10, 16, 28, 38, 0, 2, R.drawable.ch1, 0.225));
        BASE_POOL.add(new CharacterTemplate("거북왕", 17, 26, 40, 62, 2, 8, R.drawable.ch2, 0.0001));
        BASE_POOL.add(new CharacterTemplate("어니부기", 100, 130, 500, 550, 10, 15, R.drawable.ch3, 0.01));
        BASE_POOL.add(new CharacterTemplate("리자몽", 25, 36, 60, 100, 4, 7, R.drawable.ch4, 0.0002));
        BASE_POOL.add(new CharacterTemplate("리자드", 14, 26, 35, 60, 1, 4, R.drawable.ch5, 0.225));
        // 나머지 캐릭터도 min/max 구조로 작성
    }
}

