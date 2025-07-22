package com.example.myapplication.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.dao.MonitoringDao;
import com.example.myapplication.db.entity.Monitoring;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MonitoringViewModel extends AndroidViewModel {

    private final MonitoringDao monitoringDao;
    private final ExecutorService executorService;

    public MonitoringViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.monitoringDao = db.monitoringDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void getAllMonitorings(Consumer<List<Monitoring>> callback) {
        executorService.execute(() -> {
            List<Monitoring> results = monitoringDao.getAllMonitorings();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    public void insert(Monitoring monitoring, Consumer<Long> callback) {
        executorService.execute(() -> {
            Long result = monitoringDao.insert(monitoring);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }

    public void update(Monitoring monitoring, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int result = monitoringDao.update(monitoring);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }

    public void delete(Monitoring monitoring, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int result = monitoringDao.delete(monitoring);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }

    public void getById(Long id, Consumer<Monitoring> callback) {
        executorService.execute(() -> {
            Monitoring result = monitoringDao.getMonitoringById(id);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }

}
