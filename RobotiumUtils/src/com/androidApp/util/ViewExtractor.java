package com.androidApp.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * class to extract views from the android view hierarchy.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class ViewExtractor {
	protected static String getWindowManagerString(){
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			return Constants.Fields.WINDOW_MANAGER_FIELD_STATIC;
		} else {
			return Constants.Fields.WINDOW_MANAGER_FIELD;
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
	
	/**
	 * is the view a popup view?
	 * @param a parent activity
	 * @param v view to test
	 * @return true if v is a popup of a
	 */
	public static boolean isDialogOrPopup(Activity a, View v) {
		Context c = v.getContext();
		// dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
		// base context
		if (c instanceof ContextThemeWrapper) {
			ContextThemeWrapper ctw = (ContextThemeWrapper) c;
			c = ctw.getBaseContext();
		}
		// if the view has the same context (i.e. is owned by the application), and it's not the application's 
		// decor view, then a dialog is up.
		if (c.equals(a.getBaseContext())) {
			if (v != a.getWindow().getDecorView()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * popup windows are slightly different than dialogs, so we have a separate path which polls for them
	 * to set up in RecordTest
	 * @param activity
	 * @return
	 */
	public static PopupWindow findPopupWindow(Activity activity) {
		try {
			ViewExtractor viewExractor = new ViewExtractor();
			View[] views = viewExractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (ViewExtractor.isDialogOrPopup(activity, v)) {	
						String className = v.getClass().getCanonicalName();
						if (className.equals(Constants.Classes.POPUP_VIEW_CONTAINER)) {
							Class popupViewContainerClass = Class.forName(Constants.Classes.POPUP_VIEW_CONTAINER_CREATECLASS);
							PopupWindow popupWindow = (PopupWindow) ListenerIntercept.getFieldValue(v, popupViewContainerClass, Constants.Classes.THIS);
							return popupWindow;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}
}
