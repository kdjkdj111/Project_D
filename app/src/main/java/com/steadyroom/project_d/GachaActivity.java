package com.steadyroom.project_d;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class GachaActivity extends AppCompatActivity {

    private User currentUser;
    private List<Character> characterList;    // 멤버 변수
    private GachaAdapter gachaAdapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gacha);


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
                


                ViewPager2 viewPager = findViewById(R.id.shortsVP);
                gachaAdapter = new GachaAdapter(GachaActivity.this, currentUser, userRef);
                viewPager.setAdapter(gachaAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GachaActivity.this, "유저 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}