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

public class CharacterActivity extends AppCompatActivity implements MyCharacterManager.OnDataLoadListener{
    //View 요소 (UI)
    private ImageView selectedCharacterImage;
    private TextView selectedCharacterDescription;
    private TextView progressTextView;
    //캐릭터 데이터를 관리하는 리스트 및 어댑터
    private CharacterAdapter characterAdapter;
    private MyCharacterManager myCharacterManager;
    private List<Character> myCharacters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_character);

        //시스템 패딩(UI)설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventoryRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //VIEW 초기화
        selectedCharacterImage = findViewById(R.id.selectedItemImage);
        selectedCharacterDescription = findViewById(R.id.selectedItemDescription);
        progressTextView = findViewById(R.id.progressTextView);
        Button CollectButton = findViewById(R.id.collectButton);

        //뒤로가기 버튼 설정
        setupBackButton();

        // MyCharacterManager를 초기화하고 리스너로 설정
        myCharacterManager = new MyCharacterManager(this);

        // RecyclerView 초기화(3열 그리드 레이아웃)
        RecyclerView characterRecyclerView = findViewById(R.id.inventoryRecyclerView);
        characterRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // 어댑터 초기화 및 연결: 보유 캐릭터 리스트와 아이템 클릭 리스너 연결
        characterAdapter = new CharacterAdapter(myCharacters, this::displaySelectedItemDetails);
        characterRecyclerView.setAdapter(characterAdapter);


        // 도감 버튼 클릭 시 CollectFragment(도감화면) 띄우기
        if (CollectButton != null) {
            CollectButton.setOnClickListener(v -> {
                CollectFragment fragment = new CollectFragment();
                fragment.show(getSupportFragmentManager(), "collect");
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        myCharacterManager.loadMyCharacters();
    }

    // MyCharacterManager로부터 데이터 로드 성공 시 호출
    @Override
    public void onDataLoaded(List<Character> characters) {
        // 기존 리스트를 비우고 새로 받아온 캐릭터들로 채워서 UI 갱신
        myCharacters.clear();
        myCharacters.addAll(characters);
        characterAdapter.notifyDataSetChanged();

        // 첫 번째 아이템 상세 정보 표시
        if (!myCharacters.isEmpty()) {
            displaySelectedItemDetails(myCharacters.get(0));
        }

        // 도감 진행률 업데이트: 보유 캐릭터 수/ 전체 캐릭터 수
        int totalCount = CharacterList.BASE_POOL.size();
        int acquiredCount = myCharacters.size();
        progressTextView.setText(String.format(Locale.US, "%d/%d", acquiredCount, totalCount));
    }


    // 데이터 로드 실패 시 호출
    @Override
    public void onDataLoadFailed(String errorMessage) {
        Log.e("CharacterActivity", "데이터 로드 실패: " + errorMessage);
    }
    //선택된 아이템 상세보기
    private void displaySelectedItemDetails(Character item) {
        if (item != null) {
            int imageId = getResources().getIdentifier(
                    item.getImageId(),
                    "drawable",
                    getPackageName()
            );
            if(imageId !=0){
                selectedCharacterImage.setImageResource(imageId);
            }
            selectedCharacterDescription.setText(
                    String.format(Locale.US, "%s - HP: %d / ATK: %d \nDIRT: %d", item.getName(), item.getHp(), item.getAttack(), item.getDirt())
            );
        }
    }

    //뒤로가기 버튼 기능 설정
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
