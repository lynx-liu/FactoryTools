package org.hermit.instruments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

import org.hermit.core.SurfaceRunner;
import org.hermit.utils.CharFormatter;

public class PowerGauge {
    private static final int METER_PEAKS = 4;
    private static final int METER_PEAK_TIME = 4000;
    private static final int METER_AVERAGE_COUNT = 32;
    private static final int METER_POWER_COL = Color.BLUE;
    private static final int METER_AVERAGE_COL = 0xA0FF9000;
    private static final int METER_PEAK_COL = 0xFF0000;
    private int barWidth = METER_AVERAGE_COUNT;
    private int dispX = 0;
    private int dispY = 0;
    private int dispWidth = 0;
    private int dispHeight = 0;
    private float labelSize = 0.0F;
    private float meterBarTop = 0.0F;
    private float meterBarGap = 0.0F;
    private float meterLabY = 0.0F;
    private float meterTextX = 0.0F;
    private float meterTextY = 0.0F;
    private float meterTextSize = 0.0F;
    private float meterSubTextX = 0.0F;
    private float meterSubTextY = 0.0F;
    private float meterSubTextSize = 0.0F;
    private float meterBarMargin = 0.0F;
    private float currentPower = 0.0F;
    private float prevPower = 0.0F;
    private float[] powerHistory = null;
    private int historyIndex = 0;
    private float averagePower = -100.0F;
    private float[] meterPeaks = null;
    private long[] meterPeakTimes = null;
    private float meterPeakMax = 0.0F;
    private char[] dbBuffer = null;
    private char[] pkBuffer = null;
    private Paint drawPaint = null;

    PowerGauge(SurfaceRunner parent) {
        meterPeaks = new float[4];
        meterPeakTimes = new long[4];
        powerHistory = new float[METER_AVERAGE_COUNT];

        for(int i = 0; i < METER_AVERAGE_COUNT; ++i) {
            powerHistory[i] = -100.0F;
        }

        averagePower = -100.0F;
        dbBuffer = "-100.0dB".toCharArray();
        pkBuffer = "-100.0dB peak".toCharArray();
    }

    public void setBarWidth(int width) {
        barWidth = width;
    }

    public void setLabelSize(float size) {
        labelSize = size;
    }

    public float getLabelSize() {
        return labelSize;
    }

    public void setGeometry(Rect bounds) {
        drawPaint = new Paint();
        dispX = bounds.left;
        dispY = bounds.top;
        dispWidth = bounds.width();
        dispHeight = bounds.height();
        int mw = dispWidth;
        int mh = dispHeight;
        if (labelSize == 0.0F) {
            labelSize = (float)mw / 24.0F;
        }

        meterBarTop = 0.0F;
        meterBarGap = (float)(barWidth / 4);
        meterLabY = meterBarTop + (float)barWidth + labelSize;
        meterBarMargin = labelSize;
        float th = (float)mh - meterLabY;
        float subWidth = ((float)mw - meterBarMargin * 2.0F) / 2.0F;
        meterTextSize = findTextSize(subWidth, th, "-100.0dB", drawPaint);
        drawPaint.setTextSize(meterTextSize);
        meterTextX = meterBarMargin;
        meterTextY = (float)mh - drawPaint.descent();
        meterSubTextSize = findTextSize(subWidth, th, "-100.0dB peak", drawPaint);
        drawPaint.setTextSize(meterSubTextSize);
        meterSubTextX = meterTextX + subWidth;
        meterSubTextY = (float)mh - drawPaint.descent();
        if (meterTextSize <= th / 2.0F) {
            float split = th * 1.0F / 3.0F;
            meterTextSize = (th - split) * 0.9F;
            drawPaint.setTextSize(meterTextSize);
            float tw = drawPaint.measureText("-100.0dB");
            meterTextX = ((float)mw - tw) / 2.0F;
            meterTextY = (float)mh - split - drawPaint.descent();
            meterSubTextSize = (th - meterTextSize) * 0.9F;
            drawPaint.setTextSize(meterSubTextSize);
            float pw = drawPaint.measureText("-100.0dB peak");
            meterSubTextX = ((float)mw - pw) / 2.0F;
            meterSubTextY = (float)mh - drawPaint.descent();
        }
    }

    private float findTextSize(float w, float h, String template, Paint paint) {
        float size = h;

        do {
            paint.setTextSize(size);
            int sw = (int)paint.measureText(template);
            if ((float)sw <= w) {
                break;
            }

            --size;
        } while(size > 12.0F);

        return size;
    }

