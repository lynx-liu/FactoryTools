package com.android.factorytest;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPort.onDataReceivedListener;

public class SerialportActivity extends TestItemBaseActivity {
	private final int MSG_SEND_DATA = 1;
	private final int MSG_RECEIVE_DATA = 2;

	private static int change_count = 0;
	private List<SerialPort> mSerialPorts = new ArrayList<SerialPort>();
	private EditText mMessage = null;

	private final int AUTO_TEST_TIMEOUT = 10;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 5;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setContentView(R.layout.serialport_layout);
		super.onCreate(savedInstanceState);

		mMessage = (EditText) findViewById(R.id.et_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkSerialportState();
	}

	private void checkSerialportState() {
		mMessage.setText("");
		final String[] path = Build.DISPLAY.contains("PD101B")?new String[]{ "/dev/ttyS0", "/dev/ttyS5"}:new String[]{ "/dev/ttyS0", "/dev/ttyS3", "/dev/ttyS4"};
        for (int i=0;i<path.length;i++) {
            try {
                mSerialPorts.add(new SerialPort(path[i],115200, 0, new onDataReceivedListener() {
                    @Override
                    public boolean onDataReceived(final byte[] buffer, final int size) {
                        if (size > 0) {
                            Message msg = new Message();
                            msg.what = MSG_RECEIVE_DATA;
                            msg.obj = path[0] + ":" + new String(buffer, 0, size);
                            mHandler.sendMessage(msg);

                            if (testOKCount()==path.length) {
                                synchronized (this) {
                                    stopAutoTest(true);
                                }
                            }
                        }
                        return false;
                    }
                }));
                mMessage.append(path[i] + ": open success\r\n");
            } catch (Exception e) {
                mMessage.append(path[i] + ": open fail!"+e.toString());
            }
        }

		if (testFail()) {
			synchronized (this) {
				stopAutoTest(false);
			}
		} else {
			mHandler.sendEmptyMessageDelayed(MSG_SEND_DATA, 500);
		}
	}

    private boolean testFail() {
        if(mSerialPorts==null)
            return true;
        for(SerialPort serialport:mSerialPorts) {
            if(serialport==null)
                return true;
        }
        return false;
    }

    private int testOKCount() {
        int count = 0;
        for(SerialPort serialport:mSerialPorts) {
            if(serialport!=null && serialport.isReceived())
                count++;
        }
        return count;
    }

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	@Override
	protected void onPause() {
		mHandler.removeMessages(MSG_SEND_DATA);
		mHandler.removeMessages(MSG_RECEIVE_DATA);
        if(mSerialPorts!=null) {
            for (SerialPort serialport : mSerialPorts) {
                if (serialport != null) serialport.Release();
            }
            mSerialPorts.clear();
        }
		super.onPause();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SEND_DATA: {
					SerialPort serialPort = mSerialPorts.get((change_count++) % mSerialPorts.size());
                    if (serialPort != null) {
                        serialPort.send("test:" + System.currentTimeMillis());
                    }
					mHandler.sendEmptyMessageDelayed(MSG_SEND_DATA, 500);
				}
				break;

				case MSG_RECEIVE_DATA: {
					if (mMessage != null) {
						if(mMessage.getLineCount()>5000){
							mMessage.setText((String) msg.obj+"\r\n");
							mMessage.setSelection(mMessage.getText().length());
						} else mMessage.append((String) msg.obj+"\r\n");
					}
				}
				break;
			}
		}
	};
}
