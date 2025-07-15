package com.example.myapplication.settings;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.LogHelper;
import com.example.myapplication.utils.WiFiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WiFiSetting extends Fragment {

    private WiFiHelper wifiHelper;
    private List<ScanResult> scanResults;
    private BroadcastReceiver wifiReceiver;
    private LogHelper logHelper = new LogHelper();


    private Button searchBtn;
    private Button wifiConnectBtn;
    private CheckBox autoConnectCheckBox;

    private String[][] wifiList = {
    };


    private int selectedRowIndex = -1;  // Track selected row index

    public WiFiSetting() {

    }

    public void displayWiFiList(TableLayout tableLayout, String[][] data) {
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
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        TableLayout tableLayout = view.findViewById(R.id.wifi_list_tb);
        searchBtn = view.findViewById(R.id.wifi_search_btn);
        wifiConnectBtn = view.findViewById(R.id.wifi_connect_btn);
        autoConnectCheckBox = view.findViewById(R.id.wifi_auto_connect_checkbox);
        logHelper.sendLog(Constants.LOGGING_BASE_URL, Constants.LOGGING_REQUEST_METHOD, "This is start of wifi scanning", Constants.LOGGING_BEARER_TOKEN);
        StringBuilder builder = new StringBuilder();
        builder.append("End of scanning,  ");
        wifiHelper = new WiFiHelper(requireContext(), requireActivity());
        wifiHelper.scanWifiNetworks(scanResults -> {
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
                builder.append(wifi[0]).append(" ");
                builder.append(wifi[1]).append(" ");
                builder.append(wifi[2]).append(" ");
                builder.append(wifi[0]).append(" ");
                builder.append(", ");
                wifiList[i] = wifi;
            }
            LogHelper.sendLog(
                    Constants.LOGGING_BASE_URL,
                    Constants.LOGGING_REQUEST_METHOD,
                    builder.toString().trim(),
                    Constants.LOGGING_BEARER_TOKEN);

            displayWiFiList(tableLayout, wifiList);
        });
//        wifiHelper.connectToNetwork("ssid", "password");

        wifiConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickConnect(selectedRowIndex);
            }
        });
        return view;
        //return inflater.inflate(R.layout.fragment_wifi, container, false);
    }

    public void handleClickConnect(int index) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Confirm connection")
            .setMessage("Are you sure you want to proceed?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (index < 0) return;
                    String selectedNetSSID = wifiList[index - 1][0];
                    wifiHelper.connectToNetwork(selectedNetSSID, "");
                    Toast.makeText(requireContext(), "Connect successfully!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .setCancelable(true)
            .show();

    }
}
