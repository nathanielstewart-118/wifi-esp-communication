package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.getNumberOfBytesFromDataTypeString;
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
import com.prtech.spiapp.db.viewmodel.ESPPacketViewModel;
import com.prtech.spiapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActuatorSetting extends Fragment {

    private Spinner spinner;
    private EditText variableNameEdit;
    private EditText numberOfChannelsEdit;
    private CheckBox monitoringCheckbox;
    private CheckBox realTimeControlCheckbox;
    private Button saveBtn;
    private Button loadBtn;

    private Button addButton;
    private TableLayout actuatorListTable;
    private List<ESPPacket> actuators = new ArrayList<>();
    private ESPPacketViewModel espPacketViewModel;
    private AppDatabase db;
    private AutoCompleteTextView idAutocomplete;
    private Boolean delegated = false;
    private Long delegatedId = (long) -1;
    private Integer currentTableRowIndex = -1;
    private List<String> allTitles;
    private Spinner dataTypeSpinner;

    public ActuatorSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[2]);
        View view = inflater.inflate(R.layout.fragment_actuator, container, false);
        db = AppDatabase.getInstance(requireContext());
        espPacketViewModel = new ViewModelProvider(
                requireActivity()
        ).get(ESPPacketViewModel.class);
        espPacketViewModel.getAllActuators().observe(getViewLifecycleOwner(), data -> {
//            this.displayTable(data);
//            actuators.addAll(data);
            if(delegated) {
                editActuator(delegatedId);
                delegated = false;
            }
        });

        espPacketViewModel.getAllTitles().observe(getViewLifecycleOwner(), results -> {
            initAutoCompleteWithSuggestionList(idAutocomplete, results, requireContext());
            allTitles = results;
        });

        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.actuator_setting_id_autocomplete);
        dataTypeSpinner = (Spinner) view.findViewById(R.id.data_type_spinner);
        variableNameEdit = (EditText) view.findViewById(R.id.variable_name_input);
        numberOfChannelsEdit = (EditText) view.findViewById(R.id.number_of_channels_input);
        monitoringCheckbox = (CheckBox) view.findViewById(R.id.monitoring_esp_tx_checkbox);
        realTimeControlCheckbox = (CheckBox) view.findViewById(R.id.real_time_control_checkbox);
        addButton = (Button) view.findViewById(R.id.actuator_add_btn);
        saveBtn = (Button) view.findViewById(R.id.actuator_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        loadBtn = (Button) view.findViewById(R.id.actuator_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn(v));
        actuatorListTable = (TableLayout) view.findViewById(R.id.actuator_list_tb);
        addButton.setOnClickListener(v -> handleClickAddBtn(v));


        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Constants.DATA_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataTypeSpinner.setAdapter((adapter));
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
    /**
     * add a row data to the sensor settings table
     *
     * @param data: a sensor setting data
     * @param index: sequence no in the table of that sensor setting record
     */
    public void addTableRow(ESPPacket data, int index) {
        TableRow tableRow;
        if(currentTableRowIndex == -1) {
            tableRow = new TableRow(requireContext());
            tableRow.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));
            tableRow.setTag(data.getId());
            tableRow.setVerticalGravity(Gravity.CENTER);
            TextView orderText = new TextView(requireContext());

            orderText.setText(String.valueOf(index == -1 ? actuatorListTable.getChildCount() + 1 : index));
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

            tableRow.addView(btnLayout);
            actuatorListTable.addView(tableRow);
        } else {
            tableRow = (TableRow) actuatorListTable.getChildAt(index);
            TextView vNameView = (TextView) tableRow.getChildAt(1);
            TextView dTypeView = (TextView) tableRow.getChildAt(2);
            TextView nChannelsView = (TextView) tableRow.getChildAt(3);
            TextView mView = (TextView) tableRow.getChildAt(5);
            TextView rtView = (TextView) tableRow.getChildAt(6);

            vNameView.setText(data.getVariableName());
            dTypeView.setText(data.getDataType());
            nChannelsView.setText(String.valueOf(data.getNumberOfChannels()));
            mView.setText(data.getMonitoring() == 1 ? "Yes" : "No");
            rtView.setText(data.getRealTimeControl() == 1 ? "Yes" : "No");
        }

    }

    public void initEditControls() {
        variableNameEdit.setText("");
        dataTypeSpinner.setSelection(0, true);
        numberOfChannelsEdit.setText("");
        monitoringCheckbox.setChecked(false);
        realTimeControlCheckbox.setChecked(false);
    }

    public void editActuator(Long actuatorId) {
        int cnt = actuatorListTable.getChildCount();
        for (int i = 1; i < cnt; i ++) {
            View rowView = actuatorListTable.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow tableRow = (TableRow) rowView;
                Long tagId = (long) tableRow.getTag();
                if(!Objects.equals(tagId, actuatorId)) continue;
                View layoutView = tableRow.getChildAt(6);
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

            ESPPacket sensor = new ESPPacket("", variableName, 1, dataType, Integer.parseInt(numberOfChannels), monitoring, realTimeControl, System.currentTimeMillis(), new ArrayList<>());
            int index = -1;
            if (checkIfFirstAdd(actuatorListTable)) {
                actuatorListTable.removeViewAt(1);
                index = 1;
            }
            else index = currentTableRowIndex == -1 ? actuatorListTable.getChildCount() : currentTableRowIndex ;
            addTableRow(sensor, index);
            initEditControls();
            currentTableRowIndex = -1;
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
            int rowsCnt = actuatorListTable.getChildCount();
            for (int i = 1; i < rowsCnt; i++) {
                ESPPacket result = getSensorActuatorFromTableRow(i, sensorSetTitle);
                if (result == null) continue;
                results.add(result);
            }
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "Please add some data to the table", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error occurred while fetching data from ui inputs", Toast.LENGTH_SHORT).show();
        }
        String msg = "";
        if (allTitles.contains(sensorSetTitle)) msg = "Records with the same title already exist. Are you sure you want to update this Sensor Setting Data?";
        else msg = "Are you sure you want to save this Sensor Setting Data?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage(msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {

                        if (!allTitles.contains(sensorSetTitle)) {
                            espPacketViewModel.insertBatch(results, insertResults -> {
                                if (results.size() == insertResults.size()) {
                                    Toast.makeText(requireContext(), "Actuator Setting saved successfully.", Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    actuatorListTable.removeViews(1, actuatorListTable.getChildCount() - 1);
                                    idAutocomplete.setText("");
                                } else {
                                    Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            espPacketViewModel.updateBatch(results, updateResults -> {
                                if (results.size() == updateResults.size()) {
                                    Toast.makeText(requireContext(), "Sensor Setting updated successfully!", Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    actuatorListTable.removeViews(1, actuatorListTable.getChildCount() - 1);
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
            TableRow tableRow = (TableRow) actuatorListTable.getChildAt(index);
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
                    1,
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
                initEditControls();
                actuatorListTable.removeViews(1, actuatorListTable.getChildCount() - 1);
                return;
            }
            else {
                Toast.makeText(requireContext(), "Successfully loaded Sensor Setting from db.", Toast.LENGTH_SHORT).show();
                int len = results.size();
                actuatorListTable.removeViews(1, actuatorListTable.getChildCount() - 1);
                for (int i = 0; i < len; i ++) {
                    addTableRow(results.get(i), i + 1);
                }
            }
        });

    }

    public void resetOrdersOfTable() {
        int len = actuatorListTable.getChildCount();
        if(len < 2) return;
        for (int i = 1; i < len; i ++) {
            try {
                TableRow tableRow = (TableRow) actuatorListTable.getChildAt(i);
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
                    if(row != null) actuatorListTable.removeView(row);
                    resetOrdersOfTable();
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

            int rowIndex = actuatorListTable.indexOfChild(row);
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
}
