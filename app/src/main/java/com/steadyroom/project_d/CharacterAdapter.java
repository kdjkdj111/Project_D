package com.steadyroom.project_d;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List; // List 인터페이스를 사용하기 위해 필요
// CharacterAdapter:
// RecyclerView에 표시될 아이템(캐릭터) 목록을 관리하고,
// 각 아이템을 화면에 어떻게 보여줄지 결정하여 그려주는 역할 (어댑터 패턴)
public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.InventoryViewHolder> {

    private List<Character> itemList; // 어댑터가 화면에 표시할 캐릭터 데이터 리스트
    private OnItemClickListener listener;// 아이템 클릭 이벤트를 외부에 전달하기 위한 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(Character item);
    }
    // 어댑터 생성자: 데이터 리스트를 전달받습니다.
    public CharacterAdapter(List<Character> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    // 뷰 홀더 생성: 화면에 표시할 새 아이템 뷰가 필요할 때 호출
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_slot, parent, false);
        return new InventoryViewHolder(view);
    }

    // 뷰에 데이터 바인딩: 뷰 홀더와 아이템 데이터를 연결합니다.
    // 재활용되는 뷰에 새로운 데이터를 설정하는 곳
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Character item = itemList.get(position); // 현재 위치의 아이템 데이터 가져오기

        holder.itemName.setText(item.getName()); // 아이템(캐릭터) 이름 설정
        holder.itemImage.setImageResource(item.getImageId()); // 아이템(캐릭터) 이미지 설정

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });
    }

    // 전체 아이템(캐릭터) 개수: RecyclerView가 표시할 총 아이템(캐릭터) 개수
    @Override
    public int getItemCount() {
        return itemList.size();
    }


    // 각 아이템 뷰 내의 UI 구성 요소들을 참조하는 역할을 합니다.
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;


        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage); // item_character_slot.xml에 정의된 이미지 뷰 ID
            itemName = itemView.findViewById(R.id.itemName);   // item_character_slot.xml에 정의된 텍스트 뷰 ID

        }
    }
}