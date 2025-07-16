package com.example.myapplication.utils;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WiFiHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final WifiManager wifiManager;
    private final Context context;
    private final Activity activity;

    public WiFiHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void scanWifiNetworks(Consumer<List<ScanResult>> callback) {
        new Thread(() -> {
            try {
                // Check if wifiManager is null
                if (wifiManager == null) {
                    Log.e("Error", "wifiManager is null.");
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "wifiManager is null",
                            Constants.LOGGING_BEARER_TOKEN
                    );
                    return;
                }

                // Check and request permission if needed
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    Log.w("Permission", "ACCESS_FINE_LOCATION not granted. Requesting...");
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "ACCESS_FINE_LOCATION permission not granted. Requesting...",
                            Constants.LOGGING_BEARER_TOKEN
                    );

                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE
                    );

                    callback.accept(Collections.emptyList()); // Return early
//                return;
                }

                // Register receiver to listen for scan results
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent intent) {
                        try {
                            context.unregisterReceiver(this);
                            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                            if (!success) {
                                Log.w("Warning", "Wi-Fi scan did not update results.");
                                LogHelper.sendLog(
                                        Constants.LOGGING_BASE_URL,
                                        Constants.LOGGING_REQUEST_METHOD,
                                        "Wi-Fi scan results not updated",
                                        Constants.LOGGING_BEARER_TOKEN
                                );
                            }

                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.

                                return;
                            }
                            List<ScanResult> results = wifiManager.getScanResults();
                            if (results == null || results.isEmpty()) {
                                Log.w("Warning", "No Wi-Fi networks found.");
                                LogHelper.sendLog(
                                        Constants.LOGGING_BASE_URL,
                                        Constants.LOGGING_REQUEST_METHOD,
                                        "No Wi-Fi networks found",
                                        Constants.LOGGING_BEARER_TOKEN
                                );
                            }
                            callback.accept(results);
                        } catch (Exception e) {
                            Log.e("Exception", "Error in BroadcastReceiver: " + e.getMessage(), e);
                            LogHelper.sendLog(
                                    Constants.LOGGING_BASE_URL,
                                    Constants.LOGGING_REQUEST_METHOD,
                                    "BroadcastReceiver exception: " + e.getMessage(),
                                    Constants.LOGGING_BEARER_TOKEN
                            );
                        }
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                // Start Wi-Fi scan
                boolean scanStarted = wifiManager.startScan();
                if (!scanStarted) {
                    Log.e("Error", "Wi-Fi scan could not be started.");
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "Wi-Fi scan could not be started",
                            Constants.LOGGING_BEARER_TOKEN
                    );
                }
            } catch (Exception e) {
                Log.e("Exception", "Unexpected error in scanWifiNetworks: " + e.getMessage(), e);
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "scanWifiNetworks exception: " + e.getMessage(),
                    Constants.LOGGING_BEARER_TOKEN
                );
            }
        })
            .start();

    }

    public void connectToNetwork(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + password + "\"";

        int netId = wifiManager.addNetwork(config);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }
}
