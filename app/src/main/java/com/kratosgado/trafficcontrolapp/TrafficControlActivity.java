package com.kratosgado.trafficcontrolapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class TrafficControlActivity extends AppCompatActivity {
    private static final String tag = "TrafficControlActivity";
    MainActivity.ConnectedThread  connectedThread = MainActivity.connectedThread;
    private CircleView lred, lyellow, lgreen, rred, ryellow, rgreen;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Starting TrafficActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_layout);
        Log.d(tag, "Started TrafficActivity");
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        setup();
    }

    private enum TrafficState {
        RRED, RYELLOW, RGREEN, LRED, LYELLOW, LGREEN, RHUMAN, LHUMAN, NORHUMAN, NOLHUMAN
    }

    private void setTrafficState(TrafficState trafficState, boolean send) {
        if (trafficState.compareTo(TrafficState.LRED) < 0) {
            rgreen.setAlpha(0);
            rred.setAlpha(0);
            ryellow.setAlpha(0);
        }else if (trafficState.compareTo(TrafficState.RHUMAN) < 0){
            lgreen.setAlpha(0);
            lred.setAlpha(0);
            lyellow.setAlpha(0);
        }
        switch (trafficState) {
            case RRED:
                rred.setAlpha(1);
                break;
            case RGREEN:
                rgreen.setAlpha(1);
                break;
            case RYELLOW:
                ryellow.setAlpha(1);
                break;
            case LGREEN:
                lgreen.setAlpha(1);
                break;
            case LRED:
                lred.setAlpha(1);
                break;
            case LYELLOW:
                lyellow.setAlpha(1);
                break;
        }
        if (send) {
            runOnUiThread(() -> Toast.makeText(this, "Sending Traffic State: " + trafficState.toString(), Toast.LENGTH_SHORT).show());
            connectedThread.write(trafficState.ordinal());
        }
    }

    public  void handleMessage(int cmd){
        try{
            TrafficState trafficState = TrafficState.values()[cmd];
            Log.d(tag, "state: " + trafficState.toString());

            switch (trafficState){
                case RHUMAN:
                    setTrafficState(TrafficState.RRED, false);
                    setRightEnabled(false);
                    toolbar.setSubtitle("Human coming on the right");
                    break;
                case NORHUMAN:
                    setRightEnabled(true);
                    setTrafficState(TrafficState.RRED, false);
                    toolbar.setSubtitle("Idle");
                    break;
                case LHUMAN:
                    setTrafficState(TrafficState.LRED, false);
                    setLeftEnabled(false);
                    toolbar.setSubtitle("Human coming on the left");
                    break;
                case NOLHUMAN:
                    setLeftEnabled(true);
                    setTrafficState(TrafficState.LRED, false);
                    toolbar.setSubtitle("Idle");
                    break;
                case RRED:
                    setTrafficState(TrafficState.RRED, false);
                    toolbar.setSubtitle("Idle");
                    break;
                case RGREEN:
                    setTrafficState(TrafficState.RGREEN, false);
                    toolbar.setSubtitle("Incoming car on the right");
                    break;
                case LRED:
                   setTrafficState(TrafficState.LRED, false);
                    toolbar.setSubtitle("Idle");
                    break;
                case LGREEN:
                   setTrafficState(TrafficState.LGREEN, false);
                    toolbar.setSubtitle("Incoming car on the left");
                    break;
                case RYELLOW:
                case LYELLOW:
                    setTrafficState(trafficState, false);
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

    private void setup(){
        toolbar = findViewById(R.id.materialToolbar);
        toolbar.setSubtitle("Connected to thread: " + connectedThread.getName());
        lred = findViewById(R.id.lred);
        lyellow = findViewById(R.id.lyellow);
        lgreen = findViewById(R.id.lgreen);
        rred = findViewById(R.id.rred);
        ryellow = findViewById(R.id.ryellow);
        rgreen = findViewById(R.id.rgreen);

        lyellow.setColor(Color.YELLOW);
        lyellow.setAlpha(0);
        lgreen.setColor(Color.GREEN);
        lgreen.setAlpha(0);
        lyellow.setOnClickListener(v -> setTrafficState(TrafficState.LYELLOW, true));
        lgreen.setOnClickListener(v -> setTrafficState(TrafficState.LGREEN, true));
        lred.setOnClickListener(v -> setTrafficState(TrafficState.LRED, true));

        ryellow.setColor(Color.YELLOW);
        ryellow.setAlpha(0);
        rgreen.setColor(Color.GREEN);
        rgreen.setAlpha(0);
        ryellow.setOnClickListener(v -> setTrafficState(TrafficState.RYELLOW, true));
        rgreen.setOnClickListener(v -> setTrafficState(TrafficState.RGREEN, true));
        rred.setOnClickListener(v -> setTrafficState(TrafficState.RRED, true));

        Button disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(v -> {
            connectedThread.cancel();
            getOnBackPressedDispatcher().onBackPressed();
            finish();
        });

        Handler handler = new Handler(msg -> {
            handleMessage(msg.what);
            return true;
        });

        if (connectedThread != null){
            connectedThread.setHandler(handler);
        }
    }

    private void setRightEnabled(boolean enabled){
        rred.setEnabled(enabled);
        rgreen.setEnabled(enabled);
        ryellow.setEnabled(enabled);
        findViewById(R.id.rightTraffic).setAlpha(enabled ? 1.0f : 0.5f);
    }
    private void setLeftEnabled(boolean enabled){
        lred.setEnabled(enabled);
        lgreen.setEnabled(enabled);
        lyellow.setEnabled(enabled);
        findViewById(R.id.leftTraffic).setAlpha(enabled ? 1.0f : 0.5f);
    }

}
