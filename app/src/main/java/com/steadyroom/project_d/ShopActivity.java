package com.steadyroom.project_d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ShopActivity extends AppCompatActivity {

    // 아이템 공통 인터페이스
    public interface ShopItem {
        String getName();
        void purchase(Context context);
        String getDescription();
    }

    // ItemPackage는 abstract 제거, 구체 클래스가 됨
    public static class ItemPackage implements ShopItem {
        @Override
        public String getName() {
            return "패키지";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "패키지 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "패키지 아이템 설명입니다.";
        }
    }

    public static class Percent implements ShopItem {
        @Override
        public String getName() {
            return "확률 업!";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "확률 업 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "확률을 높여주는 아이템입니다.";
        }
    }

    public static class AutoCatch implements ShopItem {
        @Override
        public String getName() {
            return "자동 포획 5분";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "자동 포획 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "5분간 자동으로 포획해 줍니다.";
        }
    }

    public static class AdvertisementRemove implements ShopItem {
        @Override
        public String getName() {
            return "광고 제거";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "광고 제거 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "광고를 제거해 쾌적한 환경을 만듭니다.";
        }
    }

    public static class Generate_X2 implements ShopItem {
        @Override
        public String getName() {
            return "먼지 2배 생성";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "먼지 2배 생성 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "먼지 생성량을 2배로 늘립니다.";
        }
    }

    public static class SweetSmell implements ShopItem {
        @Override
        public String getName() {
            return "달콤한 향기";
        }

        @Override
        public void purchase(Context context) {
            Toast.makeText(context, "달콤한 향기 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getDescription() {
            return "먼지들이 알 수 없는 힘에 의해 끌려 오게 됩니다!.\n전설의 먼지들도 올지도??";
        }
    }

    // 버튼 선언
    private Button btnItemPackage;
    private Button btnRateUp;
    private Button btnAutoCatch;
    private Button btnRemoveAds;
    private Button btnItemDoubleMonster;
    private Button btnSweetSmell;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        // 버튼 초기화
        btnItemPackage = findViewById(R.id.btnBuy_package);
        btnRateUp = findViewById(R.id.btnBuy_rateUp);
        btnAutoCatch = findViewById(R.id.btnBuy_autoCatch);
        btnRemoveAds = findViewById(R.id.btnBuy_removeAds);
        btnItemDoubleMonster = findViewById(R.id.btnBuy_doubleMonster);
        btnSweetSmell = findViewById(R.id.btnBuy_sweetSmell);

        // 버튼 클릭 시 popup 호출
        btnItemPackage.setOnClickListener(v -> shop_popup.show(this, new ItemPackage()));
        btnRateUp.setOnClickListener(v -> shop_popup.show(this, new Percent()));
        btnAutoCatch.setOnClickListener(v -> shop_popup.show(this, new AutoCatch()));
        btnRemoveAds.setOnClickListener(v -> shop_popup.show(this, new AdvertisementRemove()));
        btnItemDoubleMonster.setOnClickListener(v -> shop_popup.show(this, new Generate_X2()));
        btnSweetSmell.setOnClickListener(v -> shop_popup.show(this, new SweetSmell()));
    }
}
