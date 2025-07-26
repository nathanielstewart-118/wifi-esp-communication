package com.example.myapplication.settings;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.entity.Experiment;
import com.example.myapplication.db.viewmodel.CommandViewModel;
import com.example.myapplication.db.viewmodel.ExperimentViewModel;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.commonuis.MultiSelectDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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


    private Experiment currentExperiment;
    public ExperimentSetting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Constants.TITLES[6]);
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


        currentExperiment = new Experiment("", "", new ArrayList<>(), 0, 0F, 0F, 0F, 0F);
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

        commandList = view.findViewById(R.id.experiment_commands_select);
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
                if (candidates.length == 0) {
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
                        commandList.setText(commandListString.substring(0, commandListString.length() - 2));
                        currentExperiment.setCommands(selected);
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
        set1Btn.setOnClickListener(v -> {
            currentExperiment.setExperimentSet("Set1");
        });

        set2Btn = (Button) view.findViewById(R.id.experiment_set2_btn);
        set2Btn.setOnClickListener(v -> {
            currentExperiment.setExperimentSet("Set2");
        });

        set3Btn = (Button) view.findViewById(R.id.experiment_set3_btn);
        set3Btn.setOnClickListener(v -> {
            currentExperiment.setExperimentSet("Set3");
        });

        set4Btn = (Button) view.findViewById(R.id.experiment_set4_btn);
        set4Btn.setOnClickListener(v -> {
            currentExperiment.setExperimentSet("Set4");
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void handleClickSaveBtn() {
        String experimentId = idAutocomplete.getText().toString();
        if (experimentId.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_the_experiment_title, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.are_you_sure_you_want_to_save_this_data)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                // Handle Yes button click
                int nTrials = Integer.parseInt(nTrialsEdit.getText().toString());
                float command = Float.parseFloat(commandEdit.getText().toString());
                float rest = Float.parseFloat(restEdit.getText().toString());
                float rest_random = Float.parseFloat(restRandomEdit.getText().toString());
                float pre_run = Float.parseFloat(restRandomEdit.getText().toString());
                float post_run = Float.parseFloat(postRunEdit.getText().toString());

                currentExperiment.setExperimentId(experimentId);
                currentExperiment.setNumberOfTrials(nTrials);
                currentExperiment.setCommand(command);
                currentExperiment.setRest(rest);
                currentExperiment.setRestRandom(rest_random);
                currentExperiment.setPreRun(pre_run);
                currentExperiment.setPostRun(post_run);

                experimentViewModel.findExperimentsByExperimentId(experimentId, data -> {
                    if (data.isEmpty()) {
                        currentExperiment.setId(null);
                        experimentViewModel.insert(currentExperiment);
                        experimentViewModel.getInsertResult().observe(getViewLifecycleOwner(), id -> {
                            if (id != null && id > 0) {
                                Toast.makeText(getContext(), R.string.insert_success, Toast.LENGTH_SHORT).show();
                                currentExperiment.setId(-1L);
                                initUIValues(0);
                            } else {
                                Toast.makeText(getContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        experimentViewModel.update(currentExperiment);
                        experimentViewModel.getUpdateResult().observe(getViewLifecycleOwner(), id -> {
                            if (id != null && id > 0) {
                                Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                // Handle No button click (optional)
                dialog.dismiss();
            })
            .show();

    }

    public void handleClickLoadBtn() {
        String experimentId = idAutocomplete.getText().toString();
        experimentViewModel.findExperimentsByExperimentId(experimentId, data -> {
            if (data.isEmpty()) {
                Toast.makeText(requireContext(), R.string.can_t_find_the_corresponding_experiment_data, Toast.LENGTH_SHORT).show();
                return;
            }
            List<Experiment> results = (List<Experiment>) data;
            currentExperiment.copyFrom(results.get(0));
            setValuesIntoUIs(currentExperiment);
            Toast.makeText(requireContext(), R.string.loaded_experiment_data_successfully, Toast.LENGTH_SHORT).show();
        });
    }

    public void handleClickReloadBtn() {
        initUIValues(1);
    }

    public void handleClickUpdateBtn() {

    }

    @SuppressLint("SetTextI18n")
    public void setValuesIntoUIs(Experiment experiment) {
        idAutocomplete.setText(experiment.getExperimentId());
        nTrialsView.setText(String.valueOf(experiment.getNumberOfTrials()));
        commandView.setText(String.valueOf(experiment.getCommand()));
        restView.setText(String.valueOf(experiment.getRest()));
        restRandomView.setText(String.valueOf(experiment.getRestRandom()));
        preRunView.setText(String.valueOf(experiment.getPreRun()));
        postRunView.setText(String.valueOf(experiment.getPostRun()));
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


}
