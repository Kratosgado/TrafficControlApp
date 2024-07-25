package com.example.trafficcontrolapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TrafficLightView extends View {
    private static final String tag = "TrafficLightView";
    private Paint redPaint;
    private Paint yellowPaint;
    private Paint greenPaint;
    private Paint outlinePaint;
    private State currentState;
    private OnLightTouchListener onLightTouchListener;

    public enum State {
        RED, YELLOW, GREEN
    }

    public TrafficLightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        redPaint = new Paint();
        redPaint.setColor(Color.RED);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);

        outlinePaint = new Paint();
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(10);


        currentState = State.RED; // Start with red light
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int lightRadius = width / 3;

        // Draw red light
        canvas.drawCircle(width / 2.0f, height / 4.0f, lightRadius, redPaint);
        canvas.drawCircle(width / 2.0f, height / 4.0f, lightRadius, outlinePaint);

        // Draw yellow light
        canvas.drawCircle(width / 2.0f, height / 2.0f, lightRadius, yellowPaint);
        canvas.drawCircle(width / 2.0f, height / 2.0f, lightRadius, outlinePaint);

        // Draw green light
        canvas.drawCircle(width / 2.0f, 3 * height / 4.0f, lightRadius, greenPaint);
        canvas.drawCircle(width / 2.0f, 3 * height / 4.0f, lightRadius, outlinePaint);

        // Dim inactive lights
        redPaint.setAlpha(80);
        yellowPaint.setAlpha(80);
        greenPaint.setAlpha(80);
        switch (currentState){
            case RED:
                redPaint.setAlpha(255);
                break;
            case YELLOW:
                yellowPaint.setAlpha(255);
                break;
            case GREEN:
                greenPaint.setAlpha(255);
                break;
        }
    }

    public void setOnLightTouchListener(OnLightTouchListener listener){
        this.onLightTouchListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();
        State state = State.RED;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int height = getHeight();
            float y = event.getY();

            if (y < (height / 3.0f)) {
            } else if (y < 2 * height / 3.0f) {
                state = (State.YELLOW);
            } else {
                state = (State.GREEN);
            }

            if (onLightTouchListener != null && state != currentState){
                onLightTouchListener.onLightTouched(state);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setState(State newState) {
        invalidate();
        Log.d(tag, "setting state: " + newState.toString());
        this.currentState = newState;
    }
}
