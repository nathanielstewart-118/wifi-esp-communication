package com.example.myapplication; // Replace with your package name

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.db.viewmodel.TCPUDPReceiveViewModel;
import com.example.myapplication.settings.ActuatorSetting;
import com.example.myapplication.settings.CommandSetting;
import com.example.myapplication.settings.ESPRXRTSetting;
import com.example.myapplication.settings.ESPTXSetting;
import com.example.myapplication.settings.ExperimentSetting;
import com.example.myapplication.settings.MonitoringSetting;
import com.example.myapplication.settings.ReproductionSetting;
import com.example.myapplication.settings.SensorSetting;
import com.example.myapplication.settings.VisualizationSetting;
import com.example.myapplication.settings.WiFiSetting;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.LocaleHelper;
import com.example.myapplication.utils.LogHelper;
import com.example.myapplication.utils.communications.WiFiSocketManager;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.net.SocketException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private WiFiSocketManager socketManager = WiFiSocketManager.getInstance();

    private TCPUDPReceiveViewModel receiveViewModel;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML layout with DrawerLayout

        receiveViewModel = new ViewModelProvider(this).get(TCPUDPReceiveViewModel.class);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        // Set up the toolbar
        setSupportActionBar(toolbar);

        // Set up the toggle (hamburger) button
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            // Load the default fragment (e.g., WiFiFragment)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new WiFiSetting())
                    .commit();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    super.onBackPressed();
//                }
                finish(); // Or moveTaskToBack(true), etc.
            }
        });

        // Handle navigation menu item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            Fragment selectedFragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.wifi_menu_item) {
                    selectedFragment = new WiFiSetting();
                }
                else if (id == R.id.sensor_menu_item) {
                    selectedFragment = new SensorSetting();
                } else if (id == R.id.actuator_menu_item) {
                    selectedFragment = new ActuatorSetting();
                } else if (id == R.id.command_menu_item) {
                    selectedFragment = new CommandSetting();
                } else if (id == R.id.experiment_menu_item) {
                    selectedFragment = new ExperimentSetting();
                } else if (id == R.id.visualization_menu_item) {
                    selectedFragment = new VisualizationSetting();
                } else if (id == R.id.monitoring_menu_item) {
                    selectedFragment = new MonitoringSetting();
                } else if (id == R.id.reproduction_menu_item) {
                    selectedFragment = new ReproductionSetting();
                } else if (id == R.id.esp_tx_menu_item) {
                    selectedFragment = new ESPTXSetting();
                } else if (id == R.id.esp_rxrt_menu_item) {
                    selectedFragment = new ESPRXRTSetting();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                }

                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "fr"));  // "fr" should be dynamically set
    }

    public void connectToESP() {
        socketManager.connectTCP(Constants.tcpServerIp, Constants.tcpServerPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(byte[] response) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "TCP connected!!!",
                    Constants.LOGGING_BEARER_TOKEN
                );
                runOnUiThread(() -> {
                    Toast.makeText(getBaseContext(), "TCP connected", Toast.LENGTH_SHORT).show();
                });
                Log.d("WiFiSocketManager", "TCP Connected: " + Arrays.toString(response));
            }

            @Override
            public void onError(Exception e) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "TCP connection failed due to " + e.toString(),
                    Constants.LOGGING_BEARER_TOKEN
                );
                runOnUiThread(() -> {
                    Toast.makeText(getBaseContext(), "TCP connection failed", Toast.LENGTH_SHORT).show();
                });

                Log.e("WiFiSocketManager", "TCP Connect error", e);
            }
        });

        socketManager.setTCPMessageListener(new WiFiSocketManager.TCPMessageListener() {
            @Override
            public void onMessageReceived(byte[] data) {
                receiveViewModel.setData(data);
                String byteString = Arrays.toString(data);
                Log.d("TCP", "Received from server: " + byteString);
                LogHelper.sendLog(
                        Constants.LOGGING_BASE_URL,
                        Constants.LOGGING_REQUEST_METHOD,
                        "Data: " + byteString + " arrived via TCP",
                        Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("WiFi Success", "Data: " + byteString + "arrived via TCP");
                // If you need to update UI:
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Received from server: " + byteString, Toast.LENGTH_SHORT).show();
                    // update UI safely here
                });
            }
        });

        socketManager.startUDPListening(Constants.udpLocalPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(byte[] message) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Data: " + Arrays.toString(message) + "arrived via UDP",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("WiFi Success", "Data: " + Arrays.toString(message) + "arrived via UDP");
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Received: " + Arrays.toString(message), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(Exception e) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "UDP receiving error : " + e.toString(),
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.e("UDP", "Error receiving", e);
            }
        });


    }

    public void sendTCP(byte[] data) {
        socketManager.sendTCP(data, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(byte[] message) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Sent TCP message: " + Arrays.toString(message),
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Info", "Sent TCP message: " + Arrays.toString(message));
                runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), "Received: " + Arrays.toString(message), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(Exception e) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Error sending data via TCP: " + e.toString(),
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.e("WiFi TCP Error", "Error receiving", e);
            }
        });
    }

    public void sendUDP(byte[] data) {
        socketManager.sendUDP(data, Constants.tcpServerIp, Constants.udpPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(byte[] message) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Sent UDP message: " + Arrays.toString(message),
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("WiFi UDP Success", "Sent UDP message: " + Arrays.toString(message));
                runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), "Received: " + Arrays.toString(message), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(Exception e) {
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Error while sending UDP message due to : " + e.toString(),
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.e("WiFi UDP Error", "Error while sending UDP message due to : ", e);
            }
        });
    }
}
