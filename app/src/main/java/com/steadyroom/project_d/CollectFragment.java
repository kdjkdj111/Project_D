// CollectFragment.java
// 캐릭터 도감 UI를 구성하고 캐릭터 목록을 불러와서 CollectAdapter에 연결해 보여주는 프래그먼트
package com.steadyroom.project_d;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast; // 테스트용 토스트
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CollectFragment extends DialogFragment implements CollectAdapter.OnCodexEntryClickListener {

    private RecyclerView codexRecyclerView;
    private CollectAdapter codexAdapter;
    private List<CollectCharacter> allCodexEntries; // 모든 도감 항목 (획득 여부 포함)
    // --- 진행률 업데이트를 위한 인터페이스 정의 ---
    public interface OnCodexProgressListener {
        void onCodexProgressUpdated(int acquiredCount, int totalCount);
    }
    private OnCodexProgressListener progressListener;
    public void setOnCodexProgressListener(OnCodexProgressListener listener) {
        this.progressListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 다이얼로그 스타일 설정 (풀스크린에 가깝게)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 다이얼로그의 배경을 투명하게 만들어 둥근 모서리 등이 잘 보이게 함 (선택 사항, 풀스크린 시 덜 필요)
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE)); // 도감 배경은 흰색으로
            getDialog().getWindow().setDimAmount(0.8f); // 뒷 배경 어둡게
        }
        // 여기가 핵심: codex_dialog.xml 파일을 인플레이트합니다.
        return inflater.inflate(R.layout.fragment_collect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        codexRecyclerView = view.findViewById(R.id.codexRecyclerView);
        codexRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4칸 그리드

        // --- 초기 도감 데이터 설정 (테스트용) ---
        allCodexEntries = new ArrayList<>();
        // 예시: 미리 정의된 캐릭터 ID 목록
        String[] characterIds = {"char_001", "char_002", "char_003", "char_004", "char_005", "char_006", "char_007", "char_008"};
        String[] characterNames = {"ㄷㅈ", "ㅎㅈ", "ㅁㄱ", "ㄱㅈ", "ㅁㅈ", "ㅅㅇ", "ㅁㅂ", "ㅈㅇ"};
        // R.drawable.ic_launcher_foreground 는 안드로이드 프로젝트에 기본으로 있는 아이콘입니다.
        // 실제 캐릭터 이미지를 res/drawable 폴더에 넣고 R.drawable.your_character_image_name 처럼 사용하세요.
        int[] characterImages = {
                R.drawable.ic_launcher_foreground, // 예시 이미지 1
                R.drawable.ic_launcher_foreground, // 예시 이미지 2
                R.drawable.ic_launcher_foreground, // 예시 이미지 3
                R.drawable.ic_launcher_foreground, // 예시 이미지 4
                R.drawable.ic_launcher_foreground, // 예시 이미지 5
                R.drawable.ic_launcher_foreground, // 예시 이미지 6
                R.drawable.ic_launcher_foreground, // 예시 이미지 7
                R.drawable.ic_launcher_foreground  // 예시 이미지 8
        };


        for (int i = 0; i < characterIds.length; i++) {
            boolean acquired = (i % 2 == 0); // 짝수 번째는 획득으로 가정
            if (acquired) {
                allCodexEntries.add(new CollectCharacter(characterIds[i], characterNames[i], characterImages[i], true));
            } else {
                // 미획득 시에는 "???" 이름과 물음표 이미지 사용
                allCodexEntries.add(new CollectCharacter(characterIds[i], "???", R.drawable.sharp_block_24, false));
            }
        }
        // --- 초기 도감 데이터 설정 끝 ---

        codexAdapter = new CollectAdapter(allCodexEntries, this); // this는 OnCodexEntryClickListener 구현
        codexRecyclerView.setAdapter(codexAdapter);

        // --- 도감 진행률 업데이트 호출 ---
        updateCodexProgress(); // 초기 로드 시 진행률 업데이트

        // TODO: 여기서 실제 플레이어의 획득 이력을 바탕으로 allCodexEntries 리스트의 isAcquired 상태를 업데이트해야 합니다.
        // 예: List<String> playerAcquiredCharIds = getPlayerAcquiredCharacters(); // 플레이어가 획득한 캐릭터 ID 목록을 가져오는 가상의 메서드
        // for (CollectCharacter entry : allCodexEntries) {
        //     if (playerAcquiredCharIds.contains(entry.getCharacterId())) {
        //         entry.setAcquired(true);
        //         // 실제 이름과 이미지로 업데이트 (CodexEntry에 setter 필요 시)
        //         // entry.setDisplayName(getActualCharacterName(entry.getCharacterId()));
        //         // entry.setDisplayImageResId(getActualCharacterImage(entry.getCharacterId()));
        //     }
        // }
        // codexAdapter.notifyDataSetChanged(); // 변경된 데이터로 UI 갱신
    }

    @Override
    public void onResume() {
        super.onResume();
        // 다이얼로그 크기 조절 (풀스크린)
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    // CollectAdapter.OnCodexEntryClickListener 구현 (도감 항목 클릭 시)
    @Override
    public void onCodexEntryClick(CollectCharacter entry) {
        if (entry.isAcquired()) {
            Toast.makeText(getContext(), entry.getDisplayName() + " 상세 정보 보기", Toast.LENGTH_SHORT).show();
            // TODO: 획득한 캐릭터의 상세 정보를 표시하는 팝업 또는 새 프래그먼트 띄우기
        } else {
            Toast.makeText(getContext(), "아직 획득하지 않은 캐릭터입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // --- 도감 진행률 계산 및 콜백 메서드 ---
    private void updateCodexProgress() {
        if (progressListener != null) {
            int acquiredCount = 0;
            for (CollectCharacter entry : allCodexEntries) {
                if (entry.isAcquired()) {
                    acquiredCount++;
                }
            }
            int totalCount = allCodexEntries.size();
            progressListener.onCodexProgressUpdated(acquiredCount, totalCount);
        }
    }
}