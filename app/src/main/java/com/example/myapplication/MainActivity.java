package com.example.myapplication; // Replace with your package name

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.example.myapplication.utils.communications.WiFiSocketManager;
import com.google.android.material.navigation.NavigationView;

import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private WiFiSocketManager socketManager = WiFiSocketManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML layout with DrawerLayout


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

    public void connectToESP(String serverIp, int tcpPort, int udpLocalPort, int udpPort) {
        socketManager.connectTCP(serverIp, tcpPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(String response) {
                Log.d("WiFiSocketManager", "TCP Connected: " + response);
            }

            @Override
            public void onError(Exception e) {
                Log.e("WiFiSocketManager", "TCP Connect error", e);
            }
        });

        socketManager.setTCPMessageListener(new WiFiSocketManager.TCPMessageListener() {
            @Override
            public void onMessageReceived(String message) {
                Log.d("TCP", "Received from server: " + message);
                // If you need to update UI:
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Received from server: " + message, Toast.LENGTH_SHORT).show();
                    // update UI safely here
                });
            }
        });

        socketManager.startUDPListening(udpLocalPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Received: " + message, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(Exception e) {
                Log.e("UDP", "Error receiving", e);
            }
        });

        socketManager.sendUDP("Hello via UDP!", serverIp, udpPort, new WiFiSocketManager.Callback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Received: " + message, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(Exception e) {
                Log.e("UDP", "Error receiving", e);
            }
        });
    }

}
