package com.android.factorytest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LCDActivity extends TestItemBaseActivity {

	private LinearLayout ll_Rgb = null;
	private LinearLayout ll_Ladder = null;
	private TextView tv_Message = null;
	private int[] color = {Color.RED,Color.GREEN,Color.BLUE, Color.BLACK, Color.WHITE};
	private int index = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.lcd_layout);
		super.onCreate(savedInstanceState);
		
		ll_Rgb = (LinearLayout)findViewById(R.id.ll_lcd);
		ll_Ladder = (LinearLayout)findViewById(R.id.ll_ladder);
		tv_Message = (TextView)findViewById(R.id.tv_message);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			ll_Rgb.setBackgroundColor(color[index++%color.length]);
			if(ll_Ladder.isShown()) ll_Ladder.setVisibility(View.GONE);
			if(tv_Message.isShown()) tv_Message.setVisibility(View.GONE);
		}
		return super.onTouchEvent(event);
	}
}
