package com.example.trafficcontrolapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.function.Function;

public class TrafficControlActivity extends AppCompatActivity {
    private static final String tag = "TrafficControlActivity";

    MainActivity.ConnectedThread connectedThread = MainActivity.connectedThread;

    private static MaterialSwitch leftSwitch;
    private static MaterialSwitch rightSwitch;
    private Button disconnectButton;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_layout);

        toolbar = findViewById(R.id.materialToolbar);
        toolbar.setSubtitle("Connected to thread: " + connectedThread.getName());
         leftSwitch = findViewById(R.id.leftSwitch);
         rightSwitch = findViewById(R.id.rightSwitch);
         disconnectButton = findViewById(R.id.disconnectButton);

        // animate the two switches, rotate them 90 degrees
        leftSwitch.animate().rotation(90);
        rightSwitch.animate().rotation(90);
        leftSwitch.setTextOff("RED");
        leftSwitch.setTextOn("GREEN");
        rightSwitch.setTextOff("RED");
        rightSwitch.setTextOn("GREEN");

        disconnectButton.setOnClickListener(v -> {
            connectedThread.cancel();
            getOnBackPressedDispatcher().onBackPressed();
            finish();
        });


        leftSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setTrafficState(TrafficState.LYELLOW);
                setTrafficState(TrafficState.LGREEN);
            } else {

                setTrafficState(TrafficState.LRED);
            }
        });

        rightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setTrafficState( TrafficState.RYELLOW);
                setTrafficState( TrafficState.RGREEN);
            } else {
                setTrafficState( TrafficState.RRED);
            }
        });
    }

    private enum TrafficState {
        RRED, RYELLOW, RGREEN, LRED, LYELLOW, LGREEN, RED, YELLOW, GREEN
    }

    private void setTrafficState(TrafficState trafficState) {
        switch (trafficState) {
            case RRED:
                rightSwitch.setThumbTintList(getColorStateList(R.color.red));
                rightSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
            case RYELLOW:
                rightSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case RGREEN:
                rightSwitch.setThumbTintList(getColorStateList(R.color.green));
                rightSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
            case LRED:
                leftSwitch.setThumbTintList(getColorStateList(R.color.red));
                leftSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
            case LYELLOW:
                leftSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case LGREEN:
                leftSwitch.setThumbTintList(getColorStateList(R.color.green));
                leftSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
            case RED:
                leftSwitch.setThumbTintList(getColorStateList(R.color.red));
                leftSwitch.setTrackTintList(getColorStateList(R.color.green));
                rightSwitch.setThumbTintList(getColorStateList(R.color.red));
                rightSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
            case YELLOW:
                leftSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                rightSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case GREEN:
                leftSwitch.setThumbTintList(getColorStateList(R.color.green));
                leftSwitch.setTrackTintList(getColorStateList(R.color.red));
                rightSwitch.setThumbTintList(getColorStateList(R.color.green));
                rightSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
        }
        connectedThread.write(trafficState.ordinal());
    }

    public static void handleMessage(int cmd){
        TrafficState trafficState = TrafficState.values()[cmd];
        switch (trafficState){
        case RRED:
                leftSwitch.setChecked(false);
                rightSwitch.setChecked(false);
                break;
        case RGREEN:
                leftSwitch.setChecked(true);
                rightSwitch.setChecked(true);
                break;
        case RYELLOW:
//            leftSwitch.setChecked(false);
//            rightSwitch.setChecked(false);
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }

}
