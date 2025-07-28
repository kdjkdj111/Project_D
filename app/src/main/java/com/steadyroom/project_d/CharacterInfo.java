// CharacterInfo.java
package com.steadyroom.project_d;
//캐릭터 하나 가 가질 정보를 정의하는 공간
public class CharacterInfo {
    private String id; // 캐릭터의 고유 ID
    private String name; // 아이템 이름
    private int imageResId; // 캐릭터 이미지의 Drawable 리소스 ID (예: R.drawable.my_item_icon)
    private int quantity; // 캐릭터 수량
    private String description; // 1. 캐릭터 설명 필드 추가

    // 생성자: 캐릭터 객체를 만들 때 필요한 정보를 초기화
    public CharacterInfo(String id, String name, int imageResId, int quantity, String description) {
        this.id = id;
        this.name = name;
        this.imageResId = imageResId;
        this.quantity = quantity;
        this.description = description;
    }

    // Getter 메서드: 외부에서 캐릭터의 정보를 가져갈 때 사용합니다.
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getQuantity() {
        return quantity;
    }
    public String getDescription(){
        return description;
    }
}