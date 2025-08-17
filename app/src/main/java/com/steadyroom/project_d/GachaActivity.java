package com.steadyroom.project_d;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GachaActivity extends AppCompatActivity {

    private User currentUser;
    private GachaAdapter gachaAdapter;
    private ViewPager2 viewPager;

    private TextView textPoint;

    private static final int GACHA_POINT_COST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gacha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textPoint = findViewById(R.id.text_point);
        updatePointText();
        setupBackButton();

        // (1) 로그인 유저 UID 확인
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = firebaseUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class); // 만약 null일 경우 예외처리 필요
                if (currentUser == null) {
                    Toast.makeText(GachaActivity.this, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }


                


                viewPager = findViewById(R.id.shortsVP);
                gachaAdapter = new GachaAdapter(GachaActivity.this, currentUser, userRef);
                viewPager.setAdapter(gachaAdapter);
                viewPager.setPageTransformer(new SliderTransformer());

                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    private int lastPosition = 0;

                    @Override
                    public void onPageSelected(int position) {
                        if (Math.abs(position - lastPosition) > 0) {
                            if (!tryUsingPoint()) {
                                Toast.makeText(GachaActivity.this, "포인트 부족!", Toast.LENGTH_SHORT).show();
                                // 포인트 부족이면 이동 취소 (이전 카드로 복귀)
                                viewPager.setCurrentItem(lastPosition, true);
                                return;
                            }
                        }
                        lastPosition = position;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GachaActivity.this, "유저 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void updatePointText(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            textPoint.setText("포인트: -점");
            return;
        }
        String uid = firebaseUser.getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("userPoint")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Long dbPoint = snapshot.getValue(Long.class);
                        if (dbPoint != null) {
                            textPoint.setText("포인트: " + dbPoint + "점");
                        } else {
                            textPoint.setText("포인트: 0점");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        textPoint.setText("포인트: -점");
                    }
                });
    }


    private boolean tryUsingPoint() {
        if (currentUser == null) return false;
        if (currentUser.userPoint < GACHA_POINT_COST) return false;

        currentUser.userPoint -= GACHA_POINT_COST;
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.user_id)
                .child("userPoint")
                .setValue(currentUser.userPoint)
                .addOnCompleteListener(task -> updatePointText()); // DB 반영 후 표기 갱신

        return true;
    }

    //개발용 키보드 슬라이드
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                // 아래 방향키로 다음 페이지
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                // 위 방향키로 이전 페이지
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                return true;
            }
            // 다른 키 추가 매핑 가능
        }
        return super.dispatchKeyEvent(event);
    }
}