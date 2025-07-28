package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.Constants.COLORS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prtech.spiapp.R;
import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.MonitoringDao;
import com.prtech.spiapp.db.entity.Monitoring;
import com.prtech.spiapp.db.entity.RangeDTO;
import com.prtech.spiapp.db.entity.Visualization;
import com.prtech.spiapp.db.entity.VisualizationRange;
import com.prtech.spiapp.db.viewmodel.MonitoringViewModel;
import com.prtech.spiapp.db.viewmodel.SensorActuatorViewModel;
import com.prtech.spiapp.db.viewmodel.VisualizationViewModel;
import com.prtech.spiapp.utils.CommonUtils;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.communications.PacketParser;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayBack extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<Long, Object> accordionContentMap = new HashMap<>();

    private MonitoringViewModel monitoringViewModel;
    private VisualizationViewModel visualizationViewModel;
    private MonitoringDao monitoringDao;
    private Visualization currentVisualization;
    private SensorActuatorViewModel sensorActuatorViewModel;
    private List<RangeDTO> rangeDTOs;
    private Map<Long, Object> chartsMap = new HashMap<>();
    private Boolean isPlaying = true;
    private Long lastTimestamp;
    private Map<Long, Float> currentWindowStartMap = new HashMap<>();
    private final Float windowSize = 500F;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public PlayBack() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reproduction, container, false);
        AppDatabase db = AppDatabase.getInstance(requireContext());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[9]);

        monitoringDao = db.monitoringDao();

        accordionContainer = view.findViewById(R.id.playback_accordion_container);

        monitoringViewModel = new ViewModelProvider(requireActivity()).get(MonitoringViewModel.class);
        monitoringViewModel.getAllMonitorings(results -> {
            Log.d("Success", "Loaded all playback data");
        });
        visualizationViewModel = new ViewModelProvider(requireActivity()).get(VisualizationViewModel.class);
        visualizationViewModel.getActivatedVisualization(result -> {
            if (result == null) {
                currentVisualization = new Visualization("", 0, 0, 0, new ArrayList<>(), 0, "", 0, System.currentTimeMillis());
                rangeDTOs = new ArrayList<>();
                Toast.makeText(requireContext(), "There is no completed Visualization setting.", Toast.LENGTH_SHORT).show();
            } else {
                currentVisualization = result;
                displayAccordion(currentVisualization.getRanges());
                visualizationViewModel.getCorrespondingSAs(currentVisualization.getId(), results -> {
                    rangeDTOs = results;
                    startPlayback();
                });
            }

        });

        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);

        return view;
    }


    private void addAccordionSection(VisualizationRange visualizationRange) {

        sensorActuatorViewModel.getById(visualizationRange.getSensorActuatorId(), result -> {
            // Header
            TextView header = new TextView(requireContext());
            header.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            header.setText(result.getVariableName());
            header.setTextSize(18);
            header.setTypeface(null, Typeface.BOLD);
            header.setPadding(24, 24, 24, 24);
            header.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bs_primary));

            header.setTextColor(Color.WHITE);

            // Content container
            LinearLayout contentLayout = new LinearLayout(requireContext());
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setVisibility(View.GONE);
            contentLayout.setPadding(24, 24, 24, 24);
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

//        scatterChart.invalidate(); // refresh chart

            // Content TextView

            ScatterChart scatterChart = new ScatterChart(requireContext());
            scatterChart.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            scatterChart.getDescription().setEnabled(false);
            scatterChart.getAxisRight().setEnabled(false);
            scatterChart.getXAxis().setAxisMinimum(0f);
            scatterChart.getXAxis().setAxisMaximum(500f);
            scatterChart.setVisibleXRangeMaximum(500f);
            scatterChart.setTouchEnabled(true);
            scatterChart.setDragEnabled(true);
            scatterChart.setScaleEnabled(true);
            scatterChart.setPinchZoom(true);
            scatterChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//            scatterChart.getXAxis().setValueFormatter(new ValueFormatter() {
//                private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//
//                @Override
//                public String getFormattedValue(float value) {
//                    long millis = (long) value;
//                    return sdf.format(new Date(millis));
//                }
//            });
            scatterChart.getAxisLeft().setAxisMinimum(Float.parseFloat(Constants.Y_AXIS_RANGES[visualizationRange.getyAxisRange()][1]));
            scatterChart.getAxisLeft().setAxisMaximum(Float.parseFloat(Constants.Y_AXIS_RANGES[visualizationRange.getyAxisRange()][2]));
            scatterChart.getXAxis().setDrawGridLines(false);
            scatterChart.getLegend().setForm(Legend.LegendForm.SQUARE);
            chartsMap.put(visualizationRange.getSensorActuatorId(), scatterChart);
            currentWindowStartMap.put(visualizationRange.getSensorActuatorId(), 0F);
            contentLayout.addView(scatterChart);
            contentLayout.setVisibility(View.VISIBLE);
            // Save reference for updates
            accordionContentMap.put(result.getId(), contentLayout);

            // Toggle logic
            header.setOnClickListener(v -> {
                contentLayout.setVisibility(
                        contentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                );
            });

            accordionContainer.addView(header);
            accordionContainer.addView(contentLayout);

        });
    }

    public void displayAccordion(List<VisualizationRange> data) {
        for (VisualizationRange vr: data) {
            addAccordionSection(vr);
        }
    }

    private void startPlayback() {
        isPlaying = true;
        new Thread(() -> {
            long startTime = monitoringDao.getFirstTimestamp();
            lastTimestamp = startTime;
                List<Monitoring> batch = monitoringDao.getRecordsAfter(lastTimestamp, currentVisualization.getBufferSize() * currentVisualization.getBufferSize());
                if (batch.isEmpty()) return;
                for(Monitoring m: batch) {
                    if(!isPlaying) break;
                    long waitTime = m.getCreated_at() - lastTimestamp;
                    lastTimestamp = m.getCreated_at();
                    Map<Long, Object> parsed = PacketParser.parse(rangeDTOs, CommonUtils.fromStringToByteArray(m.getData()));
                    for (RangeDTO rangeDTO: rangeDTOs) {
                        List<Object> values = (List<Object>) parsed.get(rangeDTO.getSensorActuatorId());
                        ScatterChart scatterChart = (ScatterChart) chartsMap.get(rangeDTO.getSensorActuatorId());
                        Runnable updater = new Runnable() {
                            @Override
                            public void run() {
                                updateChartWithData(scatterChart, values, rangeDTO.getSensorActuatorId());
                            }
                        };
                        handler.post(updater);
                    }
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        }).start();
    }

    private void updateChartWithData(ScatterChart scatterChart, List<Object> data, Long saId) {
        float maxX = 0;
        if(scatterChart == null || data.isEmpty()) return;
        ScatterData scatterData= scatterChart.getData();
        int cnt = data.size();
        if (scatterData == null) {
            scatterData = new ScatterData();
            for (int i = 0; i < cnt; i ++) {
                ScatterDataSet set = new ScatterDataSet(new ArrayList<>(), requireContext().getString(R.string.channel) + " " + String.valueOf(i + 1));
                set.setDrawValues(false);
                set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
                set.setColor(COLORS[i % COLORS.length]);
                Object obj = data.get(i);
                if (obj instanceof Integer) {
                    Integer value = (Integer) obj;
                    set.addEntry(new Entry(0, value));
                }
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
                    if (scatterDataSet.getEntryCount() > 0) {
                        Entry last = scatterDataSet.getEntryForIndex(scatterDataSet.getEntryCount() - 1);
                        prevX = last.getX();
                    }
                    Object obj = data.get(i);
                    if (obj instanceof Integer) {
                        Integer value = (Integer) obj;
                        float newX = prevX + 10;
                        scatterDataSet.addEntry(new Entry(newX, value));
                        if(newX > maxX) maxX = newX;
                        Log.d("Chart", "MAXX is " + String.valueOf(maxX));
                    }
                }
            }
        }
        Log.d("Chart", "Maximum values is " + String.valueOf(scatterChart.getHighestVisibleX()));
        scatterData.notifyDataChanged();
        scatterChart.notifyDataSetChanged();
        float start = currentWindowStartMap.get(saId);
        if (maxX > start + windowSize) {
            currentWindowStartMap.put(saId, start + windowSize);
            scatterChart.getXAxis().setAxisMinimum(start + windowSize);
            scatterChart.getXAxis().setAxisMaximum(start + windowSize * 2);
        }
        scatterChart.invalidate();
//        scatterChart.postDelayed(() -> updateChartWithData(scatterChart, data, saId), 10000);
//                    Toast.makeText(requireContext(), "MaxX value is" + String.valueOf(maxX), Toast.LENGTH_SHORT).show();
//                    scatterChart.moveViewToAnimated(maxX, 0f, YAxis.AxisDependency.LEFT, 300);

    }


}
