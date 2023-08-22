package com.android.factorytest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.factorytest.bean.WifiStrength;
import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.manager.WifiStatusManager;
import com.android.factorytest.view.WifiStrengthView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

public class WifiTestActivity extends TestItemBaseActivity implements Callback {

	private List<WifiStrength> scanResultsList;

	private WifiStatusManager manager;
	private Handler mHandler;

	private WifiStrengthView strengthView;
	private Thread wifiStateMonitor;
	private final int AUTO_TEST_TIMEOUT = 10;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间
	private boolean thread_flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.wifi_layout);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
				finish();
			}
		}

		strengthView = (WifiStrengthView) findViewById(R.id.strengthView);
		manager = new WifiStatusManager(this);

		mHandler = new Handler(this);
		mHandler.sendEmptyMessage(0);
		wifiStateMonitor = new WifiStateMonitor();
		wifiStateMonitor.start();
		super.onCreate(savedInstanceState);
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	protected void onDestroy() {
		try {
			if (!wifiStateMonitor.interrupted()) {
				thread_flag = false;
				wifiStateMonitor.interrupt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	protected void onResume() {
        if(!manager.isWifiEnabled()) {
            manager.openWifi();//打开wifi，停止测试4G关闭WIFI后，返回测试WIFI不通过
        }
		manager.startScan();
		super.onResume();
	}

	class WifiStateMonitor extends Thread {

		public void run() {
			while (thread_flag) {
				try {
					mHandler.sendEmptyMessage(0);
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		scanResultsList = manager.getWifiInfoStrength();
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			checkAutoTestResult(scanResultsList);
		}
		strengthView.updateData(scanResultsList);
		manager.startScan();
		return false;
	}

	/**
	 * 检测自动测试结果
	 */
	private Map<String, Integer> preStrengthMap = new HashMap<String, Integer>();

	private void checkAutoTestResult(List<WifiStrength> list) {

		boolean flag1 = false;// 会有一个信号大于等于-70的
		boolean flag2 = false;// 两次信号差值大于20
		WifiStrength wifiStrength;

		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				wifiStrength = list.get(i);
				if (wifiStrength.strength >= -70) {
					flag1 = true;
				}
				if (preStrengthMap.size() > 0) {
					Integer preStrength = preStrengthMap.get(wifiStrength.label);
				}
				preStrengthMap.put(wifiStrength.label, wifiStrength.strength);
			}
		}
		if (flag1 ) {
			if (!wifiStateMonitor.interrupted()) {
			}
			stopAutoTest(true);
		}
	}
}
