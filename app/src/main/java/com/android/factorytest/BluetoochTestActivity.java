package com.android.factorytest;

import com.android.factorytest.manager.BluetoothManager;
import com.android.factorytest.manager.BluetoothManager.BluetoochState;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class BluetoochTestActivity extends TestItemBaseActivity implements
		Callback {

	private BluetoothManager bluetoothManager;
	private Handler mHandler;
	private TextView bt_address;
	private TextView bt_status;
	private EditText bt_scan;
    private long timestamp = 0;
	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	private final int MSG_BLUETOOTH_LE_SCAN = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setContentView(R.layout.bluetooch_test_layout);
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
				finish();
			}
		}

		mHandler = new Handler(this);

		bluetoothManager = new BluetoothManager(this, mHandler);
		bluetoothManager.openBluetooth();

		bt_address = (TextView) findViewById(R.id.txt_bt_address);
		bt_status = (TextView) findViewById(R.id.txt_bt_status);
		bt_scan = (EditText) findViewById(R.id.et_bt_scan);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!bluetoothManager.isOpenBluetooch()) {
			bt_status.setText(R.string.closed);
			synchronized (this) {
				stopAutoTest(false);
			}
		} else {
			bt_status.setText(R.string.opened);
			bt_address.setText(bluetoothManager.getMacAdress());
			if (bluetoothManager.getMacAdress() != null) {
				bluetoothManager.startLeScan(mLeScanCallback);
			}
		}
	}

	@Override
	public void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	protected void onPause() {
		mHandler.removeMessages(MSG_BLUETOOTH_LE_SCAN);
		bluetoothManager.stopLeScan(mLeScanCallback);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		bluetoothManager.unregisterBluethoothReceiver();
		super.onDestroy();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case BluetoochState.BLUETOOCH_STATE_ON:
				Log.i("vita", "------ 已开启蓝牙");
				bt_status.setText(R.string.opened);
				bt_address.setText(bluetoothManager.getMacAdress());
				break;
			case BluetoochState.BLUETOOCH_STATE_TURNING_ON:
				Log.i("vita", "------ 正在打开");
			case BluetoochState.BLUETOOCH_STATE_OFF:
				Log.i("vita", "-------关闭");
				bt_status.setText(R.string.closed);
				bt_address.setText("");
				break;
			case BluetoochState.BLUETOOCH_STATE_TURNING_OFF:
				Log.i("vita", "--------正在关闭");
				break;
			case BluetoochState.BLUETOOCH_STATE_FAIL:
				Log.i("vita", "------失败");
				bt_status.setText(R.string.fail);
				bt_address.setText("");
			case MSG_BLUETOOTH_LE_SCAN:
				if(bt_scan.getLineCount()>5000){
					bt_scan.setText((String)msg.obj);
					bt_scan.setSelection(bt_scan.getText().length());
				} else bt_scan.append((String)msg.obj);
				break;
			default:
				break;
		}

		return false;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(bt_scan.getLineCount()<=bt_scan.getMaxLines() || System.currentTimeMillis()-timestamp>500) {
                Message msg = new Message();
                msg.what = MSG_BLUETOOTH_LE_SCAN;
                msg.obj = device.getAddress() + "\t\t:\t\t" + rssi + "\r\n";
                mHandler.sendMessage(msg);

                timestamp = System.currentTimeMillis();
            }

			if (rssi > -90) {
				synchronized (this) {
					stopAutoTest(true);
				}
			}
		}
	};
}
