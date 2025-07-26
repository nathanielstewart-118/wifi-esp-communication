package com.example.myapplication.settings;

import static com.example.myapplication.utils.CommonUtils.getNumberOfBytesFromDataTypeString;
import static com.example.myapplication.utils.UIUtils.getIndexFromSpinnerContent;
import static com.example.myapplication.utils.UIUtils.initAutoCompleteWithSuggestionList;
import static com.example.myapplication.utils.UIUtils.setSpinnerWithContent;
import static com.example.myapplication.utils.UIUtils.setupOperationalButtons;

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

import com.example.myapplication.R;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;
import com.example.myapplication.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SensorSetting extends Fragment {
    private EditText variableNameEdit;
    private Spinner dataTypeSpinner;
    private EditText numberOfChannelsEdit;
    private CheckBox monitoringCheckbox;
    private CheckBox realTimeControlCheckbox;
    private Button addButton;
    private Button saveBtn;
    private Button loadBtn;

    private TableLayout sensorListTable;
    private final List<SensorActuator> sensors = new ArrayList<>();
    private SensorActuatorViewModel sensorActuatorViewModel;
    private AppDatabase db;
    private AutoCompleteTextView idAutocomplete;
    private Boolean delegated = false;
    private Long delegatedId = (long) -1;
    private int currentTableRowIndex = -1;
    private String currentSettingTitle = "";
    private List<String> allTitles;
    public SensorSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[1]);
        db = AppDatabase.getInstance(requireContext());
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        sensorActuatorViewModel = new ViewModelProvider(
                requireActivity()
        ).get(SensorActuatorViewModel.class);
        sensorActuatorViewModel.getAllSensors().observe(getViewLifecycleOwner(), data -> {
//            this.displayTable(data);
//            sensors.addAll(data);
            if (delegated) {
                editSensor(delegatedId);
                delegated = false;
            }
        });

        sensorActuatorViewModel.getAllTitles(0).observe(getViewLifecycleOwner(), results -> {
            initAutoCompleteWithSuggestionList(idAutocomplete, results, requireContext());
            allTitles = results;
        });
        dataTypeSpinner = (Spinner) view.findViewById(R.id.data_type_spinner);
        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.sensor_setting_id_autocomplete);
        variableNameEdit = (EditText) view.findViewById(R.id.variable_name_input);
        numberOfChannelsEdit = (EditText) view.findViewById(R.id.number_of_channels_input);
        monitoringCheckbox = (CheckBox) view.findViewById(R.id.monitoring_esp_tx_checkbox);
        realTimeControlCheckbox = (CheckBox) view.findViewById(R.id.real_time_control_checkbox);
        sensorListTable = (TableLayout) view.findViewById(R.id.sensor_list_tb);

        addButton = (Button) view.findViewById(R.id.sensor_add_btn);
        addButton.setOnClickListener(v -> handleClickAddBtn(v));

        saveBtn = (Button) view.findViewById(R.id.sensor_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        loadBtn = (Button) view.findViewById(R.id.sensor_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn(v));

        String[] options = { "uint8", "int8", "uint16", "int16", "uint24", "int24", "uint32", "int32", "float", "double"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
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

    public void addTableRow(SensorActuator data, int index) {

        TableRow tableRow;
        if(currentTableRowIndex == -1) {
            tableRow = new TableRow(requireContext());
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

            tableRow.addView(btnLayout);
            sensorListTable.addView(tableRow);
        } else {
            tableRow = (TableRow) sensorListTable.getChildAt(index);
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

    public void handleClickAddBtn(View view) {
        try {
            String variableName = variableNameEdit.getText().toString();
            String dataType = dataTypeSpinner.getSelectedItem().toString();
            String numberOfChannels = numberOfChannelsEdit.getText().toString();
            Integer monitoring = monitoringCheckbox.isChecked() ? 1 : 0;
            Integer realTimeControl = realTimeControlCheckbox.isChecked() ? 1 : 0;

            SensorActuator sensor = new SensorActuator("", variableName, 0, dataType, Integer.parseInt(numberOfChannels), monitoring, realTimeControl, System.currentTimeMillis());
            int index = -1;
            if (checkIfFirstAdd(sensorListTable)) {
                sensorListTable.removeViewAt(1);
                index = 1;
            }
            else index = currentTableRowIndex == -1 ? sensorListTable.getChildCount() : currentTableRowIndex ;
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
        List<SensorActuator> results = new ArrayList<>();
        try {
            int rowsCnt = sensorListTable.getChildCount();
            for (int i = 1; i < rowsCnt; i++) {
                SensorActuator result = getSensorActuatorFromTableRow(i, sensorSetTitle);
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

                    if (!allTitles.contains(sensorSetTitle)) {
                        sensorActuatorViewModel.insertBatch(results, insertResults -> {
                            if (results.size() == insertResults.size()) {
                                Toast.makeText(requireContext(), R.string.sensor_setting_saved_successfully, Toast.LENGTH_SHORT).show();
                                initEditControls();
                                sensorListTable.removeViews(1, sensorListTable.getChildCount() - 1);
                                currentSettingTitle = "";
                                idAutocomplete.setText("");
                            } else {
                                Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        sensorActuatorViewModel.updateBatch(results, updateResults -> {
                            if (results.size() == updateResults.size()) {
                                Toast.makeText(requireContext(), "Sensor Setting updated successfully!", Toast.LENGTH_SHORT).show();
                                initEditControls();
                                sensorListTable.removeViews(1, sensorListTable.getChildCount() - 1);
                                currentSettingTitle = "";
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

    public SensorActuator getSensorActuatorFromTableRow(int index, String title) {
        try {
            TableRow tableRow = (TableRow) sensorListTable.getChildAt(index);
            if (tableRow == null) {
                return null;
            }
            TextView vNameView = (TextView) tableRow.getChildAt(1);
            TextView dTypeView = (TextView) tableRow.getChildAt(2);
            TextView nChannelsView = (TextView) tableRow.getChildAt(3);
            TextView mView = (TextView) tableRow.getChildAt(5);
            TextView rtView = (TextView) tableRow.getChildAt(6);

            SensorActuator result = new SensorActuator(
                    title,
                    vNameView.getText().toString().trim(),
                    0,
                    dTypeView.getText().toString().trim(),
                    Integer.parseInt(nChannelsView.getText().toString().trim()),
                    mView.getText().toString().trim().equals("Yes") ? 1 : 0,
                    rtView.getText().toString().trim().equals("Yes") ? 1 : 0,
                    System.currentTimeMillis()
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
        sensorActuatorViewModel.getByTitle(title, 0, results -> {
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "There is no records with that title", Toast.LENGTH_SHORT).show();
                initEditControls();
                sensorListTable.removeViews(1, sensorListTable.getChildCount() - 1);
                return;
            }
            else {
                Toast.makeText(requireContext(), "Successfully loaded Sensor Setting from db.", Toast.LENGTH_SHORT).show();
                currentSettingTitle = results.get(0).getTitle();
                int len = results.size();
                sensorListTable.removeViews(1, sensorListTable.getChildCount() - 1);
                for (int i = 0; i < len; i ++) {
                    addTableRow(results.get(i), i + 1);
                }
            }
        });

    }
    public void resetOrdersOfTable() {
        int len = sensorListTable.getChildCount();
        if(len < 2) return;
        for (int i = 1; i < len; i ++) {
            try {
                TableRow tableRow = (TableRow) sensorListTable.getChildAt(i);
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
                    if(row != null) sensorListTable.removeView(row);
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

            int rowIndex = sensorListTable.indexOfChild(row);
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
