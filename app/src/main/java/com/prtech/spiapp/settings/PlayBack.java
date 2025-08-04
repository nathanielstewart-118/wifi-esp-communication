package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.fromStringToByteArray;
import static com.prtech.spiapp.utils.Constants.COLORS;
import static com.prtech.spiapp.utils.UIUtils.createHeaderTextView;
import static com.prtech.spiapp.utils.UIUtils.initSpinnerWithSuggestionList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.prtech.spiapp.R;
import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.MonitoringDao;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Monitoring;
import com.prtech.spiapp.db.entity.RangeDTO;
import com.prtech.spiapp.db.entity.Visualization;
import com.prtech.spiapp.db.entity.VisualizationRange;
import com.prtech.spiapp.db.viewmodel.MonitoringViewModel;
import com.prtech.spiapp.db.viewmodel.ESPPacketViewModel;
import com.prtech.spiapp.db.viewmodel.VisualizationViewModel;
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PlayBack extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<Long, Object> accordionContentMap = new HashMap<>();
    private final Map<Long, Object> chartsMap = new HashMap<>();
    private final Map<Long, Object> tablesMap = new HashMap<>();
    private final Map<Long, Float> currentWindowStartMap = new HashMap<>();
    private final Map<Long, Integer> espVisualizationMap = new HashMap<>();
    private Button startBtn;
    private Button stopBtn;
    private Button forward1sBtn;
    private Button backward1sBtn;
    private Button forward10sBtn;
    private Button backward10sBtn;
    private Button nextCommandBtn;
    private Button previousCommandBtn;
    private Button logBtn;

    private MonitoringViewModel monitoringViewModel;
    private VisualizationViewModel visualizationViewModel;
    private MonitoringDao monitoringDao;
    private ESPPacketViewModel espPacketViewModel;

    private Visualization currentVisualization;
    private List<RangeDTO> rangeDTOs;
    private Boolean isPlaying = true;
    private Long lastTimestamp;
    private final Float windowSize = 10000F;
    private List<ESPPacket> currentESPPackets = new ArrayList<>();
    private List<Visualization> currentVisualizations = new ArrayList<>();
    private List<Command> allCommands = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Long recordsCnt = 0L;
    private Integer pageSize = 10;
    private Long pageNo = 0L;
    private Integer tableRowsCnt = 10;
    private boolean isProcessingPage = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private volatile Boolean forward1S = false;
    private volatile Boolean forward10S = false;
    private volatile Boolean backward1S = false;
    private volatile Boolean backward10S = false;
    private volatile Boolean runningNextCommand = false;
    private volatile Boolean runningPrevCommand = false;
    private volatile Long forwardClickTime = 0L;
    private volatile Long backwardClickTime = 0L;


    public PlayBack() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        AppDatabase db = AppDatabase.getInstance(requireContext());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[6]);

        monitoringDao = db.monitoringDao();

        accordionContainer = view.findViewById(R.id.playback_accordion_container);

        startBtn = view.findViewById(R.id.playback_start_btn);
        startBtn.setOnClickListener(v -> {
            try {
                handleClickStartBtn();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        stopBtn = view.findViewById(R.id.playback_stop_btn);
        stopBtn.setOnClickListener(v -> handleClickStopBtn());

        forward1sBtn = view.findViewById(R.id.playback_time_forward_1s_btn);
        forward1sBtn.setOnClickListener(v -> handleClickForward1SBtn());

        forward10sBtn = view.findViewById(R.id.playback_time_forward_10s_btn);
        forward10sBtn.setOnClickListener(v -> handleClickForward10SBtn());

        backward1sBtn = view.findViewById(R.id.playback_time_backward_1s_btn);
        backward1sBtn.setOnClickListener(v -> handleClickBackward1SBtn());

        backward10sBtn = view.findViewById(R.id.playback_time_backward_10s_btn);
        backward10sBtn.setOnClickListener(v -> handleClickBackward10SBtn());

        nextCommandBtn = view.findViewById(R.id.playback_next_command_btn);
        nextCommandBtn.setOnClickListener(v -> handleClickNextCommandBtn());

        previousCommandBtn = view.findViewById(R.id.playback_previous_command_btn);
        previousCommandBtn.setOnClickListener(v -> handleClickPreviousCommandBtn());

        monitoringViewModel = new ViewModelProvider(requireActivity()).get(MonitoringViewModel.class);
        monitoringViewModel.getCount(result -> {
            if (result != null) recordsCnt = result;
        });
        visualizationViewModel = new ViewModelProvider(requireActivity()).get(VisualizationViewModel.class);

        espPacketViewModel = new ViewModelProvider(requireActivity()).get(ESPPacketViewModel.class);

        return view;
    }


    public void displayAccordion(List<Long> espIds) {

        accordionContainer.removeAllViews();
        accordionContentMap.clear();
        chartsMap.clear();
        tablesMap.clear();
        for (Long espId: espIds) {
            addAccordionSection(espId);
        }
    }

    private void addAccordionSection(long espId) {
        List<VisualizationRange> filtered = currentVisualizations.get((Integer) espVisualizationMap.get(espId)).getRanges()
                .stream()
                .filter(one -> Objects.equals(one.getEspPacketId(), espId))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) return;
        VisualizationRange visualizationRange = filtered.get(0);
        List<ESPPacket> filteredESPs = currentESPPackets
                .stream()
                .filter(one -> Objects.equals(one.getId(), espId))
                .collect(Collectors.toList());
        if(filteredESPs.isEmpty()) return;
        ESPPacket result = filteredESPs.get(0);
        // Header
        if (result == null) return;
        LinearLayout headerLayout = new LinearLayout(requireContext());
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bs_primary));
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setTag(visualizationRange.getEspPacketId());

        TextView header = new TextView(requireContext());
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));


        header.setText(result.getVariableName());
        header.setTextSize(18);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(24, 24, 24, 24);
        header.setTextColor(Color.WHITE);

        View spaceView = new View(requireContext());
        spaceView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                0,
                1
        ));

        Spinner visualizationSpinner = new Spinner(requireContext());
        visualizationSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        List<String> candidates = currentVisualizations
                .stream()
                .map(Visualization::getTitle)
                .collect(Collectors.toList());

        int color = ContextCompat.getColor(requireContext(), R.color.white); // Replace with your color
        ViewCompat.setBackgroundTintList(visualizationSpinner, ColorStateList.valueOf(color));
        initSpinnerWithSuggestionList(visualizationSpinner, candidates, requireContext(), R.layout.white_spinner_item);
        int visualizationIndex = (Integer) espVisualizationMap.get(espId);
        visualizationSpinner.setSelection(visualizationIndex);
        visualizationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                espVisualizationMap.put(espId, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        if(visualizationRange.getVisualizationType() == 0) {
            ScatterChart scatterChart = displayChart(visualizationRange, requireContext());
            chartsMap.put(visualizationRange.getEspPacketId(), scatterChart);
            currentWindowStartMap.put(visualizationRange.getEspPacketId(), 0F);
            contentLayout.addView(scatterChart);
        }
        else if(visualizationRange.getVisualizationType() == 1) {
            TableLayout tableLayout = displayTable(visualizationRange, requireContext());
            tablesMap.put(visualizationRange.getEspPacketId(), tableLayout);
            contentLayout.addView(tableLayout);
        }
        contentLayout.setVisibility(View.VISIBLE);
        // Save reference for updates
        accordionContentMap.put(result.getId(), contentLayout);
        contentLayout.setTag(visualizationRange.getVisualizationType());
        // Toggle logic
        header.setOnClickListener(v -> {
            contentLayout.setVisibility(
                    contentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });
        headerLayout.addView(header);
        headerLayout.addView(spaceView);
        headerLayout.addView(visualizationSpinner);
        accordionContainer.addView(headerLayout);
        accordionContainer.addView(contentLayout);

    }

    public void processOneMonitoringRecord(List<Monitoring> monitorings, long[] prevTime) {
        if (monitorings == null || monitorings.isEmpty()) return;
        AtomicInteger index = new AtomicInteger();
        long[] totalSleep = new long[]{0};
        Runnable[] taskHolder = new Runnable[1]; // use an array to hold a reference to the Runnable
        String title = monitorings.get(0).getEspPacketTitle();

        taskHolder[0] = () -> {
            if (index.get() >= monitorings.size()) {
                Log.d("Info", "Finished processing current page");
                isProcessingPage = false;
                handler.postDelayed(runnable, 0); // move to next page after this one finishes
                return;
            }

            Monitoring monitoring = monitorings.get(index.get());
            if ((forward1S || forward10S) && prevTime[0] != 0) {
                // Skip this record
                int delayMS = forward1S ? 1000 : 10000;
                if (forwardClickTime == 0) forwardClickTime = monitoring.getCreatedAt();
                if (monitoring.getCreatedAt() < forwardClickTime + delayMS) {
                    index.getAndIncrement();
                    handler.post(taskHolder[0]);  // Post next task immediately
                    return;
                }
                else {
                    forward1S = false;
                    forward10S = false;
                }
            }
            if (currentESPPackets.isEmpty() || !Objects.equals(currentESPPackets.get(0).getTitle(), monitoring.getEspPacketTitle())) {
                espPacketViewModel.getByTitle(title, espPackets -> {
                    currentESPPackets.clear();
                    currentESPPackets.addAll(espPackets);

                    List<Long> espIds = espPackets.stream()
                            .map(ESPPacket::getId)
                            .collect(Collectors.toList());

                    if (espVisualizationMap.isEmpty()) {
                        for (Long espId : espIds) {
                            espVisualizationMap.put(espId, 0);
                        }
                    }

                    visualizationViewModel.getByESPPacketTitle(title, vResults -> {
                        currentVisualizations.clear();
                        currentVisualizations.addAll(vResults);

                        Map<Long, Long> espVisualizationIdMap = new HashMap<>();
                        for (Map.Entry<Long, Integer> entry : espVisualizationMap.entrySet()) {
                            Long espId = entry.getKey();
                            Integer idx = entry.getValue();
                            Visualization v = currentVisualizations.get(idx);
                            espVisualizationIdMap.put(espId, v.getId());
                        }

                        long sleep = 0;
                        if (prevTime[0] != 0) {
                            sleep = monitoring.getCreatedAt() - prevTime[0];
                        }
                        totalSleep[0] += sleep;
                        prevTime[0] = monitoring.getCreatedAt();

                        displayAccordion(espIds);
                        updateViews(fromStringToByteArray(monitoring.getData()), sleep);

                        // post next task

                        Log.d("Info in if ", "Runnable sleep: " + String.valueOf(sleep) + ", createdAt is " + String.valueOf(monitoring.getCreatedAt()));
                        index.getAndIncrement();
                        handler.postDelayed(taskHolder[0], sleep);
                    });
                });
            } else {

                // post next task
                long sleep = 0;
                if (prevTime[0] != 0) {
                    sleep = monitoring.getCreatedAt() - prevTime[0];
                    if(forwardClickTime != 0) {
                        sleep = monitoring.getCreatedAt() - forwardClickTime;
                        forwardClickTime = 0L;
                    }
                }
                totalSleep[0] += sleep;
                updateViews(fromStringToByteArray(monitoring.getData()), sleep);
                Log.d("Info in else", "Runnable sleep: " + String.valueOf(sleep) + ",Total Sleep is " + totalSleep[0] + "createdAt is " + String.valueOf(monitoring.getCreatedAt()));
                prevTime[0] = monitoring.getCreatedAt();
                index.getAndIncrement();
                handler.postDelayed(taskHolder[0], sleep);
            }
        };

        handler.post(taskHolder[0]);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isPlaying) return;

            monitoringViewModel.getMonitoringsByOffset(pageNo, pageSize, results -> {
                long[] prevTime = new long[]{0};
                processOneMonitoringRecord(results, prevTime);

                if(results.isEmpty() || results.size() < pageSize) {
                    isPlaying = false;
                    Log.d("Info", "This is callback function of ");
                }
                pageNo ++;
//                handler.postDelayed(this, 0); // call again after 5s
            });
        }
    };

    private void startPlayback() throws InterruptedException {
        isPlaying = true;
        pageNo = 0L;
        currentESPPackets.clear();
        handler.post(runnable);
    }

    private void updateViews(byte[] data, long sleep) {

        if(currentESPPackets == null || currentESPPackets.isEmpty()) return;
        Map<Long, Object> parsed = PacketParser.parse(currentESPPackets, data);
        for (ESPPacket espPacket: currentESPPackets) {
            List<Object> values = (List<Object>) parsed.get(espPacket.getId());
            ScatterChart scatterChart = (ScatterChart) chartsMap.get(espPacket.getId());
            TableLayout targetTable = (TableLayout) tablesMap.get(espPacket.getId());
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    LinearLayout linearLayout = (LinearLayout)accordionContentMap.get(espPacket.getId());
                    if (linearLayout == null) return;
                    int vType = (Integer) linearLayout.getTag();
                    if (vType == 0) updateChartWithData(scatterChart, values, espPacket.getId(), sleep);
                    else if (vType == 1) updateTableWithData(targetTable, values, espPacket.getId(), requireContext());
                }
            };

            updater.run();

        }
    }

    private void updateChartWithData(ScatterChart scatterChart, List<Object> data, Long saId, long sleep) {
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
                        float newX = prevX + sleep;
                        scatterDataSet.addEntry(new Entry(newX, value));
                        if(newX > maxX) maxX = newX;
                    }
                }
            }
        }
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

    public void handleClickStartBtn() throws InterruptedException {
        isPlaying = true;
        startPlayback();
    }

    public void handleClickStopBtn() {
        isPlaying = false;
    }

    public void handleClickForward1SBtn() {
        if(!isPlaying || forward10S || backward1S || backward10S || runningNextCommand || runningPrevCommand ) return;
        forward1S = true;
    }

    public void handleClickForward10SBtn() {
        if(!isPlaying || forward1S || backward1S || backward10S || runningNextCommand || runningPrevCommand) return;
        forward10S = true;
    }

    public void handleClickBackward1SBtn() {
        if(!isPlaying || forward1S || forward10S || backward10S || runningNextCommand || runningPrevCommand) return;
        backward1S = true;
    }

    public void handleClickBackward10SBtn() {
        if(!isPlaying || forward1S || forward10S || backward1S || runningNextCommand || runningPrevCommand) return;
        backward10S = true;
    }

    public void handleClickNextCommandBtn() {
        if(!isPlaying || forward1S || forward10S || backward1S || backward10S || runningPrevCommand) return;
        runningNextCommand = true;

    }

    public void handleClickPreviousCommandBtn() {
        if(!isPlaying || forward1S || forward10S || backward1S || backward10S || runningNextCommand) return;
        runningPrevCommand = true;
    }

    public ScatterChart displayChart(VisualizationRange visualizationRange, Context context) {
        ScatterChart scatterChart = new ScatterChart(requireContext());
        scatterChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        scatterChart.getDescription().setEnabled(false);
        scatterChart.getAxisRight().setEnabled(false);
        scatterChart.getXAxis().setAxisMinimum(0f);
        scatterChart.getXAxis().setAxisMaximum(windowSize);
        scatterChart.setVisibleXRangeMaximum(windowSize);
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
        return scatterChart;
    }

    public TableLayout displayTable(VisualizationRange range, Context context) {

        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        tableLayout.setPadding(16, 16, 16, 16);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setGravity(Gravity.CENTER);

        List<ESPPacket> filteredPackets = currentESPPackets
                .stream()
                .filter(one -> Objects.equals(one.getId(), range.getEspPacketId()))
                .collect(Collectors.toList());
        if (filteredPackets.isEmpty()) return tableLayout;
        ESPPacket espPacket = filteredPackets.get(0);
        int nChannels = espPacket.getNumberOfChannels();

        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(ContextCompat.getColor(context, R.color.bs_primary));

        TextView orderHeaderView = createHeaderTextView("Order", requireContext());
        TextView channelHeaderView = createHeaderTextView("Channel", requireContext());

        TableRow.LayoutParams spanParams = new TableRow.LayoutParams();
        spanParams.span = nChannels;
        channelHeaderView.setLayoutParams(spanParams);

        headerRow.addView(orderHeaderView);
        headerRow.addView(channelHeaderView);


        TableRow channelRow = new TableRow(requireContext());
        channelRow.setBackgroundColor(ContextCompat.getColor(context, R.color.bs_primary));
        TextView orderChannelView = new TextView(requireContext());
        channelRow.addView(orderChannelView);
        for (int i = 0; i < nChannels; i ++) {
            TextView channelView = new TextView(requireContext());
            channelView.setTextColor(ContextCompat.getColor(context, R.color.white));
            channelView.setText(String.valueOf(i + 1));
            channelRow.addView(channelView);
        }

        tableLayout.addView(headerRow);
        tableLayout.addView(channelRow);
        return tableLayout;
    }

    private void updateTableWithData(TableLayout table, List<Object> values, Long espId, Context context) {
        if(table == null || values == null || values.isEmpty()) return;
        if(table.getChildCount() - 2 > tableRowsCnt - 1) table.removeViews(2, table.getChildCount() - 2);
        TableRow tableRow = new TableRow(context);

        TextView textView = new TextView(requireContext());
        textView.setText(String.valueOf(pageNo * pageSize + table.getChildCount() - 2));
        textView.setGravity(Gravity.CENTER);
        tableRow.addView(textView);

        int cnt = values.size();
        for (int i = 0; i < cnt; i ++) {
            TextView valueView = new TextView(context);
            Integer value = (int) values.get(i);
            if (value != null) {
                valueView.setText(String.valueOf(value));
            }
            tableRow.addView(valueView);
        }

        table.addView(tableRow);

    }

}
