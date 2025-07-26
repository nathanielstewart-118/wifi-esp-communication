package com.example.myapplication.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.db.entity.Command;

import java.util.List;

@Dao
public interface CommandDao {

    @Insert
    long insert(Command command);

    @Update
    int update(Command command);

    @Delete
    int delete(Command command);

    @Query("SELECT * FROM commands")
    LiveData<List<Command>> getAllCommands();

    @Query("SELECT * FROM commands WHERE id = :id")
    Command getCommandById(int id);

    @Query("SELECT DISTINCT title FROM commands")
    LiveData<List<String>> getAllTitles();

    @Query("SELECT * FROM commands WHERE title=:title")
    List<Command> getByTitle(String title);

    @Query(("DELETE FROM commands WHERE title = :title"))
    void deleteByTitle(String title);
}
