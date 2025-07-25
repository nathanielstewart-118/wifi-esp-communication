package com.example.myapplication.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication.db.dao.CommandDao;
import com.example.myapplication.db.dao.ESPRXRTDao;
import com.example.myapplication.db.dao.ESPTXDao;
import com.example.myapplication.db.dao.ExperimentDao;
import com.example.myapplication.db.dao.MonitoringDao;
import com.example.myapplication.db.dao.SensorActuatorDao;
import com.example.myapplication.db.dao.VisualizationDao;
import com.example.myapplication.db.entity.Command;
import com.example.myapplication.db.entity.ESPRXRT;
import com.example.myapplication.db.entity.ESPTX;
import com.example.myapplication.db.entity.Experiment;
import com.example.myapplication.db.entity.Monitoring;
import com.example.myapplication.db.entity.SensorActuator;
import com.example.myapplication.db.entity.Visualization;

@Database(entities = {SensorActuator.class, Command.class, ESPTX.class, ESPRXRT.class, Experiment.class, Visualization.class, Monitoring.class}, version = 15)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract SensorActuatorDao sensorActuatorDao();
    public abstract CommandDao commandDao();
    public abstract ESPTXDao espTXDao();
    public abstract ESPRXRTDao espRXRTDao();
    public abstract ExperimentDao experimentDao();
    public abstract VisualizationDao visualizationDao();
    public abstract MonitoringDao monitoringDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class, "wifi_esp1.db"
            ).addCallback(new Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    // Example raw SQL query to create a table manually
//                    db.execSQL("CREATE TABLE IF NOT EXISTS sensors_and_actuators (" +
//                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                            "variable_name TEXT NOT NULL, " +
//                            "data_type TEXT NOT NULL, " +
//                            "monitoring INTEGER NOT NULL, " +
//                            "real_time_control INTEGER NOT NULL, " +
//                            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
//                            "updated_at DATETIME)"
//                    );

                    // You can add more SQL here if needed
                }
            })
            .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
