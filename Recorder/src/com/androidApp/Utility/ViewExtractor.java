package com.androidApp.Utility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

/**
 * class to extract views from the android view hierarchy.
 * @author Matthew
 *
 */
public class ViewExtractor {
	protected static String getWindowManagerString(){
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			return "sWindowManager";
		} else {
			return "mWindowManager";
		}
	}
	
	/**
	 *  extract the hidden WindowManagerImpl class used by the super-secret hierarchyviewer, robotium, and other nefarious software
	 */

	private static Class<?> sWindowManager;
	static {
		try {
			sWindowManager = Class.forName(Constants.Classes.WINDOW_MANAGER_CLASS);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * return the views hosted by the window manager.  Hidden class WindowManagerImpl.mViews is the list of decor views.
	 * @return
	 */
	public View[] getWindowDecorViews() {

		Field viewsField;
		Field instanceField;
		try {
			viewsField = sWindowManager.getDeclaredField(Constants.Fields.VIEWS);
			instanceField = sWindowManager.getDeclaredField(getWindowManagerString());
			viewsField.setAccessible(true);
			instanceField.setAccessible(true);
			Object instance = instanceField.get(null);
			return (View[]) viewsField.get(instance);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
	
	// find a view by a matching class name.
	public static View getChildByClassName(View v, String simpleClassName) {
		if (v.getClass().getSimpleName().equals(simpleClassName)) {
			return v;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vMatch = getChildByClassName(vChild, simpleClassName);
				if (vMatch != null) {
					return vMatch;
				}				
			}
		}
		return null;
	}
}
