package com.android.factorytest.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.android.factorytest.BacklightActivity;
import com.android.factorytest.BluetoochTestActivity;
import com.android.factorytest.CameraActivity;
import com.android.factorytest.CanActivity;
import com.android.factorytest.DrawScreenActivity;
import com.android.factorytest.EthernetActivity;
import com.android.factorytest.FMTestActivity;
import com.android.factorytest.GPIOActivity;
import com.android.factorytest.GPSTestActivity;
import com.android.factorytest.GravityActivity;
import com.android.factorytest.GyroscopeActivity;
import com.android.factorytest.HeadPhoneActivity;
import com.android.factorytest.KeysActivity;
import com.android.factorytest.LCDActivity;
import com.android.factorytest.LEDActivity;
import com.android.factorytest.MagneticFieldActivity;
import com.android.factorytest.McuActivity;
import com.android.factorytest.MicrophoneTestActivity;
import com.android.factorytest.PowerActivity;
import com.android.factorytest.R;
import com.android.factorytest.RamActivity;
import com.android.factorytest.SIMActivity;
import com.android.factorytest.SdcardActivity;
import com.android.factorytest.SerialportActivity;
import com.android.factorytest.SpeakerActivity;
import com.android.factorytest.SysteminfoActivity;
import com.android.factorytest.TouchScreenActivity;
import com.android.factorytest.UDISKActivity;
import com.android.factorytest.VibrateTestActivity;
import com.android.factorytest.WifiTestActivity;
import com.android.factorytest._3GActivity;
import com.android.factorytest.bean.TestItem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.os.Build;

public class FactoryTestManager {

	private Context context;
	private SharedPreferences sp;

	public static final String CONFIG_FILE_PATH = "/mnt/sdcard/factory.cfg";// 配置文件路径
	public static final String CONFIG_PRE_STRING = "com.factory.";// 配置文件前缀
	public static final String ITEM_MAGNET = "magnet";// 磁场
	public static final String ITEM_CAMERA = "camera";// 摄像机
	public static final String ITEM_GPS = "gps";// gps
	public static final String ITEM_HEADPHONE = "headphone";// 听筒
	public static final String ITEM_MICROPHONE = "microphone";// 话筒
    public static final String ITEM_RAM = "ram";// RAM
	public static final String ITEM_SDCARD = "sdcard";// sdcard
	public static final String ITEM_UDISK = "udisk";// U盘存储测试
	public static final String ITEM_SIM = "sim";// sim
	public static final String ITEM_3G = "3g";// 3g
	public static final String ITEM_ETHERNET = "ethernet";// 有线网
	public static final String ITEM_DRAW_SCREEN = "draw_screen";// 屏幕画线
	public static final String ITEM_TOUCH_SCREEN = "touch_screen";// 多点触控
	public static final String ITEM_VIBRATE = "vibrate";// 振动
	public static final String ITEM_BLUETOOTH = "bluetooth";// 蓝牙
	public static final String ITEM_WIFI = "wifi";// wifi
	public static final String ITEM_RADIO = "radio";// radio
	public static final String ITEM_KEYS = "keys";// 按键
    public static final String ITEM_GYROSCOPE = "gyroscope";//陀螺仪
	public static final String ITEM_GRAVITY = "gravity";// 重力感应
	public static final String ITEM_LCD = "lcd";// lcd
	public static final String ITEM_BACKLIGHT = "backlight";// 背光
	public static final String ITEM_POWER = "power";// 电压电流
	public static final String ITEM_SPEAKER = "speaker";// 喇叭电流
	public static final String ITEM_GPIO = "gpio";// GPIO
	public static final String ITEM_LED = "led";// LED
	public static final String ITEM_SERIALPORT = "serialport";// 串口
	public static final String ITEM_CAN = "can";// CAN
	public static final String ITEM_MCU = "mcu";// MCU
	public static final String ITEM_SYSTEMINFO = "systeminfo";// 系统信息

	public static int currentTestMode;

	public interface TestMode {
		public static final int MODE_AUTO_TEST = 1;// 自动测试
		public static final int MODE_SINGAL_TEST = 2;// 单项测试
		public static final int MODE_RESULT_TEST = 3;// 查看测试报表
	}

