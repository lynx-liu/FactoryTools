package com.android.factorytest.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

public class SystemUtils {

	/**
	 *
	 * 方法描述：软件修改时间
	 *
	 * @return
	 */
	public static String getSoftWareVersion(Context context) {
		String sVersion = null;
		try {
			SimpleDateFormat date = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
			sVersion = date.format(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime);
		} catch (Exception e) {
			sVersion = "unknown";
			e.printStackTrace();
		}
		return sVersion;
	}


	/**
	 *
	 * 方法描述：应用版本
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getAppVersion(Context context,String packageName){
		try{
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo;
			packageInfo = pm.getPackageInfo(packageName, 0);// 取得当前的版本信息
			String versionName = packageInfo.versionName;
			return versionName;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * 方法描述：取系统属性
	 * @param field
	 * @return
	 */
	public static String getSystemProperties(String field) {
		String platform = null;
		try {
			Class<?> classType = Class.forName("android.os.SystemProperties");
			Method getMethod = classType.getDeclaredMethod("get", new Class<?>[] { String.class });
			platform = (String) getMethod.invoke(classType, new Object[] { field });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return platform;
	}

    /**
     * 获取外置sdcard和U盘路径，并区分
     * @param mContext
     * @param keyword 存储空间 = "内部共享存储空间"; SD = "SD卡"; U = "U盘"，和驱动相关，建议看看LOG打印的userLabel
     * @return
     */
    public static String getStoragePath(Context mContext,String keyword) {
        String targetpath = null;
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);
                String path = (String) getPath.invoke(storageVolumeElement);
                Log.d("llx",userLabel+": "+path);
                if(userLabel.contains(keyword)){
                    targetpath = path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetpath;
    }

    public static long getMemorySize(String sdcardPath) {

        StatFs stat = new StatFs(sdcardPath);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getBlockCount();
        return availableBlocks * blockSize;

    }

    public static long getFreeMemorySize(String sdcardPath) {
        StatFs stat = new StatFs(sdcardPath);
        long blockSize = stat.getBlockSize();
        long freeblocks = stat.getFreeBlocks();
        return freeblocks * blockSize;
    }

    /**
	 *
	 * 方法描述：取Flash大小
	 * @return
	 */
	public static String getTotalFlash(Context context) {
		File root = Environment.getDataDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		return Formatter.formatFileSize(context, blockSize * blockCount);
	}

	/**
	 *
	 * 方法描述：取内存条大小KB
	 * @return
	 */
	public static long getTotalMemory(Context context) {
		long memTotal = 0;
		try {
			FileReader localFileReader = new FileReader("/proc/meminfo"); // 系统内存信息文件
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			String meminfo = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            String[] arrayOfString = meminfo.split("\\s+");
            memTotal = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB
			localBufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return memTotal;
	}

    /**
     *
     * 方法描述：取用内存大小KB
     * @return
     */
    public static long getAvailableMemory(Context context) {
        long memTotal = 0;
        try {
            FileReader localFileReader = new FileReader("/proc/meminfo"); // 系统内存信息文件
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            localBufferedReader.readLine();// 读取meminfo第一行，MemTotal
            localBufferedReader.readLine();// 读取meminfo第二行，MemFree
            String meminfo = localBufferedReader.readLine();// 读取meminfo第三行，MemAvailable
            String[] arrayOfString = meminfo.split("\\s+");
            memTotal = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB
            localBufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memTotal;
    }

	/**
	 * @return 系统版本信息
	 */
	public static String[] getVersion() {
		String[] version = { "null", "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[0] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
		}
		version[1] = Build.VERSION.RELEASE;// firmware version
		version[2] = Build.MODEL;// model
		version[3] = Build.DISPLAY;// system version
		version[4] = Build.BRAND;// brand
		return version;
	}
}
