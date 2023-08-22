package com.android.factorytest.manager;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;

public class GPSManager {

    private Context mContext;
    private LocationManager mLocationManager;
    public GPSManager(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.mContext=context;


    }

    public boolean isGPSEnable(){
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    /**
     * 切换GPS状态
     */
    public void toggleGPS() {

        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
        }
        catch (CanceledException e) {
            e.printStackTrace();
        }
    }
}
