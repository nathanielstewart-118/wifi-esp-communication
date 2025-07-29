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

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.prtech.spiapp.db.entity.CommandThreshold;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.viewmodel.CommandViewModel;
import com.prtech.spiapp.db.viewmodel.ESPPacketViewModel;
import com.prtech.spiapp.utils.Constants;
import com.prtech.spiapp.utils.DNDHelper;
import com.prtech.spiapp.utils.LogHelper;
import com.prtech.spiapp.utils.communications.WiFiSocketManager;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private Spinner espSpinner;
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
    private List<String> allTitles = new ArrayList<>();
    private CommandViewModel commandViewModel;
    private ESPPacketViewModel espPacketViewModel;
    private AppDatabase db;
    private List<ESPPacket> currentESPPackets = new ArrayList<>();
    private List<CommandThreshold> currentCommandThresholds = new ArrayList<>();
    private List<Command> currentCommands = new ArrayList<>();
    private WiFiSocketManager socketManager = WiFiSocketManager.getInstance();
    private Integer selectedCommandIndex = -1;
    private final Gson gson = new Gson();

    public CommandSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[2]);
        View commandFragment = inflater.inflate(R.layout.fragment_command, container, false);
        db = AppDatabase.getInstance(requireContext());
        commandViewModel = new ViewModelProvider(requireActivity()).get(CommandViewModel.class);
        thresholdEditTable = commandFragment.findViewById(R.id.command_thresholds_list_tb);
        espSpinner = (Spinner) commandFragment.findViewById(R.id.command_esp_packet_spinner);
        espPacketViewModel = new ViewModelProvider(requireActivity()).get(ESPPacketViewModel.class);
        espPacketViewModel.getAllTitles().observe(getViewLifecycleOwner(), data -> {
            int dataCnt = data.size();
            String[] options =  new String[dataCnt + 1];
            options[0] = "Recalling ESP Packet Settings";
            for (int i = 0; i < dataCnt; i ++) {
                options[1 + i] = data.get(i);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            espSpinner.setAdapter(adapter);
        });

        espSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                String selectedTitle = (String) parent.getItemAtPosition(position);
                espPacketViewModel.getByTitle(selectedTitle, results -> {
                    if(results.isEmpty()) return;
                    currentESPPackets.clear();
                    currentESPPackets.addAll(results);
                    displayTables();
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        commandViewModel.getAllTitles().observe(getViewLifecycleOwner(), data -> {
            allTitles.clear();
            Collections.sort(data);
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
//        thresholdEditTable = (TableLayout) thresholdDialog.findViewById(R.id.command_threshold_edit_table);
        espPacketViewModel.getAllSensorActuators().observe(getViewLifecycleOwner(), sas -> {
//            displayThresholdTable(sas, new Command("", 0, 0, new ArrayList<>()));
//            sensorActuators.addAll(sas);
        });
    }


    public void openThresholdDialog(Long commandId) {
        dialog.show();
        List<Command> selected = currentCommands.stream()
                .filter(command -> Objects.equals(command.getId(), commandId))
                .collect(Collectors.toList());
        Command command = selected.isEmpty() ? new Command("", "", 0, 0, -1, 0, new ArrayList<>()) : selected.get(0);
        displayThresholdTable(currentESPPackets, currentCommands);
    }

    public void addTableRow(Command data) {
            TableRow row = new TableRow(requireContext());
            row.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.table_border));
            row.setVerticalGravity(Gravity.CENTER);

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
                    selectedCommandIndex = index - 1;
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
                                ImageButton delButton = (ImageButton) v;
                                LinearLayout linearLayout = (LinearLayout) delButton.getParent();
                                TableRow tr = (TableRow) linearLayout.getParent();
                                int index = commandViewTable.indexOfChild(tr);
                                if(index < 1) return;
                                currentCommands.remove(index - 1);
                                displayTables();
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
            DNDHelper.enableRowDragAndDrop(changeOrderBtn, row, commandViewTable, currentCommands, result -> {
//                result.sort()
                Log.d("success", String.valueOf(currentCommands.size()));
            });
            row.addView(btnLayout);
            commandViewTable.addView(row);
    }

    public void initEditControls() {
        commandCodeEdit.setText("");
        time1Edit.setText("");
        time2Edit.setText("");
    }

    public void displayCommandsTable(List<Command> commands) {
        if (commands.isEmpty()) return;
        commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
        commands.sort((a, b) -> a.getDisplayOrder() - b.getDisplayOrder());
        for (int i = 0; i < commands.size(); i ++) {
            addTableRow(commands.get(i));
        }
    }

    public void displayTables() {
        displayCommandsTable(currentCommands);
        displayThresholdTable(currentESPPackets, currentCommands);
    }

    public void displayThresholdTable(List<ESPPacket> packets, List<Command> commands) {
        if(packets.isEmpty()) return;
        thresholdEditTable.removeViews(0, thresholdEditTable.getChildCount());
        int packetsCnt = packets.size();
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(getResources().getColor(R.color.bs_primary));
        TextView vNameHeaderView = new TextView(requireContext());
        vNameHeaderView.setTextColor(Color.WHITE);
        vNameHeaderView.setTypeface(null, Typeface.BOLD);
        vNameHeaderView.setGravity(Gravity.CENTER);
        vNameHeaderView.setText("Variable Name/\nData Type");
        headerRow.setPadding(20, 20, 20, 20);
        headerRow.addView(vNameHeaderView);

        TextView orderHeaderView = new TextView(requireContext());
        orderHeaderView.setText("Channel Order");
        orderHeaderView.setTextColor(Color.WHITE);
        orderHeaderView.setTypeface(null, Typeface.BOLD);
        orderHeaderView.setGravity(Gravity.CENTER);
        headerRow.addView(orderHeaderView);

        for (int i = 0; i < commands.size(); i ++) {
            LinearLayout linearLayout = new LinearLayout(requireContext());
            TextView commandHeaderView = new TextView(requireContext());
            commandHeaderView.setText(commands.get(i).getCommandCode());
            commandHeaderView.setTextColor(Color.WHITE);
            CheckBox cmdThresCheckBox = new CheckBox(requireContext());
            if (currentCommands.get(i).getActivated() == 1) cmdThresCheckBox.setChecked(true);
            linearLayout.addView(commandHeaderView);
            linearLayout.addView(cmdThresCheckBox);
            linearLayout.setGravity(Gravity.CENTER);
            headerRow.addView(linearLayout);
        }
        TextView lowerHeaderView = new TextView(requireContext());
        lowerHeaderView.setTextColor(Color.WHITE);
        lowerHeaderView.setText("Lower Threshold");
        lowerHeaderView.setTypeface(null, Typeface.BOLD);
        lowerHeaderView.setGravity(Gravity.CENTER);
        headerRow.addView(lowerHeaderView);
        TextView upperHeaderView = new TextView(requireContext());
        upperHeaderView.setText("Upper Threshold");
        upperHeaderView.setTextColor(Color.WHITE);
        upperHeaderView.setTypeface(null, Typeface.BOLD);
        upperHeaderView.setGravity(Gravity.CENTER);
        headerRow.addView(upperHeaderView);
        thresholdEditTable.addView(headerRow);
        for (int i = 0; i < packetsCnt; i ++) {             // for each loaded ESP Packet
            ESPPacket packet = packets.get(i);
            int nChannels = packet.getNumberOfChannels();
            for (int j = 0; j < nChannels; j ++) {          // for each channel in the ESP Packet
                TableRow tableRow = new TableRow(requireContext());
                tableRow.setPadding(20, 20, 20, 20);
                TextView vNameView = new TextView(requireContext());
                if(j == 0) {
                    vNameView.setText(packet.getVariableName() + "/" + packet.getDataType());
                }
                vNameView.setGravity(Gravity.CENTER);
                tableRow.addView(vNameView);
                TextView orderView = new TextView(requireContext());
                orderView.setGravity(Gravity.CENTER);
                orderView.setText(String.valueOf(j + 1));
                tableRow.addView(orderView);
                int commandsCnt = commands.size();
                for (int k = 0; k < commandsCnt; k ++) {            // For each command, add that to new column
                    EditText thresholdEdit = new EditText(requireContext());
                    thresholdEdit.setGravity(Gravity.CENTER);
                    try {
                        List<CommandThreshold> filtered = commands.get(k).getThresholds()
                                .stream()
                                .filter(t -> Objects.equals(t.getEspPacketId(), packet.getId()))
                                .collect(Collectors.toList());
                        if (!filtered.isEmpty())
                            thresholdEdit.setText(String.valueOf(filtered.get(0).getThresholds().get(j)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tableRow.addView(thresholdEdit);
                }

                TextView lowerView = new TextView(requireContext());
                lowerView.setGravity(Gravity.CENTER);
                lowerView.setText(String.valueOf(packet.getThresholds().get(j).getLowerLimit()));
                tableRow.addView(lowerView);

                TextView upperView = new TextView(requireContext());
                upperView.setGravity(Gravity.CENTER);
                upperView.setText(String.valueOf(packet.getThresholds().get(j).getUpperLimit()));
                tableRow.addView(upperView);
                thresholdEditTable.addView(tableRow);
            }
        }
    }

    public void handleClickSendCommandBtn() {

        if (socketManager.isTCPConnected()) {
            String dataString = "";

            if (currentCommands.size() == 0) {
                Toast.makeText(requireContext(), R.string.no_command_to_send, Toast.LENGTH_SHORT).show();
            }
            Command command = currentCommands.get(0);
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
        Command command = new Command("", commandCode, time1, time2, -1, 0, new ArrayList<>());
        if (selectedCommandIndex == -1) currentCommands.add(command);
        else currentCommands.set(selectedCommandIndex, command);
        initEditControls();
        displayTables();
        selectedSetting = -1;
    }

    public void handleClickSaveBtn(View v) {
        String title = idAutoComplete.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_the_title_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentCommands.isEmpty()) {
            Toast.makeText(requireContext(), "Please add some data to the table", Toast.LENGTH_SHORT).show();
            return;
        }
        String msg = "";
        if (allTitles.contains(title)) msg = "Records with the same title already exist. Are you sure you want to update this Sensor Setting Data?";
        else msg = "Are you sure you want to save this Sensor Setting Data?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage(msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        currentCommands = currentCommands
                            .stream()
                            .map(command -> {
                                command.setId(null);
                                command.setTitle(title);
                                return command;
                            })
                            .collect(Collectors.toList());
                        int commandsCnt = currentCommands.size();
                        int totalChannelsCnt = 1;
                        int packetsCnt = currentESPPackets.size();
                        for (int i = 0; i <packetsCnt; i ++) {
                            ESPPacket espPacket = currentESPPackets.get(i);
                            int nChannels = espPacket.getNumberOfChannels();
                            for (int j = 0; j < nChannels; j ++) {
                                TableRow tableRow = (TableRow) thresholdEditTable.getChildAt(totalChannelsCnt);
                                for (int k = 0; k < commandsCnt; k ++) {
                                    EditText commandThresholdEdit = (EditText) tableRow.getChildAt(2 + k);
                                    String val = commandThresholdEdit.getText().toString().trim();
                                    int threshold = 0;
                                    try {
                                        threshold = Integer.parseInt(val);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(currentCommands.get(k).getThresholds().size() < i + 2) {
                                        currentCommands.get(k).getThresholds().add(new CommandThreshold(0L, new ArrayList<>()));
                                    }
                                    currentCommands.get(k).getThresholds().get(i).setEspPacketId(espPacket.getId());
                                    if (currentCommands.get(k).getThresholds().get(i).getThresholds() == null) currentCommands.get(k).getThresholds().get(i).setThresholds(new ArrayList<>());
                                    currentCommands.get(k).getThresholds().get(i).getThresholds().add(threshold);
//                                    else {
//                                        currentCommands.get(k).getThresholds().add(cThreshold);
//                                    }
                                }
                                totalChannelsCnt ++;
                            }
                        }
                        TableRow headerRow = (TableRow) thresholdEditTable.getChildAt(0);
                        if(!currentESPPackets.isEmpty()) {
                            for (int i = 0; i < commandsCnt; i++) {
                                LinearLayout linearLayout = (LinearLayout) headerRow.getChildAt(2 + i);
                                CheckBox checkBox = (CheckBox) linearLayout.getChildAt(1);
                                if (checkBox.isChecked()) currentCommands.get(i).setActivated(1);
                                else currentCommands.get(i).setActivated(0);
                            }
                        }
                        if (!allTitles.contains(title)) {
                            commandViewModel.insertBatch(currentCommands, insertResults -> {
                                if (currentCommands.size() == insertResults.size()) {
                                    Toast.makeText(requireContext(), R.string.sensor_setting_saved_successfully, Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                                    thresholdEditTable.removeAllViews();
                                    currentSettingTitle = "";
                                    idAutoComplete.setText("");
                                } else {
                                    Toast.makeText(requireContext(), R.string.an_error_occurred_while_saving_please_load_and_check, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            commandViewModel.updateBatch(currentCommands, updateResults -> {
                                if (currentCommands.size() == updateResults.size()) {
                                    Toast.makeText(requireContext(), "Sensor Setting updated successfully!", Toast.LENGTH_SHORT).show();
                                    initEditControls();
                                    commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                                    thresholdEditTable.removeAllViews();
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
            Toast.makeText(requireContext(), "Please enter Command Setting title.", Toast.LENGTH_SHORT).show();
            return;
        }
        commandViewModel.getByTitle(title, results -> {
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "There is no records with that title", Toast.LENGTH_SHORT).show();
                initEditControls();
                commandViewTable.removeViews(1, commandViewTable.getChildCount() - 1);
                thresholdEditTable.removeAllViews();
                return;
            }
            else {
                Toast.makeText(requireContext(), "Successfully loaded Sensor Setting from db.", Toast.LENGTH_SHORT).show();
                currentCommands.clear();
                currentCommands.addAll(results);
                List<Long> packetIds = results.get(0).getThresholds()
                                .stream()
                                .map(t -> t.getEspPacketId())
                                .collect(Collectors.toList());
                espPacketViewModel.getByIds(packetIds, resultPackets-> {
                    currentESPPackets.clear();
                    currentESPPackets.addAll(resultPackets);
                    displayTables();
                });
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
                    0,
                    new ArrayList<>()
            );
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
