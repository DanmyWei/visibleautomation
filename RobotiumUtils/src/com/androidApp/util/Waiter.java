package com.androidApp.util;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * for a view, we need to traverse the view hierarchy and find out if it is obscured by another view
 * 1:  The view rectangle overlaps and contains the target view
 * 2:  The view is at or below the level of the target view.
 * 3:  Its containing view also contains the target widget
 * 4:  It is shown 
 * @author matt2
 *
 */
public class Waiter {
	protected static final String TAG = "Waiter";
	
	/**
	 * recursively test if an object is obscured
	 * @param v view to search against
	 * @return true if somebody overlaps it, and they're in front of us.
	 */
	public static boolean isObscured(View v) {
		if (!v.isShown() || (v.getHeight() == 0) || (v.getWidth() == 0)) {
			return true;
		}
		View viewRoot = v.getRootView();
		Rect targetRect = new Rect();
		v.getGlobalVisibleRect(targetRect);
		Rect viewRect = new Rect();
		int level = getLevel(viewRoot, v);
		return isObscured(viewRoot, v, viewRect, targetRect, level, 0);		
	}
	
	public static boolean isObscured(View view, View viewTarget, Rect viewRect, Rect targetRect, int targetLevel, int level) {
		Log.i(TAG, "obscured view = " + view + " viewrect = " + view.getLeft() + "," + view.getTop() + "," + view.getRight() + "," + view.getBottom() +
					" viewTarget = " + viewTarget + " targetrect = "  + viewTarget.getLeft() + "," + viewTarget.getTop() + "," + viewTarget.getRight() + "," + viewTarget.getBottom());
		if (view == viewTarget) {
			return false;
		}
		if (!view.isShown()) {
			return false;
		}
		view.getGlobalVisibleRect(viewRect);
		if (viewRect.contains(targetRect) && view.isClickable()) {
			if (level >= targetLevel) {
				return true;
			} else {
				if (view instanceof ViewGroup) {
					ViewGroup vg = (ViewGroup) view;
					for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
						View vChild = vg.getChildAt(iChild);
						if (isObscured(vChild, viewTarget, viewRect, targetRect, targetLevel, level + 1)) {
							return true;
						}
					}
				}
			}
		}
		return false;		
	}
	
	public static int getLevel(View viewRoot, View v) {
		int level = 0;
		while (v != viewRoot) {
			v = (View) v.getParent();
			level++;
		}
		return level;
	}

	/**
	 * wait for a view to be unobscured.
	 * @param v view to wait for 
	 * @param timeoutMsec duration to wait.
	 * @return
	 */
	public static boolean waitForView(View v, long timeoutMsec) {
		while (isObscured(v) && (timeoutMsec > 0)) {
			Log.i(TAG, v + " is obscured");
			timeoutMsec -= RobotiumUtils.WAIT_INCREMENT_MSEC;
			RobotiumUtils.sleep(RobotiumUtils.WAIT_INCREMENT_MSEC);
		}
		if (timeoutMsec > 0) {
			Log.i(TAG, v + " is shown");
			return true;
		} else {
			Log.i(TAG, v + " is obscured after timeout");
			return false;
		}
	}

}
