package com.example.myapplication.db.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.dao.ESPTXDao;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.entity.ESPTX;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ESPTXViewModel extends AndroidViewModel {

    private final ESPTXDao espTXDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();

    public ESPTXViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.espTXDao = db.espTXDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ESPTX>> getAllESPTXes() {
        return espTXDao.getAllESPTXes();
    }

    // --- INSERT ---
    public void insert(ESPTX esptx) {
        executorService.execute(() -> {
            long result = espTXDao.insert(esptx);
            insertResult.postValue(result);
        });
    }

    public LiveData<Long> getInsertResult() {
        return insertResult;
    }

    // --- UPDATE ---
    public void update(ESPTX esptx) {
        executorService.execute(() -> {
            int result = espTXDao.update(esptx);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    // --- DELETE ---
    public void delete(ESPTX esptx) {
        executorService.execute(() -> {
            int result = espTXDao.delete(esptx);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }
}
