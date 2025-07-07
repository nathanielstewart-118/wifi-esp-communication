package com.example.myapplication.db.dao;

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
    List<Command> getAllCommands();

    @Query("SELECT * FROM commands WHERE id = :id")
    Command getCommandById(int id);

}
