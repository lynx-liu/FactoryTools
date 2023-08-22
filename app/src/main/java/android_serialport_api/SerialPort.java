package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class SerialPort extends Thread{

	private static final String TAG = "SerialPort";

	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	private onDataReceivedListener mListener = null;
    private boolean received = false;

	public interface onDataReceivedListener {
		boolean onDataReceived(final byte[] buffer, final int size);
	}

	public SerialPort(String path, int baudrate, int flags, onDataReceivedListener listener) throws SecurityException, IOException {
		File device = new File(path);
		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/xbin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
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

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}

		mListener = listener;

		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
        start();
	}

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
                    received = true;
                    if(mListener!=null) mListener.onDataReceived(buffer, size);
                } else {
                    sleep(1);//降低CPU使用率
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public boolean isReceived() {
        return received;
    }

	public void send(String str) {
		char[] text = new char[str.length()];
		for (int i=0; i<str.length(); i++) {
			text[i] = str.charAt(i);
		}
		try {
			mFileOutputStream.write(new String(text).getBytes());
			mFileOutputStream.write('\r');
			mFileOutputStream.write('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Release() {
		mListener = null;
        interrupt();
		close();
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}
