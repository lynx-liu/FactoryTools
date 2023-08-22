package com.android.factorytest;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SIMActivity extends TestItemBaseActivity {

	private TextView txtSIMState;
	private TextView txt_sim_imsi;
	private TelephonyManager telManager;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.sim_layout);
		telManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		txtSIMState = (TextView) findViewById(R.id.txt_sim_state);
		txt_sim_imsi = (TextView) findViewById(R.id.txt_sim_imsi);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {

		super.onResume();
		checkSIMState();
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);

	}

	private void checkSIMState() {
		if (isSimCardAccess()) {
			txtSIMState.setText(R.string.available);
			String imsi = telManager.getSubscriberId();
			txt_sim_imsi.setText(telManager.getSubscriberId());

			if (imsi != null) {
				synchronized (this) {

					stopAutoTest(true);
				}
			}
		} else {
			txtSIMState.setText(R.string.not_available);
			synchronized (this) {

				stopAutoTest(false);
			}
		}

	}

	/**
	 * 判断sim状况
	 *
	 * @return
	 */
	private boolean isSimCardAccess() {

		boolean flag = false;
		switch (telManager.getSimState()) {
			case TelephonyManager.SIM_STATE_READY:
				flag = true;
				break;
			case TelephonyManager.SIM_STATE_ABSENT:// 无SIM卡
				flag = false;
				break;
			default:// SIM卡被锁定或未知状态
				flag = false;
				break;
		}
		return flag;
	}

}
