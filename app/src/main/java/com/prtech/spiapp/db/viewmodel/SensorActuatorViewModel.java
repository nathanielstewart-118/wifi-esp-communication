package com.prtech.spiapp.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.dao.SensorActuatorDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SensorActuatorViewModel extends AndroidViewModel {
    private final SensorActuatorDao sensorActuatorDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();

    public SensorActuatorViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.sensorActuatorDao = db.sensorActuatorDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ESPPacket>> getAllSensorActuators() {
        return sensorActuatorDao.getAllSensorsAndActuators();
    }

    public LiveData<List<ESPPacket>> getAllSensors() {
        return sensorActuatorDao.getAllSensors();
    }

    public LiveData<List<ESPPacket>> getAllActuators() {
        return sensorActuatorDao.getAllActuators();
    }


    // --- INSERT ---
    public void insert(ESPPacket espPacket) {
        executorService.execute(() -> {
            long result = sensorActuatorDao.insert(espPacket);
            insertResult.postValue(result);
        });
    }

    public void insertBatch(List<ESPPacket> sas, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            for (ESPPacket sa: sas) {
                long insertedId = sensorActuatorDao.insert(sa);
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
            int result = sensorActuatorDao.update(espPacket);
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
                sensorActuatorDao.deleteByTitle(espPackets.get(0).getTitle());
                for (ESPPacket sa : espPackets) {
                    Long result = sensorActuatorDao.insert(sa);
                    if (result > 0) results.add(result);
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
            int result = sensorActuatorDao.delete(espPacket);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    public void getById(Long id, Consumer<ESPPacket> callback) {
        executorService.execute(() -> {
            ESPPacket espPacket = sensorActuatorDao.getSensorActuatorById(id);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(espPacket);
            });
        });
    }

    public void getByTitle(String title, int sensorOrActuator, Consumer<List<ESPPacket>> callback) {
        executorService.execute(() -> {
            List<ESPPacket> results = sensorActuatorDao.getByTitle(title, sensorOrActuator);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }

    public LiveData<List<String>> getAllTitles(int sensorOrActuator) {
        return sensorActuatorDao.getAllTitles(sensorOrActuator);
    }




}
