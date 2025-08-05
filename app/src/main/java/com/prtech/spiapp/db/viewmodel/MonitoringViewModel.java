package com.prtech.spiapp.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.MonitoringDao;
import com.prtech.spiapp.db.entity.Monitoring;

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

    public void getCount(Consumer<Long> callback) {
        executorService.execute(() -> {
            Long cnt = monitoringDao.getRecordsCount();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(cnt);
            });
        });

    }

    public void getMonitoringsByOffset(Long no, Integer size, Consumer<List<Monitoring>> callback) {
        executorService.execute(() -> {
            List<Monitoring> results = monitoringDao.getMonitoringsByOffset(no * size, size);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });

    }

    public void deleteExcept20mins() {
        executorService.execute(() -> {
            long latestTimestamp = monitoringDao.getLatestTimestamp();

            // If there are no records, do nothing
            if (latestTimestamp == 0) return;
            long threshold = latestTimestamp - (20 * 60 * 1000); // 20 mins in millis
            monitoringDao.deleteOlderThan(threshold);
        });
    }

}
