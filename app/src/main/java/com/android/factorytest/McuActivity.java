package com.android.factorytest;

import com.android.factorytest.utils.CommonUtils;
import com.android.factorytest.utils.DialogUtils;
import com.android.manager.PdsManager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class McuActivity extends TestItemBaseActivity {
	private PdsManager pdsManager = null;
	private TextView tv_AccStatus = null;
	private TextView tv_Version = null;
	private TextView tv_Voltage = null;
    private EditText etFilename = null;
    private Button btnUpdate = null;

	private int received = 0;
	private final static int ACCSTATUS = 0x01;
	private final static int VERSION = 0x02;
	private final static int VOLTAGE = 0x04;
	private static int RECEIVEDALL = ACCSTATUS | VERSION | VOLTAGE;

	private final static int MSG_UPDATE_TEXT = 0x08;
	private final static int MSG_UPDATE_FINISH = 0x10;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.mcu_layout);
		super.onCreate(savedInstanceState);

		tv_AccStatus = (TextView) findViewById(R.id.tv_acc);
		tv_Version = (TextView) findViewById(R.id.tv_version);
		tv_Voltage = (TextView) findViewById(R.id.tv_voltage);

        etFilename = (EditText)findViewById(R.id.et_filename);
        etFilename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.selectFile(McuActivity.this/*注：不能传getApplicationContext()*/, new DialogUtils.DialogSelection() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        etFilename.setText(files[0]);
                    }
                });
            }
        });

        btnUpdate = (Button)findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = etFilename.getText().toString();
                if(filename==null || filename.length()<=1) {
                    Toast.makeText(getApplicationContext(),R.string.select_correct_file, Toast.LENGTH_LONG).show();
                    return;
                }
                btnUpdate.setEnabled(false);
                etFilename.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.obj = getString(R.string.wait_for_upgrade);
                        msg.what = MSG_UPDATE_TEXT;
                        mHandler.sendMessage(msg);

                        if (pdsManager.upgradeMcu(etFilename.getText().toString())) {
                            msg = Message.obtain();
                            msg.obj = getString(R.string.upgrade_successful);
                            msg.what = MSG_UPDATE_TEXT;
                            mHandler.sendMessage(msg);
                        } else {
                            msg = Message.obtain();
                            msg.obj = getString(R.string.upgrade_fail);
                            msg.what = MSG_UPDATE_TEXT;
                            mHandler.sendMessage(msg);
                        }
                        mHandler.sendEmptyMessage(MSG_UPDATE_FINISH);
                    }
                }).start();
            }
        });
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ACCSTATUS: {
					switch (pdsManager.getAccState()) {
						case PdsManager.ACC_OFF:
							tv_AccStatus.setText("ACC OFF");
							tv_AccStatus.setTextColor(Color.GREEN);
							received |= ACCSTATUS;
							break;

						case PdsManager.ACC_ON:
							tv_AccStatus.setText("ACC ON");
							tv_AccStatus.setTextColor(Color.GREEN);
							received |= ACCSTATUS;
							break;

						default:
							Log.e("llx","ACC状态查询失败");
							break;
					}
					mHandler.sendEmptyMessageDelayed(VERSION, 50);
				}
				break;

				case VERSION: {
					String mcuVersion = pdsManager.getMcuSWVersion();
					if(mcuVersion!=null) {
						tv_Version.setText(mcuVersion);
						tv_Version.setTextColor(Color.GREEN);
						received |= VERSION;
					} else {
						Log.e("llx","MCU版本号查询失败");
					}
					mHandler.sendEmptyMessageDelayed(VOLTAGE, 50);
				}
				break;

				case VOLTAGE: {
					String mcuVoltage = pdsManager.getVoltage();
					if(mcuVoltage==null) {
						Log.e("llx","电源电压查询失败");
					} else {
						tv_Voltage.setText(mcuVoltage);
						tv_Voltage.setTextColor(Color.GREEN);
						received |= VOLTAGE;
					}

                    if ((received & RECEIVEDALL) == RECEIVEDALL) {
                        synchronized (this) {
                            stopAutoTest(true);
                        }
                    } else {
                        synchronized (this) {
                            stopAutoTest(false);
                        }
                    }
				}
				break;

                case MSG_UPDATE_TEXT:
                    String text = (String)msg.obj;
                    Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                    break;

                case MSG_UPDATE_FINISH:
                    etFilename.setEnabled(true);
                    btnUpdate.setEnabled(true);
                    mHandler.sendEmptyMessageDelayed(VERSION, 1800);
                    break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
        pdsManager = new PdsManager(getApplicationContext(), new PdsManager.PdsManagerListener() {
			@Override
			public void onServiceConnection() {
				mHandler.sendEmptyMessageDelayed(ACCSTATUS, 100);
			}

			@Override
			public void onServiceDisconnected() {

			}

			@Override
			public boolean onAccNotify(byte state) {
				switch (state) {
					case PdsManager.ACC_OFF:
						tv_AccStatus.setText("ACC OFF");
						tv_AccStatus.setTextColor(Color.GREEN);
						received |= ACCSTATUS;
						if ((received & RECEIVEDALL) == RECEIVEDALL) {
							synchronized (this) {
								stopAutoTest(true);
							}
						}
						break;

					case PdsManager.ACC_ON:
						tv_AccStatus.setText("ACC ON");
						tv_AccStatus.setTextColor(Color.GREEN);
						received |= ACCSTATUS;
						if ((received & RECEIVEDALL) == RECEIVEDALL) {
							synchronized (this) {
								stopAutoTest(true);
							}
						}
						break;

					default:
						Log.e("llx","ACC状态错误");
						break;
				}
				return false;
			}

			@Override
			public void onCanData(int i, long l, boolean b, boolean b1, byte[] bytes) {

			}
		});
	}

	@Override
	protected void onPause() {
        mHandler.removeMessages(MSG_UPDATE_FINISH);
        mHandler.removeMessages(MSG_UPDATE_TEXT);
		mHandler.removeMessages(VOLTAGE);
		mHandler.removeMessages(VERSION);
		mHandler.removeMessages(ACCSTATUS);
		super.onPause();
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}
}
