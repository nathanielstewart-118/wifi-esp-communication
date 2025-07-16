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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.myapplication.db.entity.ESPTX;
import com.example.myapplication.db.entity.ESPTXOutlier;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.viewmodel.ESPTXViewModel;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ESPTXSetting extends Fragment {

    private View outlierEditDialog;
    private Dialog dialog;
    private TableLayout espTXListTable;
    private TableLayout espOutliersListTable;
    private TableLayout outlierEditTable;
    private Button outlierDialogCloseBtn;
    private Button outlierSaveBtn;
    private Button loadSensorsBtn;
    private Button loadActuatorsBtn;
    private Button tcpBtn;
    private Button udpBtn;
    private Switch crcSwitch;


    private ESPTXViewModel espTXViewModel;
    private SensorActuatorViewModel sensorActuatorViewModel;


    List<ESPTX> esptxes = new ArrayList<>();
    List<SensorActuator> sensorsAndActuators = new ArrayList<>();
    List<ESPTX> sensorActuatorsToDisplay = new ArrayList<>();
    List<ESPTX> esptxesAndSensorsAndActuators = new ArrayList<>() ;
    ESPTX currentESPTX = new ESPTX((long)-1, null, 0, (long) 0, (long) 0);
    private final Gson gson = new Gson();

    public ESPTXSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_esp_tx, container, false);
        espTXListTable = (TableLayout) view.findViewById(R.id.esp_tx_list_tb);
        espTXViewModel = new ViewModelProvider(
                requireActivity()
        ).get(ESPTXViewModel.class);
        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), data -> {
            sensorsAndActuators.clear();
            sensorsAndActuators.addAll(data);
        });

        espTXViewModel.getAllESPTXes().observe(getViewLifecycleOwner(), data -> {
            esptxes.clear();
            esptxes.addAll(data);
            displayESPTXTable();
        });

        loadSensorsBtn = (Button) view.findViewById(R.id.esp_tx_load_sensors_btn);
        loadSensorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SensorActuator> sensors = sensorsAndActuators.stream()
                        .filter(s -> s.getSensorOrActuator() == 0)
                        .collect(Collectors.toList());
                List<ESPTX> filterTargets = new ArrayList<>();
                filterTargets.addAll(esptxes);                                  // list of displaying sensors
                filterTargets.addAll(sensorActuatorsToDisplay);
                for (SensorActuator sa: sensors) {
                    // filter the newly added sensors among whole sensors data
                    List<ESPTX> matches = filterTargets.stream()
                            .filter(e -> Objects.equals(e.getSensorActuatorId(), sa.getId()) && sa.getSensorOrActuator() == 0)
                            .collect(Collectors.toList());
                    if (!matches.isEmpty()) continue;
                    List<ESPTXOutlier> outliers = new ArrayList<>();
                    Long currentTime = System.currentTimeMillis();
                    ESPTX esptx = new ESPTX(sa.getId(), outliers, 0, currentTime, currentTime);
                    sensorActuatorsToDisplay.add(esptx);
                }
                displayESPTXTable();
            }
        });

        loadActuatorsBtn = (Button) view.findViewById(R.id.esp_tx_load_actuators_btn);
        loadActuatorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SensorActuator> actuators = sensorsAndActuators.stream()
                        .filter(s -> s.getSensorOrActuator() == 1)
                        .collect(Collectors.toList());
                List<ESPTX> filterTargets = new ArrayList<>();
                filterTargets.addAll(esptxes);                                  // list of displaying sensors
                filterTargets.addAll(sensorActuatorsToDisplay);
                for (SensorActuator sa: actuators) {
                    List<ESPTX> matches = filterTargets.stream()
                            .filter(e -> Objects.equals(e.getSensorActuatorId(), sa.getId()) && sa.getSensorOrActuator() == 1)
                            .collect(Collectors.toList());
                    if (!matches.isEmpty()) continue;
                    List<ESPTXOutlier> outliers = new ArrayList<>();
                    Long currentTime = System.currentTimeMillis();
                    ESPTX esptx = new ESPTX(sa.getId(), outliers, 0, currentTime, currentTime);
                    sensorActuatorsToDisplay.add(esptx);
                    displayESPTXTable();
                }
            }
        });



        tcpBtn = (Button) view.findViewById(R.id.esp_tx_tcp_btn);
        udpBtn = (Button) view.findViewById(R.id.esp_tx_udp_btn);
        tcpBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.tr_active));

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        outlierEditDialog = inflater.inflate(R.layout.esp_tx_outliers_dialog, null);
        outlierEditTable = (TableLayout) outlierEditDialog.findViewById(R.id.esp_tx_outliers_edit_tb);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(outlierEditDialog);
        dialog = builder.create();

        outlierSaveBtn = (Button) outlierEditDialog.findViewById(R.id.esp_tx_save_outliers_btn);
        outlierSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cnt = outlierEditTable.getChildCount();
                List<ESPTXOutlier> outliers = new ArrayList<>();
                for (int i = 1; i < cnt; i ++) {
                    View rowView = outlierEditTable.getChildAt(i);
                    if (rowView instanceof TableRow) {
                        TableRow tableRow = (TableRow) rowView;
                        EditText outlierText = (EditText) tableRow.getChildAt(1);
                        Long outlier = Long.valueOf(outlierText.getText().toString());
                        EditText comparisonText = (EditText) tableRow.getChildAt(2);
                        String comparison = comparisonText.getText().toString();
                        CheckBox activateCheckBox = (CheckBox) tableRow.getChildAt(3);
                        Integer activate = activateCheckBox.isChecked() ? 1 : 0;
                        ESPTXOutlier espTXOutlier = new ESPTXOutlier(i, outlier.intValue(), comparison, activate);
                        outliers.add(espTXOutlier);
                    }
                }
                currentESPTX.setOutliers(outliers);
                if(currentESPTX.getId() != null && currentESPTX.getId() > 0) {
                    espTXViewModel.update(currentESPTX);
                    espTXViewModel.getUpdateResult().observe(getViewLifecycleOwner(), id -> {
                        if (id != null && id > 0) {
                            Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                            sensorActuatorsToDisplay = sensorActuatorsToDisplay.stream()
                                    .filter(sa -> !Objects.equals(sa.getSensorActuatorId(), currentESPTX.getSensorActuatorId()))
                                    .collect(Collectors.toList());
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Update failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    espTXViewModel.insert(currentESPTX);
                    espTXViewModel.getInsertResult().observe(getViewLifecycleOwner(), id -> {
                        if (id != null && id > 0) {
                            Toast.makeText(getContext(), "Insert success!", Toast.LENGTH_SHORT).show();
                            sensorActuatorsToDisplay = sensorActuatorsToDisplay.stream()
                                    .filter(sa -> !Objects.equals(sa.getSensorActuatorId(), currentESPTX.getSensorActuatorId()))
                                    .collect(Collectors.toList());
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Insert failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        outlierDialogCloseBtn = (Button) outlierEditDialog.findViewById(R.id.esp_tx_close_outliers_modal_btn);
        outlierDialogCloseBtn.setOnClickListener(v -> dialog.dismiss());


    }
    public void displayESPTXTable() {
        espTXListTable.removeViews(1, espTXListTable.getChildCount() - 1);
        List<ESPTX> wholeData = new ArrayList<>();
        wholeData.addAll(esptxes);
        wholeData.addAll(sensorActuatorsToDisplay);
        int cnt = wholeData.size();
        for (int i = 0; i < cnt; i ++) {
            ESPTX esptx = wholeData.get(i);
            if (esptx.getDeleted() == 0) {
                addESPTXTableRow(esptx, i + 1);
            }
        }
    }

    public void addESPTXTableRow(ESPTX data, int order) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                50,
                80
        );
        List<SensorActuator> correspongingSAs = sensorsAndActuators.stream()
                .filter(s -> Objects.equals(s.getId(), data.getSensorActuatorId()))
                .collect(Collectors.toList());
        Long id = data.getId();
        TableRow tableRow = new TableRow(requireContext());
        tableRow.setTag(id);
        TextView orderText = new TextView(requireContext());
        orderText.setText(String.valueOf(order));
        orderText.setGravity(Gravity.CENTER);
        TextView nameText = new TextView(requireContext());
        nameText.setGravity(Gravity.CENTER);
        TextView numberOfChannelsText = new TextView(requireContext());
        numberOfChannelsText.setGravity(Gravity.CENTER);
        TextView dataTypeText = new TextView(requireContext());
        dataTypeText.setGravity(Gravity.CENTER);

        TextView nBytesText = new TextView(requireContext());
        nBytesText.setGravity(Gravity.CENTER);

        try {
            nameText.setText(correspongingSAs.get(0).getVariableName());
            numberOfChannelsText.setText(String.valueOf(correspongingSAs.get(0).getNumberOfChannels()));
            dataTypeText.setText(correspongingSAs.get(0).getDataType());
            nBytesText.setText(String.valueOf(getNumberOfBytesFromDataTypeString(correspongingSAs.get(0).getDataType())));
        } catch (IndexOutOfBoundsException e) {
            Log.e("Error", e.toString());
        }

        Button outliersBtn = new Button(requireContext());
        outliersBtn.setGravity(Gravity.CENTER);
        outliersBtn.setLayoutParams(params);
        outliersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThresholdDialog(data);
                currentESPTX.setSensorActuatorId(data.getSensorActuatorId());

            }
        });
        outliersBtn.setTag(id);
        outliersBtn.setText("Outliers");
        outliersBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bs_info));

        tableRow.addView(orderText);
        tableRow.addView(nameText);
        tableRow.addView(dataTypeText);
        tableRow.addView(numberOfChannelsText);
        tableRow.addView(nBytesText);
        tableRow.addView(outliersBtn);

        List<ImageButton> operationalButtons = setupOperationalButtons(id, requireContext());

        ImageButton changeValueBtn = operationalButtons.get(0);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                long saId = data.getSensorActuatorId();
                List<SensorActuator> filtered = sensorsAndActuators.stream()
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
        });
        tableRow.addView(changeValueBtn);
        ImageButton changeOrderBtn = operationalButtons.get(1);
        tableRow.addView(changeOrderBtn);
        ImageButton deleteBtn = operationalButtons.get(2);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Handle Yes button click
                        Long id = (Long) v.getTag();
                        List<SensorActuator> selectedSensorActuators = sensorsAndActuators.stream()
                            .filter(sa -> Objects.equals(sa.getId(), id))
                            .collect(Collectors.toList());
                        if (selectedSensorActuators.isEmpty()) {
                            return;
                        }
                        espTXViewModel.softDelete(data.getId());
                        espTXViewModel.getSoftDeleteResult().observe(getViewLifecycleOwner(), res -> {
                            if (Objects.equals(res, data.getId())) {
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
        });

        tableRow.addView(deleteBtn);
        espTXListTable.addView(tableRow);
    }

    public void openThresholdDialog(ESPTX data) {
        List<SensorActuator> sas = sensorsAndActuators.stream()
                .filter(s -> Objects.equals(s.getId(), data.getSensorActuatorId()))
                .collect(Collectors.toList());
        int cnt = 0;
        if (!sas.isEmpty()) cnt = sas.get(0).getNumberOfChannels();
        currentESPTX.setId(data.getId());
        currentESPTX.setSensorActuatorId(data.getSensorActuatorId());
        outlierEditTable = (TableLayout) outlierEditDialog.findViewById(R.id.esp_tx_outliers_edit_tb);
        outlierEditTable.removeViews(1, outlierEditTable.getChildCount() - 1);
        dialog.show();
        List<ESPTXOutlier> outliers = data.getOutliers();
        for (int i = 0; i < cnt; i ++) {

            TableRow row = new TableRow(requireContext());
            row.setTag(data.getSensorActuatorId());
            TextView orderView = new TextView(requireContext());
            orderView.setText(String.valueOf(i + 1));
            orderView.setGravity(Gravity.CENTER);
            row.addView(orderView);

            EditText outlierEdit = new EditText(requireContext());
            outlierEdit.setGravity(Gravity.CENTER);
            row.addView(outlierEdit);

            EditText comparisonEdit = new EditText(requireContext());
            comparisonEdit.setGravity(Gravity.CENTER);
            row.addView(comparisonEdit);

            CheckBox activateCheckbox = new CheckBox(requireContext());
            activateCheckbox.setGravity(Gravity.END);
            row.addView(activateCheckbox);

            try {
                int finalI = i;
                List<ESPTXOutlier> filtered = outliers.stream()
                    .filter(o -> o.getOrder() == finalI + 1)
                    .collect(Collectors.toList());
                outlierEdit.setText(String.valueOf(filtered.get(0).getOutlier()));
                comparisonEdit.setText(filtered.get(0).getComparison());
                activateCheckbox.setChecked(filtered.get(0).getActivate() == 1);
            } catch(IndexOutOfBoundsException e) {
                Log.e("Error", "can't get outlier.");
            }

            outlierEditTable.addView(row);
        }
    }

    public int saveOutliers() {
        return 0;
    }


}
