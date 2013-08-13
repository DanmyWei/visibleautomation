package com.androidApp.Utility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

/**
 * class to extract views from the android view hierarchy.
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ViewExtractor {
	protected static String getWindowManagerString(){
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			return Constants.Fields.WINDOW_MANAGER_FIELD_STATIC.mName;
		} else {
			return Constants.Fields.WINDOW_MANAGER_FIELD.mName;
		}
	}
	
	/**
	 *  extract the hidden WindowManagerImpl class used by the super-secret hierarchyviewer, robotium, and other nefarious software
	 */

	private static Class<?> sWindowManager;
	static {
		try {
			sWindowManager = Class.forName(Constants.Classes.WINDOW_MANAGER);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * return the views hosted by the window manager.  Hidden class WindowManagerImpl.mViews is the list of decor views.
	 * NOTE: change this to use the reflection utilities
	 * @return
	 */
	public static View[] getWindowDecorViews() {

		Field viewsField;
		Field instanceField;
		try {
			viewsField = sWindowManager.getDeclaredField(Constants.Fields.VIEWS.mName);
			instanceField = sWindowManager.getDeclaredField(getWindowManagerString());
			viewsField.setAccessible(true);
			instanceField.setAccessible(true);
			Object instance = instanceField.get(null);
			return (View[]) viewsField.get(instance);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * flatten the view hierarchy into a list.
	 * @param vg view group parent
	 * @param childList list to populate.
	 */
	public void addChildren(ViewGroup vg, List<View> childList) {
		int nChild = vg.getChildCount();
		for (int iChild = 0; iChild < nChild; iChild++) {
			View child = vg.getChildAt(iChild);
			childList.add(child);
			if (child instanceof ViewGroup) {
				addChildren((ViewGroup) child, childList);
			}
		}
	}
	
	/**
	 * recursively retrieve the views associated with an activity
	 * @param activity
	 * @return list of views (flattened hierarchy)
	 */
	public List<View> getActivityViews(Activity activity) {
		View[] decorViews = getWindowDecorViews();
		View currentDecorView = null;
		List<View> viewList = new ArrayList<View>();
		
		for (int iDecorView = 0; iDecorView < decorViews.length; iDecorView++) {
			if (decorViews[iDecorView].getContext() == activity) {
				currentDecorView = decorViews[iDecorView];
				addChildren((ViewGroup) currentDecorView, viewList);
			}
		}
		return viewList;
	}
	
	/**
	 * recursively retreive the views from an activity which inherit form a specified class
	 */
	
	public static <T extends View> List<T> getActivityViews(Activity activity, Class<T> clsFilter) {
		View[] decorViews = getWindowDecorViews();
		View currentDecorView = null;
		List<T> viewList = new ArrayList<T>();
		
		for (int iDecorView = 0; iDecorView < decorViews.length; iDecorView++) {
			if (decorViews[iDecorView].getContext() == activity) {
				currentDecorView = decorViews[iDecorView];
				addChildren((ViewGroup) currentDecorView, viewList, clsFilter);
			}
		}
		return viewList;
	}
	/**
	 * flatten the view hierarchy into a list and filter by view class
	 * @param vg view group parent
	 * @param childList list to populate.
	 */
	public static <T extends View> void addChildren(ViewGroup vg, List<T> childList, Class<T> clsFilter) {
		int nChild = vg.getChildCount();
		for (int iChild = 0; iChild < nChild; iChild++) {
			View child = vg.getChildAt(iChild);
			if (clsFilter.isAssignableFrom(child.getClass())) {
				childList.add((T) child);
			}
			if (child instanceof ViewGroup) {
				addChildren((ViewGroup) child, childList, clsFilter);
			}
		}
	}
}
