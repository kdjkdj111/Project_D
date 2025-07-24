package com.steadyroom.project_d;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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



        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // 로그인 화면으로 이동 후 MainActivity 종료
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
