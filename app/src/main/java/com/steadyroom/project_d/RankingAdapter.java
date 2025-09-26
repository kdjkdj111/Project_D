package com.steadyroom.project_d;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
    private List<Ranking> rankingList;

    public RankingAdapter(List<Ranking> rankingList) {
        this.rankingList = rankingList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvPoint;
        public ViewHolder(View v) {
            super(v);
            tvRank = v.findViewById(R.id.tvRank);
            tvName = v.findViewById(R.id.tvName);
            tvPoint = v.findViewById(R.id.tvRankPoint);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ranking user = rankingList.get(rankingList.size() - 1 - position); // 역순 표출
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(user.nickname != null ? user.nickname : user.uid);
        holder.tvPoint.setText(user.rankingPoint + "점");
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }
}

