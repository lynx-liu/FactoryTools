package com.android.factorytest.manager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class GpioManager {

    boolean outputio1 = false;
    boolean outputio2 = false;
    boolean outputio3 = false;
    boolean gpspowerctl = false;
    boolean gpsresetctl = false;

	public enum GPIO{OutputIO1,OutputIO2,OutputIO3,GPS_POWER_CTL,GPS_RESET_CTL,InputIO0,InputIO1,InputIO2;}

	public GpioManager() {
        outputio1 = getEnable(GPIO.OutputIO1);
        outputio2 = getEnable(GPIO.OutputIO2);
        outputio3 = getEnable(GPIO.OutputIO3);
        gpspowerctl = getEnable(GPIO.GPS_POWER_CTL);
        gpsresetctl = getEnable(GPIO.GPS_RESET_CTL);
	}

	public void recovery() {
        setEnabled(GPIO.OutputIO1,outputio1);
        setEnabled(GPIO.OutputIO2,outputio2);
        setEnabled(GPIO.OutputIO3,outputio3);
        setEnabled(GPIO.GPS_POWER_CTL,gpspowerctl);
        setEnabled(GPIO.GPS_RESET_CTL,gpsresetctl);
    }

    public boolean setEnabled(GPIO gpio, boolean enabled){
		try {
			FileOutputStream mFileOutputStream = new FileOutputStream(String.format("/proc/pdss_gpio/%s", gpio.name()), false);
			mFileOutputStream.write(enabled? '1':'0');
			mFileOutputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean getEnable(GPIO gpio) {
	    boolean enable = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(String.format("/proc/pdss_gpio/%s", gpio.name())));
            String gpioState = reader.readLine();
            if (gpioState != null && gpioState.trim().length() > 0) {
                enable = (Integer.valueOf(gpioState).intValue()>0)?true:false;
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
        return enable;
    }
}
