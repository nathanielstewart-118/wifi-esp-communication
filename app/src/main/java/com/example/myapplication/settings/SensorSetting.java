package com.example.myapplication.settings;

import static com.example.myapplication.utils.CommonUtils.getNumberOfBytesFromDataTypeString;
import static com.example.myapplication.utils.UIUtils.setupOperationalButtons;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SensorSetting extends Fragment {
    private Spinner spinner;
    private EditText variableNameEdit;
    private EditText numberOfChannelsEdit;
    private CheckBox monitoringCheckbox;
    private CheckBox realTimeControlCheckbox;
    private Button addButton;
    private TableLayout sensorListTable;
    private final List<SensorActuator> sensors = new ArrayList<>();
    private int selectedSetting = -1;
    private SensorActuatorViewModel sensorActuatorViewModel;
    private AppDatabase db;

    private Boolean delegated = false;
    private Long delegatedId = (long) -1;

    public SensorSetting() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = AppDatabase.getInstance(requireContext());
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        sensorActuatorViewModel = new ViewModelProvider(
                requireActivity()
        ).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensors().observe(getViewLifecycleOwner(), data -> {
            this.displayTable(data);
            sensors.addAll(data);
            if (delegated) {
                editSensor(delegatedId);
                delegated = false;
            }
        });
        spinner = (Spinner) view.findViewById(R.id.data_type_spinner);
        variableNameEdit = (EditText) view.findViewById(R.id.variable_name_input);
        numberOfChannelsEdit = (EditText) view.findViewById(R.id.number_of_channels_input);
        monitoringCheckbox = (CheckBox) view.findViewById(R.id.monitoring_esp_tx_checkbox);
        realTimeControlCheckbox = (CheckBox) view.findViewById(R.id.real_time_control_checkbox);
        addButton = (Button) view.findViewById(R.id.sensor_add_btn);
        sensorListTable = (TableLayout) view.findViewById(R.id.sensor_list_tb);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String variableName = variableNameEdit.getText().toString();
                String dataType = spinner.getSelectedItem().toString();
                String numberOfChannels = numberOfChannelsEdit.getText().toString();
                Integer monitoring = monitoringCheckbox.isChecked() ? 1 : 0;
                Integer realTimeControl = realTimeControlCheckbox.isChecked() ? 1 : 0;

                SensorActuator sensor = new SensorActuator(variableName, 0, dataType, Integer.parseInt(numberOfChannels), monitoring, realTimeControl);
                if (selectedSetting == -1) {
                    sensorActuatorViewModel.insert(sensor);
                    sensorActuatorViewModel.getInsertResult().observe(getViewLifecycleOwner(), id -> {
                        if (id != null && id > 0) {
                            Toast.makeText(getContext(), R.string.insert_success, Toast.LENGTH_SHORT).show();
                            sensor.setId(id);
                        } else {
                            Toast.makeText(getContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else {
                    sensor.setId((long)selectedSetting);
                    sensorActuatorViewModel.update(sensor);
                    sensorActuatorViewModel.getUpdateResult().observe(getViewLifecycleOwner(), res -> {
                        if(res != null && res != 0) {
                            Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                SensorSetting.this.initEditControls();
                selectedSetting = -1;
            }
        });

        String[] options = { "uint8", "int8", "uint16", "int16", "uint24", "int24", "uint32", "int32", "float", "double"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter((adapter));



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            delegated = true;
            delegatedId = args.getLong("id");
        }
    }

    public void addTableRow(SensorActuator data, int index) {
        TableRow tableRow = new TableRow(requireContext());
        tableRow.setTag(data.getId());
        tableRow.setVerticalGravity(Gravity.CENTER);
        TextView orderText = new TextView(requireContext());
        orderText.setText(String.valueOf(index));
        orderText.setGravity(Gravity.CENTER);
        TextView nameText = new TextView(requireContext());
        nameText.setText(data.getVariableName());
        nameText.setGravity(Gravity.CENTER);
        TextView numberOfChannelsText = new TextView(requireContext());
        numberOfChannelsText.setText(String.valueOf(data.getNumberOfChannels()));
        numberOfChannelsText.setGravity(Gravity.CENTER);

        TextView numberOfBytesText = new TextView(requireContext());
        numberOfBytesText.setText(String.valueOf(getNumberOfBytesFromDataTypeString(data.getDataType()) * data.getNumberOfChannels()));
        numberOfBytesText.setGravity(Gravity.CENTER);

        TextView dataTypeText = new TextView(requireContext());
        dataTypeText.setText(data.getDataType());
        dataTypeText.setGravity(Gravity.CENTER);
        TextView monitoringText = new TextView(requireContext());
        monitoringText.setText(data.getMonitoring() == 1 ? "Yes": "No");
        monitoringText.setGravity(Gravity.CENTER);
        TextView realTimeControlText = new TextView(requireContext());
        realTimeControlText.setText(data.getRealTimeControl() == 1 ? "Yes" : "No");
        realTimeControlText.setGravity(Gravity.CENTER);

        tableRow.addView(orderText);
        tableRow.addView(nameText);
        tableRow.addView(dataTypeText);
        tableRow.addView(numberOfChannelsText);
        tableRow.addView(numberOfBytesText);
        tableRow.addView(monitoringText);
        tableRow.addView(realTimeControlText);

        List<ImageButton> operationalButtons = setupOperationalButtons(data.getId(), requireContext());

        ImageButton changeValueBtn = operationalButtons.get(0);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long index = (Long) v.getTag();
                selectedSetting = index.intValue();

                List<SensorActuator> selectedSensors = sensors.stream()
                                .filter(s -> Objects.equals(s.getId(), index))
                                .collect(Collectors.toList());
                if (selectedSensors.isEmpty()) {
                 return;
                }
                variableNameEdit.setText(selectedSensors.get(0).getVariableName());
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                int position = adapter.getPosition(selectedSensors.get(0).getDataType());
                spinner.setSelection(position);
                numberOfChannelsEdit.setText(String.valueOf(selectedSensors.get(0).getNumberOfChannels()));
                monitoringCheckbox.setChecked(selectedSensors.get(0).getMonitoring() == 1);
                realTimeControlCheckbox.setChecked(selectedSensors.get(0).getRealTimeControl() == 1);
            }
        });

        ImageButton changeOrderBtn = operationalButtons.get(1);
        changeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton deleteBtn = operationalButtons.get(2);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to proceed?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Handle Yes button click
                        Long id = (Long) v.getTag();
                        List<SensorActuator> selectedSensorActuators = sensors.stream()
                                        .filter(sa -> Objects.equals(sa.getId(), id))
                                        .collect(Collectors.toList());
                        if (selectedSensorActuators.isEmpty()) {
                            return;
                        }
                        sensorActuatorViewModel.delete(selectedSensorActuators.get(0));
                        sensorActuatorViewModel.getDeleteResult().observe(getViewLifecycleOwner(), res -> {
                            if (res != null && res > 0) {
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

        LinearLayout btnLayout = new LinearLayout(requireContext());
        btnLayout.setGravity(Gravity.CENTER);
        btnLayout.addView(changeValueBtn);
        btnLayout.addView(changeOrderBtn);
        btnLayout.addView(deleteBtn);

        tableRow.addView(btnLayout);
        sensorListTable.addView(tableRow);
    }

    public void initEditControls() {
        variableNameEdit.setText("");
        spinner.setSelection(1, true);
        numberOfChannelsEdit.setText("");
        monitoringCheckbox.setChecked(false);
        realTimeControlCheckbox.setChecked(false);
    }

    public void displayTable(List<SensorActuator> sas) {
        Log.d("displayTable", "This is inside displayTable");
        sensorListTable.removeViews(1, sensorListTable.getChildCount() - 1);
        for (int i = 0; i < sas.size(); i ++) {
            addTableRow(sas.get(i), i + 1);
        }
    }

    public void editSensor(Long sensorId) {
        int cnt = sensorListTable.getChildCount();
        for (int i = 1; i < cnt; i ++) {
            View rowView = sensorListTable.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow tableRow = (TableRow) rowView;
                Long tagId = (long) tableRow.getTag();
                if(!Objects.equals(tagId, sensorId)) continue;
                View layoutView = tableRow.getChildAt(7);
                if (layoutView instanceof LinearLayout) {
                    LinearLayout linearLayout = (LinearLayout) layoutView;
                    ImageButton editBtn = (ImageButton) linearLayout.getChildAt(0);
                    editBtn.performClick();
                }
            }
        }
    }
}
