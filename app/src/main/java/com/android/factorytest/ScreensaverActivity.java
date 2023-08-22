package com.android.factorytest;

import com.android.factorytest.view.BallView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ScreensaverActivity extends Activity {

	private BallView ballView = null;
    private MediaPlayer mMediaPlayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        ballView = new BallView(this,null);
        setContentView(ballView);
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility==View.VISIBLE) {//AlertDialog会使隐藏的虚拟键显示,此处监听虚拟键显示之后,关闭AlertDialog后恢复隐藏状态
                    View decorView = getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        });
    }
    
    @Override
    protected void onResume() {
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
    	super.onResume();

        try {
            mMediaPlayer = MediaPlayer.create(this, R.raw.whistle);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        if(mMediaPlayer!=null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction()==MotionEvent.ACTION_DOWN) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScreensaverActivity.this);
            builder.setTitle(getString(R.string.exit_burn));
            builder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which) {
                    finish();
                }
            });

            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
    	}
    	return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		onBackPressed();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
}