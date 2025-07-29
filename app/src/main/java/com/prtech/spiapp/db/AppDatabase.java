package com.prtech.spiapp.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.prtech.spiapp.db.dao.CommandDao;
import com.prtech.spiapp.db.dao.ESPPacketDao;
import com.prtech.spiapp.db.dao.ESPRXRTDao;
import com.prtech.spiapp.db.dao.ESPTXDao;
import com.prtech.spiapp.db.dao.ExperimentDao;
import com.prtech.spiapp.db.dao.MonitoringDao;
import com.prtech.spiapp.db.dao.VisualizationDao;
import com.prtech.spiapp.db.entity.Command;
import com.prtech.spiapp.db.entity.ESPRXRT;
import com.prtech.spiapp.db.entity.ESPTX;
import com.prtech.spiapp.db.entity.Experiment;
import com.prtech.spiapp.db.entity.Monitoring;
import com.prtech.spiapp.db.entity.ESPPacket;
import com.prtech.spiapp.db.entity.Visualization;

@Database(entities = {ESPPacket.class, Command.class, ESPTX.class, ESPRXRT.class, Experiment.class, Visualization.class, Monitoring.class}, version = 23)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ESPPacketDao espPacketDao();
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
