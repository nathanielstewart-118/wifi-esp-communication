package com.prtech.spiapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.function.Consumer;

public class WiFiHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final WifiManager wifiManager;
    private final Context context;
    private final Activity activity;
    private Consumer<List<ScanResult>> pendingWifiCallback;

    public WiFiHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }



    public void scanWifiNetworks(FragmentActivity activity, LinearProgressIndicator linearProgressIndicator, Consumer<String[][]> callback) {
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


                // Register receiver to listen for scan results
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Starting to register receiver to listen for scan results",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Info", "Starting to register receiver to listen for scan results");
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
                                return;
                            }
                            LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "Trying to call callback function with the scanned results",
                                Constants.LOGGING_BEARER_TOKEN
                            );
                            int cnt = results.size();
                            String[][] wifiLists = new String[cnt][4];
                            for (int i = 0; i < cnt; i ++) {
                                ScanResult scanResult = results.get(i);
                                String ssid = scanResult.SSID;
                                int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                                wifiLists[i][0] = ssid;
                                wifiLists[i][1] = String.valueOf(level);
                                wifiLists[i][2] = "";
                                String capabilities = scanResult.capabilities;

                                wifiLists[i][3] = getSecurityType(capabilities);
                            }
                            callback.accept(wifiLists);
                            Log.d("Info", "Trying to call callback function with the scanned results");

                            activity.runOnUiThread(() -> {
                                linearProgressIndicator.setVisibility(View.GONE);
                            });

                        } catch (Exception e) {
                            Log.e("Exception", "Error in BroadcastReceiver: " + e.getMessage(), e);
                            LogHelper.sendLog(
                                Constants.LOGGING_BASE_URL,
                                Constants.LOGGING_REQUEST_METHOD,
                                "BroadcastReceiver exception: " + e.getMessage(),
                                Constants.LOGGING_BEARER_TOKEN
                            );
                            activity.runOnUiThread(() -> {
                                linearProgressIndicator.setVisibility(View.GONE);
                            });
                        }
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Starting to scan wifi networks ...",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Info", "Starting to scan wifi networks ...");
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
                    activity.runOnUiThread(() -> {
                        linearProgressIndicator.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                Log.e("Exception", "Unexpected error in scanWifiNetworks: " + e.getMessage(), e);
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "scanWifiNetworks exception: " + e.getMessage(),
                    Constants.LOGGING_BEARER_TOKEN
                );
                activity.runOnUiThread(() -> {
                    linearProgressIndicator.setVisibility(View.GONE);
                });
            }
        })
        .start();

    }

    public void connectToNetwork(String ssid, String password, Consumer<Integer> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password) // or setIsHiddenSsid(true) if needed
                    .build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(specifier)
                    .build();

            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    // Bind to this network
                    connectivityManager.bindProcessToNetwork(network); // or setProcessDefaultNetwork()

                    callback.accept(1);
                }

                @Override
                public void onUnavailable() {
                    LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "WiFi Connection Failed",
                            Constants.LOGGING_BEARER_TOKEN
                    );
                    Log.e("WiFi", "Failed to connect");
                    callback.accept(-1);
                }
            });
        } else {
//            WifiConfiguration config = new WifiConfiguration();
//            config.SSID = "\"" + ssid + "\"";
//            config.preSharedKey = "\"" + password + "\"";
//
//            int netId = wifiManager.addNetwork(config);
//            if (netId != -1) {
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(netId, true);
//                wifiManager.reconnect();
//            }

            int netID = -1;
            String confSSID = String.format("\"%s\"", ssid);
            String confPassword = String.format("\"%s\"", password);
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = confSSID;
            config.preSharedKey = confPassword;
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            forgotAllNetwork(context);


            netID = wifiManager.addNetwork(config);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netID, true);
            wifiManager.reconnect();

            config = new WifiConfiguration();
            config.SSID = confSSID;
            config.preSharedKey = confPassword;
            netID = wifiManager.addNetwork(config);
            callback.accept(netID);
        }
    }

    public static void forgotAllNetwork(Context context) {

        WifiManager wm= (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

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
        List<WifiConfiguration> list = wm.getConfiguredNetworks();

        if (list == null){

            return ;

        }

        for (int i = 0; i<list.size(); i++){

            WifiConfiguration conf = list.get(i);

            wm.removeNetwork(conf.networkId);
        }
    }

    private String getSecurityType(String capabilities) {
        if (capabilities.contains("WPA3")) {
            return "WPA3";
        } else if (capabilities.contains("WPA2")) {
            return "WPA2";
        } else if (capabilities.contains("WPA")) {
            return "WPA";
        } else if (capabilities.contains("WEP")) {
            return "WEP";
        } else {
            return "Open";
        }
    }

}
