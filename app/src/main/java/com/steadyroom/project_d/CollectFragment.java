package com.steadyroom.project_d;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectFragment extends DialogFragment implements CollectAdapter.OnCodexEntryClickListener {
    private List<CollectCharacter> collectCharacterList;
    private RecyclerView codexRecyclerView;
    private CollectAdapter codexAdapter;

    // Firebase에서 불러온 획득 기록을 저장할 Map
    private Map<String, Boolean> acquiredCodexEntries = new HashMap<>();

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
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);
        collectCharacterList = new ArrayList<>();

        //  Firebase에서 획득 기록을 불러옴
        loadAcquiredCharactersFromFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            getDialog().getWindow().setDimAmount(0.8f);
        }
        return inflater.inflate(R.layout.fragment_collect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView closeButton = view.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }

        codexRecyclerView = view.findViewById(R.id.codexRecyclerView);
        if (codexRecyclerView != null) {
            codexRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            // 아직 데이터가 없으므로 빈 리스트로 어댑터 초기화
            codexAdapter = new CollectAdapter(collectCharacterList, this);
            codexRecyclerView.setAdapter(codexAdapter);
        }
    }

    //사용자가 획득한 캐릭터 기록(true/false)
    private void loadAcquiredCharactersFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Firebase의 'codex' 노드 참조
            DatabaseReference codexRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("codex");

            codexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    acquiredCodexEntries.clear();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        // 획득한 캐릭터의 이름/ID를 Map에 저장
                        acquiredCodexEntries.put(childSnapshot.getKey(), true);
                    }
                    // 데이터 로드 후 도감 항목 생성 및 UI 업데이트
                    createCodexEntries();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CollectFragment", "Firebase에서 도감 데이터 로드 실패: " + error.getMessage());
                }
            });
        }
    }

    private void createCodexEntries() {
        collectCharacterList.clear();
        for (CharacterTemplate characterTemplate : CharacterList.BASE_POOL) {
            // acquiredCodexEntries Map을 사용하여 캐릭터 획득 여부 확인
            boolean isAcquired = acquiredCodexEntries.containsKey(characterTemplate.getName());

            String displayName = isAcquired ? characterTemplate.getName() : "???";
            int displayImageResId;
            if (isAcquired) {
                String imageIdString = characterTemplate.getImageId();
                displayImageResId = requireContext().getResources().getIdentifier(
                        imageIdString,
                        "drawable",
                        requireContext().getPackageName()
                );
            } else {
                displayImageResId = R.drawable.sharp_block_24;
            }
            collectCharacterList.add(new CollectCharacter(displayName, displayImageResId, isAcquired));
        }

        // 데이터가 변경될시 갱신
        if (codexAdapter != null) {
            codexAdapter.notifyDataSetChanged();
            updateCodexProgress();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onCodexEntryClick(CollectCharacter entry) {
        if (entry.isAcquired()) {
            Toast.makeText(getContext(), entry.getDisplayName() + " 상세 정보 보기", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "아직 획득하지 않은 캐릭터입니다.", Toast.LENGTH_SHORT).show();
        }
    }

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