package com.example.myapplication.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
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
import com.example.myapplication.db.entity.SensorActuator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActuatorSetting extends Fragment {

    private Spinner spinner;
    private EditText variableNameEdit;
    private EditText numberOfChannelsEdit;
    private CheckBox monitoringCheckbox;
    private CheckBox realTimeControlCheckbox;
    private Button addButton;
    private TableLayout actuatorListTable;
    private List<SensorActuator> actuators = new ArrayList<>();
    private int selectedSetting = -1;


    public ActuatorSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actuator, container, false);
        spinner = (Spinner) view.findViewById(R.id.data_type_spinner);
        variableNameEdit = (EditText) view.findViewById(R.id.variable_name_input);
        numberOfChannelsEdit = (EditText) view.findViewById(R.id.number_of_channels_input);
        monitoringCheckbox = (CheckBox) view.findViewById(R.id.monitoring_esp_tx_checkbox);
        realTimeControlCheckbox = (CheckBox) view.findViewById(R.id.real_time_control_checkbox);
        addButton = (Button) view.findViewById(R.id.actuator_add_btn);
        actuatorListTable = (TableLayout) view.findViewById(R.id.actuator_list_tb);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String variableName = variableNameEdit.getText().toString();
                String dataType = spinner.getSelectedItem().toString();
                String numberOfChannels = numberOfChannelsEdit.getText().toString();
                Integer monitoring = monitoringCheckbox.isChecked() ? 1 : 0;
                Integer realTimeControl = realTimeControlCheckbox.isChecked() ? 1 : 0;
                SensorActuator actuator = new SensorActuator(variableName, dataType, Integer.parseInt(numberOfChannels), monitoring, realTimeControl);
                ActuatorSetting.this.addTableRow(actuator, selectedSetting);
                ActuatorSetting.this.initEditControls();
                if (selectedSetting == -1) actuators.add(actuator);
                else {
                    actuators = actuators.stream()
                            .map(s -> Objects.equals(s.getVariableName(), actuator.getVariableName()) ? actuator : s)
                            .collect(Collectors.toList());
                    actuatorListTable.removeViewAt(selectedSetting + 1);
                }
                selectedSetting = -1;
            }
        });

        String[] options = { "uint8", "int8", "uint16", "int16", "uint24", "int24", "uint32", "int32", "float", "double"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter((adapter));



        return view;
    }

    public void addTableRow(SensorActuator data, int pos) {
        int order = pos == -1 ? actuatorListTable.getChildCount() : pos;

        TableRow tableRow = new TableRow(requireContext());
        tableRow.setVerticalGravity(Gravity.CENTER);
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
        tableRow.addView(monitoringText);
        tableRow.addView(realTimeControlText);

        ImageButton iconButton = new ImageButton(requireContext());
        iconButton.setImageResource(R.drawable.baseline_edit_24); // your drawable icon
        iconButton.setBackgroundColor(Color.TRANSPARENT); // optional styling

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                100,
                80
        );
        ImageButton changeValueBtn = new ImageButton(requireContext());
        changeValueBtn.setImageResource(R.drawable.baseline_edit_24);
        changeValueBtn.setBackgroundColor(Color.TRANSPARENT);
        changeValueBtn.setColorFilter(Color.parseColor("#198754"));
        changeValueBtn.setLayoutParams(params);
        changeValueBtn.setTag(order);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                selectedSetting = index;
                SensorActuator selectedActuator = actuators.get(index - 1);
                variableNameEdit.setText(selectedActuator.getVariableName());
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                int position = adapter.getPosition(selectedActuator.getDataType());
                spinner.setSelection(position);
                numberOfChannelsEdit.setText(String.valueOf(selectedActuator.getNumberOfChannels()));
                monitoringCheckbox.setChecked(selectedActuator.getMonitoring() == 1);
                realTimeControlCheckbox.setChecked(selectedActuator.getRealTimeControl() == 1);
            }
        });
        LinearLayout btnLayout = new LinearLayout(requireContext());
        btnLayout.setGravity(Gravity.CENTER);
        btnLayout.addView(changeValueBtn);

        ImageButton changeOrderBtn = new ImageButton(requireContext());
        changeOrderBtn.setImageResource(R.drawable.baseline_bar_chart_24);
        changeOrderBtn.setBackgroundColor(Color.TRANSPARENT);
        changeOrderBtn.setColorFilter(Color.parseColor("#0dcaf0"));
        btnLayout.addView(changeOrderBtn);

        ImageButton deleteBtn = new ImageButton(requireContext());
        deleteBtn.setImageResource(R.drawable.baseline_delete_24);
        deleteBtn.setBackgroundColor(Color.TRANSPARENT);
        deleteBtn.setColorFilter(Color.parseColor("#dc3545"));
        deleteBtn.setTag(order);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to proceed?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Handle Yes button click
                            View parentRow = (View) v.getParent();  // Get the TableRow (parent of Button)
                            TableLayout tableLayout = (TableLayout) parentRow.getParent();  // TableLayout is parent of TableRow

                            int rowIndex = tableLayout.indexOfChild(parentRow);
                            actuatorListTable.removeViewAt(rowIndex);
                            Toast.makeText(requireContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Handle No button click (optional)
                            dialog.dismiss();
                        })
                        .show();
            }
        });
        btnLayout.addView(deleteBtn);
        tableRow.addView(btnLayout);
        if(pos > -1) actuatorListTable.addView(tableRow, pos);
        else actuatorListTable.addView(tableRow);
    }

    public void initEditControls() {
        variableNameEdit.setText("");
        spinner.setSelection(1, true);
        numberOfChannelsEdit.setText("");
        monitoringCheckbox.setChecked(false);
        realTimeControlCheckbox.setChecked(false);
    }
}