	public static boolean isPCBA = false;

	public static List<TestItem> testList = new ArrayList<TestItem>();
	static FactoryTestManager instance = null;

	public static FactoryTestManager getInstance(Context context) {
		if (instance == null) {
			instance = new FactoryTestManager(context);
		}
		return instance;
	}

	public List<TestItem> getTestList() {
		return testList;
	}

	private FactoryTestManager(Context context) {
		this.context = context;
		sp = context.getSharedPreferences("factoryTest", Context.MODE_PRIVATE);
		// readConfigFile();
		initTestList();
	}

	public TestItem getActivityTestItem(Activity activity) {
		for (int i = 0; i < testList.size(); i++) {
			Class cls = testList.get(i).activityCls;
			if (cls == activity.getClass()) {
				return testList.get(i);
			}
		}
		return null;
	}

	public void initTestList() {

		TestItem magnet = new TestItem(MagneticFieldActivity.class, R.drawable.magnet, ITEM_MAGNET, context.getString(R.string.magnet), true);
		add2List(magnet);

		TestItem gyroscope = new TestItem(GyroscopeActivity.class, R.drawable.gyroscope, ITEM_GYROSCOPE, context.getString(R.string.gyroscope), true);
		add2List(gyroscope);

		TestItem gravity = new TestItem(GravityActivity.class, R.drawable.gravity, ITEM_GRAVITY, context.getString(R.string.gravity), true);
		add2List(gravity);

		TestItem power = new TestItem(PowerActivity.class, R.drawable.power, ITEM_POWER, context.getString(R.string.power), true);
		add2List(power);

        TestItem ram = new TestItem(RamActivity.class, R.drawable.ram, ITEM_RAM, context.getString(R.string.ram), true);
        add2List(ram);

		TestItem sdcard = new TestItem(SdcardActivity.class, R.drawable.sd_card, ITEM_SDCARD, context.getString(R.string.sd_card), true);
		add2List(sdcard);

		TestItem udisk = new TestItem(UDISKActivity.class, R.drawable.udisk, ITEM_UDISK, context.getString(R.string.udisk), true);
		add2List(udisk);

		TestItem bluetooth = new TestItem(BluetoochTestActivity.class, R.drawable.bluetooth, ITEM_BLUETOOTH, context.getString(R.string.bluetooth),
				true);
		add2List(bluetooth);

		TestItem wifi = new TestItem(WifiTestActivity.class, R.drawable.wifi, ITEM_WIFI, context.getString(R.string.wifi), true);
		add2List(wifi);

		TestItem sim = new TestItem(SIMActivity.class, R.drawable.sim_card, ITEM_SIM, context.getString(R.string.sim_card), true);
		add2List(sim);

		TestItem _3g = new TestItem(_3GActivity.class, R.drawable._3g, ITEM_3G, context.getString(R.string._3g), true);
		add2List(_3g);

        if(!Build.DISPLAY.contains("PD101B")) {
            TestItem gps = new TestItem(GPSTestActivity.class, R.drawable.gps, ITEM_GPS, context.getString(R.string.gps), true);
            add2List(gps);

            TestItem microphone = new TestItem(MicrophoneTestActivity.class, R.drawable.microphone, ITEM_MICROPHONE,
                    context.getString(R.string.microphone), true);
            add2List(microphone);

            TestItem ethernet = new TestItem(EthernetActivity.class, R.drawable.ethernet, ITEM_ETHERNET, context.getString(R.string.ethernet), true);
            add2List(ethernet);
        }

		TestItem serialport = new TestItem(SerialportActivity.class, R.drawable.serialport, ITEM_SERIALPORT, context.getString(R.string.serialport), true);
		add2List(serialport);

		TestItem mcu = new TestItem(McuActivity.class, R.drawable.mcu, ITEM_MCU, context.getString(R.string.mcu), false);
		add2List(mcu);

        if(Camera.getNumberOfCameras()>0) {
            TestItem camera = new TestItem(CameraActivity.class, R.drawable.camera, ITEM_CAMERA, context.getString(R.string.camera), false);
            add2List(camera);
        }

        if(!Build.DISPLAY.contains("PD101B")) {
            TestItem can = new TestItem(CanActivity.class, R.drawable.can, ITEM_CAN, context.getString(R.string.can), false);
            add2List(can);
        }

		TestItem headphone = new TestItem(HeadPhoneActivity.class, R.drawable.headphone, ITEM_HEADPHONE, context.getString(R.string.headphone), false);
		add2List(headphone);

		TestItem speaker = new TestItem(SpeakerActivity.class, R.drawable.speaker, ITEM_SPEAKER, context.getString(R.string.speaker), false);
		add2List(speaker);

		TestItem gpio = new TestItem(GPIOActivity.class, R.drawable.gpio, ITEM_GPIO, context.getString(R.string.gpio), true);
		add2List(gpio);

		TestItem led = new TestItem(LEDActivity.class, R.drawable.led, ITEM_LED, context.getString(R.string.led), true);
		add2List(led);

		TestItem vibrate = new TestItem(VibrateTestActivity.class, R.drawable.vibrate, ITEM_VIBRATE, context.getString(R.string.vibrate), false);
		add2List(vibrate);

		TestItem keys = new TestItem(KeysActivity.class, R.drawable.keys, ITEM_KEYS, context.getString(R.string.keys), false);
		add2List(keys);

		if (!isPCBA) {
			TestItem draw_screen = new TestItem(DrawScreenActivity.class, R.drawable.draw_screen, ITEM_DRAW_SCREEN, context.getString(R.string.draw_screen), false);
			add2List(draw_screen);

			TestItem touch_screen = new TestItem(TouchScreenActivity.class, R.drawable.touch_screen, ITEM_TOUCH_SCREEN, context.getString(R.string.touch_screen), false);
			add2List(touch_screen);

			TestItem lcd = new TestItem(LCDActivity.class, R.drawable.lcd, ITEM_LCD, context.getString(R.string.lcd), false);
			add2List(lcd);

			TestItem backlight = new TestItem(BacklightActivity.class, R.drawable.backlight, ITEM_BACKLIGHT, context.getString(R.string.backlight), false);
			add2List(backlight);
		}

		TestItem radio = new TestItem(FMTestActivity.class, R.drawable.radio, ITEM_RADIO, context.getString(R.string.radio), false);
		add2List(radio);

		TestItem systeminfo = new TestItem(SysteminfoActivity.class, R.drawable.systeminfo, ITEM_SYSTEMINFO, context.getString(R.string.systeminfo),
				false);
		add2List(systeminfo);
	}

