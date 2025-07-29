package com.prtech.spiapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.entity.ESPPacket;

import java.util.List;

public class ESPPacketAdapter extends ArrayAdapter<ESPPacket> {
    public ESPPacketAdapter(Context context, List<ESPPacket> espPackets) {
        super(context, android.R.layout.simple_spinner_item, espPackets);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);

        TextView text = view.findViewById(android.R.id.text1);
        ESPPacket espPacket = getItem(position);
        if (espPacket != null) text.setText(espPacket.getVariableName());

        return view;
    }
}
