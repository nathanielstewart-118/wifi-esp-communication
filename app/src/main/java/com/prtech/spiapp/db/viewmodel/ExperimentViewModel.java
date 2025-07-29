package com.prtech.spiapp.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.ExperimentDao;
import com.prtech.spiapp.db.entity.Experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ExperimentViewModel extends AndroidViewModel {
    private final ExperimentDao experimentDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();
    private final MutableLiveData<List<Experiment>> experimentsByExperimentId = new MutableLiveData<>();

    public ExperimentViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.experimentDao = db.experimentDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Experiment>> getAllExperiments() {
        return experimentDao.getAllExperiments();
    }

    // --- INSERT ---
    public void insert(Experiment experiment) {
        executorService.execute(() -> {
            long result = experimentDao.insert(experiment);
            insertResult.postValue(result);
        });
    }

    public void insertBatch(List<Experiment> experiments, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            for (Experiment experiment: experiments) {
                long result = experimentDao.insert(experiment);
                results.add(result);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }

    public LiveData<Long> getInsertResult() {
        return insertResult;
    }

    // --- UPDATE ---
    public void update(Experiment experiment) {
        executorService.execute(() -> {
            int result = experimentDao.update(experiment);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    // --- DELETE ---
    public void delete(Experiment experiment) {
        executorService.execute(() -> {
            int result = experimentDao.delete(experiment);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    public void getAllTitles(Consumer<List<String>> callback) {
        executorService.execute(() -> {
            List<String> results = experimentDao.getAllTitles();
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }

    public void findExperimentsByTitle(String title, Consumer<List<Experiment>> callback) {
        executorService.execute(() -> {
            List<Experiment> results = experimentDao.getExperimentsByTitle(title);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    public void updateBatch(List<Experiment> experiments, Consumer<List<Long>> callback) {
        if(experiments.isEmpty()) {
            callback.accept(new ArrayList<>());
        }
        executorService.execute(() -> {
            String title = experiments.get(0).getTitle();
            experimentDao.deleteByTitle(title);
            List<Long> results = new ArrayList<>();
            for (Experiment exp: experiments) {
                Long result = experimentDao.insert(exp);
                results.add(result);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }
}
