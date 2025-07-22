package com.example.myapplication.settings;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.LogHelper;
import com.example.myapplication.utils.WiFiHelper;

import java.util.List;
import java.util.function.Consumer;


public class WiFiSetting extends Fragment {

    private WiFiHelper wifiHelper;
    private List<ScanResult> scanResults;
    private BroadcastReceiver wifiReceiver;
    private TableLayout wifiListTable;

    private Button searchBtn;
    private Button wifiConnectBtn;
    private CheckBox autoConnectCheckBox;

    private MainActivity mainActivity;
    private Boolean clickSearch = false;

    private static final int WIFI_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Consumer<List<ScanResult>> pendingCallback;

    private String[][] wifiList = {
    };


    private int selectedRowIndex = -1;  // Track selected row index

    public WiFiSetting() {

    }

    public void displayWiFiList(TableLayout tableLayout, String[][] data) {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        for (int i = 0; i < this.wifiList.length; i++) {
            TableRow tableRow = new TableRow(requireContext());
            tableRow.setBackgroundResource(R.drawable.row_selector);
            tableRow.setTag(i + 1);
            for (int j = 0; j < this.wifiList[i].length; j++) {
                TextView textView = new TextView(requireContext());
                textView.setText(this.wifiList[i][j]);
                textView.setPadding(16, 16, 16, 16);
                tableRow.addView(textView);
            }
            // Click Listener to change background permanently
            tableRow.setOnClickListener(v -> {
                selectedRowIndex = (int) v.getTag();  // Save selected row index
                if(selectedRowIndex == 0) return;
                updateRowBackgrounds(tableLayout, data.length);  // Refresh backgrounds
                LogHelper.sendLog(
                        Constants.LOGGING_BASE_URL,
                        Constants.LOGGING_REQUEST_METHOD,
                        "User selected wifi: " + wifiList[selectedRowIndex - 1][0],
                        Constants.LOGGING_BEARER_TOKEN
                );
            });
            tableLayout.addView(tableRow);
        }
    }

    private void updateRowBackgrounds(TableLayout tableLayout, int rowCount) {
        for (int i = 1; i <= rowCount; i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            if (i == selectedRowIndex) {
                row.setBackgroundColor(getResources().getColor(R.color.tr_active));
            } else {
                row.setBackgroundColor(getResources().getColor(R.color.tr_light));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        wifiListTable = view.findViewById(R.id.wifi_list_tb);
        wifiHelper = new WiFiHelper(requireContext(), requireActivity());

        searchBtn = view.findViewById(R.id.wifi_search_btn);
        searchBtn.setOnClickListener(v -> {
            handleClickSearch();
        });

        wifiConnectBtn = view.findViewById(R.id.wifi_connect_btn);
        autoConnectCheckBox = view.findViewById(R.id.wifi_auto_connect_checkbox);

        wifiConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickConnect(selectedRowIndex);
            }
        });

