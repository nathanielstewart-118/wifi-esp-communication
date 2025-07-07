package com.example.myapplication.settings;

import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.SensorActuator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReproductionSetting extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<String, Object> accordionContentMap = new HashMap<>();

    public ReproductionSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reproduction, container, false);
        accordionContainer = view.findViewById(R.id.playback_accordion_container);

        List<SensorActuator> actuators = new ArrayList<>();
        for (SensorActuator ad: actuators) {
            addAccordionSection(ad.getVariableName());
        }


        return view;
    }

    private void addAccordionSection(String headerText) {

        // Header
        TextView header = new TextView(requireContext());
        header.setText(headerText);
        header.setTextSize(18);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(24, 24, 24, 24);
        header.setBackgroundColor(Color.parseColor("#CCCCCC"));
        header.setTextColor(Color.BLACK);

        // Content container
        LinearLayout contentLayout = new LinearLayout(requireContext());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setVisibility(View.GONE);
        contentLayout.setPadding(24, 24, 24, 24);
        contentLayout.setBackgroundColor(Color.parseColor("#EEEEEE"));

        // Content TextView
        TextView contentTextView = new TextView(requireContext());
        contentTextView.setText("This is content of Accordion");
        contentTextView.setTextSize(16);
        contentLayout.addView(contentTextView);
        // Save reference for updates
        accordionContentMap.put(headerText, contentTextView);

        // Toggle logic
        header.setOnClickListener(v -> {
            contentLayout.setVisibility(
                    contentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        accordionContainer.addView(header);
        accordionContainer.addView(contentLayout);
    }


}
