package com.prtech.spiapp.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> items;
    private final int textColor;
    private final int backgroundColor;
    private final int dropdownTextColor;
    private final int dropdownBackgroundColor;
    private int minItemWidthInDp = 100; // Minimum width in dp

    public CustomSpinnerAdapter(Context context, List<String> items,
                                int textColor, int backgroundColor,
                                int dropdownTextColor, int dropdownBackgroundColor, int minItemWidthInDp) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
        this.items = items;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.dropdownTextColor = dropdownTextColor;
        this.dropdownBackgroundColor = dropdownBackgroundColor;
        this.minItemWidthInDp = minItemWidthInDp;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = (TextView) view;
        text.setTextColor(textColor);
        text.setBackgroundColor(backgroundColor);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView text = (TextView) view;
        applyMinWidth(view);
        text.setTextColor(dropdownTextColor);
        text.setBackgroundColor(dropdownBackgroundColor);
        return view;
    }

    private void applyMinWidth(View view) {
        int minWidthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                minItemWidthInDp,
                context.getResources().getDisplayMetrics()
        );
        view.setMinimumWidth(minWidthPx);
    }

}

