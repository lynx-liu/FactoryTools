package com.android.factorytest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.android.factorytest.utils.CommonUtils;

public class LEDActivity extends TestItemBaseActivity {
	private final int MSG_LED_CHANGED = 1;
	private static boolean ledOn = true;
	private static int id[] = {100000,100002,100004,100016,100006,100008};//GPS, REC, 4G, USER, OUT1, OUT2
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.led_layout);
		super.onCreate(savedInstanceState);
		mHandler.sendEmptyMessage(MSG_LED_CHANGED);
	}
	
	@Override
	protected void onDestroy() {
		mHandler.removeMessages(MSG_LED_CHANGED);
		for(int i=0;i<id.length;i++) {
			CommonUtils.setEnabled(id[i],false);
		}
		super.onDestroy();
	}
	
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_LED_CHANGED:
	            {
	            	ledOn = !ledOn;
	            	for(int i=0;i<id.length;i++) {
						CommonUtils.setEnabled(id[i],ledOn);
	            		try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	}
	            	mHandler.sendEmptyMessageDelayed(MSG_LED_CHANGED, 300);
	            }
	            break;
            }
        }
    };
}
