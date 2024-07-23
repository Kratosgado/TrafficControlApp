package com.example.trafficcontrolapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class TrafficControlActivity extends AppCompatActivity {
    private static final String tag = "TrafficControlActivity";

    MainActivity.ConnectedThread connectedThread = MainActivity.connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_layout);

        Switch leftSwitch = findViewById(R.id.leftSwitch);
        Switch rightSwitch = findViewById(R.id.rightSwitch);
        Button disconnectButton = findViewById(R.id.disconnectButton);

        // animate the two switches, rotate them 90 degrees
        leftSwitch.animate().rotation(90);
        rightSwitch.animate().rotation(90);


        // set the thumb asset of the switches
        leftSwitch.setThumbResource(R.drawable.red);
        rightSwitch.setThumbResource(R.drawable.red);

        connectedThread.write("0");
    }
}
