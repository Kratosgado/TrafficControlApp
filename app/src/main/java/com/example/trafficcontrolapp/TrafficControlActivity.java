package com.example.trafficcontrolapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class TrafficControlActivity extends AppCompatActivity {
    private static final String tag = "TrafficControlActivity";
    private Handler handler;

    MainActivity.ConnectedThread connectedThread = MainActivity.connectedThread;

    private static MaterialSwitch leftSwitch;
    private static MaterialSwitch rightSwitch;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_layout);

        toolbar = findViewById(R.id.materialToolbar);
        toolbar.setSubtitle("Connected to thread: " + connectedThread.getName());
         leftSwitch = findViewById(R.id.leftSwitch);
         rightSwitch = findViewById(R.id.rightSwitch);
        Button disconnectButton = findViewById(R.id.disconnectButton);

        // animate the two switches, rotate them 90 degrees
        leftSwitch.animate().rotation(90);
        rightSwitch.animate().rotation(90);

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

        handler = new Handler(msg -> {
            handleMessage(msg.what);
            return true;
        });

        if (connectedThread != null){
            connectedThread.setHandler(handler);
        }
    }

    private enum TrafficState {
        RRED, RYELLOW, RGREEN, LRED, LYELLOW, LGREEN, RED, YELLOW, GREEN
    }

    private void setTrafficState(TrafficState trafficState) {
        runOnUiThread(() -> Toast.makeText(this, "Sending Traffic State: " + trafficState.toString(), Toast.LENGTH_SHORT).show());
        switch (trafficState) {
            case RRED:
                rightSwitch.setThumbTintList(getColorStateList(R.color.green));
                rightSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
            case RYELLOW:
                rightSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case RGREEN:
                rightSwitch.setThumbTintList(getColorStateList(R.color.red));
                rightSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
            case LRED:
                leftSwitch.setThumbTintList(getColorStateList(R.color.green));
                leftSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
            case LYELLOW:
                leftSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case LGREEN:
                leftSwitch.setThumbTintList(getColorStateList(R.color.red));
                leftSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
            case RED:
                leftSwitch.setThumbTintList(getColorStateList(R.color.green));
                leftSwitch.setTrackTintList(getColorStateList(R.color.red));
                rightSwitch.setThumbTintList(getColorStateList(R.color.green));
                rightSwitch.setTrackTintList(getColorStateList(R.color.red));
                break;
            case YELLOW:
                leftSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                rightSwitch.setTrackTintList(getColorStateList(R.color.yellow));
                break;
            case GREEN:
                leftSwitch.setThumbTintList(getColorStateList(R.color.red));
                leftSwitch.setTrackTintList(getColorStateList(R.color.green));
                rightSwitch.setThumbTintList(getColorStateList(R.color.red));
                rightSwitch.setTrackTintList(getColorStateList(R.color.green));
                break;
        }
        connectedThread.write(trafficState.ordinal());
    }

    public  void handleMessage(int cmd){
        try{
            TrafficState trafficState = TrafficState.values()[cmd];
            Log.d(tag, "state: " + trafficState.toString());

            switch (trafficState){
                case RRED:
                    setTrafficState(trafficState);
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
        }catch (Exception error){
            Log.e(tag, "Error: " + error.getMessage());
            error.printStackTrace();
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
