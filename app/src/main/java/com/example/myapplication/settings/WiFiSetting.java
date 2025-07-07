package com.example.myapplication.settings;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.R;

public class WiFiSetting extends Fragment {
    private String[][] wifiList = {
            {"WiFi 1", "8", "200", "Secure", ""},
            {"WiFi 2", "6", "230", "Insecure", ""},
            {"WiFi 3", "8", "150", "Secure", ""},
            {"WiFi 4", "8", "170", "Secure", ""},
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

        // Sample data
        String[][] data = {
                {"Name", "Age", "City"},
                {"Alice", "24", "New York"},
                {"Bob", "30", "Chicago"},
                {"Charlie", "28", "San Francisco"}
        };
        displayWiFiList(tableLayout, data);
        return view;
        //return inflater.inflate(R.layout.fragment_wifi, container, false);
    }
}
