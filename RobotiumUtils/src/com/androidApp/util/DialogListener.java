package com.androidApp.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;

import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimerTask;

/**
 * Listen for dialogs to appear, and if a codeDefinition matches the handler, then execute it.
 * @author matt2
 */
public class DialogListener {
	
	public static boolean waitForDialog(Activity activity, CodeDefinition codeDef, long timeoutMsec) {
		while (timeoutMsec > 0) {
			Dialog dialog = findDialog(activity);
			if (dialog != null) {
				if (codeDef.matchDialog(activity, dialog)) {
					return true;
				}
			}
			RobotiumUtils.sleep(RobotiumUtils.WAIT_INCREMENT_MSEC);
			timeoutMsec -= RobotiumUtils.WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	/**
	 * see if this activity has popped up a dialog.
	 * @param activity activity to test
	 * @return Dialog or null
	 */
	public static Dialog findDialog(Activity activity) {
		try {
			Class phoneDecorViewClass = Class.forName(Constants.Classes.PHONE_DECOR_VIEW);
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (isDialogOrPopup(activity, v)) {	
						Dialog dialog = getDialog(v);
						if (dialog != null) {
							return dialog;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}
	/**
	 * given a PhoneWindow$DecorView, see if it has a WindowCallback which derives from Dialog.  If so, then return that
	 * dialog, otherwise return null.
	 * @param phoneWindowDecorView PhoneWindow$DecorView
	 * @return Dialog or null;
	 */
	public static Dialog getDialog(View phoneWindowDecorView) {
		try {
			Class<? extends View> phoneDecorViewClass = (Class<? extends View>) Class.forName(Constants.Classes.PHONE_DECOR_VIEW);
			if (phoneWindowDecorView.getClass() == phoneDecorViewClass) {
				Window phoneWindow = (Window) ReflectionUtils.getFieldValue(phoneWindowDecorView, phoneDecorViewClass, Constants.Classes.THIS);
				Window.Callback callback = phoneWindow.getCallback();
				if (callback instanceof Dialog) {
					Dialog dialog = (Dialog) callback;
					return dialog;
				}
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * is this view in the same context as the activity, but has a different window? Then it is contained in a popup window
	 * and may or may not be a dialog.
	 * @param a the activity
	 * @param v a PhoneWindow$DecorView or PopupContainerView or something like that
	 * @return true, or maybe false.  probably true, though
	 */
	public static boolean isDialogOrPopup(Activity a, View v) {
		if (v != null) {
			Context viewContext = v.getContext();
			// dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
			// base context
			if (viewContext instanceof ContextThemeWrapper) {
				ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
				viewContext = ctw.getBaseContext();
			}
			Context activityContext = a;
			Context activityBaseContext = a.getBaseContext();
			return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext)) && (v != a.getWindow().getDecorView());
		} else {
			return false;
		}
	}
}
