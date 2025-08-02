package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.Constants.COLORS;
import static com.prtech.spiapp.utils.UIUtils.createHeaderTextView;
import static com.prtech.spiapp.utils.UIUtils.initSpinnerWithSuggestionList;
import static com.prtech.spiapp.utils.UIUtils.uncheckOtherToggles;
import static com.prtech.spiapp.utils.communications.PacketParser.encodeCommand;

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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.prtech.spiapp.MainActivity;
import com.prtech.spiapp.R;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.entity.CommandThreshold;
import com.prtech.spiapp.db.entity.CommandThresholdWithDataType;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.ESPSendData;
import com.prtech.spiapp.db.entity.Experiment;
import com.prtech.spiapp.db.entity.Monitoring;
import com.prtech.spiapp.db.entity.RangeDTO;
import com.prtech.spiapp.db.entity.Visualization;
import com.prtech.spiapp.db.entity.VisualizationRange;
import com.prtech.spiapp.db.viewmodel.CommandViewModel;
import com.prtech.spiapp.db.viewmodel.ExperimentViewModel;
import com.prtech.spiapp.db.viewmodel.MonitoringViewModel;
import com.prtech.spiapp.db.viewmodel.ESPPacketViewModel;
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
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.prtech.spiapp.utils.components.CustomLoadingButton;


public class MonitoringSetting extends Fragment {

    private LinearLayout accordionContainer;
    private final Map<Long, Object> accordionContentMap = new HashMap<>();
    private final Map<Long, Object> chartsMap = new HashMap<>();
    private final Map<Long, Object> tablesMap = new HashMap<>();
    private final Map<Long, Float> currentWindowStartMap = new HashMap<>();
    private final Map<Long, Integer> espVisualizationMap = new HashMap<>();

    private Spinner experimentSpinner;
    private final Gson gson = new Gson();
    private List<ToggleButton> experimentToggleButtons = new ArrayList<>();
    private List<CustomLoadingButton> commandToggleButtons = new ArrayList<>();
    private Button startBtn;
    private Button stopBtn;

    private CustomLoadingButton loadingButton;
    private LinearLayout commandSetBtnLayout;

    private LinearLayout experimentSetBtnLayout;


    private ESPPacketViewModel espPacketViewModel;
    private TCPUDPReceiveViewModel receiveViewModel;
    private MonitoringViewModel monitoringViewModel;
    private VisualizationViewModel visualizationViewModel;
    private CommandViewModel commandViewModel;
    private Visualization currentVisualization;
    private ExperimentViewModel experimentViewModel;

    private List<RangeDTO> rangeDTOs;
    private List<ESPPacket> espPackets = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<String> experimentTitles = new ArrayList<>();
    private List<Experiment> currentExperiments = new ArrayList<>();
    private List<Command> currentCommands = new ArrayList<>();
    private List<ESPPacket> currentESPPackets = new ArrayList<>();
    private List<Command> allCommands = new ArrayList<>();
    private List<Visualization> currentVisualizations = new ArrayList<>();
    private Boolean bDrawing = true;
    private Integer currentExperimentSetId = 0;
    private Integer currentCommandSetIndex = 0;
    private final float windowSize = 500f;
    private MainActivity mainActivity;
    private volatile Boolean bRunning = false;


