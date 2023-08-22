package com.android.factorytest;

import com.android.factorytest.view.JudgeView;
import com.android.factorytest.view.PointLocationView;
import com.android.factorytest.view.PointLocationView.AutoTestCallBack;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class TouchScreenActivity extends TestItemBaseActivity implements AutoTestCallBack{

	private PointLocationView pv = null;
	private LinearLayout ll_touch = null;
	private JudgeView jv = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.touchscreen_layout);
        super.onCreate(savedInstanceState);
        
        ll_touch = (LinearLayout)findViewById(R.id.ll_touch);
        jv = (JudgeView)findViewById(R.id.judgeview);
        pv = (PointLocationView)findViewById(R.id.pv_touch);
        pv.setAutoTestCallBack(this);
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
           	pv.setVisibility(View.VISIBLE);
    	}
    	return super.onTouchEvent(event);
    }

	@Override
	public void autoTestResult(boolean result) {
		ll_touch.setVisibility(View.VISIBLE);
       	jv.setVisibility(View.VISIBLE);
       	pv.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if(ll_touch.getVisibility()!=View.VISIBLE) {
			ll_touch.setVisibility(View.VISIBLE);
	       	jv.setVisibility(View.VISIBLE);
	       	pv.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}
}