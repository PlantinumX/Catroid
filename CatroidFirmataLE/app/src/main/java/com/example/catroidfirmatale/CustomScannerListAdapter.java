package com.example.catroidfirmatale;

import android.app.Activity;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomScannerListAdapter extends ArrayAdapter {

    private final Activity mContext;
    private final List<ScanResult> mScanResults;

    public CustomScannerListAdapter(Activity context, List<ScanResult> scanResults) {
        super(context, R.layout.ble_scan_result, scanResults);

        this.mContext = context;
        this.mScanResults = scanResults;

    }
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.ble_scan_result, null, true);

        TextView bleIdentifierTextField = rowView.findViewById(R.id.bleIdentifierTextViewID);
        TextView bleNameTextField = rowView.findViewById(R.id.bleNameTextViewID);
        TextView bleFirmataTextField = rowView.findViewById(R.id.bleFirmataTextViewID);

        bleIdentifierTextField.setText("Adress: " + mScanResults.get(position).getDevice().getAddress());
        if(mScanResults.get(position).getDevice().getName() != null) {
            bleNameTextField.setText("Name: " + mScanResults.get(position).getDevice().getName());
        }else {
            bleNameTextField.setText("Name unknown");
        }if(checkIsUart(mScanResults.get(position))) {
            bleFirmataTextField.setText("YES");
        }
        else {
            bleFirmataTextField.setText("NO");
        }
        return rowView;
    }

    public static boolean checkIsUart(ScanResult scanResult){
        List<ParcelUuid> serviceUuids = scanResult.getScanRecord().getServiceUuids();
        if (serviceUuids != null) {
            ParcelUuid uartUuid = ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
            for (ParcelUuid serviceUuid : serviceUuids) {
                if (serviceUuid.equals(uartUuid)) {
                    return true;
                }
            }
        }
        return false;
    }
}