    public MonitoringSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[5]);
        accordionContainer = view.findViewById(R.id.monitoring_accordion_container);
        commandSetBtnLayout = view.findViewById(R.id.monitoring_command_set_btns_wrapper_layout);
        experimentSetBtnLayout = view.findViewById(R.id.monitoring_experiment_sets_wrapper_layout);
        mainActivity = (MainActivity) getActivity();

        experimentSpinner = (Spinner) view.findViewById(R.id.monitoring_experiment_select_spinner);
        experimentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String experimentTitle = experimentSpinner.getItemAtPosition(position).toString().trim();
                experimentViewModel.getByTitle(experimentTitle, results -> {
                    if(results.isEmpty()) return;
                    currentExperiments.clear();
                    currentExperiments.addAll(results);
                    Set<String> titleSets = results
                            .stream()
                            .map(Experiment::getCommands)
                            .filter(Objects::nonNull) // Skip null command lists
                            .flatMap(List::stream)
                            .collect(Collectors.toSet());
                    commandViewModel.getByTitles(new ArrayList<>(titleSets), cmds -> {
                        allCommands.clear();
                        allCommands.addAll(cmds);
                    });
                    int rowNum = currentExperimentSetId / 2;
                    int colNum = currentExperimentSetId % 2;
                    LinearLayout layout = (LinearLayout) experimentSetBtnLayout.getChildAt(rowNum);
                    ToggleButton button = (ToggleButton) layout.getChildAt(colNum);
                    uncheckOtherToggles(null, experimentToggleButtons);
                    button.performClick();
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        receiveViewModel = new ViewModelProvider(requireActivity()).get(TCPUDPReceiveViewModel.class);
        receiveViewModel.getData().observe(getViewLifecycleOwner(), data -> {
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "New Data arrived from TCP : " + Arrays.toString(data),
                    Constants.LOGGING_BEARER_TOKEN
            );
            Log.d("Monitoring Info", "New Data arrived from TCP : " + Arrays.toString(data));
            Toast.makeText(requireContext(), "Data arrived", Toast.LENGTH_SHORT).show();
            if(bRunning) updateViews(data, 6);

        });

        espPacketViewModel = new ViewModelProvider(requireActivity()).get(ESPPacketViewModel.class);
        espPacketViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), data -> {
            List<ESPPacket> actuators = (List<ESPPacket>) data;
            espPackets.clear();
            espPackets.addAll(actuators);
        });

        commandViewModel = new ViewModelProvider(requireActivity()).get(CommandViewModel.class);

        experimentViewModel = new ViewModelProvider(requireActivity()).get(ExperimentViewModel.class);
        experimentViewModel.getAllTitles().observe(getViewLifecycleOwner(), results -> {
            results.add(0, "Experiment Set");
            initSpinnerWithSuggestionList(experimentSpinner, results, requireContext(), android.R.layout.simple_spinner_item);
        });

        monitoringViewModel = new ViewModelProvider(requireActivity()).get(MonitoringViewModel.class);

        visualizationViewModel = new ViewModelProvider(requireActivity()).get(VisualizationViewModel.class);

        // Test data
        /*
        visualizationViewModel.getActivatedVisualization(result -> {
            if (result == null) {
                currentVisualization = new Visualization("", "", 0, 0, 0, new ArrayList<>(), 0, "", 0, System.currentTimeMillis());
                rangeDTOs = new ArrayList<>();
                Toast.makeText(requireContext(), "There is no completed Visualization setting.", Toast.LENGTH_SHORT).show();
            }
            else {
                currentVisualization = result;
                displayAccordion(currentVisualization.getRanges());
//                ByteBuffer buffer = ByteBuffer.allocate(4 * Constants.MAX_VARIABLE_NUMBER_IN_PACKET);
                /*
                visualizationViewModel.getCorrespondingSAs
                (currentVisualization.getId(), results -> {
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

                                        case "uint24":

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
        */
        experimentToggleButtons.add(view.findViewById(R.id.monitoring_experiment_set1_btn));
        experimentToggleButtons.add(view.findViewById(R.id.monitoring_experiment_set2_btn));
        experimentToggleButtons.add(view.findViewById(R.id.monitoring_experiment_set3_btn));
        experimentToggleButtons.add(view.findViewById(R.id.monitoring_experiment_set4_btn));

        for (ToggleButton toggle: experimentToggleButtons) {
            toggle.setOnCheckedChangeListener(this.handleCheckedChangeListener);
        }

