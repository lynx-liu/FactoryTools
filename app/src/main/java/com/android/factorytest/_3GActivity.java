package com.android.factorytest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import com.android.factorytest.manager.FactoryTestManager;
import com.android.factorytest.manager.WifiStatusManager;
import com.android.factorytest.utils.QREncode;
import com.android.factorytest.view.SignalView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class _3GActivity extends TestItemBaseActivity implements Callback {

	private TextView txt_singal_type;
	private TextView txt_connect_state;
	private TextView txt_operator;
	private TextView txt_imei;
	private TextView txt_iccid;
	private TextView txtStrength;
	private TelephonyManager telephonyManager;
	private ProgressBar singal_strength;
	private Handler mHandler;
	private SignalView signalView;
    private ImageView ivQRcode;
	private Button switchSIM;
	private int simChannel = -1;
	private int strength = 0;
	private boolean hasGetSingal = false;// 判断是否已经获取到信号
	private String imei;
	private String iccid;
    private boolean switchsim = false;
	public static final int NETWORK_CLASS_UNKNOWN = 0;
	public static final int NETWORK_CLASS_2_G = 1;
	public static final int NETWORK_CLASS_2_75_G = 4;
	public static final int NETWORK_CLASS_3_G = 2;
	public static final int NETWORK_CLASS_4_G = 3;

	public static final int NETWORK_TYPE_UNKNOWN = 0;
	public static final int NETWORK_TYPE_GPRS = 1;
	public static final int NETWORK_TYPE_EDGE = 2;
	public static final int NETWORK_TYPE_UMTS = 3;
	public static final int NETWORK_TYPE_CDMA = 4;
	public static final int NETWORK_TYPE_EVDO_0 = 5;
	public static final int NETWORK_TYPE_EVDO_A = 6;
	public static final int NETWORK_TYPE_1xRTT = 7;
	public static final int NETWORK_TYPE_HSDPA = 8;
	public static final int NETWORK_TYPE_HSUPA = 9;
	public static final int NETWORK_TYPE_HSPA = 10;
	public static final int NETWORK_TYPE_IDEN = 11;
	public static final int NETWORK_TYPE_EVDO_B = 12;
	public static final int NETWORK_TYPE_LTE = 13;
	public static final int NETWORK_TYPE_EHRPD = 14;
	public static final int NETWORK_TYPE_HSPAP = 15;

	private final int AUTO_TEST_TIMEOUT = 30;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout._3g_layout);
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
				finish();
			}
		}

		txt_imei = (TextView) findViewById(R.id.txt_imei);
		txt_iccid = (TextView) findViewById(R.id.txt_iccid);
		txtStrength = (TextView) findViewById(R.id.txtStrength);
		txt_singal_type = (TextView) findViewById(R.id.txt_singal_type);
		txt_connect_state = (TextView) findViewById(R.id.txt_connect_state);
		txt_operator = (TextView) findViewById(R.id.txt_operator);
		singal_strength = (ProgressBar) findViewById(R.id.singal_strength);
		signalView = (SignalView) findViewById(R.id.signalView);
        ivQRcode = (ImageView) findViewById(R.id.iv_qrcode);
		switchSIM = (Button) findViewById(R.id.switchSIM);
		switchSIM.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (simChannel) {
					case -1:
						switchSIM.setVisibility(View.GONE);
						break;
					case 0:
						simChannel = 1;
						setSimChannel(true);
						switchSIM.setText(R.string.switch_out);
						break;
					case 1:
						simChannel = 0;
						setSimChannel(false);
						switchSIM.setText(R.string.switch_in);
						break;
				}
			}
		});
		init();
	}

	@Override
	protected void onResume() {
        WifiStatusManager wifiManager = new WifiStatusManager(this);
        if(wifiManager.isWifiEnabled()) {
            wifiManager.closeWifi();//关闭wifi，否则isMobileConnected会返回false
        }

		simChannel = getSimChannel();
		switch (simChannel) {
			case -1:
				switchSIM.setVisibility(View.GONE);
				break;
			case 0:
				txt_connect_state.setVisibility(View.GONE);
				switchSIM.setVisibility(View.VISIBLE);
				switchSIM.setText(R.string.switch_in);
				break;
			case 1:
				txt_connect_state.setVisibility(View.GONE);
				switchSIM.setVisibility(View.VISIBLE);
				switchSIM.setText(R.string.switch_out);
				break;
		}
		super.onResume();
	}

	//-1:不支持SIM卡切换，0：外置SIM卡，1：内置SIM卡?
	public static int getSimChannel() {
		int channel = -1;
		if(!Build.DISPLAY.contains("C02"))//C02表示双SIM卡
		    return channel;

		BufferedReader reader = null;
		String path = "/sys/class/misc/sunxi-ril/rf-ctrl/rf_pwren";
		try {
			reader = new BufferedReader(new FileReader(path));
			String szValue = reader.readLine();
			if (szValue != null) {
				channel = Integer.valueOf(szValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return channel;
	}

	public static void setSimChannel(boolean state) {
		BufferedWriter writer = null;
		String path = "/sys/class/misc/sunxi-ril/rf-ctrl/rf_pwren";
		try {
			writer = new BufferedWriter(new FileWriter(path));
			writer.write(state ? '1' : '0');
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);

	}

	private void init() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(getApplicationContext(), "request permission.ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		imei = telephonyManager.getDeviceId();
		txt_imei.setText(imei == null ? getString(R.string.imei_none) : imei);
		iccid = telephonyManager.getSimSerialNumber();
		txt_iccid.setText(iccid == null ? getString(R.string.iccid_none) : iccid);
        if(iccid!=null) ivQRcode.setImageBitmap(QREncode.getQRcode(iccid.toCharArray(),5));//不滤波放大5倍，使二维码看起来更清晰

		PhoneSignalStateListener phoneSignalStateListener = new PhoneSignalStateListener();
		telephonyManager.listen(phoneSignalStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);// 信号监听

		mHandler = new Handler(this);
		mHandler.sendEmptyMessageDelayed(0, 1000);
	}

	@Override
	protected void onDestroy() {
		mHandler.removeMessages(0);
		super.onDestroy();
	}

	class PhoneSignalStateListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			updateSignalStrength(signalStrength);// 更新信号显示
            mHandler.sendEmptyMessageDelayed(0, 1000);
			super.onSignalStrengthsChanged(signalStrength);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			if (isMobileConnected(getApplicationContext())) {
				txt_connect_state.setText(R.string.comm_ok);
				txt_connect_state.setTextColor(Color.GREEN);
			} else {
				txt_connect_state.setText(R.string.comm_fail);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
					return;
				}
			}
			iccid = telephonyManager.getSimSerialNumber();
			txt_iccid.setText(iccid==null? getString(R.string.iccid_none):iccid);
            if(iccid!=null) ivQRcode.setImageBitmap(QREncode.getQRcode(iccid.toCharArray(),5));//不滤波放大5倍，使二维码看起来更清晰
            mHandler.sendEmptyMessageDelayed(0, 1000);
			super.onServiceStateChanged(serviceState);
		}
	}

	private void updateSignalStrength(SignalStrength mSignalStrength) {
		String carrierName = getCarrierName(telephonyManager
				.getNetworkOperatorName());// 运营商
		txt_operator.setText(carrierName);
		// 网络类型
		int netWorkClass = getNetworkClass(telephonyManager.getNetworkType());
		if (netWorkClass == NETWORK_CLASS_2_G) {
			txt_singal_type.setText("2G");
			txt_singal_type.setTextColor(Color.GREEN);
		} else if (netWorkClass == NETWORK_CLASS_2_75_G) {
			txt_singal_type.setText("2.75G");
			txt_singal_type.setTextColor(Color.GREEN);
		} else if (netWorkClass == NETWORK_CLASS_3_G) {
			txt_singal_type.setText("3G");
			txt_singal_type.setTextColor(Color.GREEN);
		} else if (netWorkClass == NETWORK_CLASS_4_G) {
			txt_singal_type.setText("4G");
			txt_singal_type.setTextColor(Color.GREEN);
		} else if (netWorkClass == NETWORK_CLASS_UNKNOWN) {
			txt_singal_type.setText(R.string.unknown);
		}

		int dbm = 0;
		if (mSignalStrength != null) {
			dbm = getDbm(mSignalStrength);
			strength = dbm;
			hasGetSingal = true;
			singal_strength.setProgress((int) (Math.abs(dbm) * 0.6));
			txtStrength.setText(dbm + "dbm");
		}
	}

	/**
	 * 说明:转换运营商
	 */
	public String getCarrierName(String carrier) {

		if (carrier == null) {
			return null;
		}
		String carrier_new = null;
		if (carrier.contains("CHN-CUGSM") || carrier.contains("CHN-UNICOM")
				|| carrier.contains(getString(R.string.carrier_unicom)) || carrier.contains("China Unicom")) {
			// 联通
			carrier_new = this.getResources()
					.getString(R.string.carrier_unicom);
			txt_operator.setTextColor(Color.GREEN);

		} else if (carrier.contains("CHINA MOBILE") || carrier.contains(getString(R.string.carrier_cmcc))) {
			// 移动
			carrier_new = this.getResources().getString(R.string.carrier_cmcc);
			txt_operator.setTextColor(Color.GREEN);
		} else if (carrier.contains("EVDO") || carrier.contains(getString(R.string.carrier_telecom))
				|| carrier.contains("CDMA")
				|| carrier.contains("CHINA TELECOM")) {
			// 电信
			carrier_new = this.getResources().getString(
					R.string.carrier_telecom);
			txt_operator.setTextColor(Color.GREEN);
		}
		if (carrier_new == null || "".equals(carrier_new)) {
			carrier_new = carrier;
		}
		if (!isSimCardAccess()) {// 无SIM卡
			// carrier_new = context.getResources().getString(
			// R.string.sim_no_exists);
			carrier_new = "";
		}
		return carrier_new;
	}

    int getDbm(SignalStrength signalStrength) {
        int dbm = 0;
        try {
            Method method = signalStrength.getClass().getMethod("getDbm");
            dbm = (int) method.invoke(signalStrength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbm;
    }

	/**
	 * 判断sim状况
	 *
	 * @return
	 */
	private boolean isSimCardAccess() {
		TelephonyManager telManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
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

	public static int getNetworkClass(int networkType) {
		switch (networkType) {
			case NETWORK_TYPE_GPRS:
			case NETWORK_TYPE_CDMA:
			case NETWORK_TYPE_1xRTT:
			case NETWORK_TYPE_IDEN:
				return NETWORK_CLASS_2_G;
			case NETWORK_TYPE_EDGE:
				return NETWORK_CLASS_2_75_G;
			case NETWORK_TYPE_UMTS:
			case NETWORK_TYPE_EVDO_0:
			case NETWORK_TYPE_EVDO_A:
			case NETWORK_TYPE_HSDPA:
			case NETWORK_TYPE_HSUPA:
			case NETWORK_TYPE_HSPA:
			case NETWORK_TYPE_EVDO_B:
			case NETWORK_TYPE_EHRPD:
			case NETWORK_TYPE_HSPAP:
				return NETWORK_CLASS_3_G;
			case NETWORK_TYPE_LTE:
				return NETWORK_CLASS_4_G;
			default:
				return NETWORK_CLASS_UNKNOWN;
		}
	}

	int preStrength = 0;

	@Override
	public boolean handleMessage(Message msg) {
		if (hasGetSingal) {
			signalView.onSignalChanged(strength);
		}
		// 自动化测试
		if (FactoryTestManager.currentTestMode == FactoryTestManager.TestMode.MODE_AUTO_TEST) {
			if (imei != null && iccid !=null && strength > -95 && isSimCardAccess() && isMobileConnected(getApplicationContext())) {
                if(simChannel==-1 || switchsim) {//单卡或者双卡测试完成
                    synchronized (this) {
                        stopAutoTest(true);
                    }
                } else {
                    iccid = null;
                    preStrength = 0;
                    strength = 0;
                    switchsim = true;
                    switchSIM.performClick();
                }
			}

			if (preStrength != 0 && Math.abs(preStrength - strength) > 15) {
				changeResult(false);
			}
			preStrength = strength;
		}
		return true;
	}

	public static boolean isMobileConnected(Context context) {
        boolean connected = false;
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            connected = (mInternetNetWorkInfo!=null) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
		}
		return connected;
	}
}
