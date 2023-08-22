package com.android.factorytest;

import com.android.factorytest.manager.SystemUtils;
import com.android.factorytest.utils.CommonUtils;
import com.android.factorytest.utils.DialogUtils;
import com.android.factorytest.utils.QREncode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SysteminfoActivity extends TestItemBaseActivity {
	private TextView devSN = null;
	private TextView kerVer = null;
	private TextView sysVer = null;
	private TextView softVer = null;
	private TextView basebandVer = null;
	private ImageView ivQRcode = null;
	private EditText etFilename = null;
	private Button btnUpdate = null;
	private ProgressBar prgUpdate = null;
	private static final int MSG_UPDATE_ERROR = 0;
	private static final int MSG_UPDATE_TEXT = 1;
	private static final int MSG_UPDATE_PROGRESS = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.systeminfo_layout);
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
				finish();
			}
		}

		devSN = (TextView) findViewById(R.id.tv_device_sn);
		kerVer = (TextView) findViewById(R.id.tv_kernel_version);
		sysVer = (TextView) findViewById(R.id.tv_system_version);
		softVer = (TextView) findViewById(R.id.tv_software_version);
		basebandVer = (TextView) findViewById(R.id.tv_baseband_version);
        ivQRcode = (ImageView) findViewById(R.id.iv_qrcode);

		String sn = SystemUtils.getSystemProperties("ro.serialno");
		devSN.setText(sn==null?getString(R.string.unknown):sn);//Build.SERIAL
        if(sn!=null) ivQRcode.setImageBitmap(QREncode.getQRcode(sn.toCharArray(),5));//不滤波放大5倍，使二维码看起来更清晰

		kerVer.setText(SystemUtils.getVersion()[0]);
		sysVer.setText(Build.DISPLAY);
		softVer.setText(SystemUtils.getSoftWareVersion(getApplicationContext()));
		basebandVer.setText(SystemUtils.getSystemProperties("gsm.version.baseband"));

		etFilename = (EditText)findViewById(R.id.et_filename);
		etFilename.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DialogUtils.selectFile(SysteminfoActivity.this/*注：不能传getApplicationContext()*/, new DialogUtils.DialogSelection() {
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
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Message msg = new Message();
							msg.obj = getString(R.string.wait_for_copy);
							msg.what = MSG_UPDATE_TEXT;
							mHandler.sendMessage(msg);

							File packageFile = new File("/data/ota_package/update.zip");
							CommonUtils.copyFile(new File(etFilename.getText().toString()), packageFile);

							msg = new Message();
							msg.obj = getString(R.string.wait_for_verify);
							msg.what = MSG_UPDATE_TEXT;
							mHandler.sendMessage(msg);

							RecoverySystem.verifyPackage(packageFile, new RecoverySystem.ProgressListener() {
								@Override
								public void onProgress(int i) {
									Log.d("llx","onProgress:"+i);
									Message msg = new Message();
									msg.obj = Integer.valueOf(i);
									msg.what = MSG_UPDATE_PROGRESS;
									mHandler.sendMessage(msg);
								}
							}, null);

							msg = new Message();
							msg.obj = getString(R.string.wait_for_upgrade);
							msg.what = MSG_UPDATE_TEXT;
							mHandler.sendMessage(msg);

							RecoverySystem.installPackage(getApplicationContext(), packageFile);
						} catch (Exception e) {
							Message msg = new Message();
							msg.obj = e.toString();
							msg.what = MSG_UPDATE_ERROR;
							mHandler.sendMessage(msg);
						}
					}
				}).start();
			}
		});

		prgUpdate = (ProgressBar)findViewById(R.id.prg_update);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_UPDATE_ERROR:
					String error = (String)msg.obj;
					Toast.makeText(getApplicationContext(),getString(R.string.upgrade_fail)+": "+error,Toast.LENGTH_LONG).show();
					btnUpdate.setEnabled(true);
					break;

				case MSG_UPDATE_TEXT:
					String text = (String)msg.obj;
					Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
					break;

				case MSG_UPDATE_PROGRESS:
					if(prgUpdate.getVisibility()!=View.VISIBLE)
						prgUpdate.setVisibility(View.VISIBLE);
					int progress = (Integer)msg.obj;
					prgUpdate.setProgress(progress);
					break;

				default:
					break;
			}
		}
	};
}
