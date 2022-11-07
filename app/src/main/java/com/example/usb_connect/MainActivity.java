package com.example.usb_connect;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Lists;
import java.util.List;

public class MainActivity extends Activity implements Runnable{


    // data *******************
    UsbDevice device;
    UsbManager manager;

    private Byte[] bytes;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Thread readThread;
    private UsbEndpoint usbEndpointIn;
    private UsbEndpoint usbEndpointOut;
    UsbDeviceConnection connection;
    UsbInterface usbInterface;
    TextView tvResult;
    Button btnScan, btnDisconnect;
    private boolean mRunning;

    // receivers **********************************************************************************
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice mDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(mDevice != null){
                            device = mDevice;
                            performHandCheck();
                        }
                    }
                    else {
                        Log.d("SAYED ", "permission denied for device " + device);
                    }
                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initUi();
        registerPermissionReceiver();
    }

    private void initUi(){
        tvResult = findViewById(R.id.tvHandCheck);
        btnScan = findViewById(R.id.btnScan);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnScan.setOnClickListener(v -> fetchDevice());
        btnDisconnect.setOnClickListener(v -> {
            mRunning = false;
            //readThread.stop();
            connection.releaseInterface(usbInterface);
            connection.close();
        });
    }
    private void registerPermissionReceiver(){
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }

    private void init(){
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    private void fetchDevice(){
        List<UsbDevice> devicesList = Lists.newArrayList(manager.getDeviceList().values().iterator());
        if(devicesList.isEmpty()){
            tvResult.setText("Please connect your device");
            return;
        }
        if(devicesList.size() >1){
            tvResult.setText("Please connect only one device");
            return;
        }
        device =  devicesList.get(0);
        if(manager.hasPermission(device)) performHandCheck();
        else manager.requestPermission(device, permissionIntent);
    }

    private void performHandCheck(){
         usbInterface = findHidInterface();
        if(usbInterface == null) return;
        connection = manager.openDevice(device);
        if(connection == null){
            tvResult.setText("Connection is null");
            return;
        }

        if (!connection.claimInterface(usbInterface, true)) {
            tvResult.setText("USB claim interface failed");
            return;
        }
            UsbEndpoint usbEndpointOut = null;
            UsbEndpoint usbEndpointIn = null;

            //
            // Look for Interrupt endpoints
            //
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint usbEndpoint = usbInterface.getEndpoint(i);
                if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        usbEndpointOut = usbEndpoint;
                    } else {
                        usbEndpointIn = usbEndpoint;
                    }
                }
            }

            if (usbEndpointOut == null || usbEndpointIn == null) {
                tvResult.setText("No USB endpoints found");
                return;
            }

            this.usbEndpointIn = usbEndpointIn;
            this.usbEndpointOut = usbEndpointOut;

        //SEND START COMMAND TO  THE USB DEVICE;
        int result = connection.bulkTransfer(usbEndpointOut, "AA BB".getBytes(), "AA BB".getBytes().length, 1000);
        Log.e("SEND RESULT", result + "");

        //START READING in run method
        readThread = new Thread(MainActivity.this);
        readThread.start();
    }


    /**
     * Find the HID interface.
     *
     * @return
     *      Return the HID interface if found, otherwise null.
     */
    private UsbInterface findHidInterface() {
        if (device != null) {
            final int interfaceCount = device.getInterfaceCount();

            for (int i = 0; i < interfaceCount; i++) {
                UsbInterface usbInterface = device.getInterface(i);

                //
                // Can add UsbInterface.getInterfaceSubclass() and
                // UsbInterface.getInterfaceProtocol() for more specifics.
                //

                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                    return usbInterface;
                }
            }

            tvResult.setText("HID interface not found");
        }
        return null;
    }


    @Override
    public void run() {
        mRunning = true;
        //READ VALUE UNTIL DISCONNECT
        while (mRunning) {
            byte[] bytes = new byte[usbEndpointIn.getMaxPacketSize()];
            int result = connection.bulkTransfer(usbEndpointIn, bytes, bytes.length, 1000);
            if(result > 0)
                tvResult.setText("RESULT : " + new String(bytes));
        }
        Log.d("Thread", "STOPPPED");
    }
}