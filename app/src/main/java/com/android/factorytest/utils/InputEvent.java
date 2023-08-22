///////////////////////////////////////////////////
//				date: 2022.05.28
//				author: LiuLixiang
//				email: 13651417694@126.com
//				qq: 515311445
//				weixin: 13651417694
///////////////////////////////////////////////////
package com.android.factorytest.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InputEvent {
	private FileInputStream mFileInputStream;
	private ReadThread mReadThread;
	private onInputEventListener mListener = null;
    final static int EV_KEY = 0x0001;
    public final static int KEY_POWER = 0x0074;
    public final static int DOWN = 0x00000001;
    public final static int UP = 0x00000000;

	public interface onInputEventListener {
		boolean onKeyEvent(int keycode, long keyevent);
	}

	public InputEvent(onInputEventListener listener) throws SecurityException, IOException {
		File device = new File("/dev/input/event1");
		/* Check access permission */
		if (!device.canRead()) {
			try {
				/* Missing read permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/xbin/su");
				String cmd = "chmod 444 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mListener = listener;
		mFileInputStream = new FileInputStream(device);

		/* Create a receiving thread */
		mReadThread = new ReadThread();
		mReadThread.start();
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mFileInputStream == null) return;
					size = mFileInputStream.read(buffer);
					if (size > 0) {
                        StringBuilder stringBuilder = new StringBuilder("recv:");
                        for (int i = 0; i < size; i++)
                            stringBuilder.append(String.format("%02X ", buffer[i]));
                        Log.d("llx",stringBuilder.toString());

                        long tv_sec = ((buffer[3]&0xFF)<<24)|((buffer[2]&0xFF)<<16)|((buffer[1]&0xFF)<<8)|(buffer[0]&0xFF);
                        long tv_usec = ((buffer[7]&0xFF)<<24)|((buffer[6]&0xFF)<<16)|((buffer[5]&0xFF)<<8)|(buffer[4]&0xFF);
                        int type = ((buffer[9]&0xFF)<<8)|(buffer[8]&0xFF);
                        int code = ((buffer[11]&0xFF)<<8)|(buffer[10]&0xFF);
                        long value = ((buffer[15]&0xFF)<<24)|((buffer[14]&0xFF)<<16)|((buffer[13]&0xFF)<<8)|(buffer[12]&0xFF);

						if(mListener!=null) {
                            if(type==EV_KEY) mListener.onKeyEvent(code, value);
                        }
					} else {
						sleep(1);//降低CPU使用率
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

    public void Release() {
		mListener = null;
		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
	}
}
