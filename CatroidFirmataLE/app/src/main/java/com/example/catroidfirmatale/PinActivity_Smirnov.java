package com.example.catroidfirmatale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.example.catroidfirmatale.nordic.UartService;

import name.antonsmirnov.firmata.Firmata;
import name.antonsmirnov.firmata.message.DigitalMessage;
import name.antonsmirnov.firmata.message.SetPinModeMessage;
import name.antonsmirnov.firmata.message.SysexMessage;
import name.antonsmirnov.firmata.message.SystemResetMessage;
import name.antonsmirnov.firmata.serial.ISerial;
import name.antonsmirnov.firmata.serial.ISerialListener;
import name.antonsmirnov.firmata.serial.SerialException;

import static com.example.catroidfirmatale.Smirnov_Utils.getDigitalPinValue;
import static com.example.catroidfirmatale.Smirnov_Utils.getPortFromPin;


public class PinActivity_Smirnov extends AppCompatActivity {
    private static final String NORDIC_TAG = "nordic_tag";
    private static final String FIRMATA_TAG = "firmata_tag";

    private final int PIN = 13;

    private UartService mUartService;
    private Firmata mFirmata;
    int mDigitalPort;

    private final byte[] mSendingStream = new byte[3];
    private int mSendingStreamCounter = 0;

    private ProgressBar mProgressBar;
    private Switch mModeSwitch;
    private Switch mValueSwitch;

    BroadcastReceiver mBleConnectListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(NORDIC_TAG, "Action: " + action);
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                mProgressBar.setVisibility(View.GONE);
                mValueSwitch.setVisibility(View.VISIBLE);
                mModeSwitch.setVisibility(View.VISIBLE);
            }
        }
    };

    ISerial iSerial = new ISerial() {
        @Override
        public void addListener(ISerialListener iSerialListener) {
            Log.i(FIRMATA_TAG,"addListener(ISerialListener iSerialListener)");
        }

        @Override
        public void removeListener(ISerialListener iSerialListener) {
            Log.i(FIRMATA_TAG,"removeListener(ISerialListener iSerialListener)");
        }

        @Override
        public void start() {
            Log.i(FIRMATA_TAG,"start()");
        }

        @Override
        public void stop() {
            Log.i(FIRMATA_TAG,"stop()");
        }

        @Override
        public boolean isStopping() {
            Log.i(FIRMATA_TAG,"isStopping()");
            return false;
        }

        @Override
        public int available() {
            Log.i(FIRMATA_TAG,"available()");
            return 0;
        }

        @Override
        public void clear() {
            Log.i(FIRMATA_TAG,"clear()");
        }

        @Override
        public int read() {
            Log.i(FIRMATA_TAG,"read()");
            return 0;
        }

        @Override
        public void write(int i) {
            Log.i(FIRMATA_TAG,"write(int i)");
            if(mSendingStreamCounter < 3){
                mSendingStream[mSendingStreamCounter] = (byte)i;
                mSendingStreamCounter++;
            }
            if(mSendingStreamCounter == 3){
                mUartService.writeRXCharacteristic(mSendingStream);
                mSendingStreamCounter = 0;
            }
        }

        @Override
        public void write(byte[] bytes) {
            Log.i(FIRMATA_TAG,"write(byte[] bytes)");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_controler);
        String UUID = getIntent().getStringExtra("UUID");

        mProgressBar = findViewById(R.id.progressBarID);
        mModeSwitch = findViewById(R.id.modeSwitch);
        mModeSwitch.setVisibility(View.GONE);
        mValueSwitch = findViewById(R.id.valueSwitch);
        mValueSwitch.setVisibility(View.GONE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBleConnectListener,registerReceiver());

        mFirmata = new Firmata(iSerial);

        mUartService = new UartService();
        mUartService.initialize(this);
        mUartService.connect(UUID);
        mDigitalPort = getPortFromPin(PIN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUartService.writeRXCharacteristic(new byte[]{(byte)0xFF}); //mFirmata.send(new SystemRest.... not implemented by smirnov, sending directly via UART
        mUartService.disconnect();
        mUartService.close();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBleConnectListener);
    }

    private IntentFilter registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.EXTRA_DATA);
        return intentFilter;
    }

    public void switchModeChanged(View view){
        try {
            Switch pinSwitch = (Switch)view;
            if(pinSwitch.isChecked()){
                mFirmata.send(new SetPinModeMessage(PIN,1));

            }else{
                mFirmata.send(new SetPinModeMessage(PIN,0));
            }
        } catch (SerialException e) {
            e.printStackTrace();
        }
    }

    public void switchValueChanged(View view){
        try {
        Switch pinSwitch = (Switch)view;
        int value;
        if(pinSwitch.isChecked()){
            value = getDigitalPinValue(PIN, 1);
        }else{
            value = getDigitalPinValue(PIN, 0);
        }
            mFirmata.send(new DigitalMessage(mDigitalPort,value));

        } catch (SerialException e) {
            e.printStackTrace();
        }
    }
}
