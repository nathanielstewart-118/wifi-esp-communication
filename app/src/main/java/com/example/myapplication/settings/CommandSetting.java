package com.example.myapplication.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
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
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.entity.Command;

import java.util.ArrayList;
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

    private String commandCode;
    private Float time1;
    private Float time2;
    private int selectedSetting = -1;
    private List<Command> commandDTOS = new ArrayList<>();

    public CommandSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View commandFragment = inflater.inflate(R.layout.fragment_command, container, false);

        commandViewTable = (TableLayout) commandFragment.findViewById(R.id.command_view_table);

        commandCodeEdit = (EditText) commandFragment.findViewById(R.id.command_code_input);
        time1Edit = (EditText) commandFragment.findViewById(R.id.command_time1_float_input);
        time2Edit = (EditText) commandFragment.findViewById(R.id.command_time2_float_input);

        openThresholdDialogBtn = (Button) commandFragment.findViewById(R.id.command_open_threshold_modal_btn);
        addCommandBtn = (Button) commandFragment.findViewById(R.id.command_add_btn);
        sendCommandBtn = (Button) commandFragment.findViewById(R.id.command_send_btn);
        openThresholdDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandSetting.this.openThresholdDialog(0);
            }
        });

        addCommandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandCode = commandCodeEdit.getText().toString();
                time1 = Float.parseFloat(time1Edit.getText().toString());
                time2 = Float.parseFloat(time2Edit.getText().toString());
                Command commandDTO = new Command(commandCode, time1, time2, "");
                CommandSetting.this.addTableRow(commandDTO, selectedSetting);
                CommandSetting.this.initEditControls();
                if (selectedSetting == -1) commandDTOS.add(commandDTO);
                else {
                    commandDTOS = commandDTOS.stream()
                            .map(s -> Objects.equals(s.getCommandCode(), commandDTO.getCommandCode()) ? commandDTO : s)
                            .collect(Collectors.toList());
                    commandViewTable.removeViewAt(selectedSetting + 1);
                }
                selectedSetting = -1;

            }
        });
        return commandFragment;
    }

    public void openThresholdDialog(int commandNo) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.command_layout_dialog, null);
        thresholdEditTable = (TableLayout) dialogView.findViewById(R.id.command_threshold_edit_table);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Close button action
        List<SensorActuator> settings = new ArrayList<>();
        settings.forEach(s -> {
            int cnt = s.getNumberOfChannels();
            for (int i = 0; i < cnt; i ++) {
                TableRow row = new TableRow(requireContext());

                TextView orderTypeView = new TextView(requireContext());
                orderTypeView.setText(String.format("%s / %s", s.getVariableName(), s.getDataType()));
                orderTypeView.setGravity(Gravity.CENTER);
                if (i > 0) orderTypeView.setVisibility(View.INVISIBLE);
                row.addView(orderTypeView);

                TextView channelView = new TextView(requireContext());
                channelView.setText(String.valueOf(i + 1));
                channelView.setGravity(Gravity.CENTER);
                row.addView(channelView);

                EditText thresholdView = new EditText(requireContext());
                row.addView(thresholdView);

                thresholdEditTable.addView(row);
            }

        });


        closeThresholdDialogBtn = dialogView.findViewById(R.id.command_close_threshold_modal_btn);
        closeThresholdDialogBtn.setOnClickListener(v -> dialog.dismiss());

        // Save button action
        saveThresholdBtn = dialogView.findViewById(R.id.command_threshold_save_btn);
        saveThresholdBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Data Saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Optionally close after saving
        });
    }

    public void addTableRow(Command data, int pos) {
        TableRow row = new TableRow(requireContext());
        TableRow.LayoutParams iconButtonSizeparams = new TableRow.LayoutParams(
                50,
                80
        );
        TextView sequenceView = new TextView(requireContext());
        int sequence = pos == -1 ? commandViewTable.getChildCount() : pos;
        sequenceView.setText(String.valueOf(sequence));
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
        thresholdBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bs_secondary));
        thresholdBtn.setLayoutParams(iconButtonSizeparams);
        thresholdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        thresholdBtn.setGravity(Gravity.CENTER);
        row.addView(thresholdBtn);

        ImageButton changeValueBtn = new ImageButton(requireContext());
        changeValueBtn.setImageResource(R.drawable.baseline_edit_24);
        changeValueBtn.setBackgroundColor(Color.TRANSPARENT);
        changeValueBtn.setColorFilter(Color.parseColor("#198754"));
        changeValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThresholdDialog(1);
            }
        });
        LinearLayout btnLayout = new LinearLayout(requireContext());
        btnLayout.setGravity(Gravity.CENTER);
        btnLayout.addView(changeValueBtn);

        ImageButton changeOrderBtn = new ImageButton(requireContext());
        changeOrderBtn.setImageResource(R.drawable.baseline_bar_chart_24);
        changeOrderBtn.setBackgroundColor(Color.TRANSPARENT);
        changeOrderBtn.setColorFilter(Color.parseColor("#0dcaf0"));
        changeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThresholdDialog(1);
            }
        });
        btnLayout.addView(changeOrderBtn);

        ImageButton deleteBtn = new ImageButton(requireContext());
        deleteBtn.setLayoutParams(iconButtonSizeparams);
        deleteBtn.setImageResource(R.drawable.baseline_delete_24);
        deleteBtn.setBackgroundColor(Color.TRANSPARENT);
        deleteBtn.setColorFilter(Color.parseColor("#dc3545"));
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThresholdDialog(1);
            }
        });
        btnLayout.addView(deleteBtn);
        row.addView(btnLayout);
        if (pos > -1) commandViewTable.addView(row, pos);
        else commandViewTable.addView(row);

    }

    public void initEditControls() {
        commandCodeEdit.setText("");
        time1Edit.setText("");
        time2Edit.setText("");
    }


}
