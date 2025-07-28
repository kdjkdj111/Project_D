package com.steadyroom.project_d;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.ActionCodeEmailInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.FirebaseDatabase;





public class MainActivity extends AppCompatActivity {
    private Button btnSet;
    private Button btnGacha;
    private Button btnShop;
    private Button btnChra;
    private Button btnBag;
    private Button btncheck;
    private TextView textPoint;
    
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

        checkAndCreateUserInDatabase();

        textPoint = findViewById(R.id.text_point);

        //조회 버튼 클릭 시 포인트 표시
        btncheck = findViewById(R.id.btn_check);
        btncheck.setOnClickListener(v -> {
            int earned = PointManager.getInstance().PointsEared();
            if(earned > 0){
                Toast.makeText(MainActivity.this, earned +"점이 추가 되었습니다.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "10초 이상 사용 시 포인트가 지급됩니다.", Toast.LENGTH_SHORT).show();
            }

            updatePointText();
            PointManager.getInstance().startTime();
        });

        updatePointText();
        PointManager.getInstance().startTime();

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
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 상점으로 이동
        btnShop = findViewById(R.id.btn_shop);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 캐릭터로 이동
        btnChra = findViewById(R.id.btn_chra);
        btnChra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        //클릭 시 가방으로 이동
        btnBag = findViewById(R.id.btn_bag);
        btnBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }
    //앱 사용시간에 따른 포인트 지급
    @Override
    protected void onResume(){
        super.onResume();
        PointManager.getInstance().startTime();
    }
/*
    @Override
    protected void onPause(){
        super.onPause();
        PointManager.getInstance().PointsEared();
    }*/
    private void updatePointText(){
        textPoint.setText("포인트: "+ PointManager.getInstance().getUserPoint() +"점");
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
}