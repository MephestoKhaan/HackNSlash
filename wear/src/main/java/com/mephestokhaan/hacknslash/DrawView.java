package com.mephestokhaan.hacknslash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */

public class DrawView extends View
{
    Paint paint = new Paint();
    float percentage = 1.0f;
    boolean goesRight = true;

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void SetProperties(int color, boolean goesRight)
    {
        this.goesRight = goesRight;
        paint.setColor(color);
        paint.setStrokeWidth(3);
    }

    public void SetPercentage(float percentage) {
        if(!goesRight)
        {
            percentage = 1f - percentage;
        }
        this.percentage = Math.max(0f, Math.min(1f, percentage));
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(goesRight) {
            canvas.drawRect(0, 0, canvas.getWidth() * percentage, canvas.getHeight(), paint);
        }
        else{
            canvas.drawRect(canvas.getWidth() * percentage, 0, canvas.getWidth(), canvas.getHeight(), paint);
        }
    }
}
