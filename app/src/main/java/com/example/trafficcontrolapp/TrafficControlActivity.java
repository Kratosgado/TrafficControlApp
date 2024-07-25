package com.example.trafficcontrolapp;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class TrafficControlActivity extends AppCompatActivity {
    private static final String tag = "TrafficControlActivity";

    MainActivity.ConnectedThread connectedThread = MainActivity.connectedThread;

    private static MaterialSwitch leftSwitch;
    private static MaterialSwitch rightSwitch;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_layout);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

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
                setTrafficState(TrafficState.LGREEN);
            } else {
                setTrafficState(TrafficState.LRED);
            }
        });

        rightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setTrafficState( TrafficState.RGREEN);
            } else {
                setTrafficState( TrafficState.RRED);
            }
        });

        Handler handler = new Handler(msg -> {
            handleMessage(msg.what);
            return true;
        });

        if (connectedThread != null){
            connectedThread.setHandler(handler);
        }
    }

    private enum TrafficState {
        RRED, RYELLOW, RGREEN, LRED, LYELLOW, LGREEN, RHUMAN, LHUMAN, NORHUMAN, NOLHUMAN
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
        }
        connectedThread.write(trafficState.ordinal());
    }

    public  void handleMessage(int cmd){
        try{
            TrafficState trafficState = TrafficState.values()[cmd];
            Log.d(tag, "state: " + trafficState.toString());

            switch (trafficState){
                case RHUMAN:
                    rightSwitch.setChecked(false);
                    rightSwitch.setEnabled(false);
                    toolbar.setSubtitle("Human coming on the right");
                case NORHUMAN:
                    rightSwitch.setEnabled(true);
                case LHUMAN:
                    leftSwitch.setChecked(false);
                    leftSwitch.setEnabled(false);
                    toolbar.setSubtitle("Human coming on the left");
                case NOLHUMAN:
                    leftSwitch.setEnabled(true);
                case RRED:
                    rightSwitch.setChecked(false);
                    toolbar.setSubtitle("Idle");
                    break;
                case RGREEN:
                    rightSwitch.setChecked(true);
                    toolbar.setSubtitle("Incoming car on the right");
                    break;
                case LRED:
                    leftSwitch.setChecked(false);
                    toolbar.setSubtitle("Idle");
                    break;
                case LGREEN:
                    leftSwitch.setChecked(true);
                    toolbar.setSubtitle("Incoming car on the left");
                    break;
                case RYELLOW:
                case LYELLOW:
                    setTrafficState(trafficState);
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
