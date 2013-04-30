package com.androidApp.util;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.util.Log;

public class ActivityMonitorRunnable implements Runnable {
	private static final String 				TAG = "ActivityMonitorRunnable";
	private static final int 					MINISLEEP = 100;			
	protected Object 							mWaitObject;					// wait object to notify the main thread
	protected Activity							mCurrentActivity;				// current activity at time monitor was instantiated.
	protected Activity							mNewActivity;					// activity returned from monitor.
	protected Class								mNewActivityClass;				// class of the new activity that we're waiting for
	protected long								mTimeoutMsec;
	protected Instrumentation.ActivityMonitor	mActivityMonitor;
	protected boolean							mfWaiting;						// to prevent deadlock
	
	public ActivityMonitorRunnable(Instrumentation instrumentation, Activity currentActivity, Class newActivityClass, long timeoutMsec) {
		init(instrumentation, currentActivity, newActivityClass, timeoutMsec);
	}
	
	public ActivityMonitorRunnable(Instrumentation instrumentation, Activity currentActivity, Class newActivityClass) {
		init(instrumentation, currentActivity, newActivityClass, -1);
	}
	
	public void init(Instrumentation instrumentation, Activity currentActivity, Class newActivityClass, long timeoutMsec) {
		mWaitObject = new Object();
		mCurrentActivity = currentActivity;
		mNewActivityClass = newActivityClass;
		IntentFilter intentFilter = null;
		mActivityMonitor = new Instrumentation.ActivityMonitor(intentFilter, null, false);
		instrumentation.addMonitor(mActivityMonitor);
		mTimeoutMsec = timeoutMsec;
		mNewActivity = null;
		mfWaiting = false;
	}
	
	/**
	 * wait for a notification.
	 */
	public void lockAndWait() {
		try {
			Log.d(TAG, "entering synchronized");
			synchronized(mWaitObject) {
				Log.d(TAG, "entering wait");
				mfWaiting = true;
				mWaitObject.wait(mTimeoutMsec);
			}
		} catch (InterruptedException iex) {
		}
		Log.d(TAG, "wait has been notified");
	}
	
	/**
	 * start the thread which runs the activity monitor, then send a notification
	 * @return
	 */
	public void waitForNewActivity() {
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public Activity getNewActivity() {
		lockAndWait();
		return mNewActivity;
	}

	public void run() {
		while (true) {
			if (mTimeoutMsec > 0) {
				//mNewActivity = mActivityMonitor.waitForActivityWithTimeout(mTimeoutMsec);
				mNewActivity = mActivityMonitor.waitForActivity();
			} else {
				mNewActivity = mActivityMonitor.waitForActivity();
			}
			
			// in the activity forward case, the wait() call in getNewActivity() happens first.  In the activity back
			// case, the activity monitor fires first.  We can't send a notify until getNewActivity() is waiting 
			// for it. We sleep for some period until the "waiting" flag is set, then send the notify
			//if ((mNewActivity == null) || (mNewActivity.getClass().equals(mNewActivityClass) && (mNewActivity != mCurrentActivity))) {
			if ((mNewActivity == null) || mNewActivity.getClass().equals(mNewActivityClass)) {
				long timeoutMsec = mTimeoutMsec;
				while (!mfWaiting && (timeoutMsec > 0)) {
					try {
						Thread.sleep(MINISLEEP);
						timeoutMsec -= MINISLEEP;
					} catch (InterruptedException iex) {
					}
				}
				synchronized(mWaitObject) {
					Log.d(TAG, "sending notification of new activity = " + mNewActivity);
					mWaitObject.notify();
					break;
				}
			} else {
				Log.d(TAG, "activity returned from monitor = " + mNewActivity);
			}
		}
	}	
}
