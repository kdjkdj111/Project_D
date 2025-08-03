package com.steadyroom.project_d;


import java.util.ArrayList;
import java.util.List;
public class CharacterList {
    public static final List<CharacterTemplate> BASE_POOL = new ArrayList<>();
    static {
        BASE_POOL.add(new CharacterTemplate("단데기", 10, 18, 30, 42, 0, 2, R.drawable.ch1, 0.44));
        BASE_POOL.add(new CharacterTemplate("꼬렛", 12, 20, 28, 40, 0, 2, R.drawable.ch8, 0.44));
        BASE_POOL.add(new CharacterTemplate("어니부기", 30, 44, 65, 90, 3, 7, R.drawable.ch3, 0.055));
        BASE_POOL.add(new CharacterTemplate("리자드", 32, 48, 60, 85, 3, 7, R.drawable.ch5, 0.055));
        BASE_POOL.add(new CharacterTemplate("니드리노", 65, 88, 175, 240, 5, 12, R.drawable.ch6, 0.005));
        BASE_POOL.add(new CharacterTemplate("피카츄", 70, 95, 160, 220, 5, 12, R.drawable.ch7, 0.005));
        BASE_POOL.add(new CharacterTemplate("리자몽", 130, 170, 340, 420, 10, 18, R.drawable.ch4, 0.00025));
        BASE_POOL.add(new CharacterTemplate("거북왕", 120, 165, 350, 430, 10, 18, R.drawable.ch2, 0.00025));

        // 나머지 캐릭터도 min/max 구조로 작성
    }
}

