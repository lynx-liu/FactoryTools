package com.android.factorytest;

import com.android.factorytest.bean.TestItem;
import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.view.JudgeView;
import com.android.factorytest.view.JudgeView.OnResultSelected;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class TestItemBaseActivity extends Activity implements OnResultSelected,
		OnKeyListener {

	private JudgeView judgeview;
	public FactoryTestManager testResultManager;

	private Dialog progressDialog;
	private Handler handler;
	private Runnable timeOutRunnable;
	private long enterAcvitityTime;
	private int mini_show_time = 0;
	private int time_out_show = 0;
	private boolean timeThreadStart = true;
	private int timeSpend = 0;
	private boolean waiting_for_finish = false;
	private boolean testResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		judgeview = (JudgeView) findViewById(R.id.judgeview);
		if (judgeview != null) {
			judgeview.setOnResultSelectedListener(this);
		}
		testResultManager = FactoryTestManager.getInstance(this);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 303)
				{
					Toast toast = Toast.makeText(TestItemBaseActivity.this, getCurrentTestItem().label
							+ " " + getString(R.string.test_finish), Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		};
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			executeAutoTest();
		}

		timeOutRunnable = new Runnable() {
			@Override
			public void run() {

				Log.i("test", "----------------测试超时");
				stopAutoTest(false);

			}
		};
		String testMode = "";
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			testMode = getString(R.string.auto_test_mode);
		} else {
			testMode = getString(R.string.manual_test_mode);
		}
		setTitle(this.getCurrentTestItem().label + "(" + testMode + ")");
	}

	Thread timeOutthread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (timeThreadStart) {
				try {
					Log.i("test", "------------thread");
					Thread.sleep(1000);
					runOnUiThread(new Runnable() {
						public void run() {
							timeSpend++;
							if (timeSpend > time_out_show) {
								stopAutoTest(false);
							}
							if (waiting_for_finish
									&& timeSpend >= mini_show_time) {
								finishTest();
							}
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	});

	@Override
	protected void onResume() {
		enterAcvitityTime = System.currentTimeMillis();

		super.onResume();
	}

	public void setJudeViewEnable(boolean enable) {
		judgeview.setEnabled(enable);

	}

	/**
	 *
	 * @param timeOut超时时间
	 * @param miniShowTime
	 *            最短测试时间
	 */
	protected void startAutoTest(int timeOut, int miniShowTime) {
		Log.i("test", "----------------startAutoTest");
		this.mini_show_time = miniShowTime;

		progressDialog = new Dialog(this, R.style.progressDialog);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnKeyListener(this);
		progressDialog.setContentView(R.layout.progress_dialog);
		progressDialog.show();
		this.time_out_show = timeOut;
		timeOutthread.start();
	}

	protected void stopAutoTest(final boolean result) {
		Log.i("test", "----------------stopAutoTest");
		testResult = result;
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			long timeSpend = System.currentTimeMillis() - enterAcvitityTime;
			if (mini_show_time != 0 && timeSpend >= mini_show_time * 1000) {
				finishTest();
			} else {
				waiting_for_finish = true;
			}

		}
	}

	public void changeResult(boolean result){
		testResult=result;
	}
	boolean hasFinish = false;

	private void finishTest() {
		if (!hasFinish) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}

			testResultManager.setResult(this.getCurrentTestItem().itemName,
					testResult);
			setResult(TestGridActivity.RESULT_CODE_TESTOK);
			finish();
			handler.sendEmptyMessage(303);
			handler.removeCallbacks(timeOutRunnable);
			hasFinish = true;
		}
	}

	void executeAutoTest() {

	}

	public TestItem getCurrentTestItem() {
		return testResultManager.getActivityTestItem(this);
	}

	@Override
	public void onSelectResult(boolean success) {

		if (success) {
			testResultManager.setResult(this.getCurrentTestItem().itemName,
					true);
		} else {
			testResultManager.setResult(this.getCurrentTestItem().itemName,
					false);
		}
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			Class nextActivity = testResultManager.getNextActivityClass(this
					.getClass());
			if (nextActivity != null) {
				startActivity(new Intent(this, nextActivity));
			} else {// 顯示結果
				Intent intent = new Intent(this, TestGridActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				FactoryTestManager.currentTestMode = FactoryTestManager.TestMode.MODE_SINGAL_TEST;
				startActivity(intent);

			}
		}
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			// Toast.makeText(this,
			// getCurrentTestItem().label + " keyCode=" + keyCode, 0)
			// .show();
			Log.i("test", "------------keyCode=" + keyCode);
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				Log.i("test", "------------back");
				if (progressDialog != null)
					progressDialog.dismiss();
				finish();
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		this.onKeyDown(keyCode, event);
		return false;
	}

	@Override
	protected void onDestroy() {
		timeThreadStart = false;
		if (!timeOutthread.interrupted()) {
			timeOutthread.interrupt();
		}
		if (progressDialog != null)
			progressDialog.dismiss();
		super.onDestroy();
	}
}
