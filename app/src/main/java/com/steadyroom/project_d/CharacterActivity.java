package com.steadyroom.project_d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
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
    private ImageView selectedCharacterImage;
    private TextView selectedCharacterDescription;
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

        selectedCharacterImage = findViewById(R.id.selectedItemImage);
        selectedCharacterDescription = findViewById(R.id.selectedItemDescription);
        progressTextView = findViewById(R.id.progressTextView);
        Button CollectButton = findViewById(R.id.collectButton);

        setupBackButton();

        // 캐릭터 획득 상태 초기화

        int initialTotalCount = 5;
        int initialAcquiredCount = 0;
        for (int i = 0; i < initialTotalCount; i++) {
            if (i % 2 == 0) initialAcquiredCount++; // 짝수 인덱스는 획득한 상태로 가정
        }
        progressTextView.setText(String.format(Locale.US, "%d/%d", initialAcquiredCount, initialTotalCount));


        //테스트용 캐릭터 샘플 삽입
        List<Character> Characters = new ArrayList<>();
        for (Character character : CharacterList.BASE_POOL) {
            // 캐릭터의 이름을 확인하여 원하는 캐릭터인지 판별합니다.
            if (character.getName().equals("단데기") || character.getName().equals("리자몽")) {
                Characters.add(character); // 원하는 캐릭터라면 리스트에 추가
            }
        }

        // 도감 버튼 클릭 시 CollectFragment(도감화면) 띄우기
        if (CollectButton != null) {
            CollectButton.setOnClickListener(v -> {
                CollectFragment fragment = new CollectFragment();
                /*Log.d("CharacterActivity", "Characters 리스트 크기 (Bundle 전달 전): " + Characters.size());
                if (Characters.isEmpty()) {
                    Log.w("CharacterActivity", " Characters 리스트가 비어 있습니다! 도감에 전달할 데이터가 없어요.");
                }*/
                //  인벤토리에 있는 캐릭터정보를 도감에 전달
                Bundle bundle = new Bundle();
                bundle.putSerializable("inventory", new ArrayList<>(Characters)); // Characters는 인벤토리 리스트
                fragment.setArguments(bundle);

                fragment.setOnCodexProgressListener(CharacterActivity.this);
                fragment.show(getSupportFragmentManager(), "collect");
            });
        }

        // RecyclerView 초기화
        RecyclerView characterRecyclerView = findViewById(R.id.inventoryRecyclerView);
        characterRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));



        // 어댑터 생성 및 클릭 리스너 설정
        CharacterAdapter characterAdapter = new CharacterAdapter(Characters, this::displaySelectedItemDetails);
        characterRecyclerView.setAdapter(characterAdapter);

        // 앱 시작 시 첫 번째 아이템 상세 정보 표시
        if (!Characters.isEmpty()) {
            displaySelectedItemDetails(Characters.get(0));
        }
    }
    //선택된 아이템 상세보기
    private void displaySelectedItemDetails(Character item) {
        if (item != null) {
            selectedCharacterImage.setImageResource(item.getImageId());
            selectedCharacterDescription.setText(
                    String.format("%s - HP: %d / ATK: %d", item.getName(), item.getHp(), item.getAttack())
            );
        }
    }

    @Override//도감률
    public void onCodexProgressUpdated(int acquiredCount, int totalCount) {
        progressTextView.setText(String.format(Locale.US, "%d/%d", acquiredCount, totalCount));
    }
    //뒤로가기 버튼
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
