package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.fromStringToByteArray;
import static com.prtech.spiapp.utils.CommonUtils.long2DateTimeString;
import static com.prtech.spiapp.utils.Constants.COLORS;
import static com.prtech.spiapp.utils.UIUtils.createHeaderTextView;
import static com.prtech.spiapp.utils.UIUtils.initSpinnerWithSuggestionList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;
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
    private MaterialButton startBtn;
    private MaterialButton stopBtn;
    private MaterialButton forward1sBtn;
    private MaterialButton backward1sBtn;
    private MaterialButton forward10sBtn;
    private MaterialButton backward10sBtn;
    private MaterialButton nextCommandBtn;
    private MaterialButton previousCommandBtn;
    private MaterialButton logMsgBtn;
    private MaterialSwitch logSwitch;
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
    private List<Monitoring> currentMonitorings = new ArrayList<>();
    private final long[] currentTime = new long[]{-1};
    private List<Command> allCommands = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Long recordsCnt = 0L;
    private Integer pageSize = 10;
    private Long pageNo = 0L;
    private Integer tableRowsCnt = 10;
    private boolean isProcessingPage = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Runnable playbackRunnable;
    private volatile Boolean forward1S = false;
    private volatile Boolean forward10S = false;
    private volatile Boolean backward1S = false;
    private volatile Boolean backward10S = false;
    private volatile Boolean runningNextCommand = false;
    private volatile Boolean runningPrevCommand = false;
    private volatile Long forwardClickTime = 0L;
    private volatile Long backwardClickTime = 0L;
    private List<String[]> logs = new ArrayList<>();

    private Long timeOffset = 0L;

    private Long startTime = 0L;
    private Map<Long, List<Pair<Integer, Double>>> currentStatistics = new HashMap<>();




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

        logMsgBtn = view.findViewById(R.id.monitoring_log_message_btn);
        logMsgBtn.setOnClickListener(v -> handleClickLogMsgBtn());


        logSwitch = view.findViewById(R.id.playback_log_switch);
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleLogSwitchCheckedChange(isChecked);
            }
        });
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
        int nChannels = result.getNumberOfChannels();
        List<Pair<Integer, Double>> statics = new ArrayList<>();
        for (int i = 0; i < nChannels; i ++) {
            Pair pair = new Pair<>(0, 0D);
            statics.add(pair);
        }
        currentStatistics.put(espId, statics);

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
                500 + 100 * (result.getNumberOfChannels() + 1) + 100
        ));

        // Content TextView
        if(visualizationRange.getVisualizationType() == 0) {
            ScatterChart scatterChart = displayChart(visualizationRange, requireContext());
            chartsMap.put(visualizationRange.getEspPacketId(), scatterChart);
            currentWindowStartMap.put(visualizationRange.getEspPacketId(), 0F);
            LinearLayout chartLayout = new LinearLayout(requireContext());
            chartLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            chartLayout.addView(scatterChart);

//            contentLayout.addView(scatterChart);
            contentLayout.addView(chartLayout);
        }
        else if(visualizationRange.getVisualizationType() == 1) {
            TableLayout tableLayout = displayTable(visualizationRange, requireContext());
            tablesMap.put(visualizationRange.getEspPacketId(), tableLayout);
            contentLayout.addView(tableLayout);
        }
        LinearLayout channelTableLayout = displayChannelTable(result, requireContext());
        contentLayout.addView(channelTableLayout);

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

    public void startPlayback() {
        logs.add(new String[]{"User clicks on the start Button", long2DateTimeString(System.currentTimeMillis())});
        isPlaying = true;
        pageNo = 0L;
        currentMonitorings.clear();
        currentESPPackets.clear();
        currentVisualizations.clear();
        currentTime[0] = -1;
        fetchAndStart();
    }

    public void stopPlayback() {
        logs.add(new String[]{"User clicks on the Stop Button", long2DateTimeString(System.currentTimeMillis())});
        isPlaying = false;
//        handler.removeCallbacks(playbackRunnable);
    }

    private void fetchAndStart() {
        monitoringViewModel.getMonitoringsByOffset(pageNo, pageSize, results -> {
            if (results.isEmpty()) {
                isPlaying = false;
                return;
            }
            logs.add(new String[]{"Data fetched successfully from database", long2DateTimeString(System.currentTimeMillis())});

            currentMonitorings.addAll(results);
            startNextFrame();
        });
    }

    private void startNextFrame() {
        playbackRunnable = () -> {
            if (!isPlaying) return;

            Monitoring next = getNextMonitoring();
            if (next == null) {
                pageNo++;
                fetchAndStart();
                return;
            }

            long[] sleep = {0};
            long[] offset = {0};
            if (currentTime[0] != -1) {
                sleep[0] = next.getCreatedAt() - currentTime[0];
                offset[0] = next.getCreatedAt() - currentTime[0];
            }
            else if (pageNo == 0) {
                startTime = next.getCreatedAt();
            }
            if (timeOffset != 0) {
                if(timeOffset < 0) {
                    long x = next.getCreatedAt() - startTime;
                    removeScatterEntriesUnderX(chartsMap, x);
//                    removeCurrentMonitoringsByCreatedAt(currentTime[0]);
                }
                timeOffset = 0L;
                sleep[0] = 0L;
            }
            currentTime[0] = next.getCreatedAt();

            updateDataIfNeeded(next, () -> {
                updateViews(fromStringToByteArray(next.getData()), offset[0]);
                handler.postDelayed(playbackRunnable, sleep[0]);
            });
        };

        handler.post(playbackRunnable);
    }

    private Monitoring getNextMonitoring() {
        currentMonitorings.sort((m1, m2) -> Long.compare(m1.getCreatedAt(), m2.getCreatedAt()));
        for (Monitoring m : currentMonitorings) {
            if (m.getCreatedAt() > currentTime[0] + timeOffset) {
                if(timeOffset < 0) {
                    Log.d("Infi", "Backward button is clicked.");
                }
                return m;
            }
        }
        return null;
    }

    private void updateDataIfNeeded(Monitoring monitoring, Runnable onComplete) {
        String title = monitoring.getEspPacketTitle();

        if (currentESPPackets.isEmpty() || !Objects.equals(currentESPPackets.get(0).getTitle(), title)) {
            espPacketViewModel.getByTitle(title, espPackets -> {
                currentESPPackets.clear();
                currentESPPackets.addAll(espPackets);

                List<Long> espIds = espPackets.stream().map(ESPPacket::getId).collect(Collectors.toList());

                if (espVisualizationMap.isEmpty()) {
                    for (Long id : espIds) espVisualizationMap.put(id, 0);
                }

                visualizationViewModel.getByESPPacketTitle(title, visualizations -> {
                    currentVisualizations.clear();
                    currentVisualizations.addAll(visualizations);
                    displayAccordion(espIds);
                    onComplete.run();
                });
            });
        } else {
            onComplete.run();
        }
    }

    private void seekForwardOneSecond() {
        logs.add(new String[]{"User clicks on the Forward 1s Button", long2DateTimeString(System.currentTimeMillis())});
        timeOffset = 1000L;
    }

    public void seekBackwardOneSecond() {
        logs.add(new String[]{"User clicks on the backward 1s Button", long2DateTimeString(System.currentTimeMillis())});
        timeOffset = -1000L;
    }

    public void seekForwardTenSeconds() {
        logs.add(new String[]{"User clicks on the Forward 10s Button", long2DateTimeString(System.currentTimeMillis())});
        timeOffset = 10000L;
    }

    public void seekBackwardTenSeconds() {
        logs.add(new String[]{"User clicks on the backward 10s Button", long2DateTimeString(System.currentTimeMillis())});
        timeOffset = -10000L;
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
                    LinearLayout linearLayout = (LinearLayout) accordionContentMap.get(espPacket.getId());
                    if (linearLayout == null) return;
                    int vType = (Integer) linearLayout.getTag();
                    if (vType == 0) updateChartWithData(scatterChart, values, espPacket.getId(), sleep);
                    else if (vType == 1) updateTableWithData(targetTable, values, espPacket.getId(), requireContext());
                    updateStatistics(espPacket.getId(), values);
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
        logs.add(new String[]{"User clicks on the start Button", long2DateTimeString(System.currentTimeMillis())});
        isPlaying = true;
        startPlayback();
    }

    public void handleClickStopBtn() {
        logs.add(new String[]{"User clicks on the stop Button", long2DateTimeString(System.currentTimeMillis())});
        isPlaying = false;
    }

    public void handleClickForward1SBtn() {
        seekForwardOneSecond();
    }

    public void handleClickForward10SBtn() {
        seekForwardTenSeconds();
    }

    public void handleClickBackward1SBtn() {
        seekBackwardOneSecond();
    }

    public void handleClickBackward10SBtn() {
        seekBackwardTenSeconds();
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
                500));

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

    public void removeScatterEntriesUnderX(Map<Long, Object> map, Long min) {
        for (Map.Entry<Long, Object> entry : map.entrySet()) {
            Long key = entry.getKey();
            ScatterChart chart = (ScatterChart) entry.getValue();
            removeScatterEntriesAboutXForAChart(chart, min);
        }
    }

    public void removeScatterEntriesAboutXForAChart(ScatterChart scatterChart, Long minX) {
        ScatterData data = scatterChart.getData();
        if (data == null) return;

        for (IScatterDataSet iSet : data.getDataSets()) {
            if (iSet instanceof ScatterDataSet) {
                ScatterDataSet set = (ScatterDataSet) iSet;

                List<Entry> entriesToRemove = new ArrayList<>();

                for (int i = 0; i < set.getEntryCount(); i++) {
                    Entry entry = set.getEntryForIndex(i);
                    if ((long) entry.getX() >= minX) {
                        entriesToRemove.add(entry);
                    }
                }

                for (Entry entry : entriesToRemove) {
                    set.removeEntry(entry);
                }
            }
        }

        scatterChart.notifyDataSetChanged();
        scatterChart.invalidate();
    }

    public void removeCurrentMonitoringsByCreatedAt(Long time) {
        currentMonitorings = currentMonitorings
                .stream()
                .filter(one -> one.getCreatedAt() < time)
                .collect(Collectors.toList());
    }

    private LinearLayout displayChannelTable(ESPPacket espPacket, Context context) {
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        linearLayout.setWeightSum(3);

        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                0,
                TableLayout.LayoutParams.WRAP_CONTENT,
                2f
        ));
        tableLayout.setPadding(16, 16, 16, 16);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setGravity(Gravity.CENTER);

        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(ContextCompat.getColor(context, R.color.bs_primary));

        TextView orderHeaderView = createHeaderTextView("Order", requireContext());
        TextView channelHeaderView = createHeaderTextView("Channel", requireContext());
        TextView visibleHeaderView = createHeaderTextView("Visible", requireContext());
        TextView gainHeaderView = createHeaderTextView("Gain", requireContext());
        TextView offsetHeaderView = createHeaderTextView("Offset", requireContext());

        headerRow.addView(orderHeaderView);
        headerRow.addView(channelHeaderView);
        headerRow.addView(visibleHeaderView);
        headerRow.addView(gainHeaderView);
        headerRow.addView(offsetHeaderView);
        tableLayout.addView(headerRow);

        int nChannels = espPacket.getNumberOfChannels();
        for (int i = 0; i < nChannels; i ++) {
            TableRow tableRow = new TableRow(requireContext());
            tableRow.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            TextView orderView = createHeaderTextView(String.valueOf(i + 1), requireContext());
            orderView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            TextView channelView = createHeaderTextView(espPacket.getVariableName() + (i + 1), requireContext());
            channelView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            LinearLayout visibleCheckBoxLayout = new LinearLayout(requireContext());
            visibleCheckBoxLayout.setOrientation(LinearLayout.VERTICAL);
            visibleCheckBoxLayout.setPadding(10, 20, 10, 10);
//            thresholdCheckBoxLayout.setBackgroundColor(getResources().getColor(R.color.bs_info));
            visibleCheckBoxLayout.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
            visibleCheckBoxLayout.setGravity(Gravity.CENTER);
            CheckBox visibleCheckBox = new CheckBox(requireContext());
            visibleCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            visibleCheckBox.setGravity(Gravity.CENTER);
            visibleCheckBox.setChecked(true);
            int finalI = i;
            visibleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setVisibilityToChannel(espPacket.getId(), finalI, isChecked);
                }
            });
            visibleCheckBoxLayout.addView(visibleCheckBox);

            LinearLayout gainCheckBoxLayout = new LinearLayout(requireContext());
            gainCheckBoxLayout.setOrientation(LinearLayout.VERTICAL);
            gainCheckBoxLayout.setPadding(10, 20, 10, 10);
