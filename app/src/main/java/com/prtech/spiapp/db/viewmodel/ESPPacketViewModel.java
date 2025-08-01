package com.prtech.spiapp.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.ESPPacketDao;
import com.prtech.spiapp.db.entity.ESPPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ESPPacketViewModel extends AndroidViewModel {
    private final ESPPacketDao espPacketDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();

    public ESPPacketViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.espPacketDao = db.espPacketDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ESPPacket>> getAllSensorActuators() {
        return espPacketDao.getAllSensorsAndActuators();
    }

    public LiveData<List<ESPPacket>> getAllSensors() {
        return espPacketDao.getAllSensors();
    }

    public LiveData<List<ESPPacket>> getAllActuators() {
        return espPacketDao.getAllActuators();
    }


    // --- INSERT ---
    public void insert(ESPPacket espPacket) {
        executorService.execute(() -> {
            long result = espPacketDao.insert(espPacket);
            insertResult.postValue(result);
        });
    }

    public void insertBatch(List<ESPPacket> sas, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            for (ESPPacket sa: sas) {
                long insertedId = espPacketDao.insert(sa);
                if(insertedId >= 0) results.add(insertedId);
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
    public void update(ESPPacket espPacket) {
        executorService.execute(() -> {
            int result = espPacketDao.update(espPacket);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    public void updateBatch(List<ESPPacket> espPackets, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            if (!espPackets.isEmpty()) {
                espPacketDao.deleteByTitle(espPackets.get(0).getTitle());
                for (ESPPacket sa : espPackets) {
                    Long result = espPacketDao.insert(sa);
                    if (result > 0) results.add(result);
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    public void saveBatch(List<ESPPacket> espPackets, Consumer<List<Integer>> callback) {
        executorService.execute(() -> {
            List<Integer> results = new ArrayList<>();
            for (ESPPacket espPacket: espPackets) {
                if(espPacket.getId() != null) {
                    espPacketDao.update(espPacket);
                    results.add(1);
                }
                else {
                    espPacketDao.insert(espPacket);
                    results.add(1);
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    // --- DELETE ---
    public void delete(ESPPacket espPacket) {
        executorService.execute(() -> {
            int result = espPacketDao.delete(espPacket);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    public void getById(Long id, Consumer<ESPPacket> callback) {
        executorService.execute(() -> {
            ESPPacket espPacket = espPacketDao.getSensorActuatorById(id);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(espPacket);
            });
        });
    }
    
    public void getByIds(List<Long> ids, Consumer<List<ESPPacket>> callback) {
        executorService.execute(() -> {
            List<ESPPacket> espPackets = espPacketDao.getByIds(ids);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(espPackets);
            });
        });
    }

    public void getByTitle(String title, Consumer<List<ESPPacket>> callback) {
        executorService.execute(() -> {
            List<ESPPacket> results = espPacketDao.getByTitle(title);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }

    public LiveData<List<String>> getAllTitles() {
        return espPacketDao.getAllTitles();
    }




}
