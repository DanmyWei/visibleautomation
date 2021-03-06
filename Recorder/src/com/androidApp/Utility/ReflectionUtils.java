package com.androidApp.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.util.Log;
import android.view.View;

/**
 * utilities to access and set fields in objects via reflection
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ReflectionUtils {
	protected final static String TAG = "ReflectionUtils";

	// handy debug function when extracting fields from objects
	public static void listFieldsDebug(Class cls) {
		Field fields[] = cls.getDeclaredFields();
		for (Field field : fields) {
			Log.i(TAG, "field name = " + field.getName());
		}
	}

	/**
	 * given an object, its class, a fieldName and a flag, set the value of that field to the flag
	 * @param o our intended victim
	 * @param c object class (proletariat, bourgeois, or plutocrat)
	 * @param fieldName name of the field (it better match)
	 * @param flag value to set
	 * @throws NoSuchFieldException the field didn't match anything the class had
	 * @throws IllegalAccessException I hope this never happens
	 */	
	public static void setFieldBooleanValue(Object o, Class c, String fieldName, boolean flag) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.setBoolean(o, flag);
	}

	/**
	 * given an object, its class, a fieldName and a value, set the value of that field to the object
	 * @param o our intended victim
	 * @param c object class (proletariat, bourgeois, or plutocrat)
	 * @param fieldName name of the field (it better match)
	 * @param value value to set
	 * @throws NoSuchFieldException the field didn't match anything the class had
	 * @throws IllegalAccessException I hope this never happens
	 */
	public static void setFieldValue(Object o, Class c, Constants.Fields fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName.mName);
		field.setAccessible(true);
		field.set(o, value);
	}

	/**
	 * get a field that we expect to return a boolean value
	 * @param o
	 * @param c
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static boolean getFieldBoolean(Object o, Class c, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.getBoolean(o);
	}

	/**
	 * variant which takes a Constants.Fields
	 * @param o
	 * @param c
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static boolean getFieldBoolean(Object o, Class c, Constants.Fields fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName.mName);
		field.setAccessible(true);
		return field.getBoolean(o);
	}

	public static String getFieldPath(Stack<Class> breadcrumb, Field field) {
		StringBuffer sb = new StringBuffer();
		for (int i = breadcrumb.size() - 1; i >= 0; i--) {
			Class c = breadcrumb.get(i);
			sb.append(c.getCanonicalName());
			sb.append('.');
		}
		sb.append(field.getName());
		return sb.toString();
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
	public static Object getFieldValue(Object o, Class c, Constants.Fields fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName.mName);
		field.setAccessible(true);
		return field.get(o);
	}
	
	/**
	 * variant which just takes a field name with no backup
	 * @param o
	 * @param c
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static Object getFieldValue(Object o, Class c, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = c.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(o);
	}

	public static boolean inBreadcrumb(Stack<Class> breadcrumb, Class c) {
		for (Class cCand : breadcrumb) {
			if (cCand == c) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Given an object and a class, return all the fields in that class and any object that it contains which
	 * are of that class (used only in testing, this should not be in the shipped product).  
	 * It uses a "breadcrumb trail" to prevent circular references, but I've not tested it in practices
	 * @param o
	 * @param c
	 * @return
	 * @throws IllegalAccessException
	 */
	public static List<String> getMatchingFieldsByTypeRecursively(Object o, Class c) throws IllegalAccessException {
		Stack<Class> breadcrumb = new Stack<Class>();
		breadcrumb.push(o.getClass());
		List<String> result = new ArrayList<String>();
		getMatchingFieldsByTypeRecursively(o, o.getClass(), c, breadcrumb, result);
		return result;
	}

	public static void getMatchingFieldsByTypeRecursively(Object o, Class objectClass, Class c, Stack<Class> breadcrumb, List<String> result) throws IllegalAccessException {
		while ((objectClass != null) && (objectClass != Object.class)) {
			Field fields[] = objectClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getType() == c) {
					result.add(getFieldPath(breadcrumb, field));
				} else if (!field.getType().isPrimitive() && (field.getType() != String.class)) {
					field.setAccessible(true);
					Object fieldValue = field.get(o);
					if ((fieldValue != null) && !inBreadcrumb(breadcrumb, field.getType())) {
						breadcrumb.push(field.getType());
						getMatchingFieldsByTypeRecursively(fieldValue, field.getType(), c, breadcrumb, result);
						breadcrumb.pop();
					}
				}
			}
			objectClass = objectClass.getSuperclass();
		}
	}
	
	/**
	 * invoke a method on a class with parameters.
	 * @param object
	 * @param cls
	 * @param methodName
	 * @param args
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static boolean execMethodBoolean(Object object, Class cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class[] parameterTypes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = args.getClass();
		}
		Method method = cls.getDeclaredMethod(methodName, parameterTypes);
		Boolean b = (Boolean) method.invoke(object, args);
		return b.booleanValue();
	}
	
	/**
	 * given a method name, find the class in which it is defined.  This is used to find the first ancestor which
	 * defines this method.  For example, we want to find out if someone overrode the android onClick() method to 
	 * determine if we want to attach an OnClickListener to this object
	 * @param object object to search up from
	 * @param methodName name of the method
	 * @param types for the method parameters.
	 * @return
	 */
	public static Class getClassForMethod(Object object, String methodName, Class ... methodParameters) {
		Class c = object.getClass();
		while (c != Object.class) {
			try {
				Method method = c.getDeclaredMethod(methodName, methodParameters);
				if (method != null) {
					return c;
				}
			} catch (NoSuchMethodException nsmex) {
			}
			c = c.getSuperclass();
		}
		return null;
	}
}
