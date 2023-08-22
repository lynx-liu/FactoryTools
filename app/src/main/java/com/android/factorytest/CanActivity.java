package com.android.factorytest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.manager.PdsManager;

public class CanActivity extends TestItemBaseActivity {
	private EditText mMessage = null;
	private PdsManager pdsManager = null;
    private SharedPreferences preferences = null;
    private Spinner spChannel = null;
    private Spinner spBaud = null;
    private static final String ChannelKey = "Channel";
    private int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setContentView(R.layout.can_layout);
		super.onCreate(savedInstanceState);
		
		mMessage = (EditText)findViewById(R.id.et_message);

        preferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        int channel = preferences.getInt(ChannelKey, 0);
        final String[] channels = {"CAN1", "CAN2"};
        ArrayAdapter<String> channelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, channels);
        spChannel = (Spinner) findViewById(R.id.sp_channel);
        spChannel.setAdapter(channelAdapter);
        spChannel.setSelection(channel);
        spChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                preferences.edit().putInt(ChannelKey, pos).apply();
                int channel = spChannel.getSelectedItemPosition() + 1;
                int baud = pdsManager.getCanBand((byte) channel);
                spBaud.setSelection(baud, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final String[] bauds = {"50K", "100K", "125K", "250K", "500K", "1M"};
        ArrayAdapter<String> baudAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bauds);
        spBaud = (Spinner) findViewById(R.id.sp_baud);
        spBaud.setAdapter(baudAdapter);
        spBaud.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                int channel = spChannel.getSelectedItemPosition() + 1;
                if (pdsManager.setCanBaudRate((byte) channel, (byte) pos)) {
                    updateText(channels[channel-1]+" baud "+bauds[pos]+" set success");
                } else {
                    updateText(channels[channel-1]+" baud "+bauds[pos]+" set failed");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        pdsManager = new PdsManager(getApplicationContext(), new PdsManager.PdsManagerListener() {
            @Override
            public void onServiceConnection() {
                int channel = spChannel.getSelectedItemPosition() + 1;
                int baud = pdsManager.getCanBand((byte) channel);
                spBaud.setSelection(baud, true);
            }

            @Override
            public void onServiceDisconnected() {

            }

            @Override
            public boolean onAccNotify(byte state) {
                return false;
            }

            @Override
            public void onCanData(int channel, long id, boolean id_extend, boolean frame_remote, byte[] data) {
                StringBuilder stringBuilder = new StringBuilder(String.format("%05d",++count)+" [通道"+channel+"] "+(id_extend?"[扩展帧]":"[标准帧]")+" "+(frame_remote?"[远程帧]":"[数据帧]")+" id:[0x"+String.format("%08X",id)+"] "+" size:" + data.length +", data: ");
                for (int i = 0; i < data.length; i++)
                    stringBuilder.append(String.format("%02X ", data[i]));
                updateText(stringBuilder.toString());
            }
        });
	}

    private void updateText(final String string) {
        if (mMessage != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if(mMessage.getLineCount()>5000){
                		mMessage.setText(string+"\n");
                		mMessage.setSelection(mMessage.getText().length());
                	} else mMessage.append(string + "\n");
                }
            });
        }
    }
}
