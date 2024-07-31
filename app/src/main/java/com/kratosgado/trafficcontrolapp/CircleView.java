package com.kratosgado.trafficcontrolapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {
    private Paint paint;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED); // Default color
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, paint); // Enable software layer for shadow effect
        updateGlow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int radius = Math.min(getWidth() / 2, getHeight() / 2);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        updateGlow();
        invalidate();
    }

    private void updateGlow() {
        int color = paint.getColor();
        paint.setShadowLayer(30, 0, 0, color); // Add shadow layer to simulate glow effect
    }
}
