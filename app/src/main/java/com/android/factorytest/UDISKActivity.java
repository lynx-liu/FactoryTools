package com.android.factorytest;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.factorytest.manager.SystemUtils;
import com.android.factorytest.utils.CommonUtils;

public class UDISKActivity extends TestItemBaseActivity {

	private TextView txtDDRFree;
	private TextView txtDDRSize;
	private TextView txtUdiskFree;
	private TextView txtUdiskSize;
	private CheckBox cbHost;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final float AUTO_TEST_RANGE = 0f;// 自动测试界限值
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	private static final int gpioUSB = 100014;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.udisk_layout);
		super.onCreate(savedInstanceState);

		txtDDRFree = (TextView) findViewById(R.id.txt_ddr_free);
		txtDDRSize = (TextView) findViewById(R.id.txt_ddr_total);
		txtUdiskFree = (TextView) findViewById(R.id.txt_udisk_free);
		txtUdiskSize = (TextView) findViewById(R.id.txt_udisk_total);
		cbHost = (CheckBox)findViewById(R.id.cb_usbhost);
		cbHost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CommonUtils.setEnabled(gpioUSB,cbHost.isChecked());
			}
		});

		registerUDiskReceiver();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	private void registerUDiskReceiver() {
		//监听USB插入 拔出
		IntentFilter usbDeviceStateFilter = new IntentFilter();
		usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        usbDeviceStateFilter.addDataScheme("file");
		registerReceiver(mUsbReceiver, usbDeviceStateFilter);
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case Intent.ACTION_MEDIA_MOUNTED://接收到U盘设备插入广播
                    String path =  intent.getData().getPath();
                    Log.d("llx",path);
                    checkStorageState();
					break;
				case Intent.ACTION_MEDIA_UNMOUNTED://接收到U盘设设备卸载广播
					Toast.makeText(getApplicationContext(),R.string.udisk_umounted,Toast.LENGTH_SHORT).show();
                    checkStorageState();
					break;
			}
		}
	};

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkStorageState();
	}

	private void checkStorageState() {
		long ddrTotalSize = 0;
		long ddrFreeSize = 0;
        String ddr =  Environment.getExternalStorageDirectory().getAbsolutePath();
        if (ddr!=null && new File(ddr).exists()) {
			ddrTotalSize = SystemUtils.getMemorySize(ddr);
			ddrFreeSize = SystemUtils.getFreeMemorySize(ddr);
			txtDDRSize.setText(Formatter.formatFileSize(getApplicationContext(), ddrTotalSize));
			txtDDRFree.setText(Formatter.formatFileSize(getApplicationContext(), ddrFreeSize));
			txtDDRSize.setTextColor(Color.GREEN);
			txtDDRFree.setTextColor(Color.GREEN);
		} else {
			txtDDRSize.setText(R.string.none);
			txtDDRFree.setText(R.string.none);
            txtDDRSize.setTextColor(Color.RED);
            txtDDRFree.setTextColor(Color.RED);
		}

        long usbTotalSize = 0;
        long usbFreeSize = 0;
        String usb = SystemUtils.getStoragePath(getApplicationContext(),"U");
		if (usb!=null && new File(usb).exists()) {
			usbTotalSize = SystemUtils.getMemorySize(usb);
			usbFreeSize = SystemUtils.getFreeMemorySize(usb);
			txtUdiskSize.setText(Formatter.formatFileSize(getApplicationContext(), usbTotalSize));
			txtUdiskFree.setText(Formatter.formatFileSize(getApplicationContext(), usbFreeSize));
			txtUdiskSize.setTextColor(Color.GREEN);
			txtUdiskFree.setTextColor(Color.GREEN);
		} else {
			txtUdiskSize.setText(R.string.none);
			txtUdiskFree.setText(R.string.none);
			txtUdiskSize.setTextColor(Color.RED);
			txtUdiskFree.setTextColor(Color.RED);
		}

		if ((ddrTotalSize > AUTO_TEST_RANGE)
				&& (usbTotalSize > AUTO_TEST_RANGE)) {
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
