package com.example.usb_connect;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {

    Button btnCheck;
    Button btnClear;
    TextView textInfo;

    ArrayList<String> listDeviceName;
    ArrayList<UsbDevice> listUsbDevice;

    UsbManager manager;
    HashMap<String, UsbDevice> deviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize View
        initView();
    }

    private void initView() {
        textInfo = findViewById(R.id.info);
        btnCheck = findViewById(R.id.check);
        btnClear = findViewById(R.id.clear);
        btnCheck.setOnClickListener(arg0 -> checkDeviceInfo());
        btnClear.setOnClickListener(arg0 -> clearText());
    }

    private void clearText() {
        textInfo.setText(" ");
    }


    private void checkDeviceInfo() {
        // Initialize usb device manager and get devices list
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        deviceList = manager.getDeviceList();
        Log.e("Devices Length------- ", String.valueOf(deviceList.size()));

        listDeviceName = new ArrayList<>();
        listUsbDevice = new ArrayList<>();

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.e("Devices --------  ", deviceIterator.toString());
        String text = "";
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            listDeviceName.add(device.getDeviceName());
            listUsbDevice.add(device);

            text =
                    "DeviceID: " + device.getDeviceId() + "\n\n\n" +
                            "DeviceName: " + device.getDeviceName() + "\n\n\n" +
                            "DeviceClass: " + device.getDeviceClass() + " - " + translateDeviceClass(device.getDeviceClass()) + "\n\n\n" +
                            "DeviceSubClass: " + device.getDeviceSubclass() + "\n\n\n" +
                            "VendorID: " + device.getVendorId() + "\n\n\n" +
                            "ProductID: " + device.getProductId() + "\n\n\n" +
                            "InterfaceCount: " + device.getInterfaceCount();
        }

        textInfo.setText(text);
    }


    private String translateDeviceClass(int deviceClass) {
        switch (deviceClass) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "Application specific USB class";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB class for audio devices";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB class for CDC devices (communications device class)";
            case UsbConstants.USB_CLASS_COMM:
                return "USB class for communication devices";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB class for content security devices";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB class for content smart card devices";
            case UsbConstants.USB_CLASS_HID:
                return "USB class for human interface devices (for example, mice and keyboards)";
            case UsbConstants.USB_CLASS_HUB:
                return "USB class for USB hubs";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB class for mass storage devices";
            case UsbConstants.USB_CLASS_MISC:
                return "USB class for wireless miscellaneous devices";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB class indicating that the class is determined on a per-interface basis";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB class for physical devices";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB class for printers";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB class for still image devices (digital cameras)";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "Vendor specific USB class";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB class for video devices";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB class for wireless controller devices";
            default:
                return "Unknown USB class!";

        }
    }


}