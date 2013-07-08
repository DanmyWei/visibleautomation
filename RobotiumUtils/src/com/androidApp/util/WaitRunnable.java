package com.androidApp.util;

import android.app.Activity;

/**
 * class to wait on a runnable which is executed on the UI thread.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class WaitRunnable implements Runnable {
	protected Object 	mWaitObject;
	protected Runnable	mRunnable;
	protected boolean	mfHasRun;
	
	public WaitRunnable(Activity a, Runnable r) {
		mWaitObject = new Object();
		mRunnable = r;
		mfHasRun = false;
		a.runOnUiThread(this);
	}
	
	public void run() {
		mRunnable.run();
		mfHasRun = true;
		synchronized(mWaitObject) {
			mWaitObject.notify();
		}
	}
	
	public boolean waitForCompletion(long timeoutMsec) {
		if (!mfHasRun) {
			try {
				synchronized(mWaitObject) {
					mWaitObject.wait(timeoutMsec);
				}
			} catch (InterruptedException iex) {
			}
		}
		return mfHasRun;
	}
}
