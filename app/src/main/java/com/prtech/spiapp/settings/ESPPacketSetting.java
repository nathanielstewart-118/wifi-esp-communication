package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.getNumberOfBytesFromDataTypeString;
import static com.prtech.spiapp.utils.CommonUtils.string2Float;
import static com.prtech.spiapp.utils.CommonUtils.string2Int;
import static com.prtech.spiapp.utils.UIUtils.initAutoCompleteWithSuggestionList;
import static com.prtech.spiapp.utils.UIUtils.setSpinnerWithContent;
import static com.prtech.spiapp.utils.UIUtils.setupOperationalButtons;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.prtech.spiapp.R;
import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.ESPThreshold;
import com.prtech.spiapp.db.viewmodel.ESPPacketViewModel;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.DNDHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ESPPacketSetting extends Fragment {
    private EditText variableNameEdit;
    private Spinner dataTypeSpinner;
    private EditText numberOfChannelsEdit;
    private CheckBox monitoringCheckbox;
    private CheckBox realTimeControlCheckbox;
    private Button addButton;
    private Button saveBtn;
    private Button loadBtn;
    private TableLayout espPacketListTable;
    private TableLayout thresholdListTable;
    private final List<ESPPacket> currentESPPackets = new ArrayList<>();
    private ESPPacketViewModel espPacketViewModel;
    private AppDatabase db;
    private AutoCompleteTextView idAutocomplete;
    private Boolean delegated = false;
    private Long delegatedId = (long) -1;
    private int currentTableRowIndex = -1;
    private List<String> allTitles;
    public ESPPacketSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[1]);
        db = AppDatabase.getInstance(requireContext());
        View view = inflater.inflate(R.layout.fragment_esp_packet, container, false);
        espPacketViewModel = new ViewModelProvider(
                requireActivity()
        ).get(ESPPacketViewModel.class);
        espPacketViewModel.getAllSensors().observe(getViewLifecycleOwner(), data -> {
//            this.displayTable(data);
//            sensors.addAll(data);
            if (delegated) {
                editSensor(delegatedId);
                delegated = false;
            }
        });



        espPacketViewModel.getAllTitles().observe(getViewLifecycleOwner(), results -> {
            initAutoCompleteWithSuggestionList(idAutocomplete, results, requireContext());
            allTitles = results;
        });
        dataTypeSpinner = (Spinner) view.findViewById(R.id.data_type_spinner);
        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.sensor_setting_id_autocomplete);
        variableNameEdit = (EditText) view.findViewById(R.id.variable_name_input);
        numberOfChannelsEdit = (EditText) view.findViewById(R.id.number_of_channels_input);
        monitoringCheckbox = (CheckBox) view.findViewById(R.id.monitoring_esp_tx_checkbox);
        realTimeControlCheckbox = (CheckBox) view.findViewById(R.id.real_time_control_checkbox);
        espPacketListTable = (TableLayout) view.findViewById(R.id.sensor_list_tb);
        thresholdListTable = (TableLayout) view.findViewById(R.id.esp_thresholds_list_tb);
        addButton = (Button) view.findViewById(R.id.sensor_add_btn);
        addButton.setOnClickListener(v -> handleClickAddBtn(v));

        saveBtn = (Button) view.findViewById(R.id.sensor_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        loadBtn = (Button) view.findViewById(R.id.sensor_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn(v));

        String[] options = { "uint8", "int8", "uint16", "int16", "uint24", "int24", "uint32", "int32", "float", "double"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataTypeSpinner.setAdapter(adapter);



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

    public void initEditControls() {
        variableNameEdit.setText("");
        dataTypeSpinner.setSelection(0, true);
        numberOfChannelsEdit.setText("");
        monitoringCheckbox.setChecked(false);
        realTimeControlCheckbox.setChecked(false);
    }

    public void displayTables(List<ESPPacket> sas) {
        Log.d("displayTable", "This is inside displayTable");
        espPacketListTable.removeViews(1, espPacketListTable.getChildCount() - 1);
        thresholdListTable.removeViews(2, thresholdListTable.getChildCount() - 2);
        for (int i = 0; i < sas.size(); i ++) {
            addTableRow(sas.get(i), i + 1);
            addThresholdTableRow(sas.get(i));
        }
    }

    public void editSensor(Long sensorId) {
        int cnt = espPacketListTable.getChildCount();
        for (int i = 1; i < cnt; i ++) {
            View rowView = espPacketListTable.getChildAt(i);
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

    public void handleClickAddBtn(View view) {
        try {
            String variableName = variableNameEdit.getText().toString();
            String dataType = dataTypeSpinner.getSelectedItem().toString();
            String numberOfChannels = numberOfChannelsEdit.getText().toString();

            Integer monitoring = monitoringCheckbox.isChecked() ? 1 : 0;
            Integer realTimeControl = realTimeControlCheckbox.isChecked() ? 1 : 0;

            ESPPacket espPacket = new ESPPacket("", variableName, 0, dataType, Integer.parseInt(numberOfChannels), monitoring, realTimeControl, System.currentTimeMillis(), new ArrayList<>());
            if(currentTableRowIndex == -1) {
                currentESPPackets.add(espPacket);
            }
            else {
                espPacket.setId(currentESPPackets.get(currentTableRowIndex - 1).getId());
                currentESPPackets.set(currentTableRowIndex - 1, espPacket);

            }
            initEditControls();
            currentTableRowIndex = -1;
            
            displayTables(currentESPPackets);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Please Enter Valid Values!", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleClickSaveBtn() {
        String sensorSetTitle = idAutocomplete.getText().toString().trim();
        if (sensorSetTitle.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_the_title_first, Toast.LENGTH_SHORT).show();
            return;
        }
        List<ESPPacket> results = new ArrayList<>();
        try {
            int rowsCnt = espPacketListTable.getChildCount();
            for (int i = 1; i < rowsCnt; i++) {
                ESPPacket result = getSensorActuatorFromTableRow(i, sensorSetTitle);
                if(allTitles.contains(sensorSetTitle) && currentESPPackets.get(i - 1).getId() != null) result.setId(currentESPPackets.get(i - 1).getId());
                if (result == null) continue;
                results.add(result);
            }
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "Please add some data to the table", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String msg = "";
        if (allTitles.contains(sensorSetTitle)) msg = "Records with the same title already exist. Are you sure you want to update this Sensor Setting Data?";
        else msg = "Are you sure you want to save this Sensor Setting Data?";
        new AlertDialog.Builder(requireContext())
            .setTitle("Confirm")
            .setMessage(msg)
            .setPositiveButton("Yes", (dialog, which) -> {
                try {
                    int thresCnt = 0;
                    for (ESPPacket espPacket: results) {
                        List<ESPThreshold> espThresholds  = new ArrayList<>();
                        int nChannels = espPacket.getNumberOfChannels();
                        for (int i = 0; i < nChannels; i ++) {
                            Float initialValue = null;
                            Float upperLimit = null;
                            Float lowerLimit = null;
                            Float outlier = null;
                            Integer order = null;
                            Integer thresholdsEnabled = null;
                            Integer outliersEnabled = null;
                            String compare = null;
                            try {
                                TableRow row = (TableRow) thresholdListTable.getChildAt(thresCnt + 2);

                                TextView orderView = (TextView) row.getChildAt(1);
                                order = string2Int(orderView.getText().toString().trim(), null);

                                EditText iEdit = (EditText) row.getChildAt(2);
                                initialValue = string2Float(iEdit.getText().toString().trim(), null);

                                EditText lEdit = (EditText) row.getChildAt(3);
                                lowerLimit = string2Float(lEdit.getText().toString().trim(), null);

                                EditText uEdit = (EditText) row.getChildAt(4);
                                upperLimit = string2Float(uEdit.getText().toString().trim(), null);

                                LinearLayout tLayout = (LinearLayout) row.getChildAt(5);
                                CheckBox tCheckBox = (CheckBox) tLayout.getChildAt(0);
                                thresholdsEnabled = tCheckBox.isChecked() ? 1 : 0;

                                EditText oEdit = (EditText) row.getChildAt(6);
                                outlier = string2Float(oEdit.getText().toString().trim(), null);

                                EditText compareEdit = (EditText) row.getChildAt(7);
                                compare = compareEdit.getText().toString().trim();

                                LinearLayout oLayout = (LinearLayout) row.getChildAt(8);
                                CheckBox oCheckBox = (CheckBox) oLayout.getChildAt(0);
                                outliersEnabled = oCheckBox.isChecked() ? 1 : 0;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ESPThreshold espThreshold = new ESPThreshold(order, initialValue, upperLimit, lowerLimit, thresholdsEnabled, outliersEnabled, new ArrayList<>(), outlier, compare);
                            espThresholds.add(espThreshold);
                            thresCnt ++;
                        }
                        espPacket.setThresholds(espThresholds);
                    }
                    if(allTitles.contains(sensorSetTitle)) {
                        espPacketViewModel.saveBatch(results, insertResults -> {
                            if (results.size() == insertResults.size()) {
                                Toast.makeText(requireContext(), R.string.sensor_setting_saved_successfully, Toast.LENGTH_SHORT).show();
                                initEditControls();
                                espPacketListTable.removeViews(1, espPacketListTable.getChildCount() - 1);
                                thresholdListTable.removeViews(2, thresholdListTable.getChildCount() - 2);
                                idAutocomplete.setText("");
                            } else {
                                Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        espPacketViewModel.insertBatch(results, insertResults -> {
                            if (results.size() == insertResults.size()) {
                                Toast.makeText(requireContext(), R.string.inserted_esp_packet_setting_successfully, Toast.LENGTH_SHORT).show();
                                initEditControls();
                                espPacketListTable.removeViews(1, espPacketListTable.getChildCount() - 1);
                                thresholdListTable.removeViews(2, thresholdListTable.getChildCount() - 2);
                                idAutocomplete.setText("");
                            } else {
                                Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), R.string.unexpected_error_occurred_please_load_and_check, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            }).show();

    }

    public ESPPacket getSensorActuatorFromTableRow(int index, String title) {
        try {
            TableRow tableRow = (TableRow) espPacketListTable.getChildAt(index);
            if (tableRow == null) {
                return null;
            }
            TextView vNameView = (TextView) tableRow.getChildAt(1);
            TextView dTypeView = (TextView) tableRow.getChildAt(2);
            TextView nChannelsView = (TextView) tableRow.getChildAt(3);
            TextView mView = (TextView) tableRow.getChildAt(5);
            TextView rtView = (TextView) tableRow.getChildAt(6);

            ESPPacket result = new ESPPacket(
                    title,
                    vNameView.getText().toString().trim(),
                    0,
                    dTypeView.getText().toString().trim(),
                    Integer.parseInt(nChannelsView.getText().toString().trim()),
                    mView.getText().toString().trim().equals("Yes") ? 1 : 0,
                    rtView.getText().toString().trim().equals("Yes") ? 1 : 0,
                    System.currentTimeMillis(),
                    new ArrayList<>()
            );

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void handleClickLoadBtn(View view) {
        String title = idAutocomplete.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Sensor Setting title.", Toast.LENGTH_SHORT).show();
            return;
        }
        espPacketViewModel.getByTitle(title, results -> {
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "There is no records with that title", Toast.LENGTH_SHORT).show();
                espPacketListTable.removeViews(1, espPacketListTable.getChildCount() - 1);
                thresholdListTable.removeViews(2, thresholdListTable.getChildCount() - 2);
                initEditControls();
                return;
            }
            else {
                Toast.makeText(requireContext(), "Successfully loaded Sensor Setting from db.", Toast.LENGTH_SHORT).show();
                currentESPPackets.clear();
                currentESPPackets.addAll(results);
                espPacketListTable.removeViews(1, espPacketListTable.getChildCount() - 1);
                thresholdListTable.removeViews(2, thresholdListTable.getChildCount() - 2);
                displayTables(results);
            }
        });

    }

    public void resetOrdersOfTable() {
        int len = espPacketListTable.getChildCount();
        if(len < 2) return;
        for (int i = 1; i < len; i ++) {
            try {
                TableRow tableRow = (TableRow) espPacketListTable.getChildAt(i);
                TextView orderView = (TextView) tableRow.getChildAt(0);
                if (orderView != null) orderView.setText(String.valueOf(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClickDelBtn(View v) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Handle Yes button click
                    ImageButton clickedButton = (ImageButton) v; // 'v' is the clicked view
                    LinearLayout linearLayout = (LinearLayout) clickedButton.getParent(); // Get the parent TableRow
                    TableRow row = (TableRow) linearLayout.getParent();
                    if(row != null) {
                        int index = espPacketListTable.indexOfChild(row);
                        currentESPPackets.remove(index - 1);
                        displayTables(currentESPPackets);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Handle No button click (optional)
                    dialog.dismiss();
                })
                .show();
    }

    public void handleClickChangeBtn(View v) {
        try {
            initEditControls();
            ImageButton clickedButton = (ImageButton) v; // 'v' is the clicked view
            LinearLayout linearLayout = (LinearLayout) clickedButton.getParent(); // Get the parent TableRow
            TableRow row = (TableRow) linearLayout.getParent();
            TextView vNameView = (TextView) row.getChildAt(1);
            TextView dTypeView = (TextView) row.getChildAt(2);
            TextView nChannelsView = (TextView) row.getChildAt(3);
            TextView mView = (TextView) row.getChildAt(5);
            TextView rtView = (TextView) row.getChildAt(6);

            variableNameEdit.setText(vNameView.getText().toString().trim());
            setSpinnerWithContent(dataTypeSpinner, dTypeView.getText().toString().trim());
            numberOfChannelsEdit.setText(nChannelsView.getText().toString().trim());
            monitoringCheckbox.setChecked(mView.getText().toString().trim().equals("Yes"));
            realTimeControlCheckbox.setChecked(rtView.getText().toString().trim().equals("Yes"));

            int rowIndex = espPacketListTable.indexOfChild(row);
            currentTableRowIndex = rowIndex;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Unexpected Error Occurred!", Toast.LENGTH_SHORT).show();
        }
    }

    public Boolean checkIfFirstAdd(TableLayout tableLayout) {
        if(tableLayout.getChildCount() < 2) return false;
        try {
            TableRow row = (TableRow) tableLayout.getChildAt(1);
            return row.getChildAt(1) == null;

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addTableRow(ESPPacket data, int index) {
        TableRow tableRow = new TableRow(requireContext());
        tableRow.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));
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
                handleClickChangeBtn(v);
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
                handleClickDelBtn(v);
            }
        });

        LinearLayout btnLayout = new LinearLayout(requireContext());
        btnLayout.setGravity(Gravity.CENTER);
        btnLayout.addView(changeValueBtn);
        btnLayout.addView(changeOrderBtn);
        btnLayout.addView(deleteBtn);

        DNDHelper.enableRowDragAndDrop(changeOrderBtn, tableRow, espPacketListTable, currentESPPackets, result -> {
            initEditControls();
            currentTableRowIndex = -1;
        });

        tableRow.addView(btnLayout);
        espPacketListTable.addView(tableRow);

    }

    public void addThresholdTableRow(ESPPacket data) {
        int nChannels = data.getNumberOfChannels();
        for (int i = 0; i < nChannels; i ++ ) {
            ESPThreshold threshold = null;
            if(data.getThresholds().size() > i) threshold = data.getThresholds().get(i);
            TableRow tableRow = new TableRow(requireContext());
            tableRow.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));
            TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            tableRow.setLayoutParams(tableRowLayoutParams);

            TextView nameView = new TextView(requireContext());
            if(i == 0) nameView.setText(data.getVariableName() + " / " + data.getDataType());
            nameView.setGravity(Gravity.CENTER);

            TextView orderView = new TextView(requireContext());
            orderView.setText(String.valueOf(i + 1));
            orderView.setGravity(Gravity.CENTER);

            EditText initialEdit = new EditText(requireContext());
            initialEdit.setGravity(Gravity.CENTER);
            EditText lowerEdit = new EditText(requireContext());
            lowerEdit.setGravity(Gravity.CENTER);
            EditText upperEdit = new EditText(requireContext());
            upperEdit.setGravity(Gravity.CENTER);

            LinearLayout thresholdCheckBoxLayout = new LinearLayout(requireContext());
            thresholdCheckBoxLayout.setOrientation(LinearLayout.VERTICAL);
            TableRow.LayoutParams checkBoxWrapperParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            thresholdCheckBoxLayout.setPadding(10, 20, 10, 10);
//            thresholdCheckBoxLayout.setBackgroundColor(getResources().getColor(R.color.bs_info));
            thresholdCheckBoxLayout.setLayoutParams(checkBoxWrapperParams);
            thresholdCheckBoxLayout.setGravity(Gravity.CENTER);
            CheckBox thresholdCheckBox = new CheckBox(requireContext());
            thresholdCheckBox.setLayoutParams(checkBoxParams);
            thresholdCheckBox.setGravity(Gravity.CENTER);
            thresholdCheckBoxLayout.addView(thresholdCheckBox);

            EditText outlierEdit = new EditText(requireContext());
            outlierEdit.setGravity(Gravity.CENTER);
            EditText compareEdit = new EditText(requireContext());
            compareEdit.setGravity(Gravity.CENTER);
            LinearLayout outlierCheckBoxLayout = new LinearLayout(requireContext());
            outlierCheckBoxLayout.setOrientation(LinearLayout.VERTICAL);
            TableRow.LayoutParams outlierCheckboxWrapperParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            outlierCheckBoxLayout.setPadding(10, 20, 10, 10);

            outlierCheckBoxLayout.setLayoutParams(outlierCheckboxWrapperParams);
            outlierCheckBoxLayout.setGravity(Gravity.CENTER);
            CheckBox outlierCheckBox = new CheckBox(requireContext());
            outlierCheckBox.setGravity(Gravity.CENTER);
            outlierCheckBox.setLayoutParams(checkBoxParams);
            outlierCheckBoxLayout.addView(outlierCheckBox);

            try {
                if(threshold != null) {
                    initialEdit.setText(threshold.getInitialValue() == null ? "" : String.valueOf(threshold.getInitialValue()));
                    lowerEdit.setText(threshold.getLowerLimit() == null ? "" : String.valueOf(threshold.getLowerLimit()));
                    upperEdit.setText(threshold.getUpperLimit() == null ? "" : String.valueOf(threshold.getUpperLimit()));
                    thresholdCheckBox.setChecked(threshold.getThresholdsEnabled() == 1);
                    outlierEdit.setText(threshold.getOutlier() == null ? "" : String.valueOf(threshold.getOutlier()));
                    compareEdit.setText(threshold.getCompare() == null ? "" : threshold.getCompare());
                    outlierCheckBox.setChecked(threshold.getOutliersEnabled() == 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            tableRow.addView(nameView);
            tableRow.addView(orderView);
            tableRow.addView(initialEdit);
            tableRow.addView(lowerEdit);
            tableRow.addView(upperEdit);
            tableRow.addView(thresholdCheckBoxLayout);
            tableRow.addView(outlierEdit);
            tableRow.addView(compareEdit);
            tableRow.addView(outlierCheckBoxLayout);
            thresholdListTable.addView(tableRow);
        }
    }

}

