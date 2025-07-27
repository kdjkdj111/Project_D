package com.steadyroom.project_d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GachaAdapter extends RecyclerView.Adapter<GachaAdapter.GachaViewHolder> {
    private List<Character> characterList;
    private Context context;
    private User currentUser;
    private DatabaseReference userRef;

    // 생성자
    public GachaAdapter(Context context, List<Character> characterList, User currentUser, DatabaseReference userRef)  {
        this.context = context;
        this.characterList = characterList;
        this.currentUser = currentUser;
        this.userRef = userRef;
    }

    @Override
    public GachaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_object, parent, false);
        return new GachaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GachaViewHolder holder, int position) {
        Character character = characterList.get(position);

        holder.tvName.setText(character.getName());
        holder.tvAttack.setText("Atk: " + character.getAttack());
        holder.tvHP.setText("HP: " + character.getHp());
        holder.tvDirt.setText("Dirt: " + character.getDirt());

        // 이미지 셋팅 (이미지도 넣었다면 이름별로 구분하거나, 기본 이미지를 사용)
        holder.imageView.setImageResource(character.getImageId());

        holder.btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Character character = characterList.get(currentPosition);

                    // 1. 로컬: 유저의 캐릭터 목록에 추가
                    currentUser.characters.add(character);

                    // 2. Firebase에 동기화
                    DatabaseReference dbRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(currentUser.user_id)
                            .child("characters");
                    dbRef.setValue(currentUser.characters);

                    // 3. (선택) 화면에서 카드 제거 등
                    characterList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);

                    // 4. (선택) 완료 콜백 등 처리
                    // dbRef.setValue(…).addOnCompleteListener(task -> { ... });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    // ViewHolder 내부 클래스
    public class GachaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvName, tvAttack, tvHP, tvDirt;
        Button btnGet;

        public GachaViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvName = itemView.findViewById(R.id.tv_Name);
            tvAttack = itemView.findViewById(R.id.tv_Attack);
            tvHP = itemView.findViewById(R.id.tv_HP);
            tvDirt = itemView.findViewById(R.id.tv_Dirt);
            btnGet = itemView.findViewById(R.id.btn_get);
        }
    }
}
