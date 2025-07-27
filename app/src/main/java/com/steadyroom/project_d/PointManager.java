package com.steadyroom.project_d;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


//싱글톤 패턴
public class PointManager {
    private static PointManager instance;
    private long startTime = 0;
    private int userPoint = 0;


    public PointManager(){}

    // 이 클래스를 앱 전체에서 하나만 사용하도록 보장 전역적으로 공유되는 PointManager 객체를 가지고 옴
    public static PointManager getInstance(){
        if(instance == null){
            instance = new PointManager();
        }
        return  instance;
    }

    public void startTime(){
        if(startTime == 0){
            startTime = System.currentTimeMillis();
            Log.d("PointManager", "세션 시작됨: " + startTime);
        }
    }

    public int PointsEared(){
        if(startTime == 0)
            return 0;

        long usedtime = System.currentTimeMillis() - startTime;
        int earned = (int)(usedtime/1000) / 10; // 10초당 1점 지급
        if(earned>0) {
            userPoint += earned;
            saveFirebase();
            Log.d("PointManager", "포인트 적립됨: " + earned + "점 (총: " + userPoint + ")");
            startTime = 0;
        }
        return earned;
    }

    //현재 포인트 반환
    public int getUserPoint(){
        return userPoint;
    }

    public void setUserPoint(int point){
        this.userPoint = point;
    }
    private void saveFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("userPoint")
                    .setValue(userPoint);
        }
    }
}
