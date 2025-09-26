// CollectCharacter.java
// 캐릭터 개별 정보(아이디, 이름, 이미지, 획득 여부 등)를 담는 데이터 모델 클래스
package com.steadyroom.project_d;


public class CollectCharacter {

    private String displayName; // 도감에 표시될 캐릭터 이름 (미획득 시 "???", 획득 시 실제 이름)
    private int displayImageResId; // 도감에 표시될 이미지 리소스 ID, 획득 시 실제 캐릭터 이미지)
    private boolean isAcquired; // 이 캐릭터를 획득했는지 여부


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
    }
}