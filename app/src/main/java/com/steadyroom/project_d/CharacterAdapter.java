package com.steadyroom.project_d;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.steadyroom.project_d.CharacterActivity.OnCharacterSelectListener;
import java.util.List;

// RecyclerView에 표시될 캐릭터 목록을 관리하고,
// 각 캐릭터를 화면에 어떻게 보여줄지 결정하여 그려주는 역할 (어댑터 패턴)
public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.InventoryViewHolder> {

    private List<CharacterInstance> CharacterGrid; // 어댑터가 화면에 표시할 캐릭터 데이터 리스트
    private OnCharacterClickListener listener;// 캐릭터 클릭 이벤트를 외부에 전달하기 위한 리스너 인터페이스
    private boolean isSellingMode = false;

    private OnCharacterSelectListener selectListener;
    private List<CharacterInstance> selectedcharacters = new ArrayList<>(); // 어댑터 내부에서 선택된 아이템을 관리할 리스트
    public interface OnCharacterClickListener {
        void OnCharacterClick(CharacterInstance instance);
    }
    // 어댑터 생성자: 데이터 리스트를 전달받습니다.
    public CharacterAdapter(List<CharacterInstance> CharacterList, OnCharacterClickListener listener, CharacterActivity.OnCharacterSelectListener selectListener) {
        this.CharacterGrid = CharacterList;
        this.listener = listener;
        this.selectListener = selectListener; // 새 리스너 초기화
    }

    // 뷰 홀더 생성: 화면에 표시할 새 캐릭터 뷰가 필요할 때 호출
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_slot, parent, false);
        return new InventoryViewHolder(view);
    }

    // 뷰에 데이터 바인딩: 뷰 홀더와 캐릭터 데이터를 연결합니다.
    // 재활용되는 뷰에 새로운 데이터를 설정하는 곳
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        CharacterInstance instance = CharacterGrid.get(position); // 현재 위치의 캐릭터 데이터 가져오기
        holder.characterName.setText(instance.getName()); // 캐릭터 이름 설정
        // String ID를 int 리소스 ID로 변환
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(
                instance.getImageId(),
                "drawable",
                holder.itemView.getContext().getPackageName()
        );
        if (imageResId != 0) {
            holder.characterImage.setImageResource(imageResId);
        }
        if (selectedcharacters.contains(instance)) {
            // 선택된 캐릭터: 노란색 테두리 적용
            holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.selected_sale_border));
        } else {
            // 선택되지 않은 캐릭터: 노란 테두리 제거
            holder.itemView.setBackground(null);
        }
        // 캐릭터 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnCharacterClick(instance);
                }
            }
        });
        // 판매 모드에 따라 클릭 리스너를 다르게 설정
        holder.itemView.setOnClickListener(v -> {
            if (isSellingMode) {
                // 판매 모드일 때
                boolean isSelected = selectedcharacters.contains(instance);
                if (isSelected) {
                    selectedcharacters.remove(instance);
                    selectListener.onCharacterSelected(instance, false);
                } else {
                    selectedcharacters.add(instance);
                    selectListener.onCharacterSelected(instance, true);
                }
                if (selectedcharacters.contains(instance)) {
                    // 선택된 캐릭터: 노란색 테두리 적용
                    holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.selected_sale_border));
                } else {
                    // 선택되지 않은 캐릭터: 노란 테두리 제거
                    holder.itemView.setBackground(null);
                }
            } else {
                // 일반 모드일 때
                if (listener != null) {
                    listener.OnCharacterClick(instance);
                }
            }
        });
    }
    public void setSellingMode(boolean isSellingMode) {
        this.isSellingMode = isSellingMode;
        if (!isSellingMode) {
            selectedcharacters.clear();
        }
        notifyDataSetChanged(); // UI 새로고침
    }

    // 전체 캐릭터 개수: RecyclerView가 표시할 총 캐릭터 개수
    @Override
    public int getItemCount() {
        return CharacterGrid.size();
    }


    // 각 아이템 뷰 내의 UI 구성 요소들을 참조하는 역할을 합니다.
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView characterImage;
        TextView characterName;


        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            characterImage = itemView.findViewById(R.id.charImage); // item_character_slot.xml에 정의된 이미지 뷰 ID
            characterName = itemView.findViewById(R.id.charName);   // item_character_slot.xml에 정의된 텍스트 뷰 ID

        }
    }
}