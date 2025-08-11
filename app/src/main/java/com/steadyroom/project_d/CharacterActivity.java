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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

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
    private List<CharacterInstance> myCharacters = new ArrayList<>();

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
        // 3가지 역할의 메서드
        setupViews();
        setupListeners();
        setupManagers();
    }
    @Override
    protected void onResume() {
        super.onResume();
        myCharacterManager.loadMyCharacters();//인벤토리 목록 불러오기
        loadCodexProgress();//도감 진행률 불러오기
    }

    // MyCharacterManager로부터 데이터 로드 성공 시 호출
    @Override
    public void onDataLoaded(List<CharacterInstance> characters) {
        // 기존 리스트를 비우고 새로 받아온 캐릭터들로 채워서 UI 갱신
        myCharacters.clear();
        myCharacters.addAll(characters);
        characterAdapter.notifyDataSetChanged();

        // 첫 번째 아이템 상세 정보 표시
        if (!myCharacters.isEmpty()) {
            displaySelectedChar(myCharacters.get(0));
        }
    }

    // 데이터 로드 실패 시 호출
    @Override
    public void onDataLoadFailed(String errorMessage) {
        Log.e("CharacterActivity", "데이터 로드 실패: " + errorMessage);
    }


    //-------------------- 역할별로 분리된 메서드 --------------------

    // 모든 뷰(UI)를 초기화하는 메서드
    //activity_character.xml 파일에서 id를 가진 뷰를 제어
    private void setupViews() {
        selectedCharacterImage = findViewById(R.id.selectedItemImage);
        selectedCharacterDescription = findViewById(R.id.selectedItemDescription);
        progressTextView = findViewById(R.id.progressTextView);
    }

    // 모든 리스너(버튼 클릭 등)를 설정하는 메서드
    private void setupListeners() {
        //"도감 버튼"을 눌렀을 때, CollectFragment를 화면에 띄움
        Button collectButton = findViewById(R.id.collectButton);
        if (collectButton != null) {
            collectButton.setOnClickListener(v -> {
                CollectFragment fragment = new CollectFragment();
                fragment.show(getSupportFragmentManager(), "collect");
            });
        }
        //"뒤로가기"버튼을 눌렀을 때, MainActivity로 돌아감
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(CharacterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    // 데이터 매니저와 어댑터 등 로직을 설정하는 메서드
    private void setupManagers() {
        myCharacterManager = new MyCharacterManager(this);
        RecyclerView characterRecyclerView = findViewById(R.id.inventoryRecyclerView);

        //캐릭터 배치(3열 그리드)
        characterRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        characterAdapter = new CharacterAdapter(myCharacters, this::displaySelectedChar);
        characterRecyclerView.setAdapter(characterAdapter);
    }

    //선택된 캐릭터의 상세정보를 상단 공간에 표시
    private void displaySelectedChar(CharacterInstance selectedchar) {
        if (selectedchar != null) {
            int imageId = getResources().getIdentifier(
                    selectedchar.getImageId(),
                    "drawable",
                    getPackageName()
            );
            if(imageId !=0){
                selectedCharacterImage.setImageResource(imageId);
            }
            this.selectedCharacterDescription.setText(
                    String.format(Locale.US, "%s - HP: %d / ATK: %d \nDIRT: %d", selectedchar.getName(), selectedchar.getHp(), selectedchar.getAttack(), selectedchar.getDirt())
            );
        }
    }
    // Firebase에서 도감 진행률을 불러옵니다
    private void loadCodexProgress() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference codexRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("codex");

            codexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int acquiredCount = (int) snapshot.getChildrenCount();
                    int totalCount = CharacterList.BASE_POOL.size();
                    progressTextView.setText(String.format(Locale.US, "%d/%d", acquiredCount, totalCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CharacterActivity", "도감 진행률 불러오기 실패: " + error.getMessage());
                }
            });
        }
    }
}
