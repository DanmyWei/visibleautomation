package com.androidApp.Utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * this should be folded into ReflectionUtils
 * resolve private classes to public classes
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class FieldUtils {
	protected final static String TAG = "FieldUtils";
	protected  HashSet<String> mWhiteList = null;
	
	// since we're converting to a jar this needs to use Class.getResourceAsStream()
	public FieldUtils(Context instrumentationContext, Context targetContext) throws IOException {
		ApplicationInfo appInfo = targetContext.getApplicationInfo();
		int targetSdkVersion = appInfo.targetSdkVersion;
		String whitelistName = "/raw/whitelist_android_" + Integer.toString(targetSdkVersion) + ".txt";
		String[] whiteListClasses = FileUtils.readJarResource(FieldUtils.class, whitelistName);
		mWhiteList = new HashSet<String>();
		for (int i = 0; i < whiteListClasses.length; i++) {
			mWhiteList.add(whiteListClasses[i]);
		}
	}
	
	// handy debug function when extracting fields from objects
	public static void listFieldsDebug(Class cls) {
		Field fields[] = cls.getDeclaredFields();
		for (Field field : fields) {
			Log.i(TAG, "field name = " + field.getName());
		}
	}
	
	// is the class an internal android class?
	public boolean isWhiteListedAndroidClass(Class cls) throws IOException {
		String canonicalName = cls.getCanonicalName();
		return mWhiteList.contains(canonicalName);
	}
	
	// for an android internal class, return the public superclass.
	public Class getPublicClassForAndroidInternalClass(Class cls) throws IOException {
		while (!isWhiteListedAndroidClass(cls)) {
			cls = cls.getSuperclass();
			Log.i(TAG, "superclass = " + cls.getCanonicalName() + " name = " + cls.getName());
		}
		return cls;
	}
}
