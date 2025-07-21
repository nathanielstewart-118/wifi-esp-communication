package com.example.myapplication.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.dao.VisualizationDao;
import com.example.myapplication.db.entity.Visualization;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class VisualizationViewModel extends AndroidViewModel {
    private final VisualizationDao visualizationDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();

    public VisualizationViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.visualizationDao = db.visualizationDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Visualization>> getAllVisualizations() {
        return visualizationDao.getAllVisualizations();
    }

    // --- INSERT ---
    public void insert(Visualization visualization, Consumer<Long> callback) {
        executorService.execute(() -> {
            long result = visualizationDao.insert(visualization);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }

    // --- UPDATE ---
    public void update(Visualization visualization, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int result = visualizationDao.update(visualization);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(result);
            });
        });
    }

    // --- DELETE ---
    public void delete(Visualization visualization) {
        executorService.execute(() -> {
            int result = visualizationDao.delete(visualization);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    public void getByVisualizationId(String vId, Consumer<List<Visualization>> callback) {
        executorService.execute(() -> {
            List<Visualization> results = visualizationDao.getByVisualizationId(vId);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }
}
