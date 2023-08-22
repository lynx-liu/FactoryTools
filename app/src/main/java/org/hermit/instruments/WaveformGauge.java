package org.hermit.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.Log;

import org.hermit.core.SurfaceRunner;

public class WaveformGauge {
    private int dispX = 0;
    private int dispY = 0;
    private int dispWidth = 0;
    private int dispHeight = 0;
    private Bitmap waveBitmap = null;
    private Canvas waveCanvas = null;
    private final SurfaceRunner parentSurface;

    WaveformGauge(SurfaceRunner parent) {
        parentSurface = parent;
    }

    public void setGeometry(Rect bounds) {
        dispX = bounds.left;
        dispY = bounds.top;
        dispWidth = bounds.width();
        dispHeight = bounds.height();
        waveBitmap = parentSurface.getBitmap(dispWidth, dispHeight);
        waveCanvas = new Canvas(waveBitmap);
        waveCanvas.drawColor(Color.WHITE);//设置背景颜色
    }

    final void update(short[] buffer, int off, int len, float bias, float range) {
        float scale = (float)Math.pow((double)(1.0F / (range / 6500.0F)), 0.7D) / 16384.0F * (float)dispHeight;
        if (scale >= 0.001F && !Float.isInfinite(scale)) {
            if (scale > 1000.0F) {
                scale = 1000.0F;
            }
        } else {
            scale = 0.001F;
        }

        float margin = (float)(dispWidth / 24);
        float gwidth = (float)dispWidth - margin * 2.0F;
        float baseY = (float)dispHeight / 2.0F;
        float uw = gwidth / (float)len;
        synchronized(this) {
            Canvas canvas = waveCanvas;
            canvas.drawColor(Color.WHITE);//重刷背景颜色

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Style.FILL);
            canvas.drawLine(margin, 0.0F, margin, (float)(dispHeight - 1), paint);

            paint.setStyle(Style.STROKE);
            for(int i = 0; i < len; ++i) {
                float x = margin + (float)i * uw;
                int flag = Math.abs(buffer[off + i]) <= 130 ? 0 : buffer[off + i];
                float y = baseY - ((float)flag - bias) * scale;
                canvas.drawLine(x, baseY, x, y, paint);
            }
        }
    }

    public final void drawBody(Canvas canvas, long now) {
        synchronized(this) {
            canvas.drawBitmap(waveBitmap, (float)dispX, (float)dispY, null);
        }
    }
}
