package com.steadyroom.project_d;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class shop_popup {

    // 아이템 객체를 직접 넘기는 방식
    public static void show(Context context, ShopActivity.ShopItem item){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_shop_popup);  // ⚠️ 'activity_shop_popup'이 아닌 'shop_popup' 사용

        TextView itemNameView = dialog.findViewById(R.id.item_name);
        TextView itemDescription = dialog.findViewById(R.id.item_description);

        // 객체에서 직접 정보 가져오기
        itemNameView.setText(item.getName());
        itemDescription.setText(item.getDescription());

        Button purchase = dialog.findViewById(R.id.btn_buy);
        purchase.setOnClickListener(v -> {
            item.purchase(context);  // 아이템 자체에 정의된 구매 로직 실행
            dialog.dismiss();
        });

        Button cancel = dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
