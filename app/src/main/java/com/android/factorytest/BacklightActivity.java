package com.android.factorytest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

public class BacklightActivity extends TestItemBaseActivity {
	
	public static final int BRIGHTNESS_DIM = 20;
	public static final int BRIGHTNESS_ON = 255;
	private static final int MINIMUM_BACKLIGHT = BRIGHTNESS_DIM + 10;
	private static final int MAXIMUM_BACKLIGHT = BRIGHTNESS_ON;
	
	private int nBacklight = MAXIMUM_BACKLIGHT;
	private ProgressBar progress = null;
	private final int MSG_BACKLIGHT_CHANGED = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.backlight_layout);
		super.onCreate(savedInstanceState);
		
		progress = (ProgressBar)findViewById(R.id.backlight);
		progress.setProgress(getBacklight());
	}

	@Override
	protected void onResume() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS}, 1);
		} else {
			nBacklight = getBacklight();
			mHandler.sendEmptyMessageDelayed(MSG_BACKLIGHT_CHANGED, 500);
		}
		super.onResume();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	@Override
	protected void onPause() {
		mHandler.removeMessages(MSG_BACKLIGHT_CHANGED);
		setBacklight(nBacklight);
		super.onPause();
	}

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_BACKLIGHT_CHANGED:
	            {
	            	int backlight = getBacklight();
	            	if(backlight<MAXIMUM_BACKLIGHT/2-1) {
	            		progress.setProgress(setBacklight(MAXIMUM_BACKLIGHT/2));
	            	} else if(backlight==MAXIMUM_BACKLIGHT/2) {
	            		progress.setProgress(setBacklight(MAXIMUM_BACKLIGHT));
	            	} else {
	            		progress.setProgress(setBacklight(MINIMUM_BACKLIGHT));
	            	}
	            	mHandler.sendEmptyMessageDelayed(MSG_BACKLIGHT_CHANGED, 500);
	            }
	            break;
            }
        }
    };
    
	int getBacklight() {
		int backlight = MAXIMUM_BACKLIGHT;
		try {
			backlight = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return backlight;
	}
	
	int setBacklight(int backlight) {
		try {
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, backlight);
			WindowManager.LayoutParams lpParam = getWindow().getAttributes();
			lpParam.screenBrightness = Float.valueOf(backlight) * (1f / 255f);
			getWindow().setAttributes(lpParam);
			return backlight;
		}catch (Exception e) {
			Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
		}
		return -1;
	}
}
