package com.steadyroom.project_d;

import static com.steadyroom.project_d.CharacterList.BASE_POOL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GachaAdapter extends RecyclerView.Adapter<GachaAdapter.GachaViewHolder> {
    private Context context;
    private User currentUser;
    private DatabaseReference userRef;

    // 생성자
    public GachaAdapter(Context context, User currentUser, DatabaseReference userRef)  {
        this.context = context;
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
        Character character = pickRandomCharacter(BASE_POOL);

        holder.tvName.setText(character.getName());
        holder.tvAttack.setText("Atk: " + character.getAttack());
        holder.tvHP.setText("HP: " + character.getHp());
        holder.tvDirt.setText("Dirt: " + character.getDirt());

        //holder.imageView.setImageResource(character.getImageId());
        // String 타입의 이미지 ID를 int 타입의 리소스 ID로 변환
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(
                character.getImageId(),
                "drawable",
                holder.itemView.getContext().getPackageName()
        );

        // 리소스 ID가 유효한지 확인하고 이미지 설정
        if (imageResId != 0) {
            holder.imageView.setImageResource(imageResId);
        }
        holder.btnGet.setEnabled(true);

        holder.btnGet.setOnClickListener(v-> {
            holder.btnGet.setEnabled(false);

            currentUser.characters.add(character);

            // 2. Firebase에 동기화
            userRef.child("characters").setValue(currentUser.characters);

            // 3. (선택) 화면에서 카드 제거 등
            holder.btnGet.setEnabled(false);
            Toast.makeText(context, character.getName() + " 획득!", Toast.LENGTH_SHORT).show();

            // 4. (선택) 완료 콜백 등 처리
            // dbRef.setValue(…).addOnCompleteListener(task -> { ... })
        });
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 무한
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
    private Character pickRandomCharacter(List<Character> pool) {
        double rand = Math.random(); // 0~1
        double cumulative = 0.0;
        for (Character c : pool) {
            cumulative += c.getAppearChance();
            if (rand < cumulative) return c;
        }
        return pool.get(pool.size()-1);
    }

}
