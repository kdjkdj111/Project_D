package com.steadyroom.project_d;

import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

public class SliderTransformer implements ViewPager2.PageTransformer {
    private static final float SCALE_MAX = 0.85f;
    private static final float SCALE_DEFAULT = 1f;
    private static final float SCALE_FACTOR = 0.9f;
    private static final float ALPHA_FACTOR = 0.8f;
    private static final float ALPHA_DEFAULT = 1.0f;
    private static final float TOP_Y = 0f;
    private static final int CARD_GAP_DP = 20;

    @Override
    public void transformPage(View page, float position) {
        CardView cardView = page.findViewById(R.id.card_view);
        int cardViewHeight = cardView.getHeight();

        if (position < 0.0f) {
            float scale = SCALE_DEFAULT + SCALE_FACTOR * position;
            // scale 값이 SCALE_MAX보다 작으면 SCALE_MAX 사용
            float clampedScale = Math.max(scale, SCALE_MAX);
            page.setScaleX(clampedScale);
            page.setScaleY(clampedScale);
            float alpha = ALPHA_DEFAULT + (position * ALPHA_FACTOR);
            page.setAlpha(alpha);
            page.setY(TOP_Y);
        } else if (position >= 0.0f && position <= 1.0f) {
            page.setScaleX(SCALE_DEFAULT);
            page.setScaleY(SCALE_DEFAULT);
            page.setAlpha(ALPHA_DEFAULT);
            page.setY(TOP_Y + position);
            page.setTranslationY(position);
        } else {
            page.setScaleX(SCALE_DEFAULT);
            page.setScaleY(SCALE_DEFAULT);
            page.setAlpha(ALPHA_DEFAULT);
            float bottomGap = page.getHeight() - cardViewHeight;
            float density = page.getResources().getDisplayMetrics().density;
            float cardGap = CARD_GAP_DP * density;

            if (bottomGap < cardGap) {
                page.setTranslationY(position);
            } else {
                page.setTranslationY(position - bottomGap + cardGap);
            }
        }
    }
}
