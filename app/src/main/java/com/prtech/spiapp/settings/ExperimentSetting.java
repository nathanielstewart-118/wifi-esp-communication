package com.prtech.spiapp.settings;

import static com.prtech.spiapp.utils.CommonUtils.string2Float;
import static com.prtech.spiapp.utils.CommonUtils.string2Int;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.button.MaterialButton;
import com.prtech.spiapp.R;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.entity.Experiment;
import com.prtech.spiapp.db.viewmodel.CommandViewModel;
import com.prtech.spiapp.db.viewmodel.ExperimentViewModel;
import com.prtech.spiapp.utils.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExperimentSetting extends Fragment {

    private AutoCompleteTextView idAutocomplete;
    private ExperimentViewModel experimentViewModel;
    private CommandViewModel commandViewModel;
    private MaterialButton saveBtn;
    private MaterialButton loadBtn;
    private MaterialButton reloadBtn;
    private MaterialButton updateBtn;
    private MaterialButton setSaveBtn;
    private EditText nTrialsEdit;
    private EditText commandEdit;
    private EditText restEdit;
    private EditText restRandomEdit;
    private EditText preRunEdit;
    private EditText postRunEdit;
    private EditText runTimeEdit;
    private EditText commandList;

    private TextView nTrialsView;
    private TextView commandView;
    private TextView restView;
    private TextView restRandomView;
    private TextView preRunView;
    private TextView postRunView;
    private TextView runTimeView;
    private Integer currentSetId = 0;
    private List<String> allTitles = new ArrayList<>();
    private List<ToggleButton> toggleButtons = new ArrayList<>();
    private List<Experiment> currentExperiments = new ArrayList<>();
    public ExperimentSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[3]);
        idAutocomplete = (AutoCompleteTextView) view.findViewById(R.id.id_autocomplete);
        nTrialsEdit = view.findViewById(R.id.experiment_n_trials_text);
        nTrialsView = view.findViewById(R.id.experiment_n_trials_view);
        commandEdit = view.findViewById(R.id.experiment_command_text);
        commandView = view.findViewById(R.id.experiment_command_view);
        restEdit = view.findViewById(R.id.experiment_rest_text);
        restView = view.findViewById(R.id.experiment_rest_view);
        restRandomEdit = view.findViewById(R.id.experiment_rest_random_text);
        restRandomView = view.findViewById(R.id.experiment_rest_random_view);
        preRunEdit = view.findViewById(R.id.experiment_pre_run_text);
        preRunView = view.findViewById(R.id.experiment_pre_run_view);
        postRunEdit = view.findViewById(R.id.experiment_post_run_text);
        postRunView = view.findViewById(R.id.experiment_post_run_view);
        runTimeView = view.findViewById(R.id.experiment_run_time_view);

        setSaveBtn = view.findViewById(R.id.experiment_set_save_btn);
        setSaveBtn.setOnClickListener(v -> handleClickSetSaveBtn());

        initCurrentExperiments();
        experimentViewModel = new ViewModelProvider(requireActivity()).get(ExperimentViewModel.class);
        experimentViewModel.getAllTitles().observe(getViewLifecycleOwner(), data -> {
            Collections.sort(data);
            allTitles.clear();
            allTitles.addAll(data);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), // or getContext() if in Fragment
                    android.R.layout.simple_dropdown_item_1line,
                    allTitles
            );
            idAutocomplete.setAdapter(adapter);
        });

        commandList = view.findViewById(R.id.experiment_commands_select);
        commandViewModel = new ViewModelProvider((requireActivity())).get(CommandViewModel.class);
        commandViewModel.getAllTitles().observe(getViewLifecycleOwner(), data -> {
            int cnt = data.size();
            boolean[] checkedItems = new boolean[cnt];
            for (int i = 0; i < cnt; i ++) {
                checkedItems[i] = false;
            }
            String[] candidates = data.toArray(new String[0]);
            commandList.setOnClickListener(v -> {
                if (data.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.no_commands_yet, Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.select_commands)
                    .setMultiChoiceItems(candidates, checkedItems, (dialog, which, isChecked) -> {
                        checkedItems[which] = isChecked;
                    })
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        List<String> selected = new ArrayList<>();
                        String commandListString = "";
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                Log.d(getString(R.string.selected), candidates[i]);
                                selected.add(candidates[i]);
                                commandListString += candidates[i] + ", ";
                            }
                        }
                        if(!commandListString.isEmpty()) commandList.setText(commandListString.substring(0, commandListString.length() - 2));
                    })
                    .setOnDismissListener(dialog -> {
                    })
                    .show();
            });
        });

        saveBtn = view.findViewById(R.id.experiment_save_btn);
        saveBtn.setOnClickListener(v -> handleClickSaveBtn());

        loadBtn = view.findViewById(R.id.experiment_load_btn);
        loadBtn.setOnClickListener(v -> handleClickLoadBtn());

        reloadBtn = view.findViewById(R.id.experiment_reload_btn);
        reloadBtn.setOnClickListener(v -> handleClickReloadBtn());

        updateBtn = view.findViewById(R.id.experiment_update_btn);
        updateBtn.setOnClickListener(v -> handleClickUpdateBtn());

        toggleButtons.add((ToggleButton) view.findViewById(R.id.experiment_set1_btn));
        toggleButtons.add((ToggleButton) view.findViewById(R.id.experiment_set2_btn));
        toggleButtons.add((ToggleButton) view.findViewById(R.id.experiment_set3_btn));
        toggleButtons.add((ToggleButton) view.findViewById(R.id.experiment_set4_btn));




        for (ToggleButton toggle: toggleButtons) {
            toggle.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (isChecked) {
                    LinearLayout linearLayout = (LinearLayout) buttonView.getParent();
                    currentSetId = linearLayout.indexOfChild(buttonView);
                    uncheckOtherToggles((ToggleButton) buttonView);
                    setValuesIntoUIs(currentExperiments.get(currentSetId));
                }
            }));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void handleClickSaveBtn() {
        String title = idAutocomplete.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_the_experiment_title, Toast.LENGTH_SHORT).show();
            return;
        }
        String msg = "";
        if (allTitles.contains(title)) msg = "Records with the same title already exist. Are you sure you want to update this Sensor Setting Data?";
        else msg = "Are you sure you want to save this Sensor Setting Data?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage(msg)
                .setPositiveButton("Yes", (dialog, which) -> {

                    Experiment experiment = getValuesFromUI();
                    currentExperiments.set(currentSetId, experiment);
                    currentExperiments = currentExperiments
                            .stream()
                            .map(exp -> {
                                exp.setId(null);
                                exp.setTitle(title);
                                return exp;
                            })
                            .collect(Collectors.toList());
                    // Handle Yes button click
                    if(allTitles.contains(title)) {
                        experimentViewModel.updateBatch(currentExperiments, results -> {
                            if(currentExperiments.size() == results.size()) {
                                Toast.makeText(requireContext(), R.string.updated_data_successfully, Toast.LENGTH_SHORT).show();
                                initUIValues(0);
                                initCurrentExperiments();
                            }
                            else {
                                Toast.makeText(requireContext(), R.string.failed_to_update_data, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        experimentViewModel.insertBatch(currentExperiments, results -> {
                            if (currentExperiments.size() == results.size()) {
                                Toast.makeText(requireContext(), R.string.saved_data_successfully, Toast.LENGTH_SHORT).show();
                                initUIValues(0);
                                initCurrentExperiments();
                            } else {
                                Toast.makeText(requireContext(), R.string.failed_to_save_data, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                // Handle No button click (optional)
                dialog.dismiss();
            })
            .show();
    }

    public void handleClickLoadBtn() {
        String title = idAutocomplete.getText().toString();
        experimentViewModel.findExperimentsByTitle(title, data -> {
            if (data.isEmpty()) {
                Toast.makeText(requireContext(), R.string.can_t_find_the_corresponding_experiment_data, Toast.LENGTH_SHORT).show();
                return;
            }
            currentExperiments.clear();
            currentExperiments.addAll(data);
            setValuesIntoUIs(currentExperiments.get(currentSetId));
            Toast.makeText(requireContext(), R.string.loaded_experiment_data_successfully, Toast.LENGTH_SHORT).show();
        });
    }

    public void handleClickReloadBtn() {
        initUIValues(1);
    }

    public void handleClickUpdateBtn() {

    }

    public void uncheckOtherToggles(ToggleButton selectedToggle) {
        for (ToggleButton toggle: toggleButtons) {
            if (toggle != selectedToggle) {
                toggle.setChecked(false);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void setValuesIntoUIs(Experiment experiment) {

//        idAutocomplete.setText(experiment.getTitle() == null ? "" : experiment.getTitle());
        nTrialsView.setText(experiment.getNumberOfTrials() == null ? "" : String.valueOf(experiment.getNumberOfTrials()));
        nTrialsEdit.setText(experiment.getNumberOfTrials() == null ? "" : String.valueOf(experiment.getNumberOfTrials()));
        commandView.setText(experiment.getCommand() == null ? "" : String.valueOf(experiment.getCommand()));
        commandEdit.setText(experiment.getCommand() == null ? "" : String.valueOf(experiment.getCommand()));
        restView.setText(experiment.getRest() == null ? "" : String.valueOf(experiment.getRest()));
        restEdit.setText(experiment.getRest() == null ? "" : String.valueOf(experiment.getRest()));
        restRandomView.setText(experiment.getRestRandom() == null ? "" : String.valueOf(experiment.getRestRandom()));
        restRandomEdit.setText(experiment.getRestRandom() == null ? "" : String.valueOf(experiment.getRestRandom()));
        preRunView.setText(experiment.getPreRun() == null ? "" : String.valueOf(experiment.getPreRun()));
        preRunEdit.setText(experiment.getPreRun() == null ? "" : String.valueOf(experiment.getPreRun()));
        postRunView.setText(experiment.getPostRun() == null ? "" : String.valueOf(experiment.getPostRun()));
        postRunEdit.setText(experiment.getPostRun() == null ? "" : String.valueOf(experiment.getPostRun()));
        commandList.setText(experiment.getCommands() == null ? "" : String.join(", ", experiment.getCommands()));

        Integer nTrials = experiment.getNumberOfTrials() == null ? 0 : experiment.getNumberOfTrials();
        Float runTime = (experiment.getPreRun() == null ? 0 : experiment.getPreRun()) +
                (experiment.getPostRun() == null ? 0 : experiment.getPostRun()) +
                (experiment.getRest() == null ? 0 : experiment.getRest()) * (nTrials - 1) +
                (experiment.getCommand() == null ? 0 : experiment.getCommand()) * nTrials;
        runTimeView.setText(String.valueOf(runTime));
     }

    public void initUIValues(int mode) {
        if (mode == 0) {
            idAutocomplete.setText("");
            nTrialsView.setText("");
            commandView.setText("");
            restView.setText("");
            restRandomView.setText("");
            preRunView.setText("");
            postRunView.setText("");
            runTimeView.setText("");
        }

        nTrialsEdit.setText("");
        commandEdit.setText("");
        restEdit.setText("");
        restRandomEdit.setText("");
        preRunEdit.setText("");
        postRunEdit.setText("");
        commandList.setText("");
    }

    public void handleClickSetSaveBtn() {
        Experiment experiment = getValuesFromUI();
        currentExperiments.set(currentSetId, experiment);
        Toast.makeText(requireContext(), R.string.set_data_saved_successfully, Toast.LENGTH_SHORT).show();
    }

    public Experiment getValuesFromUI() {
        String title = idAutocomplete.getText().toString().trim();
        Integer nTrials = null;
        Float command = null;
        Float rest = null;
        Float rest_random = null;
        Float pre_run = null;
        Float post_run = null;
        List<String> commands = new ArrayList<>();

        try {
            nTrials = string2Int(nTrialsEdit.getText().toString().trim(), null);
            command = string2Float(commandEdit.getText().toString().trim(), null);
            rest = string2Float(restEdit.getText().toString().trim(), null);
            rest_random = string2Float(restRandomEdit.getText().toString().trim(), null);
            pre_run = string2Float(preRunEdit.getText().toString().trim(), null);
            post_run = string2Float(postRunEdit.getText().toString().trim(), null);
            commands = Arrays.stream(commandList.getText().toString().trim().split(","))
                    .map(c -> c.trim())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Experiment(title, commands, currentSetId, nTrials, command, pre_run, post_run, rest, rest_random);
    }

    public void initCurrentExperiments() {
        Experiment exp = new Experiment(null, null, null, null, null, null, null, null, null);
        currentExperiments = new ArrayList<>(Arrays.asList(exp, exp, exp, exp));
    }

}
