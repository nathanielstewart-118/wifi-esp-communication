package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.getNumberOfBytesFromDataTypeString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.prtech.spiapp.MainActivity;
import com.prtech.spiapp.R;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Visualization;
import com.prtech.spiapp.db.entity.VisualizationRange;
import com.prtech.spiapp.db.viewmodel.SensorActuatorViewModel;
import com.prtech.spiapp.db.viewmodel.VisualizationViewModel;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.LogHelper;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VisualizationSetting extends Fragment {

    private AutoCompleteTextView idAutocomplete;
    private Button saveBtn;
    private Button loadBtn;
    private Button loadSensorActuatorBtn;
    private Button setupCompleteBtn;
    private TableLayout rangeTable;
    private EditText sampleRateEdit;
    private TextView sampleRateView;
    private EditText blockSizeEdit;
    private TextView blockSizeView;
    private EditText bufferSizeEdit;
    private TextView bufferSizeView;
    private EditText msUnitEdit;
    private EditText secUnitEdit;
    private EditText savePathEdit;
    private RadioGroup saveFormatRadio;

    private SensorActuatorViewModel sensorActuatorViewModel;
    private VisualizationViewModel  visualizationViewModel;
    private MainActivity mainActivity;
    private List<Visualization> visualizations = new ArrayList<>();
    private Visualization currentVisualization = new Visualization("", 0, 0, 0, new ArrayList<>(), 0, "", 0, System.currentTimeMillis());
    private List<ESPPacket> espPackets = new ArrayList<>();
    private List<VisualizationRange> sensorActuatorsToDisplay = new ArrayList<>();

    private final Gson gson = new Gson();


    public VisualizationSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualization, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[7]);
        mainActivity = (MainActivity) getActivity();
        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.visualization_id_autocomplete);

        saveBtn = view.findViewById(R.id.visualization_save_btn);
        loadBtn = view.findViewById(R.id.visualization_load_btn);
        loadSensorActuatorBtn = view.findViewById(R.id.visualization_load_sensor_actuator_btn);
        setupCompleteBtn = view.findViewById(R.id.visualization_setup_complete_btn);
        rangeTable = view.findViewById(R.id.visualization_list_tb);
        sampleRateEdit = view.findViewById(R.id.visualization_sample_rate_text);
        sampleRateView = view.findViewById(R.id.visualization_sample_rate_view);
        blockSizeEdit = view.findViewById(R.id.visualization_block_size_text);
        blockSizeView = view.findViewById(R.id.visualization_block_size_view);
        bufferSizeEdit = view.findViewById(R.id.visualization_buffer_size_text);
        bufferSizeView = view.findViewById(R.id.visualization_buffer_size_view);
        msUnitEdit = view.findViewById(R.id.visualization_ms_block_size_text);
        secUnitEdit = view.findViewById(R.id.visualization_sec_buffer_size_text);
        savePathEdit = view.findViewById(R.id.visualization_save_path_text);
        saveFormatRadio = view.findViewById(R.id.visualization_save_format_radio);

        saveBtn.setOnClickListener(v -> handleClickSaveBtn());
        loadBtn.setOnClickListener(v -> handleClickLoadBtn());
        loadSensorActuatorBtn.setOnClickListener(v -> handleClickLoadSensorActuator());
        setupCompleteBtn.setOnClickListener(v -> handleClickSetupComplete());

        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), data -> {
            espPackets.clear();
            espPackets.addAll(data);
        });
        visualizationViewModel = new ViewModelProvider(requireActivity()).get(VisualizationViewModel.class);
        visualizationViewModel.getAllVisualizations().observe(getViewLifecycleOwner(), data -> {
            visualizations.clear();
            visualizations.addAll(data);
            List<Visualization> results = (List<Visualization>) data;
            int cnt = results.size();
            String[] candidates = new String[cnt];
            for(int i = 0; i < cnt; i ++) {
                candidates[i] = results.get(i).getVisualizationId();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), // or getContext() if in Fragment
                    android.R.layout.simple_dropdown_item_1line,
                    candidates
            );
            idAutocomplete.setAdapter(adapter);
        });

        // in order to update the block size ms unit and buffer size sec unit notation
        sampleRateEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                handleFocusChangeOnSettings();
            }
        });

        blockSizeEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                handleFocusChangeOnSettings();
            }
        });

        bufferSizeEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                handleFocusChangeOnSettings();
            }
        });

        return view;
    }

    public void handleClickSaveBtn() {
        String visualizationId = idAutocomplete.getText().toString().trim();
        visualizationViewModel.getByVisualizationId(visualizationId, results -> {
            // if it's already saved
            if (!results.isEmpty()) {   // set id for update
                currentVisualization.setId(results.get(0).getId());
            }
            else {  // set id null for insertion
                currentVisualization.setId(null);
            }

            String sampleRate = sampleRateEdit.getText().toString().trim();
            String blockSize = blockSizeEdit.getText().toString().trim();
            String bufferSize = bufferSizeEdit.getText().toString().trim();
            String savePath = savePathEdit.getText().toString().trim();
            int selectedSaveFormatButtonId = saveFormatRadio.getCheckedRadioButtonId();
            int selectedSaveFormatIndex = -1;
            if(selectedSaveFormatButtonId != -1) {
                View selectedRadioButton = saveFormatRadio.findViewById(selectedSaveFormatButtonId);
                selectedSaveFormatIndex = saveFormatRadio.indexOfChild(selectedRadioButton);
                currentVisualization.setSaveFormat(selectedSaveFormatIndex);
            }
            currentVisualization.setVisualizationId(visualizationId);
            try {
                currentVisualization.setSampleRate(Integer.parseInt(sampleRate));
                currentVisualization.setBlockSize(Integer.parseInt(blockSize));
                currentVisualization.setBufferSize(Integer.parseInt(bufferSize));
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentVisualization.setSavePath(savePath);

            // get ranges value from the ranges table
            int rowCnt = rangeTable.getChildCount();
            List<VisualizationRange> cRanges = new ArrayList<>();
            for (int i = 1; i < rowCnt; i ++) {
                View rowView = rangeTable.getChildAt(i);
                if(rowView instanceof TableRow) {
                    TableRow row = (TableRow) rowView;
                    Long saId = (Long) row.getTag();
                    VisualizationRange range= new VisualizationRange(saId, 0, 0, 0F, 0F, 0L, 0L);
                    Spinner yRangeSpinner = (Spinner) row.getChildAt(6);
                    int yRangeIndex = yRangeSpinner.getSelectedItemPosition();

                    range.setyAxisRange(yRangeIndex);
                    RadioGroup visualizationRadioGroup = (RadioGroup) row.getChildAt(5);
                    int selectedRadioButtonId = visualizationRadioGroup.getCheckedRadioButtonId();
                    int selectedRadioIndex = -1;
                    if(selectedRadioButtonId != -1) {
                        View selectedRadioButton = visualizationRadioGroup.findViewById(selectedRadioButtonId);
                        selectedRadioIndex = visualizationRadioGroup.indexOfChild(selectedRadioButton);
                        range.setVisualizationType(selectedRadioIndex);
                    }
                    EditText upperLimitEdit = (EditText) row.getChildAt(7);
                    String upperLimitString = upperLimitEdit.getText().toString().trim();
                    EditText lowerLimitEdit = (EditText) row.getChildAt(8);
                    String lowerLimitString = lowerLimitEdit.getText().toString().trim();
                    try {
                        range.setUpperLimit(Long.parseLong(upperLimitEdit.getText().toString()));
                        range.setLowerLimit(Long.parseLong(lowerLimitEdit.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cRanges.add(range);
                }

            }
            currentVisualization.setRanges(cRanges);

            if (currentVisualization.getId() != null && currentVisualization.getId() > 0) {
                visualizationViewModel.update(currentVisualization, id -> {
                    if (id != null && id > 0) {
                        Toast.makeText(requireContext(), "Update Success!", Toast.LENGTH_SHORT).show();
                        initUIs("");
                        LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Visualization update success",
                                Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.d("Visualization update", "Visualization update success");

                    }
                    else {
                        Toast.makeText(requireContext(), "Update Failed!", Toast.LENGTH_SHORT).show();
                        LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Visualization update failed",
                                Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.d("Visualization update", "Visualization update failed");

                    }
                });
            }
            else {
                visualizationViewModel.insert(currentVisualization, id -> {
                    if (id != null && id > 0) {
                        initUIs("");
                        Toast.makeText(requireContext(), "Insert Success!", Toast.LENGTH_SHORT).show();
                        LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Visualization insert success",
                                Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.d("Visualization insertion", "Visualization insert success");
                        sensorActuatorsToDisplay = sensorActuatorsToDisplay
                            .stream()
                            .filter(sa -> {
                                List<VisualizationRange> filtered = currentVisualization.getRanges()
                                    .stream()
                                    .filter(r -> Objects.equals(r.getSensorActuatorId(), sa.getSensorActuatorId()))
                                    .collect(Collectors.toList());
                                return filtered.isEmpty();
                            })
                            .collect(Collectors.toList());

                    }
                    else {
                        Toast.makeText(requireContext(), "Insert Failed!", Toast.LENGTH_SHORT).show();
                        LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Visualization insert failed",
                                Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.d("Visualization insertion", "Visualization insert failed");

                    }
                });
            }


        });

    }

    /*
     *  handler function for "Load" button click event
     *
     */
    public void handleClickLoadBtn() {
        String visualizationId = idAutocomplete.getText().toString();
        visualizationViewModel.getByVisualizationId(visualizationId, results -> {
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_records_found, Toast.LENGTH_SHORT).show();
                initUIs(visualizationId);
                currentVisualization = new Visualization("", 0, 0, 0, new ArrayList<>(), 0, "", 0, System.currentTimeMillis());
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Visualization load failed : no records found",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Visualization load", "Visualization load failed : no records found");
            }
            else {
                currentVisualization.copyFrom(results.get(0));
                initUisWithData(results.get(0));
                Toast.makeText(requireContext(), R.string.record_loaded_successfully, Toast.LENGTH_SHORT).show();
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Visualization record loaded successfully!",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Visualization load", "Visualization record loaded successfully.");
            }
        });
    }

    /*
     * handler function for "Import Sensor Actuator Data" button click event
     */
    public void handleClickLoadSensorActuator() {
//        for (SensorActuator sa: sensorActuators) {
//            sensorActuatorsToDisplay.add(new VisualizationRange(sa.getId(), -1, 0, 0F, 0F, 0L, 0L));
//        }
        displayRangesTable();

    }

    /*
     * handler function for "Setup Complete" button click handler
     */
    public void handleClickSetupComplete() {
        if(currentVisualization.getId() == null) {
            Toast.makeText(requireContext(), R.string.you_need_to_save_the_visualization_setting_first_before_completing, Toast.LENGTH_SHORT).show();
            return;
        }
        visualizationViewModel.setActivated(currentVisualization.getId(), result -> {
            Toast.makeText(requireContext(), "Setup successfully completed", Toast.LENGTH_SHORT).show();
        });
        List<ESPPacket> targets = new ArrayList<>();
        for (VisualizationRange r: currentVisualization.getRanges()) {
        }
        mainActivity.sendTCP(gson.toJson(currentVisualization.getRanges()).getBytes(StandardCharsets.UTF_8));
    }

    /*
     * display ranges table from the previously saved data
     */
    public void displayRangesTable() {
        rangeTable.removeViews(1, rangeTable.getChildCount() - 1);
        List<VisualizationRange> rangesToDisplay = new ArrayList<>(currentVisualization.getRanges());
        for (ESPPacket sa: espPackets) {
            List<VisualizationRange> filtered = rangesToDisplay
                .stream()
                .filter(rtd -> Objects.equals(rtd.getSensorActuatorId(), sa.getId()))
                .collect(Collectors.toList());
            if(filtered.isEmpty()) rangesToDisplay.add(new VisualizationRange(sa.getId(), -1, 0, 0F, 0F, 0L, 0L));
        }
        for (int i = 0; i < rangesToDisplay.size(); i ++) {
            addDisplayRangeTableRow(rangesToDisplay.get(i), i + 1);
        }
    }

    /*
     * add a row with range data to the range table
     * @params:
     *      VisualizationRange range: for data display
     *      int index: row index in the range table
     * @return
     */
    public void addDisplayRangeTableRow(VisualizationRange range, int index) {
        List<ESPPacket> sas = espPackets.stream()
                .filter(s -> Objects.equals(s.getId(), range.getSensorActuatorId()))
                .collect(Collectors.toList());
        ESPPacket s = null;
        if(!sas.isEmpty()) s = sas.get(0);

        int fixedWidthInDp = 250;
        int fixedWidthInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                fixedWidthInDp,
                requireContext().getResources().getDisplayMetrics()
        );
        TableRow.LayoutParams params = new TableRow.LayoutParams(fixedWidthInPx, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        TableRow row = new TableRow(requireContext());
        row.setTag(range.getSensorActuatorId());

        TextView orderView = new TextView(requireContext());
        orderView.setText(String.valueOf(index));

        TextView variableNameView = new TextView(requireContext());
        variableNameView.setText(s.getVariableName());

        TextView dataTypeView = new TextView(requireContext());
        dataTypeView.setText(s.getDataType());

        TextView numberOfChannelsView = new TextView(requireContext());
        numberOfChannelsView.setText(String.valueOf(s.getNumberOfChannels()));

        TextView numberOfBytesView = new TextView(requireContext());
        numberOfBytesView.setText(String.valueOf(getNumberOfBytesFromDataTypeString(s.getDataType())));

        RadioGroup visualizationRadioGroup = new RadioGroup(requireContext());
        RadioButton graphRadio = new RadioButton(requireContext());
        graphRadio.setText("Graph");

        RadioButton tableRadio = new RadioButton(requireContext());
        tableRadio.setText("Table");

        RadioButton disabledRadio = new RadioButton(requireContext());
        disabledRadio.setText("Disabled");
        visualizationRadioGroup.addView(graphRadio);
        visualizationRadioGroup.addView(tableRadio);
        visualizationRadioGroup.addView(disabledRadio);
        Integer visualizationType = range.getVisualizationType();
        if (visualizationType != null && visualizationType >= 0 && visualizationType < visualizationRadioGroup.getChildCount() ) {
            RadioButton button = (RadioButton) visualizationRadioGroup.getChildAt(visualizationType);
            button.setChecked(true);
        }

        visualizationRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
        visualizationRadioGroup.setLayoutParams(params);
        Spinner yRangeSpinner = new Spinner(requireContext());
        int yAxisOptionsCnt = Constants.Y_AXIS_RANGES.length;
        String[] yAxisOptions = new String[yAxisOptionsCnt];
        for (int i = 0; i < yAxisOptionsCnt; i ++)  {
            String[][] options = Constants.Y_AXIS_RANGES;
            yAxisOptions[i] = options[i][0];
        }
        yRangeSpinner.setLayoutParams(params);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, yAxisOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yRangeSpinner.setAdapter(adapter);
        yRangeSpinner.setSelection(range.getyAxisRange());
        EditText upperLimitEdit = new EditText(requireContext());
        if (range.getUpperLimit() != null && range.getUpperLimit() > 0) upperLimitEdit.setText(String.valueOf(range.getUpperLimit()));

        EditText lowerLimitEdit = new EditText(requireContext());
        if(range.getLowerLimit() != null && range.getLowerLimit() > 0) lowerLimitEdit.setText(String.valueOf(range.getLowerLimit()));

        handleFocusChangeOnSettings();

        row.addView(orderView);
        row.addView(variableNameView);
        row.addView(dataTypeView);
        row.addView(numberOfChannelsView);
        row.addView(numberOfBytesView);
        row.addView(visualizationRadioGroup);
        row.addView(yRangeSpinner);
        row.addView(upperLimitEdit);
        row.addView(lowerLimitEdit);
        rangeTable.addView(row);
    }
    /*
     * initialize all input elements with empty string except id autcomplete with id
     * @params
     *      String id: visualization id string to fill the autocomplete
     * @return
     *      void
     */
    public void initUIs(String id) {
        idAutocomplete.setText(id);
        sampleRateEdit.setText("");
        blockSizeEdit.setText("");
        bufferSizeEdit.setText("");
        savePathEdit.setText("");
        saveFormatRadio.clearCheck();
        int cnt = rangeTable.getChildCount();
        if (cnt > 1) rangeTable.removeViews(1, cnt - 1);
    }

    /*
     * initialize all input elements with visualization data
     * @params
     *      Visualization data: data to display in the input uis
     * @return
     *      void
     */
    public void initUisWithData(Visualization data) {
        idAutocomplete.setText(data.getVisualizationId());
        sampleRateEdit.setText(String.valueOf(data.getSampleRate()));
        blockSizeEdit.setText(String.valueOf(data.getBlockSize()));
        bufferSizeEdit.setText(String.valueOf(data.getBufferSize()));
        savePathEdit.setText(data.getSavePath());
        // set ranges in range table
        displayRangesTable();
        Integer saveFormat = data.getSaveFormat();
        saveFormatRadio.clearCheck();
        if(saveFormat != null && saveFormat >= 0 && saveFormat < saveFormatRadio.getChildCount()) {
            RadioButton button = (RadioButton) saveFormatRadio.getChildAt(saveFormat);
            button.setChecked(true);
        }
    }

    /*
     * handler function to caculate the block size ms and buffer size sec when inputing sample rate, block size, buffer size
     */
    public void handleFocusChangeOnSettings() {
        try {
            Integer sampleRate = Integer.parseInt(sampleRateEdit.getText().toString().trim());
            Integer blockSize = Integer.parseInt(blockSizeEdit.getText().toString().trim());
            Integer bufferSize = Integer.parseInt(bufferSizeEdit.getText().toString().trim());
            if (sampleRate == null || blockSize == null || bufferSize == null) return;
            Float msUnit = 1000F / sampleRate * blockSize;
            Float secUnit = (float) (bufferSize * blockSize) / sampleRate;
            msUnitEdit.setText(msUnit.isNaN()? "" : String.format("%.2f", msUnit));
            secUnitEdit.setText(secUnit.isNaN() ? "" : String.format("%.2f", secUnit));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
