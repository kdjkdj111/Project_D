package com.steadyroom.project_d;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class shop_popup {

    public static void show(Context context, ShopActivity.ShopItem item) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_shop_popup);

        TextView itemNameView = dialog.findViewById(R.id.item_name);
        TextView itemDescription = dialog.findViewById(R.id.item_description);

        itemNameView.setText(item.getName());
        itemDescription.setText(item.getDescription());

        Button purchase = dialog.findViewById(R.id.btn_buy);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        purchase.setOnClickListener(v -> {
            if (item instanceof ShopActivity.Generate_X2) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                long now = System.currentTimeMillis();

                HashMap<String, Object> itemData = new HashMap<>();
                itemData.put("isActive", true);
                itemData.put("purchasedAt", now);

                userRef.child("inventory").child("dustDouble").setValue(itemData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "먼지 2배 생성 아이템 구매 완료!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "구매 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                item.purchase(context);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
