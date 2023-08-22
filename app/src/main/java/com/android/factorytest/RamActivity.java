package com.android.factorytest;

import java.io.File;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.factorytest.manager.SystemUtils;

public class RamActivity extends TestItemBaseActivity {

	private TextView txtMEMFree;
	private TextView txtMEMSize;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final float AUTO_TEST_RANGE = 0f;// 自动测试界限值
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.ram_layout);
		super.onCreate(savedInstanceState);

		txtMEMFree = (TextView) findViewById(R.id.txt_mem_free);
		txtMEMSize = (TextView) findViewById(R.id.txt_mem_total);
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);

	}

	@Override
	protected void onResume() {
		super.onResume();

		checkMemState();
	}

	private void checkMemState() {
        long memTotalSize = SystemUtils.getTotalMemory(getApplicationContext());
        long memFreeSize = SystemUtils.getAvailableMemory(getApplicationContext());
        txtMEMSize.setText(String.format("%.2f G",memTotalSize/1024f/1024f));
        txtMEMFree.setText(String.format("%.2f G",memFreeSize/1024f/1024f));
        txtMEMSize.setTextColor(Color.GREEN);
        txtMEMFree.setTextColor(Color.GREEN);

		if (memTotalSize > AUTO_TEST_RANGE) {
			synchronized (this) {
				stopAutoTest(true);
			}
		} else {
			synchronized (this) {
				stopAutoTest(false);
			}
		}
	}
}
