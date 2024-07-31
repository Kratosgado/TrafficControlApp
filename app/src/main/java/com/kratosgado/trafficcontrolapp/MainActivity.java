package com.kratosgado.trafficcontrolapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String tag = "MainActivity";

    public static BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesArrayAdapter;
    private final ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    private ConnectThread connectThread;
    public static ConnectedThread connectedThread;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Change the status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        Log.d(tag, "onCreate: Initializing Bluetooth adapter");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(tag, "onCreate: Bluetooth is not available");
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK) {
                        Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
        }

        ListView devicesListView = findViewById(R.id.devices_list_view);
        devicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        devicesListView.setAdapter(devicesArrayAdapter);

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);
            connectToDevice(device);
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Click on: " + device.getName(), Toast.LENGTH_SHORT).show());
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> discoverDevices());
    }

    @SuppressLint("MissingPermission")
    private void discoverDevices() {
        Log.d(tag, "discoverDevices: Starting device discovery");
        devicesArrayAdapter.clear();
        deviceList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d(tag, "discoverDevices: Paired device found: " + device.getName() + " [" + device.getAddress() + "]");
                devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                deviceList.add(device);
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(tag, "onReceive: Device found: " + device.getName() + " [" + device.getAddress() + "]");
                devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                deviceList.add(device);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Log.d(tag, "connectToDevice: Connecting to device: " + device.getName() + " [" + device.getAddress() + "]");
        if (connectThread != null) {
            connectThread.cancel();
        }
        connectThread = new ConnectThread(device);
//        connectThread.run();
        connectThread.start();

    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            this.device = device;
            UUID uuid = this.device.getUuids()[0].getUuid();
            Log.d(tag, "ConnectThread: Trying to create RFCOMM socket to " + uuid.toString());

            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
                Log.d(tag, "ConnectThread: RFCOMM socket created");
            } catch (IOException e) {
                Log.e(tag, "ConnectThread: Socket's create() method failed", e);
                e.printStackTrace();
            }
            this.socket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            Log.d(tag, "ConnectThread: Running connect thread");
            MainActivity.bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                Log.d(tag, "ConnectThread: Socket connected");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show());

                // navigate to TrafficControlActivity
                 Intent intent = new Intent(MainActivity.this, TrafficControlActivity.class);
                 startActivity(intent);
            } catch (IOException connectException) {
                Log.e(tag, "ConnectThread: Unable to connect; closing socket", connectException);
                try {
                    socket.close();
                    Log.d(tag, "ConnectThread: Socket closed");
                } catch (IOException closeException) {
                    Log.e(tag, "ConnectThread: Could not close the client socket", closeException);
                    closeException.printStackTrace();
                }
                return;
            }
            manageConnectedSocket(socket);
        }

        public void cancel() {
            try {
                socket.close();
                Log.d(tag, "ConnectThread: Socket closed in cancel");
            } catch (IOException e) {
                Log.e(tag, "ConnectThread: Could not close the client socket in cancel", e);

                e.printStackTrace();
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        Log.d(tag, "manageConnectedSocket: Managing connected socket");
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Handler handler;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(tag, "ConnectedThread: Streams obtained");
            } catch (IOException e) {
                Log.e(tag, "ConnectedThread: Error occurred when creating input/output stream", e);
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void setHandler(Handler handler){
            this.handler = handler;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    try {
                        int cmd = Integer.parseInt(String.valueOf(readMessage.charAt(0)));
                        if (handler != null){
                            handler.obtainMessage(cmd).sendToTarget();
                            continue;
                        }

                    } catch (NumberFormatException e) {
                        Log.e(tag, "ConnectedThread: Error parsing message: " + readMessage, e);
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Arduino: " + readMessage, Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(int bytes) {
            Log.d(tag, "Sending message to arduino");
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        if (connectThread != null) {
            connectThread.cancel();
        }
    }
}
