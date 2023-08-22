package com.android.factorytest.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class LineLocationView extends SurfaceView implements Callback, Runnable{
	private static class LineState {
		Path path = new Path();
		int color = Color.RED;
	}

	private static int nColorIndex = 0;
	private static int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };

	private final int POINT_SIZE = 48;
	private int pointSizeX = POINT_SIZE;
    private int pointSizeY = POINT_SIZE;
	private int nWidth = 0;
	private int nHeight = 0;
	private boolean[][] points = null;
	private boolean bFinish = false;
	private AutoTestCallBack autoTestCallBack;

	private Bitmap fingerBitmap = null;
	private Canvas fingerCanvas = null;
	private final Paint mBoardPaint = new Paint();
	private final Paint mPathPaint = new Paint();
	private final ArrayList<LineState> mPathList = new ArrayList<LineState>();

	private SurfaceHolder holder = null;
	private Thread thread = null;

	public LineLocationView(Context context) {
		super(context,null);
	}

    public LineLocationView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        init(context);
    }

	void init(Context context) {
		holder = getHolder();
		holder.addCallback(this);
		setFocusable(true);

		mBoardPaint.setAntiAlias(true);
		mBoardPaint.setColor(Color.GRAY);
		mBoardPaint.setStyle(Paint.Style.FILL);
		mBoardPaint.setStrokeWidth(2);

		mPathPaint.setAntiAlias(false);
		mPathPaint.setStyle(Paint.Style.STROKE);
		mPathPaint.setStrokeWidth(3);
	}

	public void clear() {
		bFinish = false;
		mPathList.clear();

		for(int i=0;i<points.length;i++) {
			for(int j=0;j<points[i].length;j++) {
				points[i][j] = false;
			}
		}
	}

	protected void draw() {
		synchronized (mPathList) {
			Canvas canvas = holder.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.WHITE);//设置背景颜色
				drawGrid(canvas);
				drawBoard(canvas);
				drawLine(canvas);
				holder.unlockCanvasAndPost(canvas);
			}
		}
	}

	private void drawGrid(Canvas canvas){
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);

		//画表格中的虚线
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(false);
		for(int x = 0 ; x<=nWidth ; x+=pointSizeX){
			canvas.drawLine(x, 0, x, nHeight, paint);
		}
		for(int y = 0 ; y<=nHeight ; y+=pointSizeY){
			canvas.drawLine(0, y, nWidth, y, paint);
		}
	}

	private void drawBoard(Canvas canvas) {
		if(points==null) return;

		boolean bSuccess = true;
		for (int x = 0; x < points.length; x++) {
			for (int y = 0; y < points[x].length; y++) {
				if (!points[x][y]) {
					bSuccess = false;
				} else {
					int left = pointSizeX * x;
					int top = pointSizeY * y;
					int right = left + pointSizeX;
					int bottom = top + pointSizeY;
					canvas.drawRect(left, top, right, bottom, mBoardPaint);
				}
			}
		}

		if(bSuccess)
			bFinish = true;
	}

	private void drawLine(Canvas canvas) {
		for (int i = 0; i < mPathList.size(); i++) {
			LineState lineState = mPathList.get(i);
			mPathPaint.setColor(lineState.color);
			fingerCanvas.drawPath(lineState.path, mPathPaint);
		}
		canvas.drawBitmap(fingerBitmap, 0, 0, null);
	}

	public void addTouchEvent(MotionEvent event) {
		synchronized (mPathPaint) {
			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {
				mPathList.clear();

				LineState lineState = new LineState();
				lineState.path.moveTo(event.getX(), event.getY());
				lineState.color = colors[nColorIndex++ % colors.length];
				mPathList.add(lineState);
			} else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
				final int NI = event.getPointerCount();
				for (int index = 0; index < NI; index++) {
					final int id = event.getPointerId(index);
					if (mPathList.size() > id) {
						LineState lineState = mPathList.get(id);
						lineState.path.moveTo(event.getX(index), event.getY(index));
					} else {
						LineState lineState = new LineState();
						lineState.color = colors[nColorIndex++ % colors.length];
						lineState.path.moveTo(event.getX(index), event.getY(index));
						mPathList.add(lineState);
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE) {
				final int NI = event.getPointerCount();
				for (int index = 0; index < NI; index++) {
					final int id = event.getPointerId(index);
					if (mPathList.size() > id) {
						LineState lineState = mPathList.get(id);
						lineState.path.lineTo(event.getX(index),event.getY(index));
					}
				}
			} else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP) {
				final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int id = event.getPointerId(index);
				if (mPathList.size() > id) {
					LineState lineState = mPathList.get(id);
					lineState.path.reset();
				}
			} else 	if (action == MotionEvent.ACTION_UP) {
				final int NI = event.getPointerCount();
				for (int i = 0; i < NI; i++) {
					final int id = event.getPointerId(i);
					if (mPathList.size() > id) {
						LineState lineState = mPathList.get(id);
						lineState.path.reset();
					}
				}
				if(bFinish && autoTestCallBack!=null){
					autoTestCallBack.autoTestResult(true);
				}
			}

			addPoint(event);
			postInvalidate();
		}
	}

	private void addPoint(MotionEvent event) {
		for (int i = 0; i < event.getPointerCount(); i++) {
			int x = (int) (event.getX(i) / pointSizeX);
			int y = (int) (event.getY(i) / pointSizeY);

			// 虚拟按键坐标会越界
			if (x>=0 && x < points.length && y>=0 && y < points[x].length)
				points[x][y] = true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		addTouchEvent(event);
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		Log.i("Pointer", "Trackball: " + event);
		return super.onTrackballEvent(event);
	}

	public void setAutoTestCallBack(AutoTestCallBack callBack){
		this.autoTestCallBack=callBack;
	}

	public interface AutoTestCallBack{
		public void autoTestResult(boolean result);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		nWidth = getWidth(); nHeight = getHeight();
        Log.d("llx",nWidth+", "+nHeight);
        pointSizeX = (int) (nWidth / 16 + 0.5f);
        pointSizeY = (int) (nHeight / 8 + 0.5f);

		points = new boolean[(int) Math.ceil(nWidth*1.0f/pointSizeX)][(int)Math.ceil(nHeight*1.0f/pointSizeY)];

		if(fingerBitmap!=null) fingerBitmap.recycle();
		fingerBitmap = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
		fingerCanvas = new Canvas(fingerBitmap);

		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("llx", "width="+width+", height="+height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(fingerBitmap!=null) {
			fingerBitmap.recycle();
			fingerBitmap = null;
		}
	}

	@Override
	public void run() {
		while(fingerBitmap!=null) {
			draw();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
