package com.android.factorytest.view;

import com.android.factorytest.R;
import com.android.factorytest.utils.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class SignalView extends View {

	private Bitmap bgGrid;
	private int maxDbm = -50;
	private int minDbm = -110;
	private int graphWidth = 100;
	private int padingLeft = 20;
	private int sigalBlockNumber = 100;
	private int[] singalBlock = new int[sigalBlockNumber];
	private int range_red = -95;
	private int range_yellow = -80;
	// private int range_blue=-95;
	private int blockIndex = 0;

	public SignalView(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		graphWidth = (int)this.getResources().getDimension(R.dimen.SignagraphWidth);
		bgGrid = BitmapFactory.decodeResource(getResources(), R.drawable.graph);
		bgGrid = ImageUtil.zoomBitmap(bgGrid, graphWidth, (bgGrid.getHeight()
				* graphWidth / bgGrid.getWidth()));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		drawBgGrid(canvas);
		drawSigal(canvas);
		super.onDraw(canvas);
	}

	private void drawSigal(Canvas canvas) {
		canvas.translate(30, 0);
		// 画线
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		float x = (bgGrid.getWidth() / sigalBlockNumber) * blockIndex;
		canvas.drawLine(x, 0, x, bgGrid.getHeight(), paint);

		// 画色块
		paint.setStyle(Style.FILL);
		for (int i = 0; i <= blockIndex; i++) {
			if (singalBlock[i] <= range_red) {
				paint.setColor(Color.RED);
			} else if (singalBlock[i] <= range_yellow) {
				paint.setColor(Color.YELLOW);
			} else {
				paint.setColor(Color.GREEN);
			}
			float startX = (i == 0 ? 2
					: ((bgGrid.getWidth() / sigalBlockNumber) * (i)))-10;
			float endX = (bgGrid.getWidth() / sigalBlockNumber) * (i + 1);
			float height = (bgGrid.getHeight() / (maxDbm - minDbm))
					* (singalBlock[i] - minDbm);
			canvas.drawRect(startX, bgGrid.getHeight() - height, endX,
					bgGrid.getHeight()-2, paint);

		}
		// 画信号值
		paint.setColor(Color.BLUE);
		float y = (bgGrid.getHeight() / (maxDbm - minDbm))
				* (singalBlock[blockIndex] - minDbm);
		canvas.drawText(singalBlock[blockIndex] + "dbm", x, y, paint);

		blockIndex++;
		if (blockIndex >= sigalBlockNumber) {
			blockIndex = 0;
		}
	}

	private void drawBgGrid(Canvas canvas) {
		//
		padingLeft = (int)this.getResources().getDimension(R.dimen.SignapadingLeft);
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		for (int i = 0; i < 5; i++) {
			int dbm = maxDbm - i * 15;
			float y = (bgGrid.getHeight()) * i / 4 + 10;
			canvas.drawText(dbm + "", 0, y, paint);
		}
		canvas.drawBitmap(bgGrid, padingLeft, 0, new Paint());

	}

	public void onSignalChanged(int strength) {

		if (blockIndex < sigalBlockNumber ) {
			if(strength==0){
				strength=minDbm;
			}
			singalBlock[blockIndex] = strength;
		}

		this.invalidate();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		this.setMeasuredDimension(this.getWidth(), bgGrid.getHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
