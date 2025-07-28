package com.steadyroom.project_d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CharacterActivity extends AppCompatActivity implements CollectFragment.OnCodexProgressListener {

    private Button encyclopediaButton;
    private RecyclerView inventoryRecyclerView;
    private CharacterAdapter inventoryAdapter;

    private ImageView selectedItemImage;
    private TextView selectedItemDescription;
    private TextView progressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_character);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventoryRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedItemImage = findViewById(R.id.selectedItemImage);
        selectedItemDescription = findViewById(R.id.selectedItemDescription);
        progressTextView = findViewById(R.id.progressTextView);
        encyclopediaButton = findViewById(R.id.encyclopediaButton);

        setupBackButton();

        // 캐릭터 획득 상태 초기화
        String[] characterIds = {
                "char_001", "char_002", "char_003", "char_004",
                "char_005", "char_006", "char_007", "char_008"
        };
        int initialTotalCount = characterIds.length;
        int initialAcquiredCount = 0;
        for (int i = 0; i < initialTotalCount; i++) {
            if (i % 2 == 0) initialAcquiredCount++; // 짝수 인덱스는 획득한 상태로 가정
        }
        progressTextView.setText(String.format(Locale.US, "%d/%d", initialAcquiredCount, initialTotalCount));

        // 도감 버튼 클릭 시 CollectFragment 띄우기
        if (encyclopediaButton != null) {
            encyclopediaButton.setOnClickListener(v -> {
                CollectFragment fragment = new CollectFragment();
                fragment.setOnCodexProgressListener(CharacterActivity.this);
                fragment.show(getSupportFragmentManager(), "encyclopedia");
            });
        }

        // RecyclerView 초기화
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        inventoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // 샘플 아이템 리스트 생성
        List<CharacterInfo> CharacterList = new ArrayList<>();
        for (int i = 1; i <= 26; i++) {
            String description = "아이템 " + i + "에 대한 설명입니다. 아이템 " + i + "는 엄청나게 긴 설명입니다.";
            CharacterList.add(new CharacterInfo(
                    "item_id_" + i,
                    "아이템 " + i,
                    R.drawable.ic_launcher_foreground,
                    i,
                    description
            ));
        }

        // 어댑터 생성 및 클릭 리스너 설정
        inventoryAdapter = new CharacterAdapter(CharacterList, this::displaySelectedItemDetails);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        // 앱 시작 시 첫 번째 아이템 상세 정보 표시
        if (!CharacterList.isEmpty()) {
            displaySelectedItemDetails(CharacterList.get(0));
        }
    }

    private void displaySelectedItemDetails(CharacterInfo item) {
        if (item != null) {
            selectedItemImage.setImageResource(item.getImageResId());
            selectedItemDescription.setText(item.getDescription());
        }
    }

    @Override
    public void onCodexProgressUpdated(int acquiredCount, int totalCount) {
        progressTextView.setText(String.format(Locale.US, "%d/%d", acquiredCount, totalCount));
    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(CharacterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
