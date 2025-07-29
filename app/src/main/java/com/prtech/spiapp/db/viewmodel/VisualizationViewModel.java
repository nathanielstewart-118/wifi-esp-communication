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
import com.prtech.spiapp.db.dao.VisualizationDao;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.RangeDTO;
import com.prtech.spiapp.db.entity.Visualization;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VisualizationViewModel extends AndroidViewModel {
    private final VisualizationDao visualizationDao;
    private final ExecutorService executorService;
    private final ESPPacketDao espPacketDao;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();

    public VisualizationViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.visualizationDao = db.visualizationDao();
        this.espPacketDao = db.espPacketDao();
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

    public void getActivatedVisualization(Consumer<Visualization> callback) {
        executorService.execute(() -> {
           List<Visualization> results = visualizationDao.getActivatedVisualization();
           new Handler(Looper.getMainLooper()).post(() -> {
               Visualization visualization;
               if (results.isEmpty()) visualization = null;
               else visualization = results.get(0);
               callback.accept(visualization);
           });
        });
    }

    public void setActivated(Long id, Consumer<Integer> callback) {
        executorService.execute(() -> {
            visualizationDao.setActivated(id);
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(1);
            });
        });
    }

    public void getCorrespondingSAs(Long id, Consumer<List<RangeDTO>> callback) {
        executorService.execute(() -> {
            Visualization v = visualizationDao.getVisualizationById(id);
            List<RangeDTO> results = v.getRanges()
                .stream()
                .map(r -> {
                    ESPPacket s = espPacketDao.getSensorActuatorById(r.getSensorActuatorId());
                    return new RangeDTO(id, s.getId(), s.getVariableName(), s.getDataType(), s.getNumberOfChannels(), r.getVisualizationType(), r.getyAxisRange(), r.getUpperLimit(), r.getLowerLimit());
                })
                .collect(Collectors.toList());
            new Handler(Looper.getMainLooper()).post(() -> {
               callback.accept(results);
            });
        });
    }
}
