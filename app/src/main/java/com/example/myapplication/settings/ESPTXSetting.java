package com.example.myapplication.settings;

import static com.example.myapplication.utils.commonUtils.getNumberOfBytesFromDataTypeString;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.entity.SensorActuator;

import java.util.ArrayList;
import java.util.List;

public class ESPTXSetting extends Fragment {

    private TableLayout listTable;
    private TableLayout outlierEditTable;
    private Button outlierDialogCloseBtn;
    private Button outlierSaveBtn;

    public ESPTXSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_esp_tx, container, false);
        listTable = (TableLayout) view.findViewById(R.id.esp_tx_list_tb);


        List<SensorActuator> actuators = new ArrayList<>();
        this.loadActuators(actuators);
        this.loadSensors(new ArrayList<>());
        return view;
    }

    public void loadSensors(List<SensorActuator> dtos) {
        for (SensorActuator sd: dtos) {

        }
    }

    public void loadActuators(List<SensorActuator> dtos) {
        for (SensorActuator ad: dtos) {
            this.addTableRow(ad);
        }
    }

    public void addTableRow(SensorActuator data) {
        int order = listTable.getChildCount();
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

        Button outliersBtn = new Button(requireContext());
        outliersBtn.setGravity(Gravity.CENTER);
        outliersBtn.setLayoutParams(params);
        outliersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ESPTXSetting.this.openThresholdDialog(data.getNumberOfChannels());
            }
        });
        outliersBtn.setText("Outliers");
        outliersBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bs_info));

        tableRow.addView(orderText);
        tableRow.addView(nameText);
        tableRow.addView(dataTypeText);
        tableRow.addView(numberOfChannelsText);
        tableRow.addView(nBytesText);
        tableRow.addView(outliersBtn);

        Button changeValueBtn = new Button(requireContext());
        changeValueBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_edit_24, 0, 0, 0);
        changeValueBtn.setLayoutParams(params);
        changeValueBtn.setTag(order);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        tableRow.addView(changeValueBtn);
        Button changeOrderBtn = new Button(requireContext());
        changeOrderBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_bar_chart_24, 0, 0, 0);
        changeOrderBtn.setLayoutParams(params);
        tableRow.addView(changeOrderBtn);
        Button deleteBtn = new Button(requireContext());
        deleteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_delete_24, 0, 0, 0);
        deleteBtn.setLayoutParams(params);
        deleteBtn.setTag(order);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        tableRow.addView(deleteBtn);

        listTable.addView(tableRow);
    }

    public void openThresholdDialog(int cnt) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.esp_tx_outliers_dialog, null);
        outlierEditTable = (TableLayout) dialogView.findViewById(R.id.esp_tx_outliers_edit_tb);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        for (int i = 0; i < cnt; i ++) {

            TableRow row = new TableRow(requireContext());
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

            EditText activateEdit = new EditText(requireContext());
            activateEdit.setGravity(Gravity.CENTER);
            row.addView(activateEdit);

            outlierEditTable.addView(row);
        }


        outlierDialogCloseBtn = dialogView.findViewById(R.id.esp_tx_close_outliers_modal_btn);
        outlierDialogCloseBtn.setOnClickListener(v -> dialog.dismiss());

        // Save button action
        outlierSaveBtn = dialogView.findViewById(R.id.esp_tx_save_outliers_btn);
        outlierSaveBtn.setOnClickListener(v -> {
            ESPTXSetting.this.saveOutliers();
            Toast.makeText(requireContext(), "Data Saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Optionally close after saving
        });
    }

    public int saveOutliers() {
        return 0;
    }
}
