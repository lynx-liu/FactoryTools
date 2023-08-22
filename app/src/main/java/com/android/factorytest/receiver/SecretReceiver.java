package com.android.factorytest.receiver;

import com.android.factorytest.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SecretReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg) {
		if (arg.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
			Log.i("llx", "----------------过拨号1985启动--------------------");
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(context,MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);

			context.startActivity(intent);
		}

	}

}
