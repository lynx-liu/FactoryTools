package org.hermit.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public abstract class SurfaceRunner extends SurfaceView implements Callback {
    public static final int SURFACE_DYNAMIC = 1;
    private static final int ENABLE_SURFACE = 1;
    private static final int ENABLE_SIZE = 2;
    private static final int ENABLE_RESUMED = 4;
    private static final int ENABLE_STARTED = 8;
    private static final int ENABLE_FOCUSED = 16;
    private static final int ENABLE_ALL = 31;
    private SurfaceHolder surfaceHolder = null;
    private int surfaceOptions = 0;
    private int enableFlags = 0;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private Config canvasConfig = null;
    private SurfaceRunner.Ticker animTicker = null;

    public SurfaceRunner(Context context) {
        super(context);
        init(0);
    }

    public SurfaceRunner(Context context, int options) {
        super(context);
        init(options);
    }

    public SurfaceRunner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(0);
    }

    private void init(int options) {
        surfaceOptions = options;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontalFadingEdgeEnabled(false);
        setVerticalFadingEdgeEnabled(false);
    }

    public boolean optionSet(int option) {
        return (surfaceOptions & option) != 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setEnable(ENABLE_SURFACE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!optionSet(ENABLE_SURFACE) && isEnable(ENABLE_SIZE)) {
            Log.e("SurfaceRunner", "ignored surfaceChanged " + width + "x" + height);
        } else {
            setSize(format, width, height);
            setEnable(ENABLE_SIZE);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        clearEnable(ENABLE_SURFACE);
    }

    public void onResume() {
        setEnable(ENABLE_RESUMED);
        setEnable(ENABLE_FOCUSED);
    }

    public void surfaceStart() {
        setEnable(ENABLE_STARTED);
    }

    public void surfaceStop() {
        clearEnable(ENABLE_STARTED);
    }

    public void onPause() {
        clearEnable(ENABLE_RESUMED);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            clearEnable(ENABLE_FOCUSED);
        } else {
            setEnable(ENABLE_FOCUSED);
        }
    }

    private boolean isEnable(int flag) {
        boolean val = false;
        synchronized(surfaceHolder) {
            val = (enableFlags & flag) == flag;
            return val;
        }
    }

    private void setEnable(int flag) {
        boolean enabled1 = false;
        boolean enabled2 = false;
        synchronized(surfaceHolder) {
            enabled1 = (enableFlags & ENABLE_ALL) == ENABLE_ALL;
            enableFlags |= flag;
            enabled2 = (enableFlags & ENABLE_ALL) == ENABLE_ALL;
        }

        if (!enabled1 && enabled2) {
            startRun();
        }
    }

    private void clearEnable(int flag) {
        boolean enabled1 = false;
        boolean enabled2 = false;
        synchronized(surfaceHolder) {
            enabled1 = (enableFlags & ENABLE_ALL) == ENABLE_ALL;
            enableFlags &= ~flag;
            enabled2 = (enableFlags & ENABLE_ALL) == ENABLE_ALL;
        }

        if (enabled1 && !enabled2) {
            stopRun();
        }
    }

    private void startRun() {
        synchronized(surfaceHolder) {
            try {
                animStart();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (animTicker != null && animTicker.isAlive()) {
                animTicker.kill();
            }

            Log.i("SurfaceRunner", "set running: start ticker");
            animTicker = (SurfaceRunner.Ticker)(!optionSet(ENABLE_SIZE) ? new SurfaceRunner.ThreadTicker() : new SurfaceRunner.LoopTicker());
        }
    }

    private void stopRun() {
        SurfaceRunner.Ticker ticker = null;
        synchronized(surfaceHolder) {
            ticker = animTicker;
        }

        if (ticker != null && ticker.isAlive()) {
            if (onSurfaceThread()) {
                ticker.kill();
            } else {
                ticker.killAndWait();
            }
        }

        synchronized(surfaceHolder) {
            animTicker = null;
        }

        try {
            animStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSize(int format, int width, int height) {
        synchronized(surfaceHolder) {
            switch(format) {
                case 1:
                    canvasConfig = Config.ARGB_8888;
                    break;
                case 4:
                    canvasConfig = Config.RGB_565;
                    break;
                case 7:
                    canvasConfig = Config.ARGB_4444;
                    break;
                case 8:
                    canvasConfig = Config.ALPHA_8;
                    break;
                default:
                    canvasConfig = Config.RGB_565;
                    break;
            }

            canvasWidth = width;
            canvasHeight = height;
            layout(canvasWidth, canvasHeight);
        }
    }

    private void tick() {
        try {
            long now = System.currentTimeMillis();
            doUpdate(now);
            refreshScreen(now);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshScreen(long now) {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas(null);
            synchronized(surfaceHolder) {
                doDraw(canvas, now);
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    protected abstract void layout(int width, int height);

    protected abstract void animStart();

    protected abstract void animStop();

    protected abstract void doUpdate(long var1);

    protected abstract void doDraw(Canvas canvas, long now);

    public Bitmap getBitmap(int w, int h) {
        return Bitmap.createBitmap(w, h, canvasConfig);
    }

    public boolean onSurfaceThread() {
        return Thread.currentThread() == animTicker;
    }

    private class LoopTicker extends Thread implements SurfaceRunner.Ticker {
        private static final int MSG_TICK = 6;
        private static final int MSG_ABORT = 9;
        private Handler msgHandler;

        private LoopTicker() {
            super("Surface Runner");
            msgHandler = null;
            start();
        }

        public void kill() {
            synchronized(this) {
                if (msgHandler != null) {
                    msgHandler.removeMessages(MSG_TICK);
                    msgHandler.sendEmptyMessage(MSG_ABORT);
                }
            }
        }

        public void killAndWait() {
            if (Thread.currentThread() == this) {
                throw new IllegalStateException("LoopTicker.killAndWait() called from ticker thread");
            } else {
                synchronized(this) {
                    if (msgHandler == null) {
                        return;
                    }

                    msgHandler.removeMessages(MSG_TICK);
                    msgHandler.sendEmptyMessage(MSG_ABORT);
                }

                if (isAlive()) {
                    boolean retry = true;
                    while(retry) {
                        try {
                            join();
                            retry = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void run() {
            Looper.prepare();
            msgHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case MSG_TICK:
                            SurfaceRunner.this.tick();
                            if (!LoopTicker.this.msgHandler.hasMessages(MSG_TICK)) {
                                LoopTicker.this.msgHandler.sendEmptyMessage(MSG_TICK);
                            }
                        case MSG_ABORT:
                            Looper.myLooper().quit();
                            break;
                        default:
                            break;
                    }
                }
            };
            msgHandler.sendEmptyMessage(MSG_TICK);
            Looper.loop();
        }
    }

    private class ThreadTicker extends Thread implements SurfaceRunner.Ticker {
        private boolean enable;

        private ThreadTicker() {
            super("Surface Runner");
            enable = false;
            enable = true;
            start();
        }

        public void kill() {
            enable = false;
        }

        public void killAndWait() {
            if (Thread.currentThread() == this) {
                throw new IllegalStateException("ThreadTicker.killAndWait() called from ticker thread");
            } else {
                enable = false;
                if (isAlive()) {
                    boolean retry = true;

                    while(retry) {
                        try {
                            join();
                            retry = false;
                        } catch (InterruptedException var3) {
                        }
                    }
                }
            }
        }

        public void run() {
            while(enable) {
                SurfaceRunner.this.tick();
            }
        }
    }

    private interface Ticker {
        void kill();
        void killAndWait();
        boolean isAlive();
    }
}