    protected void drawBackgroundBody(Canvas canvas, Paint paint) {
        canvas.drawColor(Color.WHITE);//重刷背景色
        paint.setColor(Color.MAGENTA);
        paint.setStyle(Style.STROKE);
        float mx = (float)dispX + meterBarMargin;
        float mw = (float)dispWidth - meterBarMargin * 2.0F;
        float by = (float)dispY + meterBarTop;
        float bw = mw - 1.0F;
        float bh = (float)(barWidth - 1);
        float gw = bw / 10.0F;
        canvas.drawRect(mx, by, mx + bw, by + bh, paint);

        float ls;
        for(int i = 1; i < 10; ++i) {
            ls = (float)i * bw / 10.0F;
            canvas.drawLine(mx + ls, by, mx + ls, by + bh, paint);
        }

        float ly = (float)dispY + meterLabY;
        ls = labelSize;
        paint.setColor(Color.BLUE);
        paint.setStyle(Style.FILL);
        paint.setTextSize(ls);
        int step = paint.measureText("-99") > bw / 10.0F - 1.0F ? 2 : 1;

        for(int i = 0; i <= 10; i += step) {
            String text = "" + (i * 10 - 100);
            float tw = paint.measureText(text);
            float lx = mx + (float)i * gw + 1.0F - tw / 2.0F;
            canvas.drawText(text, lx, ly, paint);
        }
    }

    final void update(double power) {
        synchronized(this) {
            if (power < -100.0D) {
                power = -100.0D;
            } else if (power > 0.0D) {
                power = 0.0D;
            }

            currentPower = (float)power;
            if (++historyIndex >= powerHistory.length) {
                historyIndex = 0;
            }

            prevPower = powerHistory[historyIndex];
            powerHistory[historyIndex] = (float)power;
            averagePower -= prevPower / METER_AVERAGE_COUNT;
            averagePower += (float)power / METER_AVERAGE_COUNT;
        }
    }

    public final void drawBody(Canvas canvas, long now) {
        synchronized(this) {
            drawBackgroundBody(canvas,drawPaint);
            calculatePeaks(now, currentPower, prevPower);
            float mx = (float)dispX + meterBarMargin;
            float mw = (float)dispWidth - meterBarMargin * 2.0F;
            float by = (float)dispY + meterBarTop;
            float bh = (float)barWidth;
            float gap = meterBarGap;
            float bw = mw - 2.0F;
            float pa = (averagePower / 100.0F + 1.0F) * bw;
            drawPaint.setStyle(Style.FILL);
            drawPaint.setColor(METER_AVERAGE_COL);
            canvas.drawRect(mx + 1.0F, by + 1.0F, mx + pa + 1.0F, by + bh - 1.0F, drawPaint);
            float p = (currentPower / 100.0F + 1.0F) * bw;
            drawPaint.setStyle(Style.FILL);
            drawPaint.setColor(METER_POWER_COL);
            canvas.drawRect(mx + 1.0F, by + gap, mx + p + 1.0F, by + bh - gap, drawPaint);
            drawPaint.setStyle(Style.FILL);

            float fac;
            for(int i = 0; i < 4; ++i) {
                if (meterPeakTimes[i] != 0L) {
                    long age = now - meterPeakTimes[i];
                    fac = 1.0F - (float)age / METER_PEAK_TIME;
                    int alpha = (int)(fac * 255.0F);
                    drawPaint.setColor(METER_PEAK_COL | alpha << 24);
                    float pp = (meterPeaks[i] / 100.0F + 1.0F) * bw;
                    canvas.drawRect(mx + pp - 1.0F, by + gap, mx + pp + 3.0F, by + bh - gap, drawPaint);
                }
            }

            float tx = (float)dispX + meterTextX + meterBarMargin;
            float ty = (float)dispY + meterTextY;
            CharFormatter.formatFloat(dbBuffer, 0, (double)averagePower, 6, 1);
            drawPaint.setStyle(Style.FILL_AND_STROKE);
            drawPaint.setColor(Color.CYAN);
            drawPaint.setTextSize(meterTextSize);
            canvas.drawText(dbBuffer, 0, dbBuffer.length, tx, ty, drawPaint);
            float px = (float)dispX + meterSubTextX + meterBarMargin;
            fac = (float)dispY + meterSubTextY;
            CharFormatter.formatFloat(pkBuffer, 0, (double)meterPeakMax, 6, 1);
            drawPaint.setTextSize(meterSubTextSize);
            canvas.drawText(pkBuffer, 0, pkBuffer.length, px, fac, drawPaint);
        }
    }

    private final void calculatePeaks(long now, float power, float prev) {
        for(int i = 0; i < 4; ++i) {
            if (meterPeakTimes[i] != 0L && (meterPeaks[i] < power || now - meterPeakTimes[i] > METER_PEAK_TIME)) {
                meterPeakTimes[i] = 0L;
            }
        }

        if (power > prev) {
            boolean done = false;

            for(int i = 0; i < 4; ++i) {
                if (meterPeakTimes[i] != 0L && (double)(meterPeaks[i] - power) < 2.5D) {
                    meterPeakTimes[i] = now;
                    done = true;
                    break;
                }
            }

            if (!done) {
                for(int i = 0; i < 4; ++i) {
                    if (meterPeakTimes[i] == 0L) {
                        meterPeaks[i] = power;
                        meterPeakTimes[i] = now;
                        break;
                    }
                }
            }
        }

        meterPeakMax = -100.0F;

        for(int i = 0; i < 4; ++i) {
            if (meterPeakTimes[i] != 0L && meterPeaks[i] > meterPeakMax) {
                meterPeakMax = meterPeaks[i];
            }
        }
    }
}
