package com.android.factorytest;

import java.io.File;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.factorytest.manager.SystemUtils;

public class SdcardActivity extends TestItemBaseActivity {

	private TextView txtDDRFree;
	private TextView txtDDRSize;
	private TextView txtSDFree;
	private TextView txtSDSize;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final float AUTO_TEST_RANGE = 0f;// 自动测试界限值
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.sdcard_layout);
		super.onCreate(savedInstanceState);

		txtDDRFree = (TextView) findViewById(R.id.txt_ddr_free);
		txtDDRSize = (TextView) findViewById(R.id.txt_ddr_total);
		txtSDFree = (TextView) findViewById(R.id.txt_sdcard_free);
		txtSDSize = (TextView) findViewById(R.id.txt_sdcard_total);
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);

	}

	@Override
	protected void onResume() {
		super.onResume();

		checkSDState();
	}

	private void checkSDState() {
		long ddrTotalSize = 0;
		long ddrFreeSize = 0;
        String ddr = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (ddr!=null && new File(ddr).exists()) {
			ddrTotalSize = SystemUtils.getMemorySize(ddr);
			ddrFreeSize = SystemUtils.getFreeMemorySize(ddr);
			txtDDRSize.setText(Formatter.formatFileSize(getApplicationContext(),ddrTotalSize));
			txtDDRFree.setText(Formatter.formatFileSize(getApplicationContext(),ddrFreeSize));
            txtDDRSize.setTextColor(Color.GREEN);
            txtDDRFree.setTextColor(Color.GREEN);
		} else {
			txtDDRSize.setText(R.string.none);
			txtDDRFree.setText(R.string.none);
            txtDDRSize.setTextColor(Color.RED);
            txtDDRFree.setTextColor(Color.RED);
		}

        long sdTotalSize = 0;
        long sdFreeSize = 0;
        String sdcard = SystemUtils.getStoragePath(getApplicationContext(),"SD");
		if (sdcard!=null && new File(sdcard).exists()) {
			sdTotalSize = SystemUtils.getMemorySize(sdcard);
			sdFreeSize = SystemUtils.getFreeMemorySize(sdcard);
			txtSDSize.setText(Formatter.formatFileSize(getApplicationContext(),sdTotalSize));
			txtSDFree.setText(Formatter.formatFileSize(getApplicationContext(),sdFreeSize));
            txtSDSize.setTextColor(Color.GREEN);
            txtSDFree.setTextColor(Color.GREEN);
		} else {
			txtSDSize.setText(R.string.none);
			txtSDFree.setText(R.string.none);
            txtSDSize.setTextColor(Color.RED);
            txtSDFree.setTextColor(Color.RED);
		}

		if ((ddrTotalSize > AUTO_TEST_RANGE)
				&& (sdTotalSize > AUTO_TEST_RANGE)) {
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
