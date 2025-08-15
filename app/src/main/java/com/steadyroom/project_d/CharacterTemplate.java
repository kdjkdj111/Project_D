package com.steadyroom.project_d;

//캐릭터 설계용 (랜덤값 범위 저장용)
public class CharacterTemplate {
    private String name;
    private int minAttack, maxAttack;
    private int minHp, maxHp;
    private int minDirt, maxDirt;
    private String imageId;
    private double appearChance;

    public CharacterTemplate() {
    }

    public CharacterTemplate(String name, int minAttack, int maxAttack, int minHp, int maxHp,
                             int minDirt, int maxDirt, String imageId, double appearChance) {
        this.name = name;
        this.minAttack = minAttack; this.maxAttack = maxAttack;
        this.minHp = minHp; this.maxHp = maxHp;
        this.minDirt = minDirt; this.maxDirt = maxDirt;
        this.imageId = imageId;
        this.appearChance = appearChance;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxAttack() { return maxAttack; }
    public void setMaxAttack(int maxAttack) { this.maxAttack = maxAttack; }
    public int getMinAttack() { return minAttack; }
    public void setMinAttack(int minAttack) { this.minAttack = minAttack; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getMinHp() { return minHp; }
    public void setMinHp(int minHp) { this.minHp = minHp; }

    public int getMaxDirt() { return maxDirt; }
    public void setMaxDirt(int maxDirt) { this.maxDirt = maxDirt; }
    public int getMinDirt() { return minDirt; }
    public void setMinDirt(int minDirt) { this.minDirt = minDirt; }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public double getAppearChance() { return appearChance; }
    public void setAppearChance(double appearChance) { this.appearChance = appearChance; }
}

