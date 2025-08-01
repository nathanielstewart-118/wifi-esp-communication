package com.prtech.spiapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.ToggleButton;

import com.prtech.spiapp.R;

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
        changeValueBtn.setBackgroundColor(Color.TRANSPARENT);
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

    public static int setSpinnerWithContent(Spinner spinner, String targetValue) {
        try {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            int position = adapter.getPosition(targetValue); // Find its position
            spinner.setSelection(position);
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getIndexFromSpinnerContent(Spinner spinner, String targetValue) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(targetValue)) {
                return i;
            }
        }
        return -1; // Not found
    }

    public static int initSpinnerWithSuggestionList(Spinner spinner, List<String> suggestions, Context context, int layoutResId) {
        if (spinner == null) return -1;
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    layoutResId, suggestions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int initAutoCompleteWithSuggestionList(AutoCompleteTextView autoCompleteTextView, List<String> suggestions, Context context) {
        if ( autoCompleteTextView == null || context == null) {
            return -1;
        }
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            autoCompleteTextView.setAdapter(adapter);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void uncheckOtherToggles(ToggleButton selectedToggle, List<ToggleButton> allToggleButtons) {
        for (ToggleButton toggle: allToggleButtons) {
            if (toggle != selectedToggle) {
                toggle.setChecked(false);
            }
        }
    }
}
