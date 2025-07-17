package com.example.myapplication.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.entity.Experiment;
import com.example.myapplication.db.viewmodel.CommandViewModel;
import com.example.myapplication.db.viewmodel.ExperimentViewModel;
import com.example.myapplication.utils.commonuis.MultiSelectDialog;

import java.util.List;

public class ExperimentSetting extends Fragment {

    private AutoCompleteTextView idAutocomplete;
    private ExperimentViewModel experimentViewModel;
    private CommandViewModel commandViewModel;
    private Button saveBtn;
    private Button loadBtn;
    private Button reloadBtn;
    private Button updateBtn;
    private Button set1Btn;
    private Button set2Btn;
    private Button set3Btn;
    private Button set4Btn;
    private EditText nTrialsEdit;
    private EditText commandEdit;
    private EditText restEdit;
    private EditText restRandom;
    private EditText preRun;
    private EditText postRun;
    private EditText runTime;
    private MultiSelectDialog commandSelect;
    private EditText commandList;

    private Experiment currentExperiment;
    public ExperimentSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment, container, false);
        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.id_autocomplete);
        currentExperiment = new Experiment("", "", -1L, 0, 0F, 0F, 0F, 0F);
        experimentViewModel = new ViewModelProvider(requireActivity()).get(ExperimentViewModel.class);
        experimentViewModel.getAllExperiments().observe(getViewLifecycleOwner(), data -> {
            List<Experiment> dataArray = (List<Experiment>) data;
            int cnt = dataArray.size();
            String[] candidates = new String[cnt];
            for (int i = 0; i < cnt; i ++) {
                candidates[i] = dataArray.get(i).getExperimentId();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), // or getContext() if in Fragment
                    android.R.layout.simple_dropdown_item_1line,
                    candidates
            );

            idAutocomplete.setAdapter(adapter);
        });

        commandList = view.findViewById(R.id.experiment_command_select);
        commandViewModel = new ViewModelProvider((requireActivity())).get(CommandViewModel.class);
        commandViewModel.getAllCommands().observe(getViewLifecycleOwner(), data -> {
           List<Command> dataArray = (List<Command>) data;
           int cnt = dataArray.size();
            String[] candidates = new String[cnt];
            boolean[] checkedItems = new boolean[cnt];
            for (int i = 0; i < cnt; i ++) {
                candidates[i] = dataArray.get(i).getCommandCode();
                checkedItems[i] = false;
            }
            commandList.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Select Commands")
                    .setMultiChoiceItems(candidates, checkedItems, (dialog, which, isChecked) -> {
                        checkedItems[which] = isChecked;
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                Log.d("SELECTED", candidates[i]);
                            }
                        }
                    })
                    .show();
            });
        });

        saveBtn = (Button) view.findViewById(R.id.experiment_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        loadBtn = (Button) view.findViewById(R.id.experiment_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn());

        reloadBtn = (Button) view.findViewById(R.id.experiment_reload_btn);
        reloadBtn.setOnClickListener(v -> handleClickReloadBtn());

        updateBtn = (Button) view.findViewById(R.id.experiment_update_btn);
        updateBtn.setOnClickListener(v -> handleClickUpdateBtn());

        set1Btn = (Button) view.findViewById(R.id.experiment_set1_btn);


        set2Btn = (Button) view.findViewById(R.id.experiment_set2_btn);


        set3Btn = (Button) view.findViewById(R.id.experiment_set3_btn);


        set4Btn = (Button) view.findViewById(R.id.experiment_set4_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void handleClickSaveBtn() {
        int nTrials = Integer.parseInt(nTrialsEdit.getText().toString());
        float command = Float.parseFloat(commandEdit.getText().toString());
        float rest = Float.parseFloat(restEdit.getText().toString());
        float rest_random = Float.parseFloat(restRandom.getText().toString());
        float pre_run = Float.parseFloat(restRandom.getText().toString());
        float post_run = Float.parseFloat(postRun.getText().toString());


        currentExperiment.setNumberOfTrials(nTrials);
        currentExperiment.setCommand(command);
        currentExperiment.setRest(rest);
        currentExperiment.setRestRandom(rest_random);
        currentExperiment.setPreRun(pre_run);
        currentExperiment.setPostRun(post_run);

        int experimentIndex = idAutocomplete.getListSelection();
        String experimentId = idAutocomplete.getText().toString();
    }

    public void handleClickLoadBtn() {

    }

    public void handleClickReloadBtn() {

    }

    public void handleClickUpdateBtn() {

    }
}
