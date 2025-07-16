package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.TableRow;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class UIUtils {
    public static List<ImageButton> setupOperationalButtons(Long id, Context context) {
        ImageButton iconButton = new ImageButton(context);
        iconButton.setImageResource(R.drawable.baseline_edit_24); // your drawable icon
        iconButton.setBackgroundColor(Color.TRANSPARENT); // optional styling

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                100,
                80
        );
        ImageButton changeValueBtn = new ImageButton(context);
        changeValueBtn.setImageResource(R.drawable.baseline_edit_24);
        changeValueBtn.setBackgroundColor(Color.WHITE);
        changeValueBtn.setColorFilter(Color.parseColor("#198754"));
        changeValueBtn.setLayoutParams(params);
        changeValueBtn.setTag(id);
        ImageButton changeOrderBtn = new ImageButton(context);
        changeOrderBtn.setImageResource(R.drawable.baseline_bar_chart_24);
        changeOrderBtn.setBackgroundColor(Color.TRANSPARENT);
        changeOrderBtn.setColorFilter(Color.parseColor("#0dcaf0"));
        changeOrderBtn.setTag(id);
        ImageButton deleteBtn = new ImageButton(context);
        deleteBtn.setImageResource(R.drawable.baseline_delete_24);
        deleteBtn.setBackgroundColor(Color.TRANSPARENT);
        deleteBtn.setColorFilter(Color.parseColor("#dc3545"));
        deleteBtn.setTag(id);

        List<ImageButton> buttons = new ArrayList<>();
        buttons.add(changeValueBtn);
        buttons.add(changeOrderBtn);
        buttons.add(deleteBtn);
        return buttons;

    }
}
