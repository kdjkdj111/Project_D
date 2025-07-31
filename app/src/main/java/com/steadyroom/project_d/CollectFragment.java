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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CollectFragment extends DialogFragment implements CollectAdapter.OnCodexEntryClickListener {
    private List<CollectCharacter> collectCharacterList;
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

        collectCharacterList = new ArrayList<>();

        //  CollectFragment가 Bundle을 잘 받았는지 확인하는 로그
        if (getArguments() != null) {
            List<CharacterTemplate> inventory = (List<CharacterTemplate>) getArguments().getSerializable("inventory");

            // 여기서 inventory 리스트를 기반으로 도감용 CollectCharacter 리스트 만들어서 사용
            //  inventory 리스트가 null이 아닌지 확인하는 로그
            if (inventory != null) {
                /*Log.d("CollectFragment", "인벤토리 리스트 받음. 크기: " + inventory.size());
                if (inventory.isEmpty()) {
                    Log.w("CollectFragment", "인벤토리 리스트가 비어 있습니다. 도감에 획득 캐릭터가 없을 수 있습니다.");
                }*/

                for (CharacterTemplate characterTemplate : CharacterList.BASE_POOL) {
                    boolean isAcquired = false;

                    for (CharacterTemplate invChar : inventory) {
                        if (characterTemplate.getName().equals(invChar.getName())) {
                            isAcquired = true;
                            break;
                        }
                    }

                    String displayName = isAcquired ? characterTemplate.getName() : "???";
                    int displayImageResId = isAcquired ? characterTemplate.getImageId() : R.drawable.sharp_block_24;

                    collectCharacterList.add(new CollectCharacter(displayName, displayImageResId, isAcquired));
                    // 💡 collectCharacterList에 항목이 추가될 때마다 확인하는 로그
                    /*Log.d("CollectFragment", "도감 항목 추가: " + displayName + ", 획득 여부: " + isAcquired); */
                }
                /*Log.d("CollectFragment", "collectCharacterList 최종 크기: " + collectCharacterList.size()); */
            } else {
                /*Log.w("CollectFragment", "전달받은 인벤토리 리스트가 null입니다."); */
            }
        } /*else {
            Log.w("CollectFragment", "getArguments()가 null입니다. Bundle이 전달되지 않았습니다."); // <-- 이 줄을 추가
        }*/
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
        if (closeButton != null) { // null 체크 추가
            closeButton.setOnClickListener(v -> dismiss());
        } else {
            /*Log.e("CollectFragment", "closeButton을 찾을 수 없습니다! XML ID를 확인하세요.");*/
        }

        codexRecyclerView = view.findViewById(R.id.codexRecyclerView);
        if (codexRecyclerView != null) { // null 체크 추가
            codexRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4)); // 4칸 그리드
            codexAdapter = new CollectAdapter(collectCharacterList, this); // <<-- 이 부분을 이렇게 수정하세요!
            codexRecyclerView.setAdapter(codexAdapter);
            /*Log.d("CollectFragment", "어댑터 설정 완료. 표시할 도감 항목 수: " + collectCharacterList.size());*/
        } else {
            /*Log.e("CollectFragment", "codexRecyclerView를 찾을 수 없습니다! XML ID가 맞는지 확인하세요.");*/
        }

        // --- 도감 데이터 생성 로직 끝 ---
        // --- 도감 진행률 업데이트 호출 ---
        updateCodexProgress(); // 초기 로드 시 진행률 업데이트
        // TODO: 여기서 실제 플레이어의 획득 이력을 바탕으로 allCodexEntries 리스트의 isAcquired 상태를 업데이트해야 합니다.
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
            for (CollectCharacter entry : collectCharacterList) {
                if (entry.isAcquired()) {
                    acquiredCount++;
                }
            }
            int totalCount = collectCharacterList.size();
            progressListener.onCodexProgressUpdated(acquiredCount, totalCount);
        }
    }
}