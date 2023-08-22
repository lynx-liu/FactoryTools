package com.android.factorytest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class EthernetActivity extends TestItemBaseActivity {

	private TextView txtMAC;
	private TextView txtIP;
	private TextView txtState;

	private final int AUTO_TEST_TIMEOUT = 3;// 自动测试超时时间
	private final int AUTO_TEST_MINI_SHOW_TIME = 2;// 自动测试超时时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.ethernet_layout);
		txtMAC = (TextView) findViewById(R.id.txt_mac);
		txtIP = (TextView) findViewById(R.id.txt_ip);
		txtState = (TextView) findViewById(R.id.txt_state);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkEthernetState();
	}

	@Override
	void executeAutoTest() {
		super.startAutoTest(AUTO_TEST_TIMEOUT, AUTO_TEST_MINI_SHOW_TIME);
	}

	private void checkEthernetState() {
		String name = "eth0";
		String mac = getMacAddress(name);
		String ip = getIpAddress(name);
		boolean state = isEthernetConnected(this);

		if (mac!=null) {
			txtMAC.setText(mac);
			txtMAC.setTextColor(Color.GREEN);
		} else {
			txtMAC.setText(R.string.not_available);
		}

		if (ip != null) {
			txtIP.setText(ip);
			txtIP.setTextColor(Color.GREEN);
		} else {
			txtIP.setText(R.string.unknown);
		}

		if(state) {
			txtState.setText(R.string.normal);
			txtState.setTextColor(Color.GREEN);
			synchronized (this) {
				stopAutoTest(true);
			}
		} else {
			txtState.setText(R.string.abnormal);
			synchronized (this) {
				stopAutoTest(false);
			}
		}
	}

	private String getMacAddress(String name) {
		BufferedReader reader = null;
		String macAddress = null;
		String path = "sys/class/net/"+name+"/address";
		try {
			reader = new BufferedReader(new FileReader(path));
			macAddress = reader.readLine();
			if (macAddress != null && macAddress.trim().length() == 0) {
				macAddress = null;
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
		return macAddress;
	}

	/*
	 * eg: eth0, wlan0
	 */
	public static String getIpAddress(String name) {
		String hostIp = null;
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			InetAddress ia = null;
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();

				if (ni.getName().equals(name)) {
					Enumeration<InetAddress> ias = ni.getInetAddresses();
					while (ias.hasMoreElements()) {

						ia = ias.nextElement();
						if (ia instanceof Inet6Address) {
							continue;// skip ipv6
						}

						String ip = ia.getHostAddress();
						if (!"127.0.0.1".equals(ip)) {// 过滤掉127段的ip地址
							hostIp = ia.getHostAddress();
							break;
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return hostIp;
	}

	public static boolean isEthernetConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			return (mInternetNetWorkInfo!=null) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
		}
		return false;
	}
}
