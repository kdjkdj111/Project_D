package com.steadyroom.project_d;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class shop_popup {


    public static void show(Context context, ShopActivity.ShopItem item){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_shop_popup);

        TextView itemNameView = dialog.findViewById(R.id.item_name);
        TextView itemDescription = dialog.findViewById(R.id.item_description);


        itemNameView.setText(item.getName());
        itemDescription.setText(item.getDescription());

        Button purchase = dialog.findViewById(R.id.btn_buy);
        purchase.setOnClickListener(v -> {
            item.purchase(context);
            dialog.dismiss();
        });

        Button cancel = dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
