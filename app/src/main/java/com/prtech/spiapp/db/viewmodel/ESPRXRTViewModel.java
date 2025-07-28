package com.prtech.spiapp.db.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.ESPRXRTDao;
import com.prtech.spiapp.db.entity.ESPRXRT;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ESPRXRTViewModel extends AndroidViewModel {
    private final ESPRXRTDao espRXRTDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();
    private final MutableLiveData<Long> softDeleteResult = new MutableLiveData<>();

    public ESPRXRTViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.espRXRTDao = db.espRXRTDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ESPRXRT>> getAllESPRXRTs() {
        return espRXRTDao.getAllESPRXRTs();
    }

    // --- INSERT ---
    public void insert(ESPRXRT esprxrt) {
        executorService.execute(() -> {
            long result = espRXRTDao.insert(esprxrt);
            insertResult.postValue(result);
        });
    }

    public LiveData<Long> getInsertResult() {
        return insertResult;
    }

    // --- UPDATE ---
    public void update(ESPRXRT esprxrt) {
        executorService.execute(() -> {
            int result = espRXRTDao.update(esprxrt);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    // --- DELETE ---
    public void delete(ESPRXRT esprxrt) {
        executorService.execute(() -> {
            int result = espRXRTDao.delete(esprxrt);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    // --- Soft Delete ---
    public void softDelete(Long id) {
        executorService.execute(() -> {
            long result = espRXRTDao.softDeleteById(id);
            softDeleteResult.postValue(result);
        });
    }
    public LiveData<Long> getSoftDeleteResult() { return softDeleteResult; }

}
