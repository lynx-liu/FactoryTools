package com.android.factorytest;

import com.android.factorytest.manager.BluetoothManager;
import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.manager.SecretManager;
import com.android.factorytest.manager.WifiStatusManager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private Button btn_singalTest;
	private Button btn_auto_Test;
	private Button btn_result_Test;
	private Button btn_burn_Test;
	private Button btn_recover;
	private Button btn_exit;
	private boolean hasCloseWifi = false;

	private WifiStatusManager wifiManager;
	private BluetoothManager bluetoothManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		String activityName = getIntent().getComponent().getClassName();
		if(activityName.equals("com.android.factorytest.FactoryLauncher")) {
			FactoryTestManager.isPCBA = true;
			Log.d("llx", "isPCBA="+FactoryTestManager.isPCBA);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
//					checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//					checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//					checkSelfPermission(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
			) {
				requestPermissions(new String[]{
						Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.CAMERA,
						Manifest.permission.READ_PHONE_STATE,
//						Manifest.permission.ACCESS_FINE_LOCATION,
//						Manifest.permission.ACCESS_COARSE_LOCATION,
//						Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
						Manifest.permission.RECORD_AUDIO}, 0);
			}
		}

		// 检查权限
		if (SecretManager.isSoftWareAuthority()) {// 有权限?

			setContentView(R.layout.main_layout);

			btn_singalTest = (Button) findViewById(R.id.btn_singalTest);
			btn_auto_Test = (Button) findViewById(R.id.btn_auto_Test);
			btn_result_Test = (Button) findViewById(R.id.btn_result_Test);
			btn_burn_Test = (Button) findViewById(R.id.btn_burn_Test);
			btn_recover = (Button) findViewById(R.id.btn_recover);
			btn_exit = (Button) findViewById(R.id.btn_exit);

			btn_singalTest.setOnClickListener(this);
			btn_result_Test.setOnClickListener(this);
			btn_auto_Test.setOnClickListener(this);
			btn_burn_Test.setOnClickListener(this);
			btn_recover.setOnClickListener(this);
			btn_exit.setOnClickListener(this);

			initDeviceState();
		} else {
			finish();
		}
	}

	private void initDeviceState() {
		// 初使化
		wifiManager = new WifiStatusManager(this);
		bluetoothManager = new BluetoothManager(this);
		bluetoothManager.openBluetooth();
		if(!wifiManager.isWifiEnabled()) {
			hasCloseWifi = true;
			wifiManager.openWifi();
		}
	}

	@Override
	protected void onDestroy() {
		if(hasCloseWifi) {
			wifiManager.closeWifi();
		}
		if(bluetoothManager!=null)
			bluetoothManager.unregisterBluethoothReceiver();
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.restartPackage(this.getApplication().getPackageName());
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btn_singalTest.getId()) {
			FactoryTestManager.currentTestMode = FactoryTestManager.TestMode.MODE_SINGAL_TEST;
			startActivity(new Intent(this, TestGridActivity.class));
		} else if (v.getId() == btn_auto_Test.getId()) {
			FactoryTestManager.currentTestMode = FactoryTestManager.TestMode.MODE_AUTO_TEST;
			startActivity(new Intent(this, TestGridActivity.class));
		} else if (v.getId() == btn_result_Test.getId()) {
			FactoryTestManager.currentTestMode = FactoryTestManager.TestMode.MODE_RESULT_TEST;
			startActivity(new Intent(this, TestGridActivity.class));
		} else if (v.getId() == btn_burn_Test.getId()) {
			startActivity(new Intent(this, ScreensaverActivity.class));
		} else if (v.getId() == btn_recover.getId()) {// 恢复出厂测试
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(getString(R.string.reset_factory));
			builder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,int which) {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
						sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
					} else {
						Intent intent = new Intent("android.intent.action.FACTORY_RESET");
						intent.setPackage("android");
						sendBroadcast(intent);
					}
				}
			});

			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		} else if (v.getId() == btn_exit.getId()) {// 退出
			this.finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(1, 1, 1, R.string.save_config_file);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			FactoryTestManager.getInstance(this).gernalConfigFile();
		}
		Toast.makeText(this, R.string.save_ok, Toast.LENGTH_SHORT).show();
		return super.onOptionsItemSelected(item);
	}
}
