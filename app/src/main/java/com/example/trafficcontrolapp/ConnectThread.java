//package com.example.trafficcontrolapp;
//
//import android.annotation.SuppressLint;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.widget.Toast;
//
//import androidx.core.app.ActivityCompat;
//
//import java.io.IOException;
//
//public class ConnectThread extends Thread {
//    private final BluetoothDevice device;
//    private BluetoothSocket socket;
//
//    public ConnectThread(Context ctx, BluetoothDevice device) {
//        BluetoothSocket tmp = null;
//        this.device = device;
//
//        try {
//            if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return TODO;
//            }
//            tmp = device.createRfcommSocketToServiceRecord(MainActivity.ARDUINO_UUID);
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        this.socket = tmp;
//    }
//
//    @SuppressLint("MissingPermission")
//    public void run(){
//        MainActivity.bluetoothAdapter.cancelDiscovery();
//
//        try {
//            socket.connect();
//            runOnUiThread(() -> Toast.makeText(ctx, "Connected to "+ device.getName(), Toast.LENGTH_SHORT).show());
//        }
//    }
//
//}
