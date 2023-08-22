package com.android.factorytest;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.factorytest.utils.InputEvent;

import java.io.IOException;

public class KeysActivity extends TestItemBaseActivity {

	private TextView txtVolumnAdd;
	private TextView txtVolumnDesc;
	private TextView txtPower;
	private TextView txtKeyF1;
	private TextView txtKeyF2;
    private InputEvent inputEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.keys_layout);
		super.onCreate(savedInstanceState);

		txtVolumnAdd = (TextView) findViewById(R.id.txt_volumn_add);
		txtVolumnDesc = (TextView) findViewById(R.id.txt_volumn_des);
		txtPower = (TextView) findViewById(R.id.txt_power);

		txtKeyF1 = (TextView) findViewById(R.id.txt_key131);
		txtKeyF2 = (TextView) findViewById(R.id.txt_key132);
        if(!Build.DISPLAY.contains("PD101B")) {
            findViewById(R.id.ll_key131).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_key132).setVisibility(View.VISIBLE);
        }

        try {
            inputEvent = new InputEvent(new InputEvent.onInputEventListener() {
                @Override
                public boolean onKeyEvent(int keycode, long keyevent) {
                    if(keycode==InputEvent.KEY_POWER) {
                        Log.d("llx","KEY_POWER "+(keyevent==InputEvent.DOWN?"DOWN":"UP"));
                        txtPower.setText(R.string.is_right);
                        txtPower.setTextColor(Color.GREEN);
                    }
                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(inputEvent!=null) {
            inputEvent.Release();
        }
        super.onDestroy();
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP||keyCode==265) {
			txtVolumnAdd.setText(R.string.is_right);
			txtVolumnAdd.setTextColor(Color.GREEN);
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN||keyCode==264) {
			txtVolumnDesc.setText(R.string.is_right);
			txtVolumnDesc.setTextColor(Color.GREEN);
		} else if(keyCode == KeyEvent.KEYCODE_F1) {
			txtKeyF1.setText(R.string.is_right);
			txtKeyF1.setTextColor(Color.GREEN);
		} else if(keyCode == KeyEvent.KEYCODE_F2) {
			txtKeyF2.setText(R.string.is_right);
			txtKeyF2.setTextColor(Color.GREEN);
		}
		return super.onKeyDown(keyCode, event);
	}

}
