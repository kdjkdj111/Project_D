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
        CharacterInstance characterInstance = pickRandomCharacter(BASE_POOL);

        holder.tvName.setText(characterInstance.getName());
        holder.tvAttack.setText("Atk: " + characterInstance.getAttack());
        holder.tvHP.setText("HP: " + characterInstance.getHp());
        holder.tvDirt.setText("Dirt: " + characterInstance.getDirt());
        holder.imageView.setImageResource(characterInstance.getImageId());

        holder.btnGet.setEnabled(true);

        holder.btnGet.setOnClickListener(v-> {
            holder.btnGet.setEnabled(false);

            currentUser.characters.add(characterInstance);

            // 2. Firebase에 동기화
            userRef.child("characters").setValue(currentUser.characters);

            // 3. (선택) 화면에서 카드 제거 등
            Toast.makeText(context, characterInstance.getName() + " 획득!", Toast.LENGTH_SHORT).show();

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
    public CharacterInstance pickRandomCharacter(List<CharacterTemplate> pool) {
        double rand = Math.random();
        double cumulative = 0.0;
        for (CharacterTemplate c : pool) {
            cumulative += c.getAppearChance();
            if (rand < cumulative) {
                // base 캐릭터 선택됨 → 랜덤 특성 인스턴스 생성 & 반환!
                return createRandomInstance(c);
            }
        }
        // 혹시 누락방지
        return createRandomInstance(pool.get(pool.size() - 1));
    }

    public CharacterInstance createRandomInstance(CharacterTemplate base) {
        int attack = getRandomInRange(base.getMinAttack(), base.getMaxAttack());
        int hp = getRandomInRange(base.getMinHp(), base.getMaxHp());
        int dirt = getRandomInRange(base.getMinDirt(), base.getMaxDirt());
        return new CharacterInstance(base.getName(), attack, hp, dirt, base.getImageId());
    }

    private int getRandomInRange(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
    }

}
