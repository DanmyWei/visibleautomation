package com.androidApp.Utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;

import com.androidApp.Test.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * this should be folded into ReflectionUtils
 * @author Matthew
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
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

	/**
	 * given an object, its class, and a fieldName, return the value of that field for the object
	 * because there's a lot of stuff you can set in android, but you can't get it.
	 * IMPORTANT NOTE: The desired field must be a member of the specified class, not a class that it derives from. 
	 * TODO: Modify this function to iterate up the class hierarchy to find the field.
	 * @param o our intended victim
	 * @param c object class (proletariat, bourgeois, or plutocrat)
	 * @param fieldName name of the field (it better match)
	 * @return
	 * @throws NoSuchFieldException the field didn't match anything the class had
	 * @throws IllegalAccessException I hope this never happens
	 */
	public static Object getFieldValue(Object o, Class c, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(o);
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
