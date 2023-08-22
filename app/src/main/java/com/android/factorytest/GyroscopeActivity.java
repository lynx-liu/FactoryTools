package com.android.factorytest;

import com.android.factorytest.manager.FactoryTestManager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *
 * 陀螺仪测试
 */
public class GyroscopeActivity extends TestItemBaseActivity implements
		SensorEventListener {

	private Sensor gyroscopeSensor;

	private SensorManager mSensorManager;

	private TextView txtX;
	private TextView txtY;
	private TextView txtZ;

	private ProgressBar pbarX;
	private ProgressBar pbarY;
	private ProgressBar pbarZ;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final float AUTO_TEST_RANGE = 0.00001f;// 自动测试界限值
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间
	private float pre_x;
	private float pre_y;
	private float pre_z;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.gyroscope_layout);
		super.onCreate(savedInstanceState);

		txtX = (TextView) findViewById(R.id.txtX);
		txtY = (TextView) findViewById(R.id.txtY);
		txtZ = (TextView) findViewById(R.id.txtZ);

		pbarX = (ProgressBar) findViewById(R.id.pbarX);
		pbarY = (ProgressBar) findViewById(R.id.pbarY);
		pbarZ = (ProgressBar) findViewById(R.id.pbarZ);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		gyroscopeSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mSensorManager.registerListener(this, gyroscopeSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);

	}

	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this, gyroscopeSensor);
		super.onDestroy();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		txtX.setText(String.format("X: %.05f", x));
		txtY.setText(String.format("Y: %.05f", y));
		txtZ.setText(String.format("Z: %.05f", z));

		double d = Math.sqrt(x * x + y * y + z * z);

		pbarX.setProgress(Math.abs((int) (100.0D * (x / d))));
		pbarY.setProgress(Math.abs((int) (100.0D * (y / d))));
		pbarZ.setProgress(Math.abs((int) (100.0D * (z / d))));

		// 自动测试测试结果计算
		boolean xResult = Math.abs(x - pre_x) > AUTO_TEST_RANGE;
		boolean yResult = Math.abs(y - pre_y) > AUTO_TEST_RANGE;
		boolean zResult = Math.abs(z - pre_z) > AUTO_TEST_RANGE;
		if (FactoryTestManager.currentTestMode==FactoryTestManager.TestMode.MODE_AUTO_TEST&&xResult && yResult && zResult) {
			synchronized (this) {
				mSensorManager.unregisterListener(this, gyroscopeSensor);
				stopAutoTest(true);
			}
		}
		pre_x=x;
		pre_y=y;
		pre_z=z;
	}

}