//        for (ToggleButton toggle: commandToggleButtons) {
//            toggle.setOnCheckedChangeListener(((buttonView, isChecked) -> {
//                if (isChecked) {
//                    uncheckOtherToggles((ToggleButton) buttonView, commandToggleButtons);
//                }
//            }));
//        }

        startBtn = view.findViewById(R.id.monitoring_start_btn);
        startBtn.setOnClickListener(v -> {
            try {
                handleClickStartBtn();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        stopBtn = view.findViewById(R.id.monitoring_stop_btn);
        stopBtn.setOnClickListener(v -> handleClickStopBtn());
        return view;
    }

    @Override
    public void onStop() {
        bDrawing = false;
        super.onStop();
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
            RangeDTO rangeDTO = new RangeDTO(currentVisualizations.get(0).getId(), visualizationRange.getEspPacketId(), result.getVariableName(), result.getDataType(), result.getNumberOfChannels(), visualizationRange.getVisualizationType(), visualizationRange.getyAxisRange(), visualizationRange.getUpperLimit(), visualizationRange.getLowerLimit());
            rangeDTOs.add(rangeDTO);
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

    public void displayAccordion(List<Long> espIds) {

        accordionContainer.removeAllViews();
        accordionContentMap.clear();
        chartsMap.clear();
        rangeDTOs.clear();
        for (Long espId: espIds) {
            addAccordionSection(espId);
        }
    }

    public void updateViews(byte[] data, int cnt) {
        Monitoring monitoring = new Monitoring(Arrays.toString(data), System.currentTimeMillis());

        monitoringViewModel.insert(monitoring, result -> {
            if (result != null && result > 0) {
//               Toast.makeText(requireContext(), R.string.insert_success, Toast.LENGTH_SHORT).show();
            }
            else {
//               Toast.makeText(requireContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
            }
        });
        if(rangeDTOs == null || rangeDTOs.isEmpty()) return;
        Map<Long, Object> parsed = PacketParser.parse(rangeDTOs, data);
        final int[] updateCnt = {0};
        for (RangeDTO rangeDTO: rangeDTOs) {
            List<Object> values = (List<Object>) parsed.get(rangeDTO.getEspPacketId());
            ScatterChart scatterChart = (ScatterChart) chartsMap.get(rangeDTO.getEspPacketId());
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateChartWithData(scatterChart, values, rangeDTO.getEspPacketId());
                }
            };

            updater.run();

        }
    }

    private void updateChartWithData(ScatterChart scatterChart, List<Object> data, Long saId) {
        float maxX = 0;
        if(scatterChart == null || data == null || data.isEmpty()) return;
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
//            scatterChart.postDelayed(() -> updateChartWithData(scatterChart, data, saId), 10000);
//                    Toast.makeText(requireContext(), "MaxX value is" + String.valueOf(maxX), Toast.LENGTH_SHORT).show();
//                    scatterChart.moveViewToAnimated(maxX, 0f, YAxis.AxisDependency.LEFT, 300);

    }

    public void handleClickStartBtn() throws InterruptedException {
        bRunning = true;
        enableOrDisableButtons(experimentToggleButtons, false);
        executorService.execute(() -> {
            List<String> commands = currentExperiments.get(currentExperimentSetId).getCommands();
            int cnt = commands.size();

            for (int i = 0; i < cnt; i ++ ) {           // for each Command Set
                int finalI = i;
                List<Command> filtered = allCommands
                        .stream()
                        .filter(c -> c.getTitle().equals(commands.get(finalI)))
                        .collect(Collectors.toList());
                if(!filtered.isEmpty()) {
                    // display accordion according esp packet variables belonging to this command
                    String espPacketTitle = filtered.get(0).getEspPacketTitle();
                    espPacketViewModel.getByTitle(espPacketTitle, results -> {
                        currentESPPackets.clear();
                        currentESPPackets.addAll(results);
                    });
                    visualizationViewModel.getByESPPacketTitle(espPacketTitle, results -> {
                        currentVisualizations.clear();
                        currentVisualizations.addAll(results);
                        if (!results.isEmpty()) {
                            // all visualizations with the same esp packet title have the same esp packet variables, so get(0)
                            List<Long> espIds = results.get(0).getRanges()
                                    .stream()
                                    .map(VisualizationRange::getEspPacketId)
                                    .collect(Collectors.toList());
                            for (Long espId: espIds) {
                                espVisualizationMap.put(espId, 0);
                            }
                            displayAccordion(espIds);

                        }
                    });
                }
                int rowNo = i / 2;
                int colNo = i % 2;
                LinearLayout linearLayout = (LinearLayout) commandSetBtnLayout.getChildAt(rowNo);
                CustomLoadingButton button = (CustomLoadingButton) linearLayout.getChildAt(colNo);
                requireActivity().runOnUiThread(() -> {

                    button.setActive(true);
                    button.showLoading();
                });
                try {
                    runCommands(currentExperimentSetId, i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                requireActivity().runOnUiThread(() -> {
                    button.setActive(false);
                    button.hideLoading();
                });


            }
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Running current Experiment set finished.", Toast.LENGTH_SHORT).show();
                enableOrDisableButtons(experimentToggleButtons, true);
            });
        });

    }

    public void handleClickStopBtn() {
        bRunning = false;
        enableOrDisableButtons(experimentToggleButtons, true);
    }

    public void displayCommandSetButtons(List<String> titles) {
        commandSetBtnLayout.removeAllViews();
        commandToggleButtons.clear();
        if(titles == null || titles.isEmpty()) return;
        int cnt = titles.size();

        for (int i = 0; i < cnt; i += 2) {
            LinearLayout wrapperLayout = new LinearLayout(requireContext());
            wrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
            wrapperLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams wrapperLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            wrapperLayout.setLayoutParams(wrapperLayoutParams);
            for (int j = 0; j < 2; j ++) {
                int index = i + j;
                if (index >= cnt) break;
                CustomLoadingButton loadButton = new CustomLoadingButton(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())
                );
                params.setMargins(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics())
                );
                loadButton.setLayoutParams(params);
//                loadButton.setEnabled(false);
                loadButton.setText(titles.get(i + j));
                // Set text and text size
//                toggleButton.setTextOn(titles.get(i + j));
//                toggleButton.setTextOff(titles.get(i + j));
//                toggleButton.setTextOff("Set " + String.valueOf(i + j + 1));
                loadButton.setTooltipText(titles.get(i + j));
                loadButton.setTooltipText(titles.get(i + j));
//                toggleButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

                // Set text color
//                toggleButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.basic_button_text_color));

                // Set background drawable
//                toggleButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.toggle_selector));
                wrapperLayout.addView(loadButton);
                commandToggleButtons.add(loadButton);
            }
            commandSetBtnLayout.addView(wrapperLayout);
        }
        /*
        for (ToggleButton toggleButton: commandToggleButtons) {
            toggleButton.setOnCheckedChangeListener(((buttonView, isChecked) ->  {
                if (isChecked) {
                    if(!bRunning) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Please press START button first.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    LinearLayout parent = (LinearLayout) buttonView.getParent();
                    LinearLayout uParent = (LinearLayout) parent.getParent();
                    int colNo = parent.indexOfChild(buttonView);
                    int rowNo = uParent.indexOfChild(parent);
                    uncheckOtherToggles((ToggleButton) buttonView, commandToggleButtons);
                    ToggleButton button =  (ToggleButton) buttonView;
                    if(!mainActivity.tcpConnected) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Please connect to ESP TCP server first!", Toast.LENGTH_SHORT).show();
                        });
                        LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Failed to transmit data to ESP TCP server due to disconnection",
                                Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.d("Monitoring Info", "Failed to transmit data to ESP TCP server due to disconnection");
//                        return;
                    }

                    String title = button.getText().toString().trim();
                    try {
                        runCommands(title);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));
        }
        */
    }

    public void runCommands(int experimentSetIndex, int commandSetIndex) throws InterruptedException {
        Experiment experiment = currentExperiments.get(experimentSetIndex);
        List<String> titles = experiment.getCommands();
        List<Command> commands = allCommands
                .stream()
                .filter(one -> titles.contains(one.getTitle()))
                .collect(Collectors.toList());

        if(!commands.isEmpty()) {
            try {
                float preRun = experiment.getPreRun();
                for (int j = 0; j < preRun; j++) {
                    if (!bRunning) return;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int cnt = commands.size();
            for (int j = 0; j < cnt; j++) {
                Command command = commands.get(j);
                sendCommand(command);
                try {
                    float runSec = experiment.getCommand() + experiment.getRest();
                    for (int k = 0; k < runSec; k++) {
                        if (!bRunning) return;
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                float postRun = experiment.getPostRun();
                for (int i = 0; i < postRun; i++) {
                    if (!bRunning) return;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (commandSetIndex + 1 < experiment.getCommands().size()) {
                try {
                    runCommands(experimentSetIndex, commandSetIndex + 1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendCommand(Command command) {
        List<CommandThreshold> thresholds = command.getThresholds();
        List<Long> espPacketIds = thresholds
                .stream()
                .map(t -> t.getEspPacketId())
                .collect(Collectors.toList());

        espPacketViewModel.getByIds(espPacketIds, results -> {
            List<CommandThresholdWithDataType> dtos = new ArrayList<>();
            ESPSendData espSendData = new ESPSendData(command.getCommandCode(), command.getTime1(), command.getTime2(), new ArrayList<>());
            for (CommandThreshold threshold: thresholds) {
                List<ESPPacket> correspondingESPPackets = results
                        .stream()
                        .filter(one -> Objects.equals(one.getId(), threshold.getEspPacketId()))
                        .collect(Collectors.toList());
                if (!correspondingESPPackets.isEmpty()) {

                    CommandThresholdWithDataType commandThresholdWithDataType = new CommandThresholdWithDataType(threshold.getEspPacketId(), correspondingESPPackets.get(0).getDataType(), threshold.getThresholds());
                    dtos.add(commandThresholdWithDataType);
                }
            }
            espSendData.setThresholds(dtos);
            byte[] bytesToTransmit = encodeCommand(espSendData);
            if(!mainActivity.tcpConnected) {
                if(isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Please connect to ESP TCP server first!", Toast.LENGTH_SHORT).show();
                    });
                }
                LogHelper.sendLog(
                        Constants.LOGGING_BASE_URL,
                        Constants.LOGGING_REQUEST_METHOD,
                        "Failed to transmit data to ESP TCP server due to disconnection",
                        Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Monitoring Info", "Data is transmitted to TCP : " + Arrays.toString(bytesToTransmit));

            }
            else {
                mainActivity.sendTCP(bytesToTransmit);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Please connect to ESP TCP server first!", Toast.LENGTH_SHORT).show();
                });
                LogHelper.sendLog(
                        Constants.LOGGING_BASE_URL,
                        Constants.LOGGING_REQUEST_METHOD,
                        "Data is transmitted to TCP : " + Arrays.toString(bytesToTransmit),
                        Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Monitoring Info", "Data is transmitted to TCP : " + Arrays.toString(bytesToTransmit));
            }
        });
    }

    CompoundButton.OnCheckedChangeListener handleCheckedChangeListener = (buttonView, isChecked) -> {
        ToggleButton thisButton = (ToggleButton) buttonView;
        if (isChecked) {
            uncheckOtherToggles(thisButton, experimentToggleButtons);
            LinearLayout parent = (LinearLayout) buttonView.getParent();
            int colNum = parent.indexOfChild(buttonView);
            LinearLayout oParent = (LinearLayout) parent.getParent();
            int rowNum = oParent.indexOfChild(parent);
            currentExperimentSetId = 2 * rowNum + colNum;
            if (currentExperiments.isEmpty() || currentExperiments.size() < currentExperimentSetId + 1)
                return;
            List<String> commands = currentExperiments.get(currentExperimentSetId).getCommands();
            displayCommandSetButtons(commands);

            commandViewModel.getByTitles(commands, results -> {
                currentCommands.clear();
                currentCommands.addAll(results);
            });
        }
    };

    public void enableOrDisableButtons(List<ToggleButton> buttons, Boolean status) {
        for (ToggleButton button: buttons) {
            button.setEnabled(status);
        }
    }

    public ScatterChart displayChart(VisualizationRange visualizationRange, Context context) {
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
        headerRow.setBackgroundColor(ContextCompat.getColor(context, R.color.bs_primary));

        TextView orderChannelView = new TextView(requireContext());
        channelRow.addView(orderChannelView);
        for (int i = 0; i < nChannels; i ++) {
            TextView channelView = new TextView(requireContext());
            channelView.setText(String.valueOf(i + 1));
            channelRow.addView(channelView);
        }

        tableLayout.addView(headerRow);
        tableLayout.addView(channelRow);
        return tableLayout;
    }

}