        LogHelper.sendLog(
            Constants.LOGGING_BASE_URL,
            Constants.LOGGING_REQUEST_METHOD,
            "The current machine's Android SDK version is : " + Build.VERSION.SDK_INT,
            Constants.LOGGING_BEARER_TOKEN
        );
        if (!hasLocationPermission()) {
            requestLocationPermission();
        }
        return view;
        //return inflater.inflate(R.layout.fragment_wifi, container, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WIFI_PERMISSION_REQUEST_CODE &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            clickSearch) {
            handleClickSearch();
        } else {
            Log.d("Info", "Permission requested but not granted");
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Permission requested but not granted",
                    Constants.LOGGING_BEARER_TOKEN);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    public void handleClickConnect(int index) {
        LogHelper.sendLog(
            Constants.LOGGING_BASE_URL,
            Constants.LOGGING_REQUEST_METHOD,
            "Just clicked on Connect button",
            Constants.LOGGING_BEARER_TOKEN
        );
        Log.d("Info", "Just clicked on Connect button");
        if (index < 0) {
            LogHelper.sendLog(
                Constants.LOGGING_BASE_URL,
                Constants.LOGGING_REQUEST_METHOD,
                "User didn't select an available WiFi network, showing toast ...",
                Constants.LOGGING_BEARER_TOKEN
            );
            Log.d("Warning", "User didn't select an available wifi network, showing toast ...");
            Toast toast = Toast.makeText(requireActivity(), R.string.please_select_a_wifi_network, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10); // 100px down from top edge
            toast.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password");
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Password");

        // Wrap EditText in a FrameLayout to add margins
        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (20 * getResources().getDisplayMetrics().density); // 20dp margin
        params.setMargins(margin, margin, margin, margin);
        input.setLayoutParams(params);
        container.addView(input);

        // Set the container as the dialog view
        builder.setView(container);


        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            // Handle password here
            Toast.makeText(requireContext(), getString(R.string.password_entered) + password, Toast.LENGTH_SHORT).show();
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "User clicked confirm connection button",
                    Constants.LOGGING_BEARER_TOKEN
            );
            Log.d("Warning", "User clicked confirm connection button");

            String selectedNetSSID = "";
            try {
                selectedNetSSID = wifiList[index - 1][0];
                wifiHelper.connectToNetwork(selectedNetSSID, password, result -> {
                    if (result == -1) {
                        Toast.makeText(requireContext(), "WiFi connection failed", Toast.LENGTH_SHORT).show();
                        LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "WiFi Connection failed",
                            Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.e("WiFi Failed", "WiFi Connection failed");
                    }
                    else {
                        Toast.makeText(requireContext(), "WiFi connection success!", Toast.LENGTH_SHORT).show();
                        LogHelper.sendLog(
                            Constants.LOGGING_BASE_URL,
                            Constants.LOGGING_REQUEST_METHOD,
                            "WiFi Connection success",
                            Constants.LOGGING_BEARER_TOKEN
                        );
                        Log.e("WiFi Success", "WiFi Connection success");
                        mainActivity.connectToESP();
                    }
                });
                LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    "Try to connect to : " + selectedNetSSID + " ...",
                    Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("WiFi Info", "Try to connect to : " + selectedNetSSID + " ...");
            } catch(Exception e) {
                LogHelper.sendLog(
                        Constants.LOGGING_BASE_URL,
                        Constants.LOGGING_REQUEST_METHOD,
                        "Connection to : " + selectedNetSSID + " failed: due to" + e.toString(),
                        Constants.LOGGING_BEARER_TOKEN
                );
                Log.d("Wifi Error", "Connection to : " + selectedNetSSID + " failed: " + e.toString());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void handleClickSearch() {
        clickSearch = true;
        if (!hasLocationPermission()) {
            Toast.makeText(requireContext(), R.string.location_permission_is_not_granted, Toast.LENGTH_SHORT).show();
            requestLocationPermission();
            return;
        }
        wifiHelper.scanWifiNetworks(scanResults -> {
            StringBuilder builder = new StringBuilder();
            builder.append("End of scanning,  ");
            wifiList = new String[scanResults.size()][];
            for (int i = 0; i < scanResults.size(); i ++) {
                ScanResult result = scanResults.get(i);
                String[] wifi = new String[5];
                String name = result.SSID;
                wifi[0] = name;
                wifi[1] = "10";
                wifi[2] = "150";
                wifi[3] = "Secure";
                wifi[4] = "";
                builder.append(wifi[0]).append(", ");
                wifiList[i] = wifi;
            }
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    builder.toString().trim(),
                    Constants.LOGGING_BEARER_TOKEN);

            displayWiFiList(wifiListTable, wifiList);
        });
        clickSearch = false;
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password");

        // Create an EditText for password input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Password");

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            // Handle password here
            Toast.makeText(requireContext(), R.string.password_entered + password, Toast.LENGTH_SHORT).show();
        });


    }

}
