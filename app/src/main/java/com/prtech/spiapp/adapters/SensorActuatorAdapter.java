package com.prtech.spiapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.prtech.spiapp.db.entity.SensorActuatorIdWithTitle;

import java.util.List;

public class SensorActuatorAdapter extends ArrayAdapter<SensorActuatorIdWithTitle> {
    public SensorActuatorAdapter(Context context, List<SensorActuatorIdWithTitle> records) {
        super(context, android.R.layout.simple_dropdown_item_1line, records);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        SensorActuatorIdWithTitle record = getItem(position);
        if (record != null) {
            view.setText(record.getAutoCompleteText()); // use your custom function
        }
        return view;
    }
}
