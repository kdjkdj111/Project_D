package com.steadyroom.project_d;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;

import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.TouchDelegate;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.ActionCodeEmailInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private ImageButton btnSet;
    private ImageButton btnGacha;
    private ImageButton btnShop;
    private ImageButton btnChra;
    private ImageButton btnBag;
    private Button btncheck;
    private Button btnBattle;
    private TextView textPoint;

    private DatabaseReference roomsRef;
    private String myUid;
    private Map<String, Object> myCharacterData; //임시 메인 캐릭터
    private ValueEventListener waitOpponentListener; // 대기 리스너 참조 변수화

    private boolean isMatching = false; //매칭 취소 로직
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
        // 🔧 사용 기록 권한 체크
        if (!hasUsageStatsPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);  // 사용자가 설정에서 권한을 수동으로 켜야 함
        }*/

        checkAndCreateUserInDatabase();


        textPoint = findViewById(R.id.text_point);

        //조회 버튼 클릭 시 포인트 표시
        btncheck = findViewById(R.id.btn_check);
        btncheck.setOnClickListener(v -> {
            // 🔁 기기 사용 시간 기반 포인트 적립
            PointManager.getInstance().PointsEarned(MainActivity.this, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "포인트 적립 완료", Toast.LENGTH_SHORT).show();
                    updatePointText();
                });
            });
        });

        updatePointText();

        //클릭 시 전투 시작. (마음 단단히 먹으시길)
        roomsRef = FirebaseDatabase.getInstance().getReference("battleRooms");
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //임시 메인 캐릭터
        myCharacterData = new HashMap<>();
        myCharacterData.put("characterName", "단데기");
        myCharacterData.put("hp", 40);
        myCharacterData.put("attack", 15);
        myCharacterData.put("dirt", 2);
        //myCharacterData.put("skillLeft",4);

        btnBattle = findViewById(R.id.btn_battle);
        btnBattle.setText("배틀 시작");
        isMatching = false;

        btnBattle.setOnClickListener(v -> {
            btnBattle.setEnabled(false); // 중복 클릭 방지

            if (!isMatching) {
                // 매칭 시작
                isMatching = true;
                btnBattle.setText("매칭 취소");
                startMatching(() -> {
                    btnBattle.setEnabled(true);}, () -> {
                    // 시작 실패 시 상태 복원
                    isMatching = false;
                    btnBattle.setText("배틀 시작");
                    btnBattle.setEnabled(true);
                });
            } else {
                // 매칭 취소
                cancelMatchSearch(() -> {
                    isMatching = false;
                    btnBattle.setText("배틀 시작");
                    btnBattle.setEnabled(true);
                    Toast.makeText(this, "매칭이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        });

        //클릭 시 설정으로 이동
        btnSet = findViewById(R.id.btn_set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 뽑기로 이동
        btnGacha = findViewById(R.id.btn_gacha);
        btnGacha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,GachaActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 상점으로 이동
        btnShop = findViewById(R.id.btn_shop);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ShopActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 캐릭터로 이동
        btnChra = findViewById(R.id.btn_chra);
        btnChra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CharacterActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 가방으로 이동
        btnBag =findViewById(R.id.btn_bag);
        btnBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }


    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void checkAndPromptUsagePermission() {
        SharedPreferences prefs = getSharedPreferences("PermissionPrefs", MODE_PRIVATE);
        boolean hasPrompted = prefs.getBoolean("hasPromptedPermission", false);

        if (!hasUsageStatsPermission() && !hasPrompted) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("hasPromptedPermission", true);
            editor.apply();

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }
    //앱 사용시간에 따른 포인트 지급
    @Override
    protected void onResume(){
        super.onResume();

        checkAndPromptUsagePermission();

        // 🔧 오프라인 포인트 지급
        PointManager.getInstance().OfflinePoints(this,
                () -> runOnUiThread(() -> {
                    Toast.makeText(this, "오프라인 포인트 적립 완료", Toast.LENGTH_SHORT).show();
                    updatePointText();
                }),
                () -> Log.d("MainActivity", "오프라인 포인트 적립 없음 또는 조건 불충분")
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        PointManager.getInstance().saveLastQuitTime(this);  // 🔧 마지막 종료 시간 저장
    }
    private void updatePointText(){
        textPoint.setText("포인트: "+ PointManager.getInstance().getUserPoint() +"점");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitOpponentListener != null && currentWaitingRoomId != null) {
            roomsRef.child(currentWaitingRoomId).removeEventListener(waitOpponentListener);
            waitOpponentListener = null;
            currentWaitingRoomId = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (waitOpponentListener != null && currentWaitingRoomId != null) {
            roomsRef.child(currentWaitingRoomId).removeEventListener(waitOpponentListener);
            waitOpponentListener = null;
            currentWaitingRoomId = null;
        }
    }

    public void checkAndCreateUserInDatabase() {
        // 1. 현재 로그인된 유저 가져오기
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // 로그인 안된 경우
            Log.e("UserInit", "로그인된 사용자 정보가 없습니다.");
            Toast.makeText(MainActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseUser.getUid();
        String displayName = firebaseUser.getDisplayName();  // 구글 계정 이름 등

        // 2. Firebase Database 경로 준비
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(uid);

        // 3. DB에서 사용자 정보 조회 (한 번만 읽기)
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 이미 정보 있음 → 불러오기/앱에 반영
                    User user = snapshot.getValue(User.class);
                    Log.d("UserInit", "기존 유저 정보 불러옴: " + user.nickname);
                    myCharacterData.put("nickname", user.nickname);
                    // 필요한 후처리: 예) Main 화면 이동 등

                    //firebase에서 포인트 가져오기
                    PointManager.getInstance().setUserPoint(user.userPoint);  // 기존 데이터 가지고 옴
                    updatePointText();
                } else {
                    // 정보 없음 → 새로 생성
                    String defaultNickname = (displayName != null && !displayName.isEmpty()) ? displayName : "신규유저";
                    User newUser = new User(uid, defaultNickname); // 캐릭터, 아이템은 빈 리스트로 자동 초기화됨

                    // DB에 저장
                    userRef.setValue(newUser)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("UserInit", "신규 유저 정보 DB에 저장 완료");
                                    Toast.makeText(MainActivity.this, "유저 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                    // 이후 처리: 예) 초기 화면 전환 등
                                    myCharacterData.put("nickname", defaultNickname);
                                    // 신규 유저 정보 확인 시 userPoint 값 0으로 설정
                                    PointManager.getInstance().setUserPoint(0);
                                    updatePointText();
                                } else {
                                    Log.e("UserInit", "DB 저장 실패: " + task.getException());
                                    Toast.makeText(MainActivity.this, "유저 정보 저장 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // DB 처리 자체가 실패할 경우
                Log.e("UserInit", "DB 요청 실패: " + error.getMessage(), error.toException());
                Toast.makeText(MainActivity.this, "유저 정보 로딩 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMatching(Runnable onReady, Runnable onFail) {
        // 이전 대기 상태 있으면 취소
        cancelMatchSearch(null);

        roomsRef.orderByChild("state").equalTo("waiting")
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // 기존 방 참여 시도
                            for (DataSnapshot roomSnap : snapshot.getChildren()) {
                                String roomId = roomSnap.getKey();
                                DatabaseReference roomRef = roomsRef.child(roomId);
                                roomRef.runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        Map<String, Object> roomData = (Map<String, Object>) currentData.getValue();
                                        if (roomData == null) return Transaction.success(currentData);

                                        String state = (String) roomData.get("state");
                                        if (!"waiting".equals(state)) return Transaction.abort();

                                        Map<String, Object> players = (Map<String, Object>) roomData.get("players");
                                        if (players == null) players = new HashMap<>();
                                        players.put(myUid, myCharacterData);

                                        myCharacterData.put("skillLeft",3);
                                        roomData.put("players", players);
                                        roomData.put("state", "playing");
                                        roomData.put("turn", myUid);
                                        roomData.put("starterUid",myUid);


                                        currentData.setValue(roomData);
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                                        if (committed) {
                                            goToBattleScreen(roomId);

                                        } else {
                                            onFail.run();
                                        }
                                    }
                                });
                                break;
                            }
                        } else {
                            // 대기방 생성
                            createNewRoomAndWait();
                            onReady.run();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(MainActivity.this, "매칭 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        onFail.run();
                    }
                });
    }

    private void goToBattleScreen(String roomId) {
        // 실제 전투 화면으로 이동 (Intent 등)
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
        btnBattle.setEnabled(true);
        btnBattle.setText("배틀 시작");
        isMatching = false;
    }


    private void createNewRoomAndWait() {
        String newRoomId = roomsRef.push().getKey();
        currentWaitingRoomId = newRoomId;

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("state", "waiting");
        Map<String, Object> players = new HashMap<>();
        myCharacterData.put("skillLeft",4);
        players.put(myUid, myCharacterData);
        roomData.put("players", players);
        roomsRef.child(newRoomId).setValue(roomData)
                .addOnSuccessListener(aVoid -> waitForOpponent(newRoomId));
    }

    private String currentWaitingRoomId = null; // 현재 대기 중인 방 ID 저장(리스너 해제 시에 사용)
    private void waitForOpponent(String roomId) {
        DatabaseReference roomRef = roomsRef.child(roomId);

        waitOpponentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Map<String, Object> roomData = (Map<String, Object>) snapshot.getValue();
                if (roomData == null) return;

                Map<String, Object> players = (Map<String, Object>) roomData.get("players");
                if (players != null && players.size() > 1) {
                    roomRef.removeEventListener(waitOpponentListener);
                    waitOpponentListener = null;
                    goToBattleScreen(roomId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        };

        roomRef.addValueEventListener(waitOpponentListener);
    }

    private void cancelMatchSearch(Runnable onCancelComplete) {
        if (waitOpponentListener != null && currentWaitingRoomId != null) {
            roomsRef.child(currentWaitingRoomId).removeEventListener(waitOpponentListener);
            waitOpponentListener = null;
        }

        if (currentWaitingRoomId != null) {
            roomsRef.child(currentWaitingRoomId).removeValue()
                    .addOnCompleteListener(task -> {
                        currentWaitingRoomId = null;
                        if (onCancelComplete != null) onCancelComplete.run();
                    });
        } else {
            if (onCancelComplete != null) onCancelComplete.run();
        }
    }
}

