package com.example.myapplication.settings;

import static com.example.myapplication.utils.UIUtils.setupOperationalButtons;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.entity.CommandThreshold;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.viewmodel.CommandViewModel;
import com.example.myapplication.db.viewmodel.SensorActuatorViewModel;
import com.example.myapplication.utils.communications.WiFiSocketManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandSetting extends Fragment {

    private EditText commandCodeEdit;
    private EditText time1Edit;
    private EditText time2Edit;

    private Button openThresholdDialogBtn;
    private Button addCommandBtn;
    private Button sendCommandBtn;
    private Button saveThresholdBtn;
    private Button closeThresholdDialogBtn;

    private TableLayout thresholdEditTable;
    private TableLayout commandViewTable;

    private View thresholdDialog;
    private AlertDialog dialog;

    private String commandCode;
    private Float time1;
    private Float time2;
    private int selectedSetting = -1;
    private List<Command> commands = new ArrayList<>();
    private CommandViewModel commandViewModel;
    private SensorActuatorViewModel sensorActuatorViewModel;
    private AppDatabase db;

    private List<SensorActuator> sensorActuators = new ArrayList<>();
    private List<CommandThreshold> currentCommandThresholds = new ArrayList<>();

    private WiFiSocketManager socketManager = WiFiSocketManager.getInstance();

    private final Gson gson = new Gson();

    public CommandSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View commandFragment = inflater.inflate(R.layout.fragment_command, container, false);
        db = AppDatabase.getInstance(requireContext());
        commandViewModel = new ViewModelProvider(
                requireActivity()
        ).get(CommandViewModel.class);
        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        commandViewModel.getAllCommands().observe(getViewLifecycleOwner(), data -> {
            displayCommandsTable(data);
            commands.clear();
            commands.addAll(data);
            initEditControls();
            if (!data.isEmpty()) {
                Log.w("Warning ------------->", gson.toJson(data.get(0)));
            }
        });

        commandViewTable = (TableLayout) commandFragment.findViewById(R.id.command_view_table);
        commandCodeEdit = (EditText) commandFragment.findViewById(R.id.command_code_input);
        time1Edit = (EditText) commandFragment.findViewById(R.id.command_time1_float_input);
        time2Edit = (EditText) commandFragment.findViewById(R.id.command_time2_float_input);

        addCommandBtn = (Button) commandFragment.findViewById(R.id.command_add_btn);
        sendCommandBtn = (Button) commandFragment.findViewById(R.id.command_send_btn);
        sendCommandBtn.setOnClickListener(v -> handleClickSendCommandBtn());


        openThresholdDialogBtn = (Button) commandFragment.findViewById(R.id.command_open_threshold_modal_btn);
        openThresholdDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandSetting.this.openThresholdDialog((long)selectedSetting);
            }
        });

        addCommandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandCode = commandCodeEdit.getText().toString();
                time1 = Float.parseFloat(time1Edit.getText().toString());
                time2 = Float.parseFloat(time2Edit.getText().toString());
                Command command = new Command(commandCode, time1, time2, currentCommandThresholds);
                if (selectedSetting == -1) {
                    commandViewModel.insert(command);
                    commandViewModel.getInsertResult().observe(getViewLifecycleOwner(), id -> {
                        if (id != null && id > 0) {
                            Toast.makeText(getContext(), "Insert success!", Toast.LENGTH_SHORT).show();
                            command.setId(id);
                        } else {
                            Toast.makeText(getContext(), "Insert failed!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else {
                    command.setId((long)selectedSetting);
                    commandViewModel.update(command);
                    commandViewModel.getUpdateResult().observe(getViewLifecycleOwner(), res -> {
                        if(res != null && res != 0) {
                            Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                CommandSetting.this.initEditControls();
                selectedSetting = -1;

            }
        });
        return commandFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        thresholdDialog = inflater.inflate(R.layout.command_layout_dialog, null);
        thresholdEditTable = (TableLayout) thresholdDialog.findViewById(R.id.command_threshold_edit_table);
        sensorActuatorViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), sas -> {
            displayThresholdTable(sas, new Command("", 0, 0, new ArrayList<>()));
            sensorActuators.addAll(sas);
        });

        closeThresholdDialogBtn = thresholdDialog.findViewById(R.id.command_close_threshold_modal_btn);
        closeThresholdDialogBtn.setOnClickListener(v -> dialog.dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(thresholdDialog);
        dialog = builder.create();

        // Save button action
        saveThresholdBtn = thresholdDialog.findViewById(R.id.command_threshold_save_btn);
        saveThresholdBtn.setOnClickListener(v -> {
            List<CommandThreshold> commandThresholds = new ArrayList<>();
            int rowCnt = thresholdEditTable.getChildCount();
            for (int i = 1; i < rowCnt; i ++) {
                Long last_sa_id = !commandThresholds.isEmpty() ? commandThresholds.get(commandThresholds.size() - 1).getSensorActuatorId() : -1;
                View rowView = thresholdEditTable.getChildAt(i);
                if(rowView instanceof TableRow) {
                    TableRow tableRow = (TableRow) rowView;
                    Long id = (Long) tableRow.getTag();
                    Double threshold = null;
                    try {
                        EditText thresholdText = (EditText) (tableRow.getChildAt(2));
                        String text = thresholdText.getText().toString().trim();
                        if(!text.isEmpty()) threshold = Double.valueOf(text);
                    } catch (Exception e) {
                        Log.e("Error", "Can't get threshold value from input");
                    }
                    if (Objects.equals(id, last_sa_id)) {
                        if(threshold != null)
                            commandThresholds.get(commandThresholds.size() - 1).getThresholds().add(threshold);
                    }
                    else {
                        if(threshold != null) {
                            CommandThreshold commandThreshold = new CommandThreshold();
                            commandThreshold.setSensorActuatorId(id);
                            List<Double> newThresholds = new ArrayList<>();
                            newThresholds.add(threshold);
                            commandThreshold.setThresholds(newThresholds);
                            commandThresholds.add(commandThreshold);
                        }
                    }
                }
            }
            currentCommandThresholds.addAll(commandThresholds);
            Log.d("_------------result -------------", new Gson().toJson(currentCommandThresholds));
            Toast.makeText(requireContext(), "Data Saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Optionally close after saving
        });
    }


    public void openThresholdDialog(Long commandId) {
        dialog.show();
        List<Command> selected = commands.stream()
                .filter(command -> Objects.equals(command.getId(), commandId))
                .collect(Collectors.toList());
        Command command = selected.isEmpty() ? new Command("", 0, 0, Collections.emptyList()) : selected.get(0);
        displayThresholdTable(sensorActuators, command);
    }

    public void addTableRow(Command data, int index) {
        TableRow row = new TableRow(requireContext());
        TableRow.LayoutParams iconButtonSizeparams = new TableRow.LayoutParams(
                50,
                80
        );
        TextView sequenceView = new TextView(requireContext());
        sequenceView.setText(String.valueOf(index));
        sequenceView.setGravity(Gravity.CENTER);
        row.addView(sequenceView);

        TextView commandCodeView = new TextView(requireContext());
        commandCodeView.setText(data.getCommandCode());
        commandCodeView.setGravity(Gravity.CENTER);
        row.addView(commandCodeView);

        TextView time1View = new TextView(requireContext());
        time1View.setText(String.valueOf(data.getTime1()));
        time1View.setGravity(Gravity.CENTER);
        row.addView(time1View);

        TextView time2View = new TextView(requireContext());
        time2View.setText(String.valueOf(data.getTime2()));
        time2View.setGravity(Gravity.CENTER);
        row.addView(time2View);

        Button thresholdBtn = new Button(requireContext());
        thresholdBtn.setText("Threshold");
        thresholdBtn.setTag(data.getId());
        thresholdBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bs_secondary));
        thresholdBtn.setLayoutParams(iconButtonSizeparams);
        thresholdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = (Long) v.getTag();
                openThresholdDialog(id);
            }
        });
        thresholdBtn.setGravity(Gravity.CENTER);
        row.addView(thresholdBtn);

        List<ImageButton> operationalButtons = setupOperationalButtons(data.getId(), requireContext());
        ImageButton changeValueBtn = operationalButtons.get(0);
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long index = (Long) v.getTag();
                selectedSetting = index.intValue();

                List<Command> selectedCommands = commands.stream()
                        .filter(s -> Objects.equals(s.getId(), index))
                        .collect(Collectors.toList());
                if (selectedCommands.isEmpty()) {
                    return;
                }
                commandCodeEdit.setText(selectedCommands.get(0).getCommandCode());
                time1Edit.setText(selectedCommands.get(0).getTime1().toString());
                time2Edit.setText(selectedCommands.get(0).getTime2().toString());
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
                            List<Command> selectedCommands = commands.stream()
                                    .filter(sa -> Objects.equals(sa.getId(), id))
                                    .collect(Collectors.toList());
                            if (selectedCommands.isEmpty()) {
                                return;
                            }
                            commandViewModel.delete(selectedCommands.get(0));
                            commandViewModel.getDeleteResult().observe(getViewLifecycleOwner(), res -> {
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

        row.addView(btnLayout);
        commandViewTable.addView(row);
    }

    public void initEditControls() {
        commandCodeEdit.setText("");
        time1Edit.setText("");
        time2Edit.setText("");
    }

    public void displayCommandsTable(List<Command> commands) {
        commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
        for (int i = 0; i < commands.size(); i ++) {
            addTableRow(commands.get(i), i + 1);
        }
    }

    public void displayThresholdTable(List<SensorActuator> sas, Command command) {
        Log.d("THis is start of displayThresholdTable", new Gson().toJson(command));
        thresholdEditTable.removeViews(1, thresholdEditTable.getChildCount() - 1);
        List<CommandThreshold> commandThresholds = command.getThresholds();
        int rowCnt = 0;
        for (int i = 0; i < sas.size(); i ++) {
            SensorActuator sa = sas.get(i);
            List<Double> thresholds = new ArrayList<>();
            try {
                List<CommandThreshold> selected = commandThresholds.stream()
                        .filter(ct -> Objects.equals(ct.getSensorActuatorId(), sa.getId()))
                        .collect(Collectors.toList());
                if (!selected.isEmpty()) thresholds = selected.get(0).getThresholds();

            } catch (Exception e) {
                Log.e("Threshold", "Error while fetching threshold", e);
            }
            int nChannels = sa.getNumberOfChannels();
            for (int j = 0; j < nChannels; j ++) {
                TableRow tableRow = new TableRow(requireContext());
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                );
                tableRow.setPadding(30, 0, 0, 0);
                tableRow.setTag(sa.getId());
                TextView orderTypeView = new TextView(this.getContext());
                String orderTypeStr = String.valueOf(rowCnt + 1) + " / " + sa.getVariableName();
                orderTypeView.setText(orderTypeStr);

                TextView channelTextView = new TextView(this.getContext());
                channelTextView.setText(String.valueOf(j + 1));

                EditText thresholdEdit = new EditText(requireContext());
                Log.e(sa.getVariableName(), "");
                if (!thresholds.isEmpty() && thresholds.size() > j && thresholds.get(j) != null) {
                    thresholdEdit.setText(String.valueOf(thresholds.get(j).intValue()));

                }
                tableRow.addView(orderTypeView);
                tableRow.addView(channelTextView);
                tableRow.addView(thresholdEdit);
                thresholdEditTable.addView(tableRow);
                rowCnt ++;
            }
        }
    }

    public void handleClickSendCommandBtn() {
        if (socketManager.isTCPConnected()) {
            String dataString = "";
            if (commands.size() > 0) {
                Command command = commands.get(0);
                dataString = gson.toJson(command);
            }
            socketManager.sendTCP("Hello from fragment, " + dataString, new WiFiSocketManager.Callback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("Fragment", "Response from TCP: " + response);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Fragment", "TCP send failed", e);
                }
            });
        } else {
            Log.e("Fragment", "TCP not connected yet");
        }
    }
}
