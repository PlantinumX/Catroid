package com.example.catroidfirmatale;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BluetoothLeScanner mBluetoothLeScanner;
    final List<ScanResult> mScanResults = new ArrayList<>();
    CustomScannerListAdapter mCustomScannerListAdapter;

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            boolean duplicate = false;
            for (ScanResult scanResult : mScanResults) {
                if (scanResult.getDevice().getAddress().equals(result.getDevice().getAddress())) {
                    duplicate = true;
                }
            }
            if (!duplicate) {
                mScanResults.add(result); // add all found ble devices into a list
                Log.i("marina", "scan result " + result.getDevice().getAddress());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCustomScannerListAdapter.notifyDataSetChanged(); // updates Adapter
                    }
                });
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i("marina", "onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("marina", "scan failed");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_list);


        ListView mListView = findViewById(R.id.MainListView);
        mCustomScannerListAdapter = new CustomScannerListAdapter(MainActivity.this, mScanResults);
        mListView.setAdapter(mCustomScannerListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ScanResult scanResult = mScanResults.get(position);
                if (CustomScannerListAdapter.checkIsUart(scanResult)) {
                    Intent gpioIntent = new Intent(MainActivity.this, PinActivity_Firmata4J.class);
                    gpioIntent.putExtra("UUID", scanResult.getDevice().getAddress()); //put Address of clicked device into new Activity
                    gpioIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(gpioIntent); //start PinActivity_Firmata4J, connection with clicked ble device will be when new activity starts
                } else {
                    Toast.makeText(getApplicationContext(), "Firmata not available", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

    }

    @SuppressLint("CheckResult")
    @Override
    protected void onStart() {
        super.onStart();
        RxPermissions rxPermissions = new RxPermissions(this); //todo implement native permission request without library usage
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION) // ask single or multiple permission once
                .subscribe(granted -> {
                    if (granted) {
                        mScanResults.clear();
                        mCustomScannerListAdapter.notifyDataSetChanged();
                        mBluetoothLeScanner.startScan(scanCallback);
                    } else {
                        System.exit(0);
                    }
                });


    }

    @Override
    protected void onStop() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(scanCallback);
        }
        super.onStop();
    }


}
