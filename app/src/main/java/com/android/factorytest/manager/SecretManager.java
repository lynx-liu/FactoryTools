package com.android.factorytest.manager;

import java.lang.reflect.Method;

public class SecretManager {
	//判断是否有运行软件的权限
	public static boolean isSoftWareAuthority() {
		return true;
	}
	
	private static String getSystemProperties(String field)
	{
		String platform = null;
		try {
			Class<?> classType = Class.forName("android.os.SystemProperties");
			Method getMethod = classType.getDeclaredMethod("get",
					new Class<?>[] { String.class });
			platform = (String) getMethod.invoke(classType,
					new Object[] { field });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return platform;
	}
}
