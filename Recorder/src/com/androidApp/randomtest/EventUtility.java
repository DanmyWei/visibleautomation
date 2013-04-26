package com.androidApp.randomtest;

import junit.framework.Assert;
import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

/**
 * event functions used in the random test suite to send key, touch, scroll, select, click, etc. events.
 * @author Matthew
 *
 */
public class EventUtility {
	private static final int MINISLEEP = 100;

	/**
	 * click on a location on the screen
	 * @param instrumentation instrumentation handle
	 * @param x absolute location
	 * @param y
	 */
	public static boolean clickOnScreen(Instrumentation instrumentation, float x, float y) {
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
		MotionEvent event2 = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
		try{
			instrumentation.sendPointerSync(event);
			instrumentation.sendPointerSync(event2);
			TestUtility.sleep(MINISLEEP);
			return true;
		} catch(SecurityException e){
			return false;
		}
	}
	
	/**
	 * click in the center of a view.
	 * @param instrumentation
	 * @param view view to click
	 * @return true if the position is visible and can be clicked.
	 */
	public static boolean clickOnScreen(Instrumentation instrumentation, View view) {
		if (view == null) {
			Assert.assertTrue("View is null and can therefore not be clicked!", false);
		}
		int[] xy = new int[2];
		view.getLocationOnScreen(xy);
		final int viewWidth = view.getWidth();
		final int viewHeight = view.getHeight();
		final float x = xy[0] + (viewWidth / 2.0f);
		float y = xy[1] + (viewHeight / 2.0f);
		if (inParentRect(view, (int) x, (int) y)) {
			return clickOnScreen(instrumentation, x, y);
		} else {
			return false;
		}
	}

	/**
	 * Long clicks a given coordinate on the screen
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param time the amount of time to long click
	 *
	 */

	public static boolean clickLongOnScreen(Instrumentation instrumentation, float x, float y) {
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
		try {
			instrumentation.sendPointerSync(event);
		} catch (SecurityException e){
			return false;
		}
		eventTime = SystemClock.uptimeMillis();
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 
				x + ViewConfiguration.getTouchSlop() / 2,
				y + ViewConfiguration.getTouchSlop() / 2, 0);
		instrumentation.sendPointerSync(event);
		TestUtility.sleep((int)(ViewConfiguration.getLongPressTimeout() * 2.5f));

		eventTime = SystemClock.uptimeMillis();
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
		instrumentation.sendPointerSync(event);
		TestUtility.sleep();
		return true;
	}
	
	public static boolean clickLongOnScreen(Instrumentation instrumentation, View view) {
		if (view == null) {
			Assert.assertTrue("View is null and can therefore not be clicked!", false);
		}
		int[] xy = new int[2];
		view.getLocationOnScreen(xy);
		final int viewWidth = view.getWidth();
		final int viewHeight = view.getHeight();
		final float x = xy[0] + (viewWidth / 2.0f);
		float y = xy[1] + (viewHeight / 2.0f);
		if (inParentRect(view, (int) x, (int) y)) {
			return clickLongOnScreen(instrumentation, x, y);
		} else {
			return false;
		}
	}
	
	public static boolean touchOnScreen(Instrumentation instrumentation, float x, float y) {
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
		try{
			instrumentation.sendPointerSync(event);
			TestUtility.sleep(MINISLEEP);
			return true;
		} catch(SecurityException e){
			return false;
		}
	}

	public static boolean touchOnScreen(Instrumentation instrumentation, View view) {
		if (view == null) {
			Assert.assertTrue("View is null and can therefore not be clicked!", false);
		}
		int[] xy = new int[2];
		view.getLocationOnScreen(xy);
		final int viewWidth = view.getWidth();
		final int viewHeight = view.getHeight();
		final float x = xy[0] + (viewWidth / 2.0f);
		float y = xy[1] + (viewHeight / 2.0f);
		if (inParentRect(view, (int) x, (int) y)) {
			return touchOnScreen(instrumentation, x, y);
		} else {
			return false;
		}
	}
	
	public static boolean inParentRect(View v, int x, int y) {
		ViewParent vp = v.getParent();
		if ((vp != null) && (vp instanceof View)) {
			View vpv = (View) vp;
			Rect r = new Rect();
			vpv.getGlobalVisibleRect(r);
			return (x > r.left) && (y > r.top) && (x < r.right) && (y < r.bottom); 		
		}
		return false;
	}

}
