package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.Constants.COLORS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.prtech.spiapp.R;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Monitoring;
import com.prtech.spiapp.db.entity.RangeDTO;
import com.prtech.spiapp.db.entity.Visualization;
import com.prtech.spiapp.db.entity.VisualizationRange;
import com.prtech.spiapp.db.viewmodel.MonitoringViewModel;
import com.prtech.spiapp.db.viewmodel.SensorActuatorViewModel;
import com.prtech.spiapp.db.viewmodel.TCPUDPReceiveViewModel;
import com.prtech.spiapp.db.viewmodel.VisualizationViewModel;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.LogHelper;
import com.prtech.spiapp.utils.communications.PacketParser;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;


public class MonitoringSetting extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<Long, Object> accordionContentMap = new HashMap<>();
    private final Map<Long, Object> chartsMap = new HashMap<>();
    private final Gson gson = new Gson();

    private SensorActuatorViewModel sensorActuatorViewModel;
    private TCPUDPReceiveViewModel receiveViewModel;
    private MonitoringViewModel monitoringViewModel;
    private VisualizationViewModel visualizationViewModel;
    private Visualization currentVisualization;
    private List<RangeDTO> rangeDTOs;
    private List<ESPPacket> espPackets = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Boolean bDrawing = true;

    private Map<Long, Float> currentWindowStartMap = new HashMap<>();
    private final float windowSize = 500f;


    public MonitoringSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[8]);
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
            List<ESPPacket> actuators = (List<ESPPacket>) data;
            espPackets.clear();
            espPackets.addAll(actuators);
        });


        monitoringViewModel = new ViewModelProvider(requireActivity()).get(MonitoringViewModel.class);

        visualizationViewModel = new ViewModelProvider(requireActivity()).get(VisualizationViewModel.class);

        // Test data
        visualizationViewModel.getActivatedVisualization(result -> {
            if (result == null) {
                currentVisualization = new Visualization("", 0, 0, 0, new ArrayList<>(), 0, "", 0, System.currentTimeMillis());
                rangeDTOs = new ArrayList<>();
                Toast.makeText(requireContext(), "There is no completed Visualization setting.", Toast.LENGTH_SHORT).show();
            }
            else {
                currentVisualization = result;
                displayAccordion(currentVisualization.getRanges());
//                ByteBuffer buffer = ByteBuffer.allocate(4 * Constants.MAX_VARIABLE_NUMBER_IN_PACKET);
                visualizationViewModel.getCorrespondingSAs(currentVisualization.getId(), results -> {
                    rangeDTOs = results;
                    executorService.execute(() -> {
                        while(bDrawing) {
                            ByteBuffer buffer = ByteBuffer.allocate(4 * Constants.MAX_VARIABLE_NUMBER_IN_PACKET);
                            for (RangeDTO sa: results) {
                                int nChannels = sa.getNumberOfChannels();
                                double low = (double) sa.getLowerLimit();
                                double high = (double) sa.getUpperLimit();
                                for (int i = 0; i < nChannels; i ++) {
                                    switch (sa.getDataType().toLowerCase()) {
                                        case "uint8": {
                                            int value = (int) randomBetween(low, high);
                                            buffer.put((byte) (value & 0xFF));
                                            break;
                                        }

                                        case "int8": {
                                            byte value = (byte) randomBetween(low, high);
                                            buffer.put(value);
                                            break;
                                        }

                                        case "uint16": {
                                            int value = (int) randomBetween(low, high);
                                            buffer.putShort((short) (value & 0xFFFF));
                                            break;
                                        }

                                        case "int16": {
                                            short value = (short) randomBetween(low, high);
                                            buffer.putShort(value);
                                            break;
                                        }

                                        case "uint24": {
                                            int value = (int) randomBetween(low, high);
                                            buffer.put(new byte[] {
                                                    (byte) ((value >> 16) & 0xFF),
                                                    (byte) ((value >> 8) & 0xFF),
                                                    (byte) (value & 0xFF)
                                            });
                                            break;
                                        }

                                        case "int24": {
                                            int value = (int) randomBetween(low, high);
                                            buffer.put(new byte[] {
                                                    (byte) ((value >> 16) & 0xFF),
                                                    (byte) ((value >> 8) & 0xFF),
                                                    (byte) (value & 0xFF)
                                            });
                                            break;
                                        }

                                        case "uint32": {
                                            long value = (long) randomBetween(low, high);
                                            buffer = ByteBuffer.allocate(4);
                                            buffer.putInt((int) (value & 0xFFFFFFFFL));
                                            break;
                                        }

                                        case "int32": {
                                            int value = (int) randomBetween(low, high);
                                            buffer.putInt(value);
                                            break;
                                        }

                                        case "float": {
                                            float value = (float) randomBetween(low, high);
                                            buffer.putFloat(value);
                                            break;
                                        }

                                        case "double": {
                                            double value = randomBetween(low, high);
                                            buffer.putDouble(value);
                                            break;
                                        }
                                        default:
                                            throw new IllegalArgumentException("Unsupported type: " + sa.getDataType());
                                    }
                                }
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            updateViews(buffer.array(), buffer.array().length);
                        }

                    });

                });
            }

        });

        return view;
    }

    @Override
    public void onStop() {
        bDrawing = false;
        super.onStop();
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

    public void updateViews(byte[] data, int cnt) {
        Monitoring monitoring = new Monitoring(Arrays.toString(data), currentVisualization.getId(), System.currentTimeMillis());

        monitoringViewModel.insert(monitoring, result -> {
           if (result != null && result > 0) {
//               Toast.makeText(requireContext(), R.string.insert_success, Toast.LENGTH_SHORT).show();
           }
           else {
//               Toast.makeText(requireContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
           }
        });
        Map<Long, Object> parsed = PacketParser.parse(rangeDTOs, data);
        final int[] updateCnt = {0};
        for (RangeDTO rangeDTO: rangeDTOs) {
            List<Object> values = (List<Object>) parsed.get(rangeDTO.getSensorActuatorId());
            ScatterChart scatterChart = (ScatterChart) chartsMap.get(rangeDTO.getSensorActuatorId());
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateChartWithData(scatterChart, values, rangeDTO.getSensorActuatorId());
                }
            };

            scatterChart.post(updater);

        }
    }

    private void updateChartWithData(ScatterChart scatterChart, List<Object> data, Long saId) {
            float maxX = 0;
            if(scatterChart == null) return;
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
            scatterChart.postDelayed(() -> updateChartWithData(scatterChart, data, saId), 10000);
//                    Toast.makeText(requireContext(), "MaxX value is" + String.valueOf(maxX), Toast.LENGTH_SHORT).show();
//                    scatterChart.moveViewToAnimated(maxX, 0f, YAxis.AxisDependency.LEFT, 300);

    }

    private static double randomBetween(double low, double high) {
        Random random = new Random();
        return low + (high - low) * random.nextDouble();
    }

}
