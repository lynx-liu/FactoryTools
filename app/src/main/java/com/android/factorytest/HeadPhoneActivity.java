package com.android.factorytest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 *
 * 听筒测试
 */
public class HeadPhoneActivity extends TestItemBaseActivity {

	private MediaPlayer mMediaPlayer;
	private AudioManager audioManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.headphone_layout);

		try {
			audioManager = (AudioManager) this
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_IN_CALL);// 把模式调成听筒放音模式
			mMediaPlayer = MediaPlayer.create(this, R.raw.walle);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.stop();
			mMediaPlayer.prepare();
			/* 开始播放 */
			mMediaPlayer.start();
		} catch (Exception e) {

			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onStop() {
		mMediaPlayer.stop();
        audioManager.setMode(AudioManager.MODE_NORMAL);// 把模式调成正常模式
		super.onStop();
	}

	public void volumn2Max(View view) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
		Toast.makeText(this, R.string.successful, Toast.LENGTH_SHORT).show();
	}

}
