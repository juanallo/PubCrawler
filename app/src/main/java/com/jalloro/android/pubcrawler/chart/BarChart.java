package com.jalloro.android.pubcrawler.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.jalloro.android.pubcrawler.R;

import java.util.ArrayList;
import java.util.List;

public class BarChart extends View {

    private List<Bar> data;
    private float scaleFactor;
    private Paint boxPaint1;
    private Paint boxPaint2;
    private Paint textPaint;


    public BarChart(Context context) {
        super(context);
        init();
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        data = new ArrayList<>();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        scaleFactor = metrics.density;
        boxPaint1 = new Paint();
        boxPaint2 = new Paint();
        textPaint = new Paint();
    }

    public void setData(List<Bar> data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = (int) ( getWidth() * scaleFactor);

        int bigPadding = (int) ( 20 * scaleFactor);

        textPaint.setColor(getResources().getColor(R.color.white));
        boxPaint1.setColor(getResources().getColor(R.color.light_blue));
        boxPaint2.setColor(getResources().getColor(R.color.pink));
        textPaint.setTextSize(14*scaleFactor);

        final int amountOfBars = data.size();

        int smallPadding = (int) ( 5 * scaleFactor);
        if(amountOfBars >0){
            final int barWidth = (width - 2 * smallPadding)/amountOfBars - amountOfBars * 2 * smallPadding;

            final int maxBarHeight = height - 2 * bigPadding;

            final int biggestBarFound = findBiggestBar(data);

            int startingPosition = smallPadding;
            boolean useFirstPaint = true;
            for(Bar bar : data){
                final int barValue = bar.getValue();
                Integer paintedBarValue = barValue * maxBarHeight / biggestBarFound;
                if(paintedBarValue < bigPadding){
                    paintedBarValue = bigPadding;
                }
                canvas.drawRect(startingPosition,height - bigPadding - paintedBarValue,barWidth + startingPosition,height   , useFirstPaint ? boxPaint1 : boxPaint2);
                int x = startingPosition + smallPadding, y = (height - bigPadding);
                for (String line: bar.getLabel().split("\n")) {
                    canvas.drawText(line,x,y, textPaint);
                    y += textPaint.descent() - textPaint.ascent();
                }
                startingPosition += smallPadding + barWidth;
                useFirstPaint = !useFirstPaint;
            }
        }
    }

    private int findBiggestBar(List<Bar> data) {
        int biggest = 0;
        for (Bar bar : data){
            if(bar.getValue()> biggest){
                biggest = bar.getValue();
            }
        }
        return biggest;
    }
}
