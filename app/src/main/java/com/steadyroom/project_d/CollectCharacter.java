// CollectCharacter.java
// 캐릭터 개별 정보(아이디, 이름, 이미지, 획득 여부 등)를 담는 데이터 모델 클래스
package com.steadyroom.project_d;


public class CollectCharacter {

    private String displayName; // 도감에 표시될 캐릭터 이름 (미획득 시 "???", 획득 시 실제 이름)
    private int displayImageResId; // 도감에 표시될 이미지 리소스 ID, 획득 시 실제 캐릭터 이미지)
    private boolean isAcquired; // 이 캐릭터를 획득했는지 여부

    // TODO: 나중에 실제 Character 객체를 참조할 필드 추가 가능
    // private Character character;

    // 생성자
    public CollectCharacter( String displayName, int displayImageResId, boolean isAcquired) {
        this.displayName = displayName;
        this.displayImageResId = displayImageResId;
        this.isAcquired = isAcquired;
    }

    // Getter 메서드


    public String getDisplayName() {
        return displayName;
    }

    public int getDisplayImageResId() {
        return displayImageResId;
    }

    public boolean isAcquired() {
        return isAcquired;
    }

    // Setter 메서드 (획득 여부 변경 시 필요)
    public void setAcquired(boolean acquired) {
        isAcquired = acquired;
        // 획득 상태 변경 시 displayName과 displayImageResId도 업데이트하는 로직 추가 가능
        // 예: if (acquired) { this.displayName = actualCharacterName; ... }
    }

    // TODO: 나중에 Character 객체를 설정하는 메서드 추가
    // public void setCharacterInfo(Character character) {
    //     this.characterInfo = characterInfo;
    //     // 실제 정보로 displayName과 displayImageResId 업데이트
    //     if (characterInfo != null) {
    //         this.displayName = characterInfo.getName(); // 실제 캐릭터 이름
    //         this.displayImageResId = characterInfo.getImageResId(); // 실제 캐릭터 이미지
    //     }
    // }
}