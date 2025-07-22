package com.example.myapplication.settings;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.ESPReceiveData;
import com.example.myapplication.db.entity.Monitoring;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.viewmodel.MonitoringViewModel;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;
import com.example.myapplication.db.viewmodel.TCPUDPReceiveViewModel;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.LogHelper;
import com.example.myapplication.utils.communications.PacketParser;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;



public class MonitoringSetting extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<String, Object> accordionContentMap = new HashMap<>();

    private final Gson gson = new Gson();

    private SensorActuatorViewModel sensorActuatorViewModel;
    private TCPUDPReceiveViewModel receiveViewModel;
    private MonitoringViewModel monitoringViewModel;
    private List<SensorActuator> sensorActuators = new ArrayList<>();
    private ScatterChart scatterChart;
    private int[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA,
            Color.CYAN, Color.YELLOW, Color.GRAY, Color.BLACK
    };


    public MonitoringSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        accordionContainer = view.findViewById(R.id.monitoring_accordion_container);
        receiveViewModel = new ViewModelProvider(requireActivity()).get(TCPUDPReceiveViewModel.class);
        receiveViewModel.getData().observe(getViewLifecycleOwner(), data -> {
            LogHelper.sendLog(
                Constants.LOGGING_BASE_URL,
                Constants.LOGGING_REQUEST_METHOD,
                "New Data arrived from TCP : " + Arrays.toString(data),
                Constants.LOGGING_BEARER_TOKEN
            );
            Log.d("Monitoring Info", "New Data arrived from TCP : " + Arrays.toString(data));
            updateViews(data, 6);

        });

        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), data -> {
            List<SensorActuator> actuators = (List<SensorActuator>) data;
            sensorActuators.clear();
            sensorActuators.addAll(actuators);
            displayAccordion(sensorActuators);
        });

        monitoringViewModel = new ViewModelProvider(requireActivity()).get(MonitoringViewModel.class);
        scatterChart = new ScatterChart(requireContext());
        scatterChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        scatterChart.getDescription().setEnabled(false);
        scatterChart.getAxisRight().setEnabled(false);
        scatterChart.getXAxis().setAxisMinimum(0f);
        scatterChart.getXAxis().setAxisMaximum(500f);
        scatterChart.getAxisLeft().setAxisMinimum(0);
        scatterChart.getAxisLeft().setAxisMaximum(100);
        scatterChart.getXAxis().setDrawGridLines(false);
        scatterChart.getLegend().setForm(Legend.LegendForm.SQUARE);

        return view;
    }

    private void addAccordionSection(String headerText) {

        // Header
        TextView header = new TextView(requireContext());
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
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
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        ));



//        ArrayList<Entry> entries = new ArrayList<>();
//        for (int i = 0; i <= 100; i += 10) {
//            entries.add(new Entry(i, i * 2)); // Example: All values = 0 (like your image)
//        }
//
//        // Create dataset
//        LineDataSet dataSet = new LineDataSet(entries, "Channel 1");
//        dataSet.setDrawCircles(true);
//        dataSet.setCircleRadius(5f);
//        dataSet.setCircleColor(Color.RED);
//        dataSet.setColor(Color.TRANSPARENT);
//        LineData lineData = new LineData(dataSet);
//        scatterChart.setData(lineData);

        scatterChart.invalidate(); // refresh chart

        // Content TextView
        TextView contentTextView = new TextView(requireContext());
        contentTextView.setText("This is content of Accordion");
        contentTextView.setTextSize(16);
        contentLayout.addView(contentTextView);
        contentLayout.addView(scatterChart);

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

    public void displayAccordion(List<SensorActuator> data) {
        for (SensorActuator ad: data) {
            addAccordionSection(ad.getVariableName());
        }
    }

    public void updateViews(byte[] data, int cnt) {
        Monitoring monitoring = new Monitoring(Arrays.toString(data), System.currentTimeMillis());
        monitoringViewModel.insert(monitoring, result -> {
           if (result != null && result > 0) {
               Toast.makeText(getContext(), R.string.insert_success, Toast.LENGTH_SHORT).show();
           }
           else {
               Toast.makeText(getContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
           }
        });
        if(scatterChart == null) return;
        ESPReceiveData espReceiveData = PacketParser.parseESPData(data, cnt, data.length);
        ScatterData scatterData= scatterChart.getData();
        if (scatterData == null) {
            scatterData = new ScatterData();
            for (int i = 0; i < cnt; i ++) {
                ScatterDataSet set = new ScatterDataSet(new ArrayList<>(), R.string.channel + " " + String.valueOf(i + 1));
                set.setDrawValues(false);
                set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
                set.setColor(colors[i % colors.length]);
                set.addEntry(new Entry(0, espReceiveData.getValues().get(i), 0));
                scatterData.addDataSet(set);
            }
            scatterChart.setData(scatterData);
        }
        else {
            for (int i = 0; i < cnt; i ++) {
                IScatterDataSet set = scatterData.getDataSetByIndex(i);
                if (set instanceof ScatterDataSet) {
                    ScatterDataSet scatterDataSet = (ScatterDataSet) set;
                    float prevX = 0;
                    prevX = scatterDataSet.getEntryForIndex(scatterDataSet.getEntryCount() - 1).getX();
                    scatterDataSet.addEntry(new Entry(prevX + 10, espReceiveData.getValues().get(i), null));
                    
                }
            }
        }
        scatterData.notifyDataChanged();
        scatterChart.notifyDataSetChanged();
        scatterChart.invalidate();
    }
}
