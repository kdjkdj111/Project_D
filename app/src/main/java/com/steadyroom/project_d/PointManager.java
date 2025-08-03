package com.steadyroom.project_d;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


//싱글톤 패턴
public class PointManager {
    private static PointManager instance;
    private long userPoint = 0;

    // 🔧 오프라인 적립 관련 상수
    private static final String PREF_NAME = "PointPrefs";
    private static final String KEY_LAST_USAGE_CHECK = "lastUsageCheckTime";
    private static final String KEY_LAST_QUIT_TIME = "lastQuitTime"; // 마지막 종료 시간 저장용


    public PointManager(){}

    // 이 클래스를 앱 전체에서 하나만 사용하도록 보장 전역적으로 공유되는 PointManager 객체를 가지고 옴
    public static PointManager getInstance(){
        if(instance == null){
            instance = new PointManager();
        }
        return  instance;
    }

    public int PointsEarned(Context context, Runnable onComplete){
        long now = System.currentTimeMillis();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        long lastTime = prefs.getLong(KEY_LAST_USAGE_CHECK, now - 60 * 60 * 1000);

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, lastTime, now
        );

        if(stats == null || stats.isEmpty()){
            Log.w("PointManager", "사용 기록 없음 - 권한 미허용 또는 데이터 부족");
            onComplete.run();
            return 0; //return;
        }

        long totalTime = 0;
        for(UsageStats usageStat : stats){
            totalTime += usageStat.getTotalTimeInForeground();
        }

        int earned = (int) (totalTime / 1000)/10;

        if(earned > 0){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child("userPoint")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            long current = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                            userPoint = current + earned;

                            saveFirebase();
                            Log.d("PointManager", "기기 사용 기반 포인트 적립됨: " + earned + "점 (총: " + userPoint + ")");

                            prefs.edit().putLong(KEY_LAST_USAGE_CHECK, now).apply(); // 성공 후 시간 갱신

                            onComplete.run();
                        });
            } else{
                onComplete.run();
            }
        } else{
            onComplete.run();
        }
        prefs.edit().putLong(KEY_LAST_USAGE_CHECK, now).apply();

        return 0;
    }

    public void OfflinePoints(Context context, Runnable onEarned, Runnable onSkipped) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastQuitTime = prefs.getLong(KEY_LAST_QUIT_TIME, System.currentTimeMillis());

        long now = System.currentTimeMillis();
        long elapseMillis = now - lastQuitTime;

        int earned = (int) (elapseMillis / 1000) / 10;

        if (earned > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child("userPoint")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            int current = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                            userPoint = current + earned;

                            saveFirebase();
                            Log.d("PointManager", "오프라인 포인트 적립: " + earned + "점 (총: " + userPoint + ")");

                            onEarned.run();
                        });
            } else {
                onSkipped.run();
            }
        } else {
            onSkipped.run();
        }
    }

    // 🔧 앱 종료 시 호출: 마지막 종료 시간 저장
    public void saveLastQuitTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_QUIT_TIME, System.currentTimeMillis()).apply();
        Log.d("PointManager", "앱 종료 시간 저장됨");
    }
    /*
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
    }*/

    //현재 포인트 반환
    public long getUserPoint(){
        return userPoint;
    }

    public void setUserPoint(long point){
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
