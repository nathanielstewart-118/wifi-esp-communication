package com.example.myapplication.db.viewmodel;

import android.app.Application;
import android.hardware.Sensor;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.dao.SensorActuatorDao;
import com.example.myapplication.db.entity.SensorActuatorIdWithTitle;

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

    public LiveData<List<SensorActuator>> getAllSensorActuators() {
        return sensorActuatorDao.getAllSensorsAndActuators();
    }

    public LiveData<List<SensorActuator>> getAllSensors() {
        return sensorActuatorDao.getAllSensors();
    }

    public LiveData<List<SensorActuator>> getAllActuators() {
        return sensorActuatorDao.getAllActuators();
    }


    // --- INSERT ---
    public void insert(SensorActuator sensorActuator) {
        executorService.execute(() -> {
            long result = sensorActuatorDao.insert(sensorActuator);
            insertResult.postValue(result);
        });
    }

    public void insertBatch(List<SensorActuator> sas, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            for (SensorActuator sa: sas) {
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
    public void update(SensorActuator sensorActuator) {
        executorService.execute(() -> {
            int result = sensorActuatorDao.update(sensorActuator);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    public void updateBatch(List<SensorActuator> sensorActuators, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            if (!sensorActuators.isEmpty()) {
                sensorActuatorDao.deleteByTitle(sensorActuators.get(0).getTitle());
                for (SensorActuator sa : sensorActuators) {
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
    public void delete(SensorActuator sensorActuator) {
        executorService.execute(() -> {
            int result = sensorActuatorDao.delete(sensorActuator);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }

    public void getById(Long id, Consumer<SensorActuator> callback) {
        executorService.execute(() -> {
            SensorActuator sensorActuator = sensorActuatorDao.getSensorActuatorById(id);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(sensorActuator);
            });
        });
    }

    public void getByTitle(String title, int sensorOrActuator, Consumer<List<SensorActuator>> callback) {
        executorService.execute(() -> {
            List<SensorActuator> results = sensorActuatorDao.getByTitle(title, sensorOrActuator);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }

    public LiveData<List<String>> getAllTitles(int sensorOrActuator) {
        return sensorActuatorDao.getAllTitles(sensorOrActuator);
    }




}
