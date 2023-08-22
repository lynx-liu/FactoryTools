package com.android.factorytest;

import com.android.factorytest.manager.GpioManager;
import com.android.factorytest.manager.GpioManager.GPIO;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GPIOActivity extends TestItemBaseActivity {
	private final int MSG_GPIO_CHANGED = 1;
	private static int change_count = 0;
	private GpioManager gpioManager = null;
	private TextView textView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.gpio_layout);
		super.onCreate(savedInstanceState);
        textView = findViewById(R.id.tv_message);

		gpioManager = new GpioManager();
		mHandler.sendEmptyMessageDelayed(MSG_GPIO_CHANGED, 500);
	}

	@Override
	protected void onDestroy() {
		mHandler.removeMessages(MSG_GPIO_CHANGED);
        gpioManager.recovery();
		super.onDestroy();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_GPIO_CHANGED:
				{
					change_count++;
					switch(change_count%2) {
						case 0: {//拉低
                            gpioManager.setEnabled(GPIO.GPS_POWER_CTL, false);
                            gpioManager.setEnabled(GPIO.GPS_RESET_CTL, false);

                            String status = getString(R.string.low);
                            String txtMsg = String.format("%s:%s\n%s:%s\n", GPIO.GPS_POWER_CTL.name(), status, GPIO.GPS_RESET_CTL.name(), status);
                            if(!Build.DISPLAY.contains("PD101B")) {
                                gpioManager.setEnabled(GPIO.OutputIO1, false);
                                gpioManager.setEnabled(GPIO.OutputIO2, false);
                                gpioManager.setEnabled(GPIO.OutputIO3, false);

                                txtMsg = String.format("%s:%s\n%s:%s\n%s:%s\n%s:%s\n%s:%s\n", GPIO.OutputIO1.name(), status, GPIO.OutputIO2.name(), status, GPIO.OutputIO3.name(), status, GPIO.GPS_POWER_CTL.name(), status, GPIO.GPS_RESET_CTL.name(), status);

                                status = gpioManager.getEnable(GPIO.InputIO0) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO0, status);

                                status = gpioManager.getEnable(GPIO.InputIO1) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO1, status);

                                status = gpioManager.getEnable(GPIO.InputIO2) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO2, status);
                            }
                            textView.setText(txtMsg);
                        }
							break;

						case 1: {//拉高
                            gpioManager.setEnabled(GPIO.GPS_POWER_CTL, true);
                            gpioManager.setEnabled(GPIO.GPS_RESET_CTL, true);

                            String status = getString(R.string.high);
                            String txtMsg = String.format("%s:%s\n%s:%s\n", GPIO.GPS_POWER_CTL.name(), status, GPIO.GPS_RESET_CTL.name(), status);
                            if(!Build.DISPLAY.contains("PD101B")) {
                                gpioManager.setEnabled(GPIO.OutputIO1, true);
                                gpioManager.setEnabled(GPIO.OutputIO2, true);
                                gpioManager.setEnabled(GPIO.OutputIO3, true);

                                txtMsg = String.format("%s:%s\n%s:%s\n%s:%s\n%s:%s\n%s:%s\n", GPIO.OutputIO1.name(), status, GPIO.OutputIO2.name(), status, GPIO.OutputIO3.name(), status, GPIO.GPS_POWER_CTL.name(), status, GPIO.GPS_RESET_CTL.name(), status);

                                status = gpioManager.getEnable(GPIO.InputIO0) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO0, status);

                                status = gpioManager.getEnable(GPIO.InputIO1) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO1, status);

                                status = gpioManager.getEnable(GPIO.InputIO2) ? getString(R.string.high) : getString(R.string.low);
                                txtMsg += String.format("%s:%s\n", GPIO.InputIO2, status);
                            }
                            textView.setText(txtMsg);
                        }
							break;
					}
					mHandler.sendEmptyMessageDelayed(MSG_GPIO_CHANGED, 500);
				}
				break;
			}
		}
	};
}