	private void add2List(TestItem item) {
		if (item.isEnable(context)) {
			testList.add(item);
		}
	}

	/**
	 * 保存配置文件
	 */
	public void gernalConfigFile() {
		try {
			File outFile = new File(CONFIG_FILE_PATH);
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(outFile);
			Properties properties = new Properties();
			for (int i = 0; i < testList.size(); i++) {
				TestItem item = testList.get(i);
				properties.setProperty(CONFIG_PRE_STRING + item.itemName, "yes");
			}
			properties.save(fos, "factory test config file ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setResult(String item, boolean result) {
		Editor edit = sp.edit();
		edit.putString(item, result ? "yes" : "no");
		edit.commit();
	}

	public Boolean getResult(String item) {
		String result = sp.getString(item, null);
		if ("yes".equals(result)) {
			return true;
		} else if ("no".equals(result)) {
			return false;
		}
		return null;
	}

	public void clearTestResult() {
		sp.edit().clear().commit();
	}

	public Class getNextActivityClass(Class currentCls) {

		if (currentCls == null && testList.size() > 0) {
			return testList.get(0).activityCls;
		}
		for (int i = 0; i < testList.size(); i++) {
			Class cls = testList.get(i).activityCls;
			if (cls == currentCls) {
				if (i == testList.size() - 1) {
					return null;
				} else {
					return testList.get(i + 1).activityCls;
				}
			}
		}
		return null;

	}
}
