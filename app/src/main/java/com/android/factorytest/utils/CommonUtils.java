package com.android.factorytest.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class CommonUtils {

	public static boolean checkAppInstalled(Intent intent, Context context) {
		PackageManager manager = context.getPackageManager();
		List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
		if (list == null || list.size() < 1)
			return false;
		return true;
	}

	public static String keep6Point(double number) {
		return String.format("%.6f", number);
	}

	public static boolean copyFile(File src, File tar) throws Exception {
		if (src.isFile()) {
			InputStream is = new FileInputStream(src);
			OutputStream outputStream = new FileOutputStream(tar);
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(outputStream);
			byte[] bt = new byte[1024 * 8];
			int len = bis.read(bt);
			while (len != -1) {
				bos.write(bt, 0, len);
				len = bis.read(bt);
			}
			bis.close();
			bos.close();
			return true;
		}else if (src.isDirectory()) {
			File[] f = src.listFiles();
			tar.mkdir();
			for (int i = 0; i < f.length; i++) {
				copyFile(f[i].getAbsoluteFile(), new File(tar.getAbsoluteFile()
						+ File.separator + f[i].getName()));
			}
			return true;
		}
		return false;
	}

	public static String getMd5ByFile(File file) throws FileNotFoundException {
		String value = null;
		FileInputStream in = new FileInputStream(file);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	public static boolean setEnabled(int gpio,boolean enabled){
		try {
			FileOutputStream mFileOutputStream = new FileOutputStream("/dev/gpio_dev", false);
			mFileOutputStream.write(String.valueOf(gpio+(enabled?1:0)).getBytes());
			mFileOutputStream.close();
			return true;
		} catch (Exception e) {
			Log.d("llx",e.toString());
		}
		return false;
	}
}
