package com.android.factorytest.manager;

import java.io.IOException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class BluetoothManager {
	private static final String TAG = "BluetoothManager";
	private Context mContext;
	private static BluetoothSocket btSocket;
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private ArrayAdapter<String> adtDevices;
	private BluetoothAdapter btAdapt;
	private Handler mHandler;

	public BluetoothManager(Context mContext) {
		this.mContext = mContext;
		init();
	}

	public BluetoothManager(Context mContext, Handler mHandler) {
		this.mContext = mContext;
		this.mHandler = mHandler;
		init();
	}

	private void init() {
		btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能
		IntentFilter intent = new IntentFilter();
		//intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mContext.registerReceiver(searchDevices, intent);
	}

	/**
	 *
	 * 方法描述：重命名设备名
	 *
	 * @param name
	 * @return
	 */
	public boolean renameDevice(String name) {
		return btAdapt.setName(name);
	}

	public String getDeviceName() {
		return btAdapt.getName();
	}

	/**
	 * 获取MAC地址
	 * @return
	 */
	public String getMacAdress(){
		return btAdapt.getAddress();
	}

	/**
	 * 注册广播
	 */
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();

			// 显示所有收到的消息及其细节
			for (int i = 0; i < lstName.length; i++) {
				String keyName = lstName[i].toString();
				Log.e(keyName, String.valueOf(b.get(keyName)));
			}
			BluetoothDevice device = null;
			// 搜索设备时，取得设备的MAC地址
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (mHandler != null) {
					mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_ACTION_FOUND);
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.STATE_OFF);
				if (mHandler != null) {

					if (state == BluetoothAdapter.STATE_OFF) {
						Log.i(TAG, "----------------BLUETOOCH_STATE_OFF--");
						mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_OFF);
					} else if (state == BluetoothAdapter.STATE_ON) {
						Log.i(TAG, "----------------BLUETOOCH_STATE_ON--");
						mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_ON);
					} else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
						mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_TURNING_OFF);
						Log.i(TAG, "----------------STATE_TURNING_OFF--");
					} else if (state == BluetoothAdapter.STATE_TURNING_ON) {
						mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_TURNING_ON);
						Log.i(TAG, "----------------STATE_TURNING_ON--");
					} else if (state == BluetoothAdapter.ERROR) {
						mHandler.sendEmptyMessage(BluetoochState.BLUETOOCH_STATE_FAIL);
						Log.i(TAG, "----------------ERROR--");

					}
				}
			}
		}
	};

	public void connectRemote(String address) {
		if (btAdapt.isDiscovering())
			btAdapt.cancelDiscovery();
		/*
		 * String str = lstDevices.get(location); String[] values =
		 * str.split("\\|"); String address = values[2]; Log.e(TAG, "address = "
		 * + values[2]);
		 */
		BluetoothDevice btDev = btAdapt.getRemoteDevice(address);
		try {
			Boolean returnValue = false;
			if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
				// 利用反射方法调用BluetoothDevice.createBond(BluetoothDevice
				// remoteDevice);
				Method createBondMethod = BluetoothDevice.class
						.getMethod("createBond");
				Log.d(TAG, "开始配对");
				returnValue = (Boolean) createBondMethod.invoke(btDev);

			} else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public boolean isOpenBluetooch() {
		boolean flag = false;
		if (btAdapt != null) {
			int state = btAdapt.getState();
			if (state == BluetoothAdapter.STATE_OFF)
				flag = false;
			else if (state == BluetoothAdapter.STATE_ON)
				flag = true;
		}
		return flag;
	}

	/**
	 *
	 * 方法描述：打开蓝牙
	 */
	public void openBluetooth() {
		if (btAdapt != null) {

			btAdapt.enable();
		}
	}

	/**
	 *
	 * 方法描述：关闭蓝牙
	 */
	public void closeBluethooth() {
		if (btAdapt != null) {
			btAdapt.disable();
		}
	}

	/**
	 *
	 * 方法描述：搜索蓝牙设备
	 */
	public void findDevice() {
		if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启
			// HSJToast.makeText(BlueToothTestActivity.this, "请先打开蓝牙",
			// 1000).show();
			Log.i(TAG, "先打开蓝牙");
			return;
		}
		if (btAdapt.isDiscovering())
			btAdapt.cancelDiscovery();
		// list.clear();
		Object[] lstDevice = btAdapt.getBondedDevices().toArray();
		for (int i = 0; i < lstDevice.length; i++) {
			BluetoothDevice device = (BluetoothDevice) lstDevice[i];
			String str = "已配对|" + device.getName() + "|" + device.getAddress();
			// lstDevices.add(str); // 获取设备名称和mac地址
			// adtDevices.notifyDataSetChanged();
			// Log.i(TAG, str);

		}
		Log.i(TAG, "本机蓝牙地址：" + btAdapt.getAddress());
		btAdapt.startDiscovery();
	}

	/**
	 *
	 * 方法描述：除了手机，电脑，其他设备一律过滤掉
	 *
	 * @return
	 */
	private boolean isAccordDevicePair(BluetoothDevice device) {
		boolean flag = false;
		BluetoothClass btClass = device.getBluetoothClass();
		if (btClass != null) {
			int major = btClass.getMajorDeviceClass();
			if (major == BluetoothClass.Device.Major.COMPUTER
					|| major == BluetoothClass.Device.Major.PHONE) {
				flag = true;
			}
		}
		return flag;
	}

	public void unregisterBluethoothReceiver() {
		try {
			if (btSocket != null)
				btSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mContext.unregisterReceiver(searchDevices);

	}



	public static class BluetoochState {
		public static final int BLUETOOCH_STATE_ACTION_FOUND = 21;// 蓝牙寻找

		public static final int BLUETOOCH_STATE_OFF = 22;// 关闭

		public static final int BLUETOOCH_STATE_ON = 23;// 开启

		public static final int BLUETOOCH_STATE_TURNING_ON = 24;// 正在开启

		public static final int BLUETOOCH_STATE_TURNING_OFF = 25;// 正在关闭

		public static final int BLUETOOCH_STATE_FAIL = 26;// 开启失败

		public static final int BLUETOOCH_BOND_STATE_CHANGED = 27;// 蓝牙绑定状态
	}

	public boolean startLeScan(LeScanCallback callback) {
		return btAdapt.startLeScan(callback);
	}

	public void stopLeScan(LeScanCallback callback) {
		if(btAdapt!=null) {
			btAdapt.stopLeScan(callback);
		}
	}
}
