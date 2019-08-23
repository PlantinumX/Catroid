package com.example.catroidfirmatale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.catroidfirmatale.nordic.UartService;

import org.firmata4j.Parser;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.firmata.FirmataMessageFactory;
import org.firmata4j.transport.TransportInterface;

import java.io.IOException;

import static org.firmata4j.Pin.Mode.INPUT;
import static org.firmata4j.Pin.Mode.OUTPUT;


public class PinActivity_Firmata4J extends AppCompatActivity {
    private static final String SERVICE_TAG = "service_tag";
    private static final String DEVICE_TAG = "device_tag";
    private final int PIN = 13;
    private final int HIGH = 1;
    private final int LOW = 0;

    private FirmataDevice mFirmata;
    private Parser mFirmataParser;

    private ProgressBar mProgressBar;
    private Switch mModeSwitch;
    private Switch mValueSwitch;

    private UartService mUartService;
    BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)   {
            String action = intent.getAction();
            Log.i(SERVICE_TAG, "Action: " + action);
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mUartService.enableTXNotification();  //Enable notifications from arduino
                try {
                    Thread.sleep(100); //enableTXNotification needs some time to finish (firmata.start() can't work without TX)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mFirmata.start(); // gets firmaware version -> query pin capabilities
                            mFirmata.ensureInitializationIsDone();  //waits for start to finish
                            mFirmata.sendMessage(FirmataMessageFactory.analogReport(false)); //start enables reporting from analog pins, we disable unnecessary noise
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    mValueSwitch.setVisibility(View.VISIBLE);
                                    mModeSwitch.setVisibility(View.VISIBLE);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            } else if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                byte[] myStream = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 mFirmataParser.parse(myStream); //processing received raw Firmata data from UartService
            }
        }
    };

    TransportInterface iSerial = new TransportInterface() {
        @Override
        public void start(){
            Log.i(DEVICE_TAG, "TransportInterface.start");
        }

        @Override
        public void stop() {
            Log.i(DEVICE_TAG, "TransportInterface.stop");
        }

        @Override
        public void write(byte[] bytes) {
            Log.i(DEVICE_TAG, "TransportInterface.write");
            mUartService.writeRXCharacteristic(bytes);
        }

        @Override
        public void setParser(Parser parser) {  //first callback after firmata initialization
            Log.i(DEVICE_TAG, "TransportInterface.setParser");
            mFirmataParser = parser;
            mFirmataParser.start();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_controler);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter()); // sign in to UART services
        mProgressBar = findViewById(R.id.progressBarID);
        mModeSwitch = findViewById(R.id.modeSwitch);
        mModeSwitch.setVisibility(View.GONE);
        mValueSwitch = findViewById(R.id.valueSwitch);
        mValueSwitch.setVisibility(View.GONE);

        String UUID = getIntent().getStringExtra("UUID");
        mUartService = new UartService();
        mUartService.initialize(this);
        mUartService.connect(UUID);
        mFirmata = new FirmataDevice(iSerial);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        try {
            mFirmata.sendMessage((byte)0xFF);  //reset all firmata pins
        } catch (IOException e) {
            e.printStackTrace();
        }
        mUartService.close();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {   //UART services
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.EXTRA_DATA);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    public void switchModeChanged(View view) throws IOException {
        if (mFirmata.getPinsCount() == 0) {
            Toast.makeText(this, "No pins!", Toast.LENGTH_SHORT).show();
            return;
        }

        Switch pinSwitch = (Switch) view;
        Pin pin13 = mFirmata.getPin(PIN);

        if (pinSwitch.isChecked()) {
            pin13.setMode(OUTPUT);
        } else {
            pin13.setMode(INPUT);
        }
    }


    public void switchValueChanged(View view) throws IOException {
        if (mFirmata.getPinsCount() == 0) {
            Toast.makeText(this, "No pins!", Toast.LENGTH_SHORT).show();
            return;
        }

        Switch pinSwitch = (Switch) view;
        Pin pin13 = mFirmata.getPin(PIN);

        if (pinSwitch.isChecked()) {
            pin13.setValue(HIGH);
        } else {
            pin13.setValue(LOW);
        }
    }

}
