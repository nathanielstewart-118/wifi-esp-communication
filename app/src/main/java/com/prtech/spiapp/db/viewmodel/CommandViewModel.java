package com.prtech.spiapp.db.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prtech.spiapp.db.AppDatabase;
import com.prtech.spiapp.db.dao.CommandDao;
import com.prtech.spiapp.db.entity.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

    public void insertBatch(List<Command> commands, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            List<Long> results = new ArrayList<>();
            for(Command com: commands) {
                Long result = commandDao.insert(com);
                if (result > 0) results.add(result);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    // --- UPDATE ---
    public void update(Command command) {
        executorService.execute(() -> {
            int result = commandDao.update(command);
            updateResult.postValue(result);
        });
    }

    public void updateBatch(List<Command> commands, Consumer<List<Long>> callback) {
        executorService.execute(() -> {
            commandDao.deleteByTitle(commands.get(0).getTitle());
            List<Long> results = new ArrayList<>();
            for(Command com: commands) {
                Long result = commandDao.insert(com);
                if (result > 0) results.add(result);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
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

    public void getByTitle(String title, Consumer<List<Command>> callback) {
        executorService.execute(() -> {
            List<Command> results = commandDao.getByTitle(title);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }

    public void getByTitles(List<String> titles, Consumer<List<Command>> callback) {
        executorService.execute(() -> {
            List<Command> results = commandDao.getByTitles(titles);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(results);
            });
        });
    }


    public LiveData<List<String>> getAllTitles() {
        return commandDao.getAllTitles();
    }



}
