package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.UIUtils.initAutoCompleteWithSuggestionList;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.prtech.spiapp.R;
import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.entity.CommandThreshold;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.viewmodel.CommandViewModel;
import com.prtech.spiapp.db.viewmodel.SensorActuatorViewModel;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.DNDHelper;
import com.prtech.spiapp.utils.LogHelper;
import com.prtech.spiapp.utils.communications.WiFiSocketManager;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Button saveBtn;
    private Button loadBtn;

    private TableLayout thresholdEditTable;
    private TableLayout commandViewTable;
    private AutoCompleteTextView idAutoComplete;
    private View thresholdDialog;
    private AlertDialog dialog;

    private String currentSettingTitle = "";
    private String commandCode;
    private Float time1;
    private Float time2;
    private int selectedSetting = -1;
    private List<Command> commands = new ArrayList<>();
    private List<String> allTitles = new ArrayList<>();
    private CommandViewModel commandViewModel;
    private SensorActuatorViewModel sensorActuatorViewModel;
    private AppDatabase db;

    private List<ESPPacket> espPackets = new ArrayList<>();
    private List<CommandThreshold> currentCommandThresholds = new ArrayList<>();
    private List<Command> currentCommands = new ArrayList<>();

    private WiFiSocketManager socketManager = WiFiSocketManager.getInstance();

    private final Gson gson = new Gson();

    public CommandSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[3]);
        View commandFragment = inflater.inflate(R.layout.fragment_command, container, false);
        db = AppDatabase.getInstance(requireContext());
        commandViewModel = new ViewModelProvider(
                requireActivity()
        ).get(CommandViewModel.class);

        sensorActuatorViewModel = new ViewModelProvider(requireActivity()).get(SensorActuatorViewModel.class);
        commandViewModel.getAllCommands().observe(getViewLifecycleOwner(), data -> {
//            displayCommandsTable(data);
            commands.clear();
            commands.addAll(data);
            initEditControls();
            if (!data.isEmpty()) {
                Log.w("Warning ------------->", gson.toJson(data.get(0)));
            }
        });

        commandViewModel.getAllTitles().observe(getViewLifecycleOwner(), data -> {
           allTitles.addAll(data);
           initAutoCompleteWithSuggestionList(idAutoComplete, data, requireContext());
        });


        saveBtn = (Button) commandFragment.findViewById(R.id.command_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn(v));

        idAutoComplete = (AutoCompleteTextView) commandFragment.findViewById(R.id.command_id_autocomplete);

        loadBtn = (Button) commandFragment.findViewById(R.id.command_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn(v));

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
                handleClickAddBtn();

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
//            displayThresholdTable(sas, new Command("", 0, 0, new ArrayList<>()));
//            sensorActuators.addAll(sas);
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
            Toast.makeText(requireContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Optionally close after saving
        });
    }


    public void openThresholdDialog(Long commandId) {
        dialog.show();
        List<Command> selected = commands.stream()
                .filter(command -> Objects.equals(command.getId(), commandId))
                .collect(Collectors.toList());
        Command command = selected.isEmpty() ? new Command("", "", 0, 0, -1, new ArrayList<>()) : selected.get(0);
        displayThresholdTable(espPackets, command);
    }

    public void addTableRow(Command data) {
        if(selectedSetting == -1) {
            currentCommands.add(data);
            TableRow row = new TableRow(requireContext());
            row.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));

            TableRow.LayoutParams iconButtonSizeparams = new TableRow.LayoutParams(
                    50,
                    80
            );
            TextView sequenceView = new TextView(requireContext());
            sequenceView.setText(String.valueOf(commandViewTable.getChildCount()));
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
            thresholdBtn.setText(R.string.threshold);
            thresholdBtn.setTag(data.getId());

            thresholdBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ripple_info_button));
            thresholdBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.basic_button_text_color));
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
                    ImageButton button = (ImageButton) v;
                    LinearLayout linearLayout = (LinearLayout) button.getParent();
                    TableRow tr = (TableRow) linearLayout.getParent();
                    int index = commandViewTable.indexOfChild(tr);
                    Command selectedCommand = currentCommands.get(index - 1);
                    commandCodeEdit.setText(selectedCommand.getCommandCode());
                    time1Edit.setText(selectedCommand.getTime1().toString());
                    time2Edit.setText(selectedCommand.getTime2().toString());
                    selectedSetting = index - 1;
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
                                        Toast.makeText(requireContext(), R.string.deleted_successfully, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
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
            Map<Integer, Command> currentCommandMap = new HashMap<>();
            int curCommandsCnt = currentCommands.size();
            DNDHelper.enableRowDragAndDrop(changeOrderBtn, row, commandViewTable, currentCommands, result -> {
//                result.sort()
            });
            row.addView(btnLayout);
            commandViewTable.addView(row);
        }
        else {
            currentCommands.set(selectedSetting, data);
            TableRow tableRow = (TableRow) commandViewTable.getChildAt(selectedSetting);
            TextView ccView = (TextView) tableRow.getChildAt(0);
            TextView t1View = (TextView) tableRow.getChildAt(1);
            TextView t2View = (TextView) tableRow.getChildAt(2);
            ccView.setText(data.getCommandCode());
            t1View.setText(String.valueOf(data.getTime1()));
            t2View.setText(String.valueOf(data.getTime2()));
        }
    }

    public void initEditControls() {
        commandCodeEdit.setText("");
        time1Edit.setText("");
        time2Edit.setText("");
    }

    public void displayCommandsTable(List<Command> commands) {
        if (commands.isEmpty()) return;
        commands.sort((a, b) -> a.getDisplayOrder() - b.getDisplayOrder());
        for (int i = 0; i < commands.size(); i ++) {
            addTableRow(commands.get(i));
        }
    }

    public void displayThresholdTable(List<ESPPacket> sas, Command command) {
        Log.d("THis is start of displayThresholdTable", new Gson().toJson(command));
        thresholdEditTable.removeViews(1, thresholdEditTable.getChildCount() - 1);
        List<CommandThreshold> commandThresholds = command.getThresholds();
        int rowCnt = 0;
        for (int i = 0; i < sas.size(); i ++) {
            ESPPacket sa = sas.get(i);
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

            if (commands.size() == 0) {
                Toast.makeText(requireContext(), R.string.no_command_to_send, Toast.LENGTH_SHORT).show();
            }
            Command command = commands.get(0);
            ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 2 + 6 + 4 + 4);
            buffer.put((byte) Integer.parseInt(command.getCommandCode().replace("0x", ""), 16));                    // 1 byte
            buffer.putFloat(command.getTime1());     // 2 bytes
            buffer.putFloat(command.getTime2());     // 2 bytes
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Sending data : via TCP",
                    Constants.LOGGING_BEARER_TOKEN
            );
            Log.d("WiFi Error", "Sending data : via TCP");

            socketManager.sendTCP(buffer.array(), new WiFiSocketManager.Callback() {
                @Override
                public void onSuccess(byte[] response) {
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "Data transmitted successfully via TCP",
                            Constants.LOGGING_BEARER_TOKEN
                    );
                    Log.d("WiFi Success", "Data transmitted successfully via TCP");
                }

                @Override
                public void onError(Exception e) {
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "Data transmission failed via TCP",
                            Constants.LOGGING_BEARER_TOKEN
                    );
                    Log.d("WiFi Success", "Data transmission failed via TCP");
                }
            });
        } else {
            Log.e("Fragment", "TCP not connected yet");
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

    public void handleClickAddBtn() {
        commandCode = commandCodeEdit.getText().toString();
        time1 = Float.parseFloat(time1Edit.getText().toString());
        time2 = Float.parseFloat(time2Edit.getText().toString());
        Command command = new Command("", commandCode, time1, time2, -1, currentCommandThresholds);
        if (checkIfFirstAdd(commandViewTable)) commandViewTable.removeViewAt(1);
        addTableRow(command);
        initEditControls();
        selectedSetting = -1;
    }

    public void handleClickSaveBtn(View v) {
        String title = idAutoComplete.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_the_title_first, Toast.LENGTH_SHORT).show();
            return;
        }
        List<Command> results = new ArrayList<>();
        try {
            int rowsCnt = commandViewTable.getChildCount();
            for (int i = 1; i < rowsCnt; i++) {
                Command result = getCommandFromTableRow(i, title);
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
        if (allTitles.contains(title)) msg = "Records with the same title already exist. Are you sure you want to update this Sensor Setting Data?";
        else msg = "Are you sure you want to save this Sensor Setting Data?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage(msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {

                        if (!allTitles.contains(title)) {
                            commandViewModel.insertBatch(results, insertResults -> {
                                if (results.size() == insertResults.size()) {
                                    Toast.makeText(requireContext(), R.string.sensor_setting_saved_successfully, Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                                    currentSettingTitle = "";
                                    idAutoComplete.setText("");
                                } else {
                                    Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            commandViewModel.updateBatch(results, updateResults -> {
                                if (results.size() == updateResults.size()) {
                                    Toast.makeText(requireContext(), "Sensor Setting updated successfully!", Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                                    currentSettingTitle = "";
                                    idAutoComplete.setText("");
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


    public void handleClickLoadBtn(View v) {
        String title = idAutoComplete.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Sensor Setting title.", Toast.LENGTH_SHORT).show();
            return;
        }
        commandViewModel.getByTitle(title, results -> {
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "There is no records with that title", Toast.LENGTH_SHORT).show();
                initEditControls();
                commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                return;
            }
            else {
                Toast.makeText(requireContext(), "Successfully loaded Sensor Setting from db.", Toast.LENGTH_SHORT).show();
                currentSettingTitle = results.get(0).getTitle();
                int len = results.size();
                commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                for (int i = 0; i < len; i ++) {
                    addTableRow(results.get(i));
                }
            }
        });
    }

    public Command getCommandFromTableRow(int index, String title) {
        try {
            TableRow tableRow = (TableRow) commandViewTable.getChildAt(index);
            if (tableRow == null) {
                return null;
            }
            TextView ccView = (TextView) tableRow.getChildAt(1);
            TextView t1View = (TextView) tableRow.getChildAt(2);
            TextView t2View = (TextView) tableRow.getChildAt(3);

            Command result = new Command(
                    title,
                    ccView.getText().toString().trim(),
                    Float.parseFloat(t1View.getText().toString().trim()),
                    Float.parseFloat(t2View.getText().toString().trim()),
                    index,
                    new ArrayList<>()
            );
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
