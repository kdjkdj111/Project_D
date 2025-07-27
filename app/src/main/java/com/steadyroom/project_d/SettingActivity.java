package com.steadyroom.project_d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    private Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        //로그아웃 버튼 클릭 시 로그인 창으로 이동
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // 로그인 화면으로 이동 후 MainActivity 종료
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PointManager.getInstance().startTime();  // 시간 측정 시작
    }
 /* // 여기 추가 되면 설정 창 나갔을 때 자동으로 포인트 쌓임
    @Override
    protected void onPause() {
        super.onPause();
        PointManager.getInstance().PointsEared();  // 시간 측정 종료 및 포인트 누적
    }*/
}