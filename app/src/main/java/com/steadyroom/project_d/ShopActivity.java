package com.steadyroom.project_d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopActivity extends AppCompatActivity {

    // 포인트 표시 텍스트뷰
    private TextView UserPoints;

    // ShopItem 인터페이스
    public interface ShopItem {
        String getName();
        void purchase(Context context);
        String getDescription();
    }

    // 개별 아이템 클래스들
    public static class ItemPackage implements ShopItem {
        @Override public String getName() { return "패키지"; }
        @Override public void purchase(Context context) {
            Toast.makeText(context, "패키지 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }
        @Override public String getDescription() { return "패키지 아이템 설명입니다."; }
    }

    public static class Percent implements ShopItem {
        @Override public String getName() { return "확률 업!"; }
        @Override public void purchase(Context context) {
            Toast.makeText(context, "확률 업 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }
        @Override public String getDescription() { return "확률을 높여주는 아이템입니다."; }
    }

    public static class AutoCatch implements ShopItem {
        @Override public String getName() { return "자동 포획 5분"; }
        @Override public void purchase(Context context) {
            Toast.makeText(context, "자동 포획 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }
        @Override public String getDescription() { return "5분간 자동으로 포획해 줍니다."; }
    }

    public static class AdvertisementRemove implements ShopItem {
        @Override public String getName() { return "광고 제거"; }
        @Override public void purchase(Context context) {
            Toast.makeText(context, "광고 제거 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }
        @Override public String getDescription() { return "광고를 제거해 쾌적한 환경을 만듭니다."; }
    }

    public static class Generate_X2 implements ShopItem {
        @Override public String getName() { return "먼지 2배 생성"; }
        @Override public void purchase(Context context) {
            // 파이어베이스 저장은 팝업에서 처리
        }
        @Override public String getDescription() { return "먼지 생성량을 2배로 늘립니다."; }
    }

    public static class SweetSmell implements ShopItem {
        @Override public String getName() { return "달콤한 향기"; }
        @Override public void purchase(Context context) {
            Toast.makeText(context, "달콤한 향기 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
        }
        @Override public String getDescription() {
            return "먼지들이 알 수 없는 힘에 의해 끌려 오게 됩니다!.\n전설의 먼지들도 올지도??";
        }
    }

    // 버튼들
    private Button btnItemPackage, btnRateUp, btnAutoCatch, btnRemoveAds, btnItemDoubleMonster, btnSweetSmell;
    private ImageButton btnBack;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        // 포인트 텍스트뷰
        UserPoints = findViewById(R.id.user_points);

        // 버튼 초기화
        btnItemPackage = findViewById(R.id.btnBuy_package);
        btnRateUp = findViewById(R.id.btnBuy_rateUp);
        btnAutoCatch = findViewById(R.id.btnBuy_autoCatch);
        btnRemoveAds = findViewById(R.id.btnBuy_removeAds);
        btnItemDoubleMonster = findViewById(R.id.btnBuy_doubleMonster);
        btnSweetSmell = findViewById(R.id.btnBuy_sweetSmell);
        btnBack = findViewById(R.id.btnBack);

        // 버튼 클릭 리스너 등록
        btnItemPackage.setOnClickListener(v -> shop_popup.show(this, new ItemPackage()));
        btnRateUp.setOnClickListener(v -> shop_popup.show(this, new Percent()));
        btnAutoCatch.setOnClickListener(v -> shop_popup.show(this, new AutoCatch()));
        btnRemoveAds.setOnClickListener(v -> shop_popup.show(this, new AdvertisementRemove()));
        btnItemDoubleMonster.setOnClickListener(v -> shop_popup.show(this, new Generate_X2()));
        btnSweetSmell.setOnClickListener(v -> shop_popup.show(this, new SweetSmell()));

        btnBack.setOnClickListener(view -> finish());

        // 포인트 불러오기
        loadUserPoints();
    }

    private void loadUserPoints() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            UserPoints.setText("로그인 필요");
            return;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.child("userPoint").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long point = snapshot.getValue(Long.class);
                if (point != null) {
                    UserPoints.setText("포인트: " + point);
                } else {
                    UserPoints.setText("포인트: 0");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                UserPoints.setText("포인트 불러오기 실패");
            }
        });
    }
}