//            thresholdCheckBoxLayout.setBackgroundColor(getResources().getColor(R.color.bs_info));
            gainCheckBoxLayout.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
            gainCheckBoxLayout.setGravity(Gravity.CENTER);
            CheckBox gainCheckBox = new CheckBox(requireContext());
            gainCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            gainCheckBox.setGravity(Gravity.CENTER);
            gainCheckBoxLayout.addView(gainCheckBox);

            LinearLayout offsetCheckBoxLayout = new LinearLayout(requireContext());
            offsetCheckBoxLayout.setOrientation(LinearLayout.VERTICAL);
            offsetCheckBoxLayout.setPadding(10, 20, 10, 10);
//            thresholdCheckBoxLayout.setBackgroundColor(getResources().getColor(R.color.bs_info));
            offsetCheckBoxLayout.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
            offsetCheckBoxLayout.setGravity(Gravity.CENTER);
            CheckBox offsetCheckBox = new CheckBox(requireContext());
            offsetCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            offsetCheckBox.setGravity(Gravity.CENTER);
            offsetCheckBoxLayout.addView(offsetCheckBox);

            tableRow.addView(orderView);
            tableRow.addView(channelView);
            tableRow.addView(visibleCheckBoxLayout);
            tableRow.addView(gainCheckBoxLayout);
            tableRow.addView(offsetCheckBoxLayout);

            tableLayout.addView(tableRow);

        }


        LinearLayout rangeLayout = new LinearLayout(requireContext());
        rangeLayout.setOrientation(LinearLayout.VERTICAL);
        rangeLayout.setGravity(Gravity.CENTER_VERTICAL);
        rangeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                1200,
                LinearLayout.LayoutParams.MATCH_PARENT,
                3f
        ));
        rangeLayout.setPadding(16, 16, 16, 16);

        RangeSlider rangeSlider = new RangeSlider(requireContext());
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sliderParams.setMargins(16, 16, 16, 32);
        sliderParams.gravity = Gravity.CENTER;
        rangeSlider.setLayoutParams(sliderParams);
        rangeSlider.setValueFrom(0);
        rangeSlider.setValueTo(1000);
        rangeSlider.setValues(0f, 1000f);
        rangeSlider.setStepSize(10);

        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            handleChangeESPRange(values, espPacket.getId());
        });

        rangeLayout.addView(rangeSlider);
        linearLayout.addView(tableLayout);
        linearLayout.addView(rangeLayout);

        return linearLayout;
    }

    private void handleChangeESPRange(List<Float> values, Long espPackeId) {
        ScatterChart scatterChart = (ScatterChart) chartsMap.get(espPackeId);
        if (scatterChart == null) return;
        scatterChart.getAxisLeft().setAxisMinimum(values.get(0));
        scatterChart.getAxisLeft().setAxisMaximum(values.get(1));

    }

    private void setVisibilityToChannel(Long espPacketId, int channelNo, boolean checked) {
        ScatterChart scatterChart = (ScatterChart) chartsMap.get(espPacketId);
        if (scatterChart == null) return;
        ScatterData scatterData = scatterChart.getData();
        if (scatterData == null || scatterData.getDataSetCount() == 0) return;
        IScatterDataSet dataSet = scatterData.getDataSetByIndex(channelNo); // or use getDataSetByLabel()

        // hides the dataset
        dataSet.setVisible(checked); // hides the dataset
        scatterChart.invalidate(); // refreshes the chart
    }


    private void updateStatistics(Long espPacketId, List<Object> data) {
        List<Pair<Integer, Double>> pairs = currentStatistics.get(espPacketId);
        if (pairs == null || pairs.isEmpty()) return;

        int cnt = pairs.size();
        for (int i = 0; i < cnt; i ++) {
            try {
                Object obj = data.get(i);
                Pair<Integer, Double> pair = pairs.get(i);
                if (obj instanceof Integer) {
                    Integer value = (Integer) obj;
                    int newCount = pair.first + 1;
                    double currentTotal = pair.second.doubleValue() * pair.first.doubleValue();  // pair.second is already Double
                    double newValue = value.doubleValue();          // Unbox Integer to double
                    double newSum = currentTotal + newValue;
                    double newAvg = newSum / newCount;
                    Pair<Integer, Double> newPair = new Pair<>(newCount, newAvg);
                    pairs.set(i, newPair);
                    LinearLayout linearLayout = (LinearLayout) accordionContentMap.get(espPacketId);
                    if(linearLayout == null) continue;
                    LinearLayout tableWrapperLayout = (LinearLayout) linearLayout.getChildAt(1);
                    if (tableWrapperLayout == null) continue;
                    TableLayout tableLayout = (TableLayout) tableWrapperLayout.getChildAt(0);
                    if(tableLayout == null) continue;
                    TableRow tableRow = (TableRow) tableLayout.getChildAt(i + 1);
                    if (tableRow == null) continue;
                    LinearLayout gainLayout = (LinearLayout) tableRow.getChildAt(3);
                    if (gainLayout == null) continue;
                    CheckBox gainCheckBox = (CheckBox) gainLayout.getChildAt(0);
                    newAvg = Math.floor(newAvg * 100) / 100D;
                    gainCheckBox.setText(String.valueOf(newAvg));
                    LinearLayout offsetLayout = (LinearLayout) tableRow.getChildAt(4);
                    if( offsetLayout == null) continue;;
                    CheckBox offsetCheckBox = (CheckBox) offsetLayout.getChildAt(0);
                    if (offsetCheckBox == null) continue;
                    double offset = Math.abs(newAvg - value);
                    offset = Math.floor(offset * 100) / 100D;
                    offsetCheckBox.setText(String.valueOf(offset));
                }
            } catch (Exception e) {
                Log.w("Exception", "Content is " + e.toString());
            }
        }

    }

    private void handleClickLogMsgBtn() {
        logs.add(new String[]{"User clicks on the Log Message Button", long2DateTimeString(System.currentTimeMillis())});
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.log_table_layout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize table
        TableLayout tableLayout = dialogView.findViewById(R.id.log_table);
        populateTable(tableLayout);

        // Close button
        MaterialButton closeButton = dialogView.findViewById(R.id.log_table_dialog_btn);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleLogSwitchCheckedChange(boolean checked) {
        logs.add(new String[]{"User clicked the log switch", long2DateTimeString(System.currentTimeMillis())});
        logMsgBtn.setEnabled(checked);
    }

    private void populateTable(TableLayout tableLayout) {
        // Clear existing views (except header if you have one)
//        tableLayout.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_header));

        String[] headers = {"Order", "Context", "DateTime"};
        for (String header : headers) {
            TextView textView = new TextView(requireContext());
            textView.setText(header);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(16);
            textView.setTypeface(null, Typeface.BOLD);
            headerRow.addView(textView);
        }
        tableLayout.addView(headerRow);

        // Add sample data rows


        for (int i = 0; i < logs.size(); i++) {
            TableRow row = new TableRow(requireContext());
            if (i % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_row_even));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_row_odd));
            }

            TextView orderView = createHeaderTextView(String.valueOf(tableLayout.getChildCount()), requireContext());
            orderView.setTextColor(Color.BLACK);
            row.addView(orderView);
            for (String cell : logs.get(i)) {
                TextView textView = new TextView(requireContext());
                textView.setText(cell);
                textView.setPadding(16, 16, 16, 16);
                textView.setTextSize(14);
                row.addView(textView);
            }
            tableLayout.addView(row);
        }
    }

}
