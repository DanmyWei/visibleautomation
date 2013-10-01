package com.androidApp.util30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import com.androidApp.util.TestException;
import com.androidApp.util.ViewExtractor;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.TestCase;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;

/**
 * utility class for monitoring activities and sending events to views.
 * TODO: we need to change the wait routines so they're instantiated before the events that they depend on are fired.
 * All the wait objects need to be created on a background thread, which will fire a callback when the waitObject is 
 * notified.  The API thread will have an external wait call, which will return immediately if the callback has been 
 * fired, or wait for a notification if it hasn't been fired yet. This will improve the performance of the tests, and their 
 * roboustness.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RobotiumUtils extends com.androidApp.util.RobotiumUtils {
	private static String TAG = "RobotiumUtilsV13";
	/**
	 * This has to be called before getActivity(), so it can intercept the first activity.
	 * @param instrumentation
	 */
	public RobotiumUtils(Class<? extends ActivityInstrumentationTestCase2> testClass, Instrumentation instrumentation) throws IOException {
		super(testClass, instrumentation);
	}
		
	
	/**
	 * dismissing a popup has to be done on the UI thread via a runnable
	 * @author Matthew
	 *
	 */
	public class DismissPopupWindowRunnable implements Runnable {
		public PopupWindow mPopupWindow;
		
		public DismissPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		
		@Override
		public void run() {
			mPopupWindow.dismiss();
		}
	}
	
	public void dismissPopupWindow(Activity activity) throws TestException {
		dismissPopupWindow(activity, VIEW_TIMEOUT_MSEC);
	}
	
	public boolean waitForPopupWindow(Activity activity, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	/**
	 * dismiss a popup window, like a menu popup, autocomplete dropdown, etc.  I hope there is only one popup.
	 * @param activity
	 */
	public boolean dismissPopupWindow(Activity activity, long waitMsec) throws TestException {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				mInstrumentation.runOnMainSync(new DismissPopupWindowRunnable(popupWindow));
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	
	public static boolean verifyPopupWindowDimissed(Activity activity, PopupWindow dismissedPopupWindow) throws TestException {
		return verifyPopupWindowDismissed(activity, dismissedPopupWindow, VIEW_TIMEOUT_MSEC);
	}

	/**
	 * verify that there are no popups displayed.
	 * NOTE: What if another popup is displayed immediately?
	 * @param activity
	 * @param waitMsec
	 * @return
	 * @throws TestException
	 */
	public static boolean verifyPopupWindowDismissed(Activity activity, PopupWindow dismissedPopupWindow, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if ((popupWindow == null) || (dismissedPopupWindow != popupWindow)) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	
	/**
	 * select the indexed tab on the action bar
	 * @param activity activity to get the action bar from
	 * @param tabIndex index of the tab to select
	 * @throws TestException if there is no action bar to select a tab from
	 */
	public void selectActionBarTab(Activity activity, int tabIndex) throws TestException {
		if (activity instanceof ActionBarActivity) {
			ActionBarActivity actionBarActivity = (ActionBarActivity) activity;
			ActionBar actionBar = actionBarActivity.getSupportActionBar();
			if (actionBar == null) {
				throw new TestException("selectActionBarTab: activity has no action bar");
			}
			mInstrumentation.runOnMainSync(new SetActionBarTabRunnable(actionBar, tabIndex));
		} else {
			Log.e(TAG, "select action bar tab " + activity + " is not an action bar activity");
		}
	}
	
	/**
	 * runnable to select an action bar tab.
	 * @author Matthew
	 *
	 */
	public class SetActionBarTabRunnable implements Runnable {
		public ActionBar 	mActionBar;
		public int 			mIndex;
		
		public SetActionBarTabRunnable(ActionBar actionBar, int index) {
			mActionBar = actionBar;
			mIndex = index;
		}
		
		public void run() {
			ActionBar.Tab tab = mActionBar.getTabAt(mIndex);
			tab.select();
		}
	}
}
