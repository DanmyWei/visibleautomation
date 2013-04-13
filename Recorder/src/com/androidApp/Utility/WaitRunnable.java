package com.androidApp.Utility;

import android.app.Activity;

public class WaitRunnable implements Runnable {
	protected Activity  mActivity;
	protected Runnable 	mRunnable;
	protected Object	mWaitObject;
	
	/**
	 * wrapper for runnable that takes a waitObject sends a notify on runnable completion so we can synchronize with the calling thread.
	 * @param activity runOnUiThread will be called from this activity
	 * @param runnable runnable to actually run
	 * @param waitObject waitObject to wait on.
	 */
	public WaitRunnable(Activity activity, Runnable runnable, Object waitObject) {
		mActivity = activity;
		mRunnable = runnable;
		mWaitObject = waitObject;
	}
	
	public void run() {
		mRunnable.run();
		synchronized(mWaitObject) {
			mWaitObject.notify();
		}
	}
}
