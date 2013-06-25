package com.androidApp.util;

import java.lang.ref.WeakReference;
import java.util.Stack;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

/**
 * This runnable runs in a background thread and maintains a stack of activities, which can be 
 * waited for and queried from the testing thread.  We use WeakReference<> so we don't hold onto 
 * the activity, which would interfere with the application under test
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class ActivityMonitorRunnable implements Runnable {
	 private static final String 					TAG = "ActivityMonitorRunnable";
	 public static final int 						MINISLEEP = 1000;
	 protected Instrumentation.ActivityMonitor		mActivityMonitor;				// activity monitor
	 protected Stack<ActivityInfo>					mActivityStack;					// stack of activities.
	 protected Instrumentation						mInstrumentation;				// so we can run stuff on the UI thread
	 /**
	  * so we can retain information associated with the activity
	  * @author mattrey
	  *
	  */
	 protected class ActivityInfo {
		 WeakReference<Activity> 					mRefActivity;
		 WeakReference<OnLayoutInterceptListener> 	mRefLayoutListener;
	 }
	
	 /**
	  * constructor
	  * @param instrumentation handle to instrumentation to add the activity monitor to.
	  */
	 public ActivityMonitorRunnable(Instrumentation instrumentation) {
		init(instrumentation);
	}
	
	 /**
	  * initialization method.  Creates the activity stack and activity monitor, which sets a
	  * filter to monitor all activities.
	  * @param instrumentation
	  */
	protected void init(Instrumentation instrumentation) {
		mInstrumentation = instrumentation;
		mActivityStack = new Stack<ActivityInfo>();
		IntentFilter intentFilter = null;
		mActivityMonitor = instrumentation.addMonitor(intentFilter, null, false);
	}
	
	public void logActivityStack() {
		for (int i = 0; i < mActivityStack.size(); i++) {
			ActivityInfo activityInfo = mActivityStack.get(i);
			Log.i(TAG, "stack[" + i + "] = " + activityInfo.mRefActivity.get());
		}
	}
	
	/**
	 * runnable for the background thread.  This waits for activities to start and stop, and adds and
	 * removes them from the stack
	 */

	public void run() {
		while (true) {
			// the activity monitor (like the postman) always rings twice. Except for the very first activity.
			// Once for the activity being paused and once for the activity being started.  This may be the same 
			// activity semantically, due to rotation, but it will always be the same object.  I'm not as sure 
			// about this as I should be, but the monitor can return activities in a different order than
			// pause, create.
			// TODO: handle the activity.isFinishing() case explicitly, since the operating system can
			// finish activities lower in the stack.
			Activity activityA = mActivityMonitor.waitForActivity();
			//if (activityA.isFinishing()) {
			if (false) {
				Log.i(TAG, "activity " + activityA + " finishing");
				removeActivityFromStack(activityA);
				Log.i(TAG, "activity stack depth = " + mActivityStack.size());
				logActivityStack();
				if (mActivityStack.empty()) {
					Log.i(TAG, "empty activity stack");
					break;
				}
			} else {
				if (!inActivityStack(activityA)) {
					Log.i(TAG, "add activity to stack " + activityA);
					addActivityToStack(activityA);
					logActivityStack();
				}
	 			Activity activityB = null;
				
				// OK, now this is very very strange, but sometimes, even though there should be a second activity,
	 			// there isn't, so we time out on the wait.
	 			// TODO: There must be a better solution for this, and we need to find out why only one activity is returned
	 			// The previous activity is getting nulled out because it's a WeakReference<> by another thread, and 
	 			// we need to detect that case.  If the previous activity in the stack is null, then do we get a double-tap
	 			// from the activity monitor as usual?  What are the messages we get?
				if (!mActivityStack.isEmpty()) {
					activityB = mActivityMonitor.waitForActivity();
				} 
				if (activityB != activityA) {
					Log.i(TAG, "activity stack depth = " + mActivityStack.size());
					if (!inActivityStack(activityB)) {
						Log.i(TAG, "add activity to stack " + activityB);
						addActivityToStack(activityB);
						logActivityStack();
					} else {
						if (isActivityGoingBack(activityA, activityB)) {
							Log.i(TAG, "remove activity from stack " + activityA);
							removeActivityFromStack(activityA);
							logActivityStack();
						} else if (isActivityGoingBack(activityB, activityA)) {
							Log.i(TAG, "remove activity from stack " + activityB);
							removeActivityFromStack(activityB);
							logActivityStack();
						}
						if (mActivityStack.empty()) {
							Log.i(TAG, "empty activity stack");
							break;
						}
					}
				}
			}
		}
	}	
	
	/**
	 * is the current activity going back to the previous activity
	 * @param activityCurrent what we think is the current activity
	 * @param activityPrevious what we think is the previous activity
	 * @return
	 */
	public boolean isActivityGoingBack(Activity activityCurrent, Activity activityPrevious) {
		if (atTopOfActivityStack(activityCurrent)) {
			if (mActivityStack.size() == 1) {
				return true;
			} else {
				if (activityPrevious == null) {
					return true;
				} else {
					ActivityInfo activityInfoPrevious = mActivityStack.get(mActivityStack.size() - 2);
					if (activityInfoPrevious.mRefActivity.get() == activityPrevious) {
						return true;
					}
				}
			} 
		}
		return false;
	}

	/**
	 * public function to wait for an activity by the "short" class name.
	 * @param activityName short class name (i.e. ApiDemos)
	 * @param timeoutMsec timeout in milliseconds
	 * @return matching activity or null.
	 */
	public Activity waitForActivity(String activityName, long timeoutMsec) {
		while (timeoutMsec >= 0) {
			Activity a = getCurrentActivity();
			if ((a != null) && a.getClass().getName().equals(activityName)) {
				return a;
			}
			timeoutMsec -= MINISLEEP;
			sleep(MINISLEEP);
		}
		return null;
	}
	
	/**
	 * public function to wait for an activity by class.
	 * @param cls activity class
	 * @param timeoutMsec timeout in milliseconds
	 * @return activity or null.
	 */
	public Activity waitForActivity(Class<? extends Activity> cls, long timeoutMsec) {
		while (timeoutMsec >= 0) {
			Activity a = getCurrentActivity();
			if ((a != null) && cls.isAssignableFrom(a.getClass())) {
				return a;
			}
			timeoutMsec -= MINISLEEP;
			sleep(MINISLEEP);
		}
		return null;
	}
	
	/**
	 * return the activity at the top of the stack
	 * @return topmost activity or null if the stack is empty, or (I hope this doesn't happen)
	 * if the topmost activity is null.
	 */
	public Activity getCurrentActivity() {
		if (!mActivityStack.isEmpty()) {
			ActivityInfo activityInfo = mActivityStack.peek();
			WeakReference<Activity> ref = activityInfo.mRefActivity;
			if (ref != null) {
				Log.d(TAG, "current activity = " + ref.get());
				return ref.get();
			}
		}
		return null;
	}
	
	/**
	 * return the previous activity from the stack
	 * @return previous activity or null if the stack does not have 2 elements.
	 */
	public Activity getPreviousActivity() {
		if (mActivityStack.size() >= 2) {
			ActivityInfo activityInfo = mActivityStack.get(mActivityStack.size() - 2);
			WeakReference<Activity> ref = activityInfo.mRefActivity;
			if (ref != null) {
				return ref.get();
			}
		}
		return null;
	}
	
	/**
	 * retrieve the layout listener associated with the current activity.
	 * @return
	 */
	
	public OnLayoutInterceptListener getCurrentLayoutListener() {
		if (!mActivityStack.isEmpty()) {
			ActivityInfo activityInfo = mActivityStack.peek();
			WeakReference<OnLayoutInterceptListener> ref = activityInfo.mRefLayoutListener;
			if (ref != null) {
				return ref.get();
			}
		}
		return null;
	}
	
	/**
	 * is the specified activity at the top of the activity stack?
	 * @param activity
	 * @return
	 */
	public boolean atTopOfActivityStack(Activity activity) {
		if ((mActivityStack != null) && !mActivityStack.isEmpty()) {
			ActivityInfo topInfo = mActivityStack.peek();
			WeakReference<Activity> ref = topInfo.mRefActivity;
			if (ref.get() == activity) {
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * scan the activity stack to see if the activity is in it.
	 * @param activity activity to search for
	 * @return if there is a reference to the activity
	 */
	protected boolean inActivityStack(Activity activity) {
		for (ActivityInfo activityInfo : mActivityStack) {
			WeakReference<Activity> ref = activityInfo.mRefActivity;
			if (ref.get() == activity) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * add the specified activity to the stack
	 * @param activity activity to be pushed on the stack
	 */
	protected void addActivityToStack(Activity activity) {
		ActivityInfo activityInfo = new ActivityInfo();
		activityInfo.mRefActivity = new WeakReference<Activity>(activity);
		OnLayoutInterceptListener listener = new OnLayoutInterceptListener();
		activityInfo.mRefLayoutListener = new WeakReference<OnLayoutInterceptListener>(listener);
	    mInstrumentation.runOnMainSync(new AddGlobalLayoutListenerRunnable(activity, listener));
		mActivityStack.push(activityInfo);
	}
		
	
	public class AddGlobalLayoutListenerRunnable implements Runnable {
		protected Activity 					mActivity;
		protected OnLayoutInterceptListener mListener;
		
		public AddGlobalLayoutListenerRunnable(Activity activity, OnLayoutInterceptListener listener) {
			mActivity = activity;
			mListener = listener;
		}
		
		public void run() {
			try {
				Window w = mActivity.getWindow();
			    View v = w.getDecorView().findViewById(android.R.id.content);
			    if (v != null) {
				        
				    // good stroke of luck, we can listen to layout events on the root view.
				    ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
				    viewTreeObserver.addOnGlobalLayoutListener(mListener);
			    }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	
	/**
	 * remove the specified activity from the stack (not necessarily popping it)
	 * @param activity activity to be removed.
	 */
	protected boolean removeActivityFromStack(Activity activity) {
		ActivityInfo activityInfo = null;
		for (ActivityInfo candActivityInfo : mActivityStack) {
			WeakReference<Activity> candRef = candActivityInfo.mRefActivity;
			if (candRef.get() == activity) {
				activityInfo = candActivityInfo;
				break;
			}
		}
		if (activityInfo != null) {
			mActivityStack.remove(activityInfo);
			return true;
		} else {
			Log.d(TAG, "failed to remove " + activity + " from stack");
		}
		return false;
	}

	// sleep utility
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
			
		}
	}
}
