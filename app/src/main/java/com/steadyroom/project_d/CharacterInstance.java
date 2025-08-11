package com.steadyroom.project_d;

public class CharacterInstance {
    private String name;
    private int attack;
    private int hp;
    private int dirt;
    private String imageId;

    public CharacterInstance() {}

    public CharacterInstance(String name, int attack, int hp, int dirt, String imageId) {
        this.name = name;
        this.attack = attack;
        this.hp = hp;
        this.dirt = dirt;
        this.imageId = imageId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getDirt() { return dirt; }
    public void setDirt(int dirt) { this.dirt = dirt; }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
}

