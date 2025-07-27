package com.steadyroom.project_d;


import java.util.ArrayList;
import java.util.List;
public class CharacterList {
    public static final List<Character> BASE_POOL = new ArrayList<>();
    static {
        BASE_POOL.add(new Character("단데기", 10, 30, 0, R.drawable.ch1, 0.20));
        BASE_POOL.add(new Character("거북왕", 17, 40, 2, R.drawable.ch2, 0.0001));
        BASE_POOL.add(new Character("어니부기", 100, 500, 10, R.drawable.ch3, 0.01));
        BASE_POOL.add(new Character("리자몽", 25, 60, 4, R.drawable.ch4, 0.0002));
        BASE_POOL.add(new Character("리자드", 14, 35, 1, R.drawable.ch5, 0.25));
    }
}
