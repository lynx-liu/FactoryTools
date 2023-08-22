package com.android.factorytest;

import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.view.GPSView;
import com.android.factorytest.view.GPSView.AutoTestCallBack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class GPSTestActivity extends TestItemBaseActivity implements
		AutoTestCallBack {

	private GPSView gpsView;

	private final int AUTO_TEST_TIMEOUT = 30;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
				finish();
			}
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.gps_test_layout);
		gpsView = (GPSView) findViewById(R.id.gps_view);
		gpsView.setAutoTestCallBack(this);
		super.onCreate(savedInstanceState);

		findViewById(R.id.coldstart).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gpsView.coldStart();
			}
		});
	}

	@Override
	protected void onResume() {
		gpsView.invalidate();
		super.onResume();
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	public void autoTestResult(boolean result) {
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
            synchronized (this) {
                stopAutoTest(result);
            }
		}
	}
}
