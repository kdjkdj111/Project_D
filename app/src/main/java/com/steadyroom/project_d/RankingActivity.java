package com.steadyroom.project_d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {
    private RecyclerView rvRanking;
    private RankingAdapter rankingAdapter;
    private List<Ranking> rankingList = new ArrayList<>();

    private Button btnGotoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rvRanking = findViewById(R.id.rv_ranking);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        rankingAdapter = new RankingAdapter(rankingList);
        rvRanking.setAdapter(rankingAdapter);

        btnGotoMain = findViewById(R.id.btn_gotoMain);

        fetchRankings();



        btnGotoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RankingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchRankings() {
        DatabaseReference rankingsRef = FirebaseDatabase.getInstance().getReference("rankings");
        rankingsRef.orderByValue().limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Ranking> updatedList = new ArrayList<>();
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            String uid = entry.getKey();
                            Long rankingPoint = entry.getValue(Long.class);
                            updatedList.add(new Ranking(uid, rankingPoint != null ? rankingPoint : 0L));
                        }

                        for (Ranking user : updatedList) {
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(user.uid).child("nickname")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot ds) {
                                            String nick = ds.getValue(String.class);
                                            user.nickname = nick != null ? nick : user.uid;
                                            rankingAdapter.notifyDataSetChanged();
                                        }
                                        public void onCancelled(DatabaseError error) { }
                                    });
                        }

                        rankingList.clear();
                        rankingList.addAll(updatedList);
                        rankingAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }
}
