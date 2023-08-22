package com.android.factorytest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SpeakerActivity extends TestItemBaseActivity {

	private MediaPlayer mMediaPlayer;
	private boolean khz = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.speaker_layout);

		setAudioChannel(true);
		try {
			mMediaPlayer = MediaPlayer.create(this, khz?R.raw.khz:R.raw.walle);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.start();
		} catch (Exception e) {

			e.printStackTrace();  
		}
		super.onCreate(savedInstanceState);
	}

	public static void setAudioChannel(boolean state) {
		BufferedWriter writer = null;
		String path = "/sys/class/misc/sunxi-gps/rf-ctrl/audio_switch_state";
		try {
			writer = new BufferedWriter(new FileWriter(path));
			writer.write(state?'1':'0');
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
	protected void onStop() {
		mMediaPlayer.stop();
		super.onStop();
	}

	public void volumn2Max(View view) {
		AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
		Toast.makeText(this, R.string.set_ok, 0).show();
	}

	public void switchMusic(View view) {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		khz = !khz;
		try {
			mMediaPlayer = MediaPlayer.create(this, khz?R.raw.khz:R.raw.walle);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.start();
		} catch (Exception e) {

			e.printStackTrace();  
		}
		Toast.makeText(this, R.string.switch_ok, 0).show();
	}
}
