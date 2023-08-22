package com.android.factorytest;

import com.android.factorytest.view.JudgeView;
import com.android.factorytest.view.LineLocationView;
import com.android.factorytest.view.LineLocationView.AutoTestCallBack;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class DrawScreenActivity extends TestItemBaseActivity implements AutoTestCallBack{

	private LineLocationView lv = null;
	private LinearLayout ll_touch = null;
	private JudgeView jv = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.drawscreen_layout);
        super.onCreate(savedInstanceState);
        
        ll_touch = (LinearLayout)findViewById(R.id.ll_touch);
        jv = (JudgeView)findViewById(R.id.judgeview);
        lv = (LineLocationView) findViewById(R.id.lv_touch);
        lv.setAutoTestCallBack(this);
    }
    
    @Override
    protected void onResume() {
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions); 
    	super.onResume();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction()==MotionEvent.ACTION_DOWN) {
            ll_touch.setVisibility(View.GONE);
           	jv.setVisibility(View.GONE);
           	lv.setVisibility(View.VISIBLE);
    	}
    	return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		onBackPressed();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
	@Override
	public void onBackPressed() {
		if(ll_touch.getVisibility()!=View.VISIBLE) {
			ll_touch.setVisibility(View.VISIBLE);
	       	jv.setVisibility(View.VISIBLE);
	       	lv.clear();
	       	lv.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void autoTestResult(boolean result) {
		ll_touch.setVisibility(View.VISIBLE);
       	jv.setVisibility(View.VISIBLE);
       	lv.clear();
       	lv.setVisibility(View.GONE);
	}
}