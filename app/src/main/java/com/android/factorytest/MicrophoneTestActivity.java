package com.android.factorytest;

import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.view.InstrumentPanel;
import org.hermit.instruments.AudioAnalyser;

import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * 麦克风测试
 *
 */
public class MicrophoneTestActivity extends TestItemBaseActivity implements
		AudioAnalyser.OnAudioDataChange {

	private InstrumentPanel audioInstrument;
	private LinearLayout ll_content;
	private final int AUTO_TEST_TIMEOUT = 10;// 自动测试超时时间

	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.microphone_layout);
		audioInstrument = new InstrumentPanel(this);

		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		ll_content.addView(audioInstrument);
		super.onCreate(savedInstanceState);

		// Get the desired sample rate.
		int sampleRate = 8000;
		audioInstrument.setSampleRate(sampleRate);

		// Get the desired block size.
		int blockSize = 128;
		audioInstrument.setBlockSize(blockSize);

		// Get the desired decimation.
		int decimateRate = 2;
		audioInstrument.setDecimation(decimateRate);

		// Get the desired Spectrum visualization.
		audioInstrument.loadInstruments();
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Take the wake lock if we want it.
		if (wakeLock != null && !wakeLock.isHeld())
			wakeLock.acquire();

		audioInstrument.onResume();

		// Just start straight away.
		audioInstrument.surfaceStart();
		audioInstrument.setOnAudioDataChange(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		audioInstrument.onPause();
	}

	private static final String TAG = "Audalyzer";
	private PowerManager.WakeLock wakeLock = null;

	@Override
	public void onAudioDataChange(double power) {
		// 自动化测试
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			if (power > -50) {
				synchronized (this) {
					stopAutoTest(true);
				}
			}
		}
	}
}
