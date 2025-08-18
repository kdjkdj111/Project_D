package com.steadyroom.project_d;

public class CharacterInstance {
    private String name;
    private int attack;
    private int hp;
    private int dirt;
    private int power;
    private String imageId;
    private String firebaseKey;
    public CharacterInstance() {}

    public CharacterInstance(String name, int attack, int hp, int dirt, String imageId) {
        this.name = name;
        this.attack = attack;
        this.hp = hp;
        this.dirt = dirt;
        this.imageId = imageId;

        calculatePower();

    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; calculatePower(); }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; calculatePower(); }

    public int getDirt() { return dirt; }
    public void setDirt(int dirt) { this.dirt = dirt; calculatePower(); }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public String getFirebaseKey() {return firebaseKey;}
    public void setFirebaseKey(String firebaseKey) {this.firebaseKey = firebaseKey;}
    private void calculatePower() {
        this.power = (int)Math.round(
                attack * 2.0 +
                        hp * 0.8 +
                        dirt * 1.5
        );
    }

    public int getPower() {
        return power;
    }
}

