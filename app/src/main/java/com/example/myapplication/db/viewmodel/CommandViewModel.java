package com.example.myapplication.db.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.db.AppDatabase;
import com.example.myapplication.db.dao.CommandDao;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.entity.SensorActuator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandViewModel extends AndroidViewModel {

    private final CommandDao commandDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Long> insertResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteResult = new MutableLiveData<>();
    
    public CommandViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.commandDao = db.commandDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Command>> getAllCommands() {
        return commandDao.getAllCommands();
    }

    // --- INSERT ---
    public void insert(Command command) {
        executorService.execute(() -> {
            long result = commandDao.insert(command);
            insertResult.postValue(result);
        });
    }

    public LiveData<Long> getInsertResult() {
        return insertResult;
    }

    // --- UPDATE ---
    public void update(Command command) {
        executorService.execute(() -> {
            int result = commandDao.update(command);
            updateResult.postValue(result);
        });
    }

    public LiveData<Integer> getUpdateResult() {
        return updateResult;
    }

    // --- DELETE ---
    public void delete(Command command) {
        executorService.execute(() -> {
            int result = commandDao.delete(command);
            deleteResult.postValue(result);
        });
    }

    public LiveData<Integer> getDeleteResult() {
        return deleteResult;
    }
    
}
