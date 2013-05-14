package com.androidApp.util;

import android.util.Log;
import android.view.ViewTreeObserver;

/**
 * Layout lister so instrumentation can be notified of a layout as the result of an operation (like showing the keyboard)
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynods.  All Rights Reserved.
 */
public class OnLayoutInterceptListener implements ViewTreeObserver.OnGlobalLayoutListener {
	protected static final String 	TAG = "OnLayoutInterceptListener";
	protected Object				mWaitObject;						// so instrumentation can wait on layout change
	
	public OnLayoutInterceptListener() {
		mWaitObject = new Object();
	}
	
	/**
	 * notify anyone who is waiting that a global layout has occurred
	 */
	public void onGlobalLayout() {
		Log.i(TAG, "layout");
		synchronized (mWaitObject) {
			mWaitObject.notify();
		}
	}
	
	/** 
	 * wait for a signal from the global layout listener that a layout has occurred.
	 * @param timeoutMsec
	 */
	public void waitForLayout(long timeoutMsec) {
		synchronized (mWaitObject) {
			try {
				mWaitObject.wait(timeoutMsec);
			} catch (InterruptedException iex) {
			}
		}
	}
}
