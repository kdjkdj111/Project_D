package com.steadyroom.project_d;

import android.os.Bundle;
import android.os.Handler;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BagActivity extends AppCompatActivity {

    private GridLayout inventoryGrid;
    private DatabaseReference userRef;
    private String userId;
    private static final long ITEM_DURATION = 5 * 60 * 1000; // 5분 (밀리초)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag);

        inventoryGrid = findViewById(R.id.inventoryGrid);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        loadInventory();

        setupBackButton();

    }

    private void setupBackButton() {
        ImageView btn_back = findViewById(R.id.btn_back);
        if (btn_back != null) {
            btn_back.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void loadInventory() {
        userRef.child("inventory").child("dustDouble").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Boolean isActive = task.getResult().child("isActive").getValue(Boolean.class);
                    Long purchasedAt = task.getResult().child("purchasedAt").getValue(Long.class);

                    if (isActive != null && isActive && purchasedAt != null) {
                        long now = System.currentTimeMillis();
                        long elapsed = now - purchasedAt;

                        if (elapsed < ITEM_DURATION) {
                            addDustDoubleItemToInventoryUI();

                            long remaining = ITEM_DURATION - elapsed;
                            new Handler().postDelayed(this::removeDustDoubleItemFromInventory, remaining);
                        } else {
                            removeDustDoubleItemFromInventory();
                        }
                    } else {
                        Toast.makeText(this, "먼지 2배 아이템 없음 또는 만료됨", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "먼지 2배 아이템 데이터 없음", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Firebase 읽기 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDustDoubleItemToInventoryUI() {
        ImageView itemImage = new ImageView(this);
        itemImage.setImageResource(R.drawable.doublemonster); // 본인 이미지 리소스
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 120;
        params.height = 120;
        params.setMargins(8, 8, 8, 8);
        itemImage.setLayoutParams(params);

        inventoryGrid.addView(itemImage);
    }

    private void removeDustDoubleItemFromInventory() {
        userRef.child("inventory").child("dustDouble").child("isActive").setValue(false)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, "먼지 2배 아이템 만료됨", Toast.LENGTH_SHORT).show();
                    inventoryGrid.removeAllViews();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "아이템 만료 처리 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
