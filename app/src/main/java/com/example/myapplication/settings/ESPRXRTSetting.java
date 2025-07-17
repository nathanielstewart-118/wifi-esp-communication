package com.example.myapplication.settings;

import static com.example.myapplication.utils.CommonUtils.getNumberOfBytesFromDataTypeString;
import static com.example.myapplication.utils.UIUtils.setupOperationalButtons;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.ESPRXRT;
import com.example.myapplication.db.entity.ESPRXRTThreshold;
import com.example.myapplication.db.entity.ESPTX;
import com.example.myapplication.db.entity.ESPTXOutlier;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.viewmodel.ESPRXRTViewModel;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;
import com.example.myapplication.utils.Constants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ESPRXRTSetting extends Fragment {

    private View thresholdEditDialog;
    private View addCustomDialog;
    private Dialog dialog;
    private TableLayout espRXRTListTable;
    private TableLayout thresholdEditTable;

    private Button thresholdDialogCloseBtn;
    private Button thresholdSaveBtn;
    private Button addCustomBtn;
    private Button loadSensorsBtn;
    private Button loadActuatorsBtn;
    private Button tcpBtn;
    private Button udpBtn;
    private Switch crcSwitch;
    private Button saveBtn;
    private Button closeAddCustomDialogBtn;
    private EditText variableNameEdit;
    private Spinner dataTypeSpinner;
    private EditText numberOfChannelsEdit;

    private ESPRXRTViewModel esprxrtViewModel;
    private SensorActuatorViewModel sensorActuatorViewModel;

    private List<ESPRXRT> esprxrts = new ArrayList<>();
    private List<SensorActuator> sensorActuators = new ArrayList<>();
    private List<ESPRXRT> sensorActuatorsToDisplay = new ArrayList<>();
    private Long currentESPRXRTSaId = (long)-1;
    private int currentTableIndex = -1;
    private final Gson gson = new Gson();

    private AlertDialog.Builder builder;


    public ESPRXRTSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_esp_rxrt, container, false);
        espRXRTListTable = (TableLayout) view.findViewById(R.id.esp_rxrt_list_tb);
        addCustomBtn = (Button) view.findViewById(R.id.esp_rxrt_add_custom_btn);
        loadActuatorsBtn = (Button) view.findViewById(R.id.esp_rxrt_load_actuators_btn);
        loadSensorsBtn = (Button) view.findViewById(R.id.esp_rxrt_load_sensors_btn);
        addCustomBtn = (Button) view.findViewById(R.id.esp_rxrt_add_custom_btn);

        tcpBtn = (Button) view.findViewById(R.id.esp_rxrt_tcp_btn);
        udpBtn = (Button) view.findViewById(R.id.esp_rxrt_udp_btn);
        tcpBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.tr_active));

        esprxrtViewModel = new ViewModelProvider( requireActivity()).get(ESPRXRTViewModel.class);
        esprxrtViewModel.getAllESPRXRTs().observe(getViewLifecycleOwner(), data -> {
            esprxrts.clear();
            esprxrts.addAll(data);
            displayESPRXRTTable();
        });

        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), data -> {
           sensorActuators.clear();
           sensorActuators.addAll(data);
        });

        loadSensorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SensorActuator> sensors = sensorActuators.stream()
                    .filter(s -> s.getSensorOrActuator() == 0)
                    .collect(Collectors.toList());
                List<ESPRXRT> filterTargets = new ArrayList<>();
                filterTargets.addAll(esprxrts);             // list of displaying sensors
                filterTargets.addAll(sensorActuatorsToDisplay);
                for (SensorActuator sa: sensors) {
                    // filter the newly added sensors among whole sensors data
                    List<ESPRXRT> matches = filterTargets.stream()
                            .filter(e -> Objects.equals(e.getSensorActuatorId(), sa.getId()))
                            .collect(Collectors.toList());
                    if (!matches.isEmpty()) continue;
                    ESPRXRT esprxrt = initESPRXRTFromSensorActuator(sa);
                    sensorActuatorsToDisplay.add(esprxrt);
                }
                displayESPRXRTTable();
            }
        });

        loadActuatorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SensorActuator> sensors = sensorActuators.stream()
                    .filter(s -> s.getSensorOrActuator() == 1)
                    .collect(Collectors.toList());
                List<ESPRXRT> filterTargets = new ArrayList<>();
                filterTargets.addAll(esprxrts);             // list of displaying sensors
                filterTargets.addAll(sensorActuatorsToDisplay);
                for (SensorActuator sa: sensors) {
                    // filter the newly added sensors among whole sensors data
                    List<ESPRXRT> matches = filterTargets.stream()
                            .filter(e -> Objects.equals(e.getSensorActuatorId(), sa.getId()))
                            .collect(Collectors.toList());
                    if (!matches.isEmpty()) continue;
                    ESPRXRT esprxrt = initESPRXRTFromSensorActuator(sa);
                    sensorActuatorsToDisplay.add(esprxrt);
                }
                displayESPRXRTTable();
            }
        });

        tcpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tcpBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.tr_active));
                tcpBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button));
                udpBtn.setBackgroundColor(Color.TRANSPARENT);
                udpBtn.setBackgroundTintList(null);
                udpBtn.setBackgroundColor(Color.TRANSPARENT);

            }
        });

        udpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                udpBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.tr_active));
                udpBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button));
                tcpBtn.setBackgroundColor(Color.TRANSPARENT);
                tcpBtn.setBackgroundTintList(null);
                tcpBtn.setBackgroundColor(Color.TRANSPARENT);

            }
        });

        addCustomBtn.setOnClickListener(v -> handleClickAddCustomBtn());


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        thresholdEditDialog = inflater.inflate(R.layout.esp_rxrt_threshold_dialog, null);
        thresholdEditTable = (TableLayout) thresholdEditDialog.findViewById(R.id.esp_rxrt_thresholds_edit_tb);
        addCustomDialog = inflater.inflate(R.layout.esp_rxrt_add_custom_dialog, null);

        variableNameEdit = (EditText) addCustomDialog.findViewById(R.id.esp_rx_rt_variable_name_edit);
        dataTypeSpinner = (Spinner) addCustomDialog.findViewById(R.id.esp_rx_rt_data_type_spinner);
        numberOfChannelsEdit = (EditText) addCustomDialog.findViewById(R.id.esp_rx_rt_channel_count_edit);

        builder = new AlertDialog.Builder(requireContext());

        thresholdSaveBtn = (Button) thresholdEditDialog.findViewById(R.id.esp_rxrt_save_thresholds_btn);
        thresholdSaveBtn.setOnClickListener(v -> handleClickThresholdSaveBtn());
        thresholdDialogCloseBtn = (Button) thresholdEditDialog.findViewById(R.id.esp_rxrt_close_thresholds_dialog_btn);
        thresholdDialogCloseBtn.setOnClickListener(v -> dialog.dismiss());
        saveBtn = (Button) addCustomDialog.findViewById(R.id.esp_rxrt_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        closeAddCustomDialogBtn = (Button) addCustomDialog.findViewById(R.id.esp_rxrt_close_add_custom_dialog_btn);
        closeAddCustomDialogBtn.setOnClickListener(v -> dialog.dismiss());

        dataTypeSpinner = (Spinner) addCustomDialog.findViewById(R.id.esp_rx_rt_data_type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Constants.DATA_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataTypeSpinner.setAdapter((adapter));


    }

    public void displayESPRXRTTable() {
        espRXRTListTable.removeViews(1, espRXRTListTable.getChildCount() - 1);
        List<ESPRXRT> wholeData = new ArrayList<>();
        wholeData.addAll(esprxrts);
        wholeData.addAll(sensorActuatorsToDisplay);
        int cnt = wholeData.size();
        int index = 1;
        for (int i = 0; i < cnt; i ++) {
            ESPRXRT esprxrt = wholeData.get(i);
            if (esprxrt.getDeleted() == 0) {
                addESPRXRTTableRow(esprxrt, index);
                index ++;
            }
        }
    }

    public void addESPRXRTTableRow(ESPRXRT data, int order) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                50,
                80
        );
        TableRow tableRow = new TableRow(requireContext());

        TextView orderText = new TextView(requireContext());
        orderText.setText(String.valueOf(order));
        orderText.setGravity(Gravity.CENTER);
        TextView nameText = new TextView(requireContext());
        nameText.setText(data.getVariableName());
        nameText.setGravity(Gravity.CENTER);
        TextView numberOfChannelsText = new TextView(requireContext());
        numberOfChannelsText.setText(String.valueOf(data.getNumberOfChannels()));
        numberOfChannelsText.setGravity(Gravity.CENTER);
        TextView dataTypeText = new TextView(requireContext());
        dataTypeText.setText(data.getDataType());
        dataTypeText.setGravity(Gravity.CENTER);

        TextView nBytesText = new TextView(requireContext());
        nBytesText.setText(String.valueOf(getNumberOfBytesFromDataTypeString(data.getDataType())));
        nBytesText.setGravity(Gravity.CENTER);

        Button thresholdsBtn = new Button(requireContext());
        thresholdsBtn.setGravity(Gravity.CENTER);
        thresholdsBtn.setLayoutParams(params);
        thresholdsBtn.setTag((Long) data.getSensorActuatorId());
        thresholdsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                currentESPRXRTSaId = ((Integer)v.getTag()).longValue();
                currentESPRXRTSaId = (long) v.getTag();
                View parent = v;
                while (!(parent instanceof TableRow) && parent != null) {
                    parent = (View) parent.getParent();
                }

                if (parent instanceof TableRow) {
                    TableRow row = (TableRow) parent;
                    TableLayout tableLayout = (TableLayout) row.getParent();

                    // Get index of this TableRow in the TableLayout
                    int rowIndex = tableLayout.indexOfChild(row);
                    currentTableIndex = rowIndex;
                    Log.d("RowIndex", "Row index: " + rowIndex);

                    // Optionally store or use it
                    // currentRowIndex = rowIndex;
                } else {
                    Log.e("RowIndex", "TableRow not found in parent hierarchy");
                }
                ESPRXRTSetting.this.openThresholdDialog(data);
            }
        });
        thresholdsBtn.setTextSize(10);
        thresholdsBtn.setText("Thresholds");
        thresholdsBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bs_info));
        tableRow.addView(orderText);
        tableRow.addView(nameText);
        tableRow.addView(dataTypeText);
        tableRow.addView(numberOfChannelsText);
        tableRow.addView(nBytesText);
        tableRow.addView(thresholdsBtn);

        List<ImageButton> operationalButtons = setupOperationalButtons(data.getId(), requireContext());

        ImageButton changeValueBtn = operationalButtons.get(0);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickChangeValueBtn(data.getSensorActuatorId());
            }
        });
        tableRow.addView(changeValueBtn);
        ImageButton changeOrderBtn = operationalButtons.get(1);
        tableRow.addView(changeOrderBtn);
        ImageButton deleteBtn = operationalButtons.get(2);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickDeleteBtn(data.getSensorActuatorId(), data.getId());
            }
        });
        tableRow.addView(deleteBtn);
        espRXRTListTable.addView(tableRow);
    }

    public void openThresholdDialog(ESPRXRT data) {
        if (thresholdEditDialog.getParent() != null) {
            ((ViewGroup) thresholdEditDialog.getParent()).removeView(thresholdEditDialog);
        }
        builder.setView(thresholdEditDialog);
        dialog = builder.create();
        dialog.show();
        int cnt = data.getNumberOfChannels();
        List<ESPRXRTThreshold> thresholds = new ArrayList<>();
        thresholdEditTable.removeViews(1, thresholdEditTable.getChildCount() - 1);
        for (int i = 0; i < cnt; i ++) {

            TableRow row = new TableRow(requireContext());
            TextView orderView = new TextView(requireContext());
            orderView.setText(String.valueOf(i + 1));
            orderView.setGravity(Gravity.CENTER);
            row.addView(orderView);

            EditText initialValueEdit = new EditText(requireContext());
            initialValueEdit.setGravity(Gravity.CENTER);
            row.addView(initialValueEdit);

            EditText upperLimitEdit = new EditText(requireContext());
            upperLimitEdit.setGravity(Gravity.CENTER);
            row.addView(upperLimitEdit);

            EditText lowerLimitEdit = new EditText(requireContext());
            lowerLimitEdit.setGravity(Gravity.CENTER);
            row.addView(lowerLimitEdit);

            CheckBox activateCheckbox = new CheckBox(requireContext());
            activateCheckbox.setGravity(Gravity.END);
            row.addView(activateCheckbox);

            try {
                int finalI = i;
                List<ESPRXRTThreshold> filtered = data.getThresholds().stream()
                        .filter(o -> o.getOrder() == finalI + 1)
                        .collect(Collectors.toList());
                initialValueEdit.setText(String.valueOf(filtered.get(0).getInitialValue()));
                upperLimitEdit.setText(String.valueOf(filtered.get(0).getUpperLimit()));
                lowerLimitEdit.setText(String.valueOf(filtered.get(0).getLowerLimit()));
                activateCheckbox.setChecked(filtered.get(0).getActive() == 1);
            } catch(IndexOutOfBoundsException e) {
                Log.e("Error", "can't get outlier.");
            }
            thresholdEditTable.addView(row);
        }
    }

    public ESPRXRT initEmptyESPRXRT() {
        List<ESPRXRTThreshold> thresholds = new ArrayList<>();
        Long currentTime = System.currentTimeMillis();
        ESPRXRT esprxrt = new ESPRXRT((long) -1, -1, "", "", -1, thresholds, -1, currentTime, currentTime);
        return esprxrt;
    }

    public ESPRXRT initESPRXRTFromSensorActuator(SensorActuator sensorActuator) {

        ESPRXRT esprxrt = initEmptyESPRXRT();
        esprxrt.setSensorActuatorId(sensorActuator.getId());
        esprxrt.setSensorOrActuator(sensorActuator.getSensorOrActuator());
        esprxrt.setVariableName(sensorActuator.getVariableName());
        esprxrt.setNumberOfChannels(sensorActuator.getNumberOfChannels());
        esprxrt.setDeleted(0);
        long currentTime = System.currentTimeMillis();
        esprxrt.setCreated_at(currentTime);
        esprxrt.setUpdated_at(currentTime);
        return esprxrt;
    }

    public void handleClickThresholdSaveBtn() {
        List<ESPRXRT> filterTargets = new ArrayList<>();
        filterTargets.addAll(esprxrts);
        filterTargets.addAll(sensorActuatorsToDisplay);
        List<ESPRXRT> currentFiltered = null;
        ESPRXRT currentESPRXRT = initEmptyESPRXRT();
        if (currentESPRXRTSaId > 0) {
            currentFiltered = filterTargets.stream()
                    .filter(e -> Objects.equals(e.getSensorActuatorId(), currentESPRXRTSaId))
                    .collect(Collectors.toList());
            if (currentFiltered.isEmpty()) return;
            currentESPRXRT = currentFiltered.get(0);
        } else if(currentTableIndex > 0) {
            View v = espRXRTListTable.getChildAt(currentTableIndex);
            if (v instanceof TableRow) {
                TableRow row = (TableRow) v;
                TextView varNameEdit = (TextView) row.getChildAt(1);
                TextView dataTypeEdit = (TextView) row.getChildAt(2);
                TextView channelCountEdit = (TextView) row.getChildAt(3);
                currentESPRXRT.setVariableName(varNameEdit.getText().toString());
                currentESPRXRT.setDataType(dataTypeEdit.getText().toString());
                currentESPRXRT.setNumberOfChannels(Integer.parseInt(channelCountEdit.getText().toString()));
                currentESPRXRT.setDeleted(0);
            }
        }

        int cnt = thresholdEditTable.getChildCount();
        List<ESPRXRTThreshold> thresholds = new ArrayList<>();
        for (int i = 1; i < cnt; i++) {
            View rowView = thresholdEditTable.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow tableRow = (TableRow) rowView;
                EditText initialValueText = (EditText) tableRow.getChildAt(1);
                int initialValue = Integer.parseInt(initialValueText.getText().toString());
                EditText upperLimitText = (EditText) tableRow.getChildAt(2);
                int upperLimit = Integer.parseInt(upperLimitText.getText().toString());
                EditText lowerLimitText = (EditText) tableRow.getChildAt(3);
                int lowerLimit = Integer.parseInt(lowerLimitText.getText().toString());

                CheckBox activateCheckBox = (CheckBox) tableRow.getChildAt(4);
                int activate = activateCheckBox.isChecked() ? 1 : 0;
                ESPRXRTThreshold threshold = new ESPRXRTThreshold(i, initialValue, upperLimit, lowerLimit, activate);
                thresholds.add(threshold);
            }
        }
        currentESPRXRT.setThresholds(thresholds);
        ESPRXRT finalCurrentESPRXRT = currentESPRXRT;
        if (currentESPRXRT.getId() != null && currentESPRXRT.getId() > 0) {
            esprxrtViewModel.update(currentESPRXRT);
            esprxrtViewModel.getUpdateResult().observe(getViewLifecycleOwner(), id -> {
                if (id != null && id > 0) {
                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    sensorActuatorsToDisplay = sensorActuatorsToDisplay.stream()
                            .filter(sa -> !Objects.equals(sa.getSensorActuatorId(), finalCurrentESPRXRT.getSensorActuatorId()))
                            .collect(Collectors.toList());
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Update failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            esprxrtViewModel.insert(currentESPRXRT);
            esprxrtViewModel.getInsertResult().observe(getViewLifecycleOwner(), id -> {
                if (id != null && id > 0) {
                    Toast.makeText(getContext(), "Insert success!", Toast.LENGTH_SHORT).show();
                    sensorActuatorsToDisplay = sensorActuatorsToDisplay.stream()
                            .filter(sa -> !Objects.equals(sa.getSensorActuatorId(), finalCurrentESPRXRT.getSensorActuatorId()))
                            .collect(Collectors.toList());
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Insert failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void handleClickChangeValueBtn(long saId) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<SensorActuator> filtered = sensorActuators.stream()
                .filter(sa -> Objects.equals(sa.getId(), saId))
                .collect(Collectors.toList());
        Bundle bundle = new Bundle();
        bundle.putLong("id", saId);
        if (filtered.isEmpty()) return;
        else {
            int sensorOrActuator = filtered.get(0).getSensorOrActuator();
            if (sensorOrActuator == 0) {
                SensorSetting sensorSetting = new SensorSetting();
                sensorSetting.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, sensorSetting);
            }
            else if (sensorOrActuator == 1) {
                ActuatorSetting actuatorSetting = new ActuatorSetting();
                actuatorSetting.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment_container, actuatorSetting);
            }
        }
        fragmentTransaction.commit();
    }

    public void handleClickDeleteBtn(long saId, long espId) {
        new AlertDialog.Builder(requireContext())
        .setTitle("Confirm")
        .setMessage("Are you sure you want to delete?")
        .setPositiveButton("Yes", (dialog, which) -> {
            // Handle Yes button click
            esprxrtViewModel.softDelete(espId);
            esprxrtViewModel.getSoftDeleteResult().observe(getViewLifecycleOwner(), res -> {
                if (Objects.equals(res, espId)) {
                    Toast.makeText(requireContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
            });
        })
        .setNegativeButton("No", (dialog, which) -> {
            // Handle No button click (optional)
            dialog.dismiss();
        })
        .show();
    }

    public void handleClickAddCustomBtn() {
        if (addCustomDialog.getParent() != null) {
            ((ViewGroup) addCustomDialog.getParent()).removeView(addCustomDialog);
        }
        builder.setView(addCustomDialog);
        dialog = builder.create();
        dialog.show();
    }

    public void handleClickSaveBtn() {
        String variableName = variableNameEdit.getText().toString();
        String dataType = dataTypeSpinner.getSelectedItem().toString();
        int numberOfChannels = Integer.parseInt(numberOfChannelsEdit.getText().toString());
        ESPRXRT esprxrt = initEmptyESPRXRT();
        esprxrt.setVariableName(variableName);
        esprxrt.setDeleted(0);
        esprxrt.setDataType(dataType);
        esprxrt.setNumberOfChannels(numberOfChannels);
        sensorActuatorsToDisplay.add(esprxrt);
        displayESPRXRTTable();
        dialog.dismiss();
        initInputUis();
    }

    public void initInputUis() {
        variableNameEdit.setText("");
        numberOfChannelsEdit.setText("");
        dataTypeSpinner.setSelection(1, true);
    }
}

