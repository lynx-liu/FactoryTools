package com.android.factorytest.view;

import java.util.Iterator;

import com.android.factorytest.R;
import com.android.factorytest.manager.GPSManager;
import com.android.factorytest.utils.CommonUtils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GPSView extends View implements Listener,
		LocationListener {

	private float graphWidth;
	private float graphHeigth;
	private float padingLeft = 20;
	private float padingRight = 20;
	private float padingTop = 100;
	private float padingBottom = 80;
	private int yLineNumber = 3;// y轴刻度数
	private int maxStarNumber = 13;// 最大星粒數
	private float singalBarWidth = 0;
	private float singalGap = 10;// 相邻柱状图的间距
	private float singalViewScale = 0.6f;
	float colorBarPadingTop = 40;
	float colorBarpadingLeft = 15;
	private float colorBarWidth = 30;// 色帶寬度
	private boolean isButtonPress = false;
	private RectF buttonRect;
	private RectF mainRect;
	private RectF dataRect;
	private Rect nemaRect;
	private int[] gps_snr = {0, 10, 20, 30, 40, 99};
	private int[] colorList = {mx_color_red, mx_color_org, mx_color_yellow,
			mx_color_green_light, mx_color_green_dark};
	static int mx_color_grap;
	static int mx_color_red;
	static int mx_color_org;
	static int mx_color_yellow;
	static int mx_color_green_light;
	static int mx_color_green_dark;
	private GPSManager gpsManager;
	private LocationManager locationManager;
	private String nemaData = "";
	private double longitude;
	private double latitude;
	private GpsStatus gpsStatus = null;
	private int satelliteInFixCount = 0;// 定到位的卫星数
	private int satelliteCount = 0;// 搜索到的卫星数
	private int timeFix = 0;
	private boolean hasFix = false;
	private String prenema;
	private boolean isNemaChange;
	private NmeaListener nmeaListener;
	private OnNmeaMessageListener nmeaMessageListener;
	private AutoTestCallBack autoTestCallBack;

	static {
		mx_color_grap = Color.rgb(192, 192, 192);
		mx_color_red = Color.rgb(255, 0, 0);
		mx_color_org = Color.rgb(255, 128, 0);
		mx_color_yellow = Color.rgb(255, 255, 0);
		mx_color_green_light = Color.rgb(217, 255, 0);
		mx_color_green_dark = Color.rgb(0, 255, 0);
	}

	private final static int MSG_COUNT = 0x01;

	@TargetApi(Build.VERSION_CODES.N)
	public GPSView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		setBackgroundColor(Color.BLACK);

		gpsManager = new GPSManager(context);
		locationManager = ((LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(context,"request permission.ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
				return;
			}
		}

        gpsStatus = locationManager.getGpsStatus(null);
		locationManager.addGpsStatusListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			nmeaMessageListener = new OnNmeaMessageListener() {
				@Override
				public void onNmeaMessage(String nmea, long timestamp) {
					nemaData = nmea;
					if (!nmea.equals(prenema)) {
						isNemaChange = true;
					}
					prenema = nmea;
					invalidate();
				}
			};
			locationManager.addNmeaListener(nmeaMessageListener);
		} else {
			nmeaListener = new NmeaListener() {
				@Override
				public void onNmeaReceived(long timestamp, String nmea) {
					nemaData = nmea;
					if (!nmea.equals(prenema)) {
						isNemaChange = true;
					}
					prenema = nmea;
					invalidate();
				}
			};
			locationManager.addNmeaListener((OnNmeaMessageListener) nmeaListener);
		}

		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 0.0f, this);
		}catch (Exception e){
			Log.d("llx",e.toString());
            timeFix = -1;//timeFix为－1表示GPS模块异常，可根据此值提前结束自动测试
		}
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsManager.toggleGPS();
        }
        mHandler.sendEmptyMessageDelayed(MSG_COUNT,1000);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_COUNT:
					if (hasFix) {
						mHandler.removeMessages(MSG_COUNT);
					} else {
					    if(timeFix<0) {
                            if(autoTestCallBack!=null) autoTestCallBack.autoTestResult(hasFix);//用于回调自动测试结果
                        }
						timeFix++;
						invalidate();
						mHandler.sendEmptyMessageDelayed(MSG_COUNT,1000);
					}
					break;
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		drawGridBg(canvas);
		drawColorBar(canvas);
		drawStarSingal(canvas);
		drawData(canvas);

		super.onDraw(canvas);
	}

	/**
	 * 绘制卫星数据
	 *
	 * @param canvas
	 */
	private void drawData(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizes));

		float textHeight = paint.measureText("正");
		// 卫星数目
		canvas.drawText("Count:" + satelliteCount, mainRect.left + 5, mainRect.top + textHeight, paint);

		// 定位卫星数
		String text = "Fix:" + satelliteInFixCount;
		float textWidth = paint.measureText(text);
		canvas.drawText(text, mainRect.right - textWidth - 5, mainRect.top + textHeight, paint);

		dataRect = new RectF(mainRect.right + colorBarpadingLeft * 8,
				mainRect.top, mainRect.left, mainRect.bottom);// 频繁刷新区域
		// 经纬度

		String lon = "0.0";
		String lat = "0.0";
		if (longitude != 0) {
			lon = CommonUtils.keep6Point(longitude)
					+ (longitude > 0 ? "E" : "W");
		}
		if (latitude != 0) {
			lat = CommonUtils.keep6Point(latitude) + (latitude > 0 ? "N" : "S");
		}
		canvas.drawText(getContext().getString(R.string.longitude) + lon, dataRect.left -getResources().getDimension(R.dimen.GPSdataRectLeft),
				(float) (mainRect.top + mainRect.height() * 0.3), paint);
		canvas.drawText(getContext().getString(R.string.latitude) + lat, dataRect.left-getResources().getDimension(R.dimen.GPSdataRectLeft), (float) (mainRect.top
				+ mainRect.height() * 0.3 + textHeight + 3), paint);

		// 定位时间
		String str = getContext().getString(R.string.positioning_time);// String.format("%02d", snr);
		textWidth = paint.measureText(str);
		float x = dataRect.left -getResources().getDimension(R.dimen.GPSdataRectLeft);
		float y = (float) (mainRect.top + mainRect.height() * 0.7);
		drawText(canvas, str, x, y, paint, 0);
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizem));
		String str_time = timeFix + "";
		float str_time_width = paint.measureText(str_time);
		canvas.drawText(str_time, x + textWidth / 2 - str_time_width / 2,
				y + 60, paint);

		// nema数据
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizes));
		if (nemaRect == null) {
			nemaRect = new Rect((int) mainRect.left, (int)getResources().getDimension(R.dimen.GPSnemaRect), (int) mainRect.right,
					(int) ((int)getResources().getDimension(R.dimen.GPSnemaRect) + textHeight));
		}
		canvas.drawText(nemaData.toCharArray(), 0, nemaData.length() > 40 ? 40
				: nemaData.length(), nemaRect.left, nemaRect.top, paint);

		// gps开关
		int buttonHeight = (int)getResources().getDimension(R.dimen.GPSbuttonHeight);
		int buttonWidth = (int)getResources().getDimension(R.dimen.GPSbuttonWidth);
		float buttonPadingTop = 20;
		if (!isButtonPress) {
			paint.setColor(Color.rgb(136, 136, 136));
		} else {
			paint.setColor(Color.rgb(80, 80, 80));
		}
		buttonRect = new RectF(dataRect.left -(int)getResources().getDimension(R.dimen.GPSdataRectLeft), buttonPadingTop, dataRect.left
				+ buttonWidth, buttonPadingTop + buttonHeight);
		canvas.drawRoundRect(buttonRect, 10, 10, paint);

		paint.setColor(Color.DKGRAY);
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizes));
		canvas.drawText(getContext().getString(R.string.gps_status), buttonRect.left + 3, buttonRect.top
				+ textHeight, paint);

		int circleRadio = (int)getResources().getDimension(R.dimen.GPScircleRadio);
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizem));
		textHeight = paint.measureText(getContext().getString(R.string.positive));
		boolean gpsEnable = gpsManager.isGPSEnable();
		String label = getContext().getString(gpsEnable ? R.string.on : R.string.off);
		int color = gpsEnable ? Color.GREEN : Color.RED;
		paint.setColor(color);
		canvas.drawCircle((float) (buttonRect.left + buttonRect.width() * 0.2),
				buttonRect.top + buttonRect.height() / 2 + circleRadio,
				circleRadio, paint);
		canvas.drawText(label,
				(float) (buttonRect.left + buttonRect.width() * 0.5),
				buttonRect.top + buttonRect.height() / 2 + circleRadio / 2
						+ textHeight / 2, paint);
	}

	@Override
	protected void finalize() throws Throwable {
		stopGPS();
		super.finalize();
	}

	@TargetApi(Build.VERSION_CODES.N)
	public void stopGPS(){
		mHandler.removeMessages(MSG_COUNT);
		locationManager.removeGpsStatusListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locationManager.removeNmeaListener(nmeaMessageListener);
		} else {
			locationManager.removeNmeaListener((OnNmeaMessageListener) nmeaListener);
		}
		locationManager.removeUpdates(this);
		if (locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			gpsManager.toggleGPS();
		}
	}

	public void coldStart() {
		mHandler.removeMessages(MSG_COUNT);
        locationManager.sendExtraCommand("gps", "force_xtra_injection", null);
        locationManager.sendExtraCommand("gps", "force_time_injection", null);
        locationManager.sendExtraCommand("gps", "delete_aiding_data", null);

		longitude = 0;
		latitude = 0;
		satelliteInFixCount = 0;
		satelliteCount = 0;
		timeFix = 0;
		hasFix = false;
		prenema = null;
		isNemaChange = false;
		invalidate();
		mHandler.sendEmptyMessageDelayed(MSG_COUNT,1000);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("test", event.getAction() + "--------------------");
		switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				if (buttonRect != null) {
					if (buttonRect.contains(event.getX(), event.getY())) {
						isButtonPress = true;
//					gpsManager.toggleGPS();
						Rect rect = new Rect();
						buttonRect.round(rect);
						invalidate(rect);
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:

			case MotionEvent.ACTION_UP:
				if (isButtonPress) {
					isButtonPress = false;
					Rect rect = new Rect();
					buttonRect.round(rect);
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
					invalidate(rect);
				}
				break;
			default:
				break;
		}

		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {

		graphWidth = getWidth();
		graphHeigth = getHeight();
		super.onLayout(changed, left, top, right, bottom);
	}

	void drawText(Canvas canvas, String text, float x, float y, Paint paint,
				  float angle) {
		if (angle != 0) {
			canvas.rotate(angle, x, y);
		}
		canvas.drawText(text, x, y, paint);
		if (angle != 0) {
			canvas.rotate(-angle, x, y);
		}
	}

	/**
	 * 画卫星信号
	 *
	 * @param canvas
	 */
	private void drawStarSingal(Canvas canvas) {
		if (gpsStatus == null) {
			return;
		}
		// 柱状图宽度
		singalBarWidth = (mainRect.right - mainRect.left) / maxStarNumber - singalGap;

		Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
		float offset = singalGap;
		satelliteInFixCount = 0;
		satelliteCount = 0;
		while (iterator.hasNext()) {
			GpsSatellite satellite = iterator.next();
			if(satellite.getSnr()>=0) {// 过滤掉snr小于0的卫星
				boolean isFix = satellite.usedInFix();// 是否定到位
				satelliteCount++;
				if (isFix) {
					satelliteInFixCount++;
				}
				if (satelliteCount <= maxStarNumber) {
					drawSingleStarInfo(offset, satellite.getSnr(), satellite.getPrn(), isFix, canvas);
					offset += singalBarWidth + singalGap;
				}
			}
		}
	}

	/**
	 * 画单个卫星信号
	 */
	private void drawSingleStarInfo(float xOffset, float snr, int pnr,
									boolean isInFix, Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.FILL_AND_STROKE);

		// 画柱状图
		if (!isInFix) {
			paint.setColor(mx_color_grap);
		} else {
			if (snr <= gps_snr[1]) {
				paint.setColor(mx_color_red);
			} else if (snr <= gps_snr[2]) {
				paint.setColor(mx_color_org);
			} else if (snr <= gps_snr[3]) {
				paint.setColor(mx_color_yellow);
			} else if (snr <= gps_snr[4]) {
				paint.setColor(mx_color_green_light);
			} else if (snr <= gps_snr[5]) {
				paint.setColor(mx_color_green_dark);
			} else {
				paint.setColor(mx_color_grap);
			}
		}

		float barHeight = (float) ((mainRect.height() * 0.9 / (gps_snr[gps_snr.length - 1])) * snr);
		int minBarHeight = 4;
		if (barHeight < minBarHeight) {
			barHeight = minBarHeight;
		}
		RectF startBarRect = new RectF(mainRect.left + xOffset, mainRect.bottom
				- barHeight, mainRect.left + xOffset + singalBarWidth,
				mainRect.bottom);
		canvas.drawRect(startBarRect, paint);

		// 画pnr值
		paint.setColor(Color.DKGRAY);
		paint.setTextSize(getResources().getDimension(R.dimen.GPSTextSizes));
		String pnrText = String.format("%02d", pnr);
		float textWidth = paint.measureText(pnrText);
		float textX = (startBarRect.right + startBarRect.left) / 2 - textWidth / 2;
		canvas.drawText(pnrText, textX, startBarRect.bottom + 30, paint);

		// 画snr值
		String snrText = (int) snr + "";// String.format("%02d", snr);
		textWidth = paint.measureText(snrText);
		textX = (startBarRect.right + startBarRect.left) / 2 - textWidth / 2;
		canvas.drawText(snrText, textX, startBarRect.top - 10, paint);
	}

	private void drawColorBar(Canvas canvas) {

		// 画色带
		float colorBarHeight = (float) ((mainRect.height()) * 0.85);
		float unit = colorBarHeight
				/ (gps_snr[gps_snr.length - 1] - gps_snr[0]);

		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setTextSize(getResources().getDimension(R.dimen.GPSTextSizes));

		float beginY = 0;
		float currentColorBarHeight = 0;

		for (int i = gps_snr.length - 1; i > 0; i--) {

			if (i > 0) {
				currentColorBarHeight = (float) ((gps_snr[i] - gps_snr[i - 1]) * unit);
				p.setColor(colorList[i - 1]);
				RectF colorRect = new RectF(
						mainRect.right + colorBarpadingLeft, mainRect.top
						+ colorBarPadingTop + beginY, mainRect.right
						+ colorBarpadingLeft + colorBarWidth,
						mainRect.top + colorBarPadingTop + beginY
								+ currentColorBarHeight);
				canvas.drawRect(colorRect, p);

				// 写刻度
				String text = gps_snr[i] + "";
				float textHeight = p.measureText(text);
				canvas.drawText(text, colorRect.right + 5, colorRect.top
						+ textHeight, p);
				beginY += currentColorBarHeight;
			}

		}
		// 画文字说明
		p.setColor(Color.DKGRAY);
		String text = "SNR";
		float textHeight = p.measureText(text);
		canvas.drawText("SNR", mainRect.right + colorBarpadingLeft,
				mainRect.top + textHeight / 2, p);// SNR

	}

	private void drawGridBg(Canvas canvas) {
		padingTop = getResources().getDimension(R.dimen.GPSpadingTop);
		padingBottom = getResources().getDimension(R.dimen.GPSpadingBottom);
		// 画外框
		mainRect = new RectF(padingLeft, padingTop, graphWidth
				* singalViewScale - padingRight, graphHeigth - padingBottom);
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(mainRect, paint);

		// 画内线
		float gap = mainRect.height() / (yLineNumber + 1);
		PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
		paint.setPathEffect(effects);
		for (int i = 0; i < yLineNumber; i++) {
			Path path = new Path();
			path.moveTo(mainRect.left, mainRect.top + (i + 1) * gap);
			path.lineTo(mainRect.right, mainRect.top + (i + 1) * gap);
			canvas.drawPath(path, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                gpsStatus = locationManager.getGpsStatus(null);
			    if(isNemaChange&&((satelliteInFixCount>0)&&satelliteCount>=3)) {
                    hasFix = true;
                    timeFix = gpsStatus.getTimeToFirstFix()/1000;
                    if(autoTestCallBack!=null) autoTestCallBack.autoTestResult(hasFix);//用于回调自动测试结果
				}
				invalidate();

				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
                gpsStatus = locationManager.getGpsStatus(null);
                if(gpsStatus!=null)
                    timeFix = gpsStatus.getTimeToFirstFix()/1000;
				hasFix = true;
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				hasFix = false;
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				hasFix = false;
				break;
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		invalidate();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		gpsStatus = locationManager.getGpsStatus(null);
		if (gpsStatus != null)
			timeFix = gpsStatus.getTimeToFirstFix()/1000;
		invalidate();
	}

	@Override
	public void onProviderEnabled(String provider) {
		invalidate();

	}

	@Override
	public void onProviderDisabled(String provider) {
		invalidate();

	}

	public void setAutoTestCallBack(AutoTestCallBack callBack){
		autoTestCallBack=callBack;
	}
	public interface AutoTestCallBack{
		public void autoTestResult(boolean result);
	}
}
