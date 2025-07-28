// CollectAdapter.java
// 캐릭터 리스트를 받아 각 캐릭터 슬롯 뷰를 만들어 화면에 표시하고 클릭 이벤트를 처리하는 어댑터
package com.steadyroom.project_d;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CollectAdapter extends RecyclerView.Adapter<CollectAdapter.CodexViewHolder> {

    private List<CollectCharacter> codexEntries;
    private OnCodexEntryClickListener listener;

    public CollectAdapter(List<CollectCharacter> codexEntries, OnCodexEntryClickListener listener) {
        this.codexEntries = codexEntries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CodexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 이 부분이 각 도감 항목의 레이아웃을 인플레이트합니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collect_slot, parent, false);
        return new CodexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CodexViewHolder holder, int position) {
        CollectCharacter entry = codexEntries.get(position);

        holder.codexName.setText(entry.getDisplayName());
        holder.codexImage.setImageResource(entry.getDisplayImageResId());

        // 획득 여부에 따른 시각적 처리
        if (entry.isAcquired()) {
            holder.codexImage.setAlpha(1.0f); // 획득 시 불투명하게
        } else {
            holder.codexImage.setAlpha(0.3f); // 미획득 시 투명하게 (물음표가 희미하게 보임)
        }

        // 항목 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCodexEntryClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return codexEntries.size();
    }

    // 데이터를 업데이트하는 메서드 (새로운 캐릭터 획득 시 호출)
    public void updateCodexEntries(List<CollectCharacter> newEntries) {
        this.codexEntries = newEntries;
        notifyDataSetChanged();
    }

    // ViewHolder 내부 클래스
    public static class CodexViewHolder extends RecyclerView.ViewHolder {
        ImageView codexImage;
        TextView codexName;

        public CodexViewHolder(@NonNull View itemView) {
            super(itemView);
            codexImage = itemView.findViewById(R.id.codexImage);
            codexName = itemView.findViewById(R.id.codexName);
        }
    }

    // 항목 클릭 이벤트를 Activity/Fragment로 전달하기 위한 인터페이스
    public interface OnCodexEntryClickListener {
        void onCodexEntryClick(CollectCharacter entry);
    }
}