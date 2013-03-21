package com.androidApp.util;

import java.lang.reflect.Field;

import android.util.Log;

/**
 * class which contains static functions used to obtain and set various listeners for android views, like
 * the onClickListener, onItemClickListener, etc).
 * @author Matthew
 *
 */
public class ListenerIntercept {
	protected static final String TAG = "ListenerIntercept";
	
	// handy debug function when extracting fields from objects
	public static void listFieldsDebug(Class cls) {
		Field fields[] = cls.getDeclaredFields();
		for (Field field : fields) {
			Log.i(TAG, "field name = " + field.getName());
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
}
