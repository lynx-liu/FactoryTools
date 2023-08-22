package com.android.factorytest.bean;

import java.io.InputStream;
import java.util.Properties;

import com.android.factorytest.TestItemBaseActivity;
import com.android.factorytest.manager.FactoryTestManager;

import android.content.Context;

public class TestItem {

	public Class<? extends TestItemBaseActivity>  activityCls;
	public int iconId;
	public String label;
	public String itemName;
	private boolean enable = true;
	public boolean isAutoSupport;//是否支持自动测试
	public TestItem(Class<? extends TestItemBaseActivity> activityCls, int iconId,
					String itemName, String label,boolean isAutoSupport) {

		this.activityCls = activityCls;
		this.iconId = iconId;
		this.label = label;
		this.itemName = itemName;
		this.isAutoSupport=isAutoSupport;
	}


	public boolean isEnable(Context context) {

		boolean result = true;
		try {
			InputStream fis = context.getAssets().open("factory.cfg");
			Properties properties = new Properties();
			properties.load(fis);

			if (properties.getProperty(FactoryTestManager.CONFIG_PRE_STRING + this.itemName).equals(
					"yes")) {
				result = true;
			} else {
				result = false;
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
}
