package com.example.myapplication.utils.commonuis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectDialog extends LinearLayout {

    private ListView listView;
    private String[] items;

    public MultiSelectDialog(Context context) {
        super(context);
    }

    public MultiSelectDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiSelectDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Initialize the view with a layout and the ListView's ID and choice mode
     */
    public void setup(@LayoutRes int layoutResId, @IdRes int listViewId, int choiceMode) {
        removeAllViews(); // prevent duplicate views
        View view = LayoutInflater.from(getContext()).inflate(layoutResId, this, true);
        listView = view.findViewById(listViewId);
        listView.setChoiceMode(choiceMode);
    }

    public void setItems(String[] items, boolean[] selected) {
        this.items = items;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_multiple_choice, items);
        listView.setAdapter(adapter);

        for (int i = 0; i < selected.length && i < items.length; i++) {
            listView.setItemChecked(i, selected[i]);
        }
    }

    public List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            if (listView.isItemChecked(i)) {
                selected.add(items[i]);
            }
        }
        return selected;
    }

    public void clearSelection() {
        for (int i = 0; i < listView.getCount(); i++) {
            listView.setItemChecked(i, false);
        }
    }

    public ListView getListView() {
        return listView;
    }
}
