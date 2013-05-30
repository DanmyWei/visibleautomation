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
	 public static final int 						MINISLEEP = 100;			
	 protected Instrumentation.ActivityMonitor		mActivityMonitor;				// activity monitor
	 protected Stack<ActivityInfo>					mActivityStack;					// stack of activities.
	 
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
		mActivityStack = new Stack<ActivityInfo>();
		IntentFilter intentFilter = null;
		mActivityMonitor = instrumentation.addMonitor(intentFilter, null, false);
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
			if (activityA.isFinishing()) {
				Log.i(TAG, "interesting");
				removeActivityFromStack(activityA);
				if (mActivityStack.empty()) {
					break;
				}
			} else {
	 			Activity activityB = null;
				
				// OK, now this is very very strange
				if (!mActivityStack.isEmpty()) {
					activityB = mActivityMonitor.waitForActivity();
				}
				if (!atTopOfActivityStack(activityA)) {
					addActivityToStack(activityA);
				} else if (!atTopOfActivityStack(activityB)) {
					addActivityToStack(activityB);
				} else {
					removeActivityFromStack(activityA);
					if (mActivityStack.empty()) {
						break;
					}
				}
			}
		}
	}	
	
	/** 
	 * we insert a global layout listener, so we can wait for new layouts, for example, when the IME is displayed
	 * @param a activity to get the window.view content from.
	 */
	public void interceptLayout(Activity a) {
		Window w = a.getWindow();
	    View v = w.getDecorView().findViewById(android.R.id.content);
	    
	    // good stroke of luck, we can listen to layout events on the root view.
	    ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
	    viewTreeObserver.addOnGlobalLayoutListener(new OnLayoutInterceptListener());
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
		synchronized(mActivityStack) {
			if (!mActivityStack.isEmpty()) {
				ActivityInfo activityInfo = mActivityStack.peek();
				WeakReference<Activity> ref = activityInfo.mRefActivity;
				if (ref != null) {
					return ref.get();
				}
			}
		}
		return null;
	}
	
	/**
	 * return the previous activity from the stack
	 * @return previous activity or null if the stack does not have 2 elements.
	 */
	public Activity getPreviousActivity() {
		synchronized(mActivityStack) {
			if (mActivityStack.size() >= 2) {
				ActivityInfo activityInfo = mActivityStack.get(mActivityStack.size() - 2);
				WeakReference<Activity> ref = activityInfo.mRefActivity;
				if (ref != null) {
					return ref.get();
				}
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
		synchronized(mActivityStack) {
			if ((mActivityStack != null) && !mActivityStack.isEmpty()) {
				ActivityInfo topInfo = mActivityStack.peek();
				WeakReference<Activity> ref = topInfo.mRefActivity;
				if (ref.get() == activity) {
					return true;
				}
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
		synchronized(mActivityStack) {
			for (ActivityInfo activityInfo : mActivityStack) {
				WeakReference<Activity> ref = activityInfo.mRefActivity;
				if (ref.get() == activity) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * add the specified activity to the stack
	 * @param activity activity to be pushed on the stack
	 */
	protected void addActivityToStack(Activity activity) {
		synchronized (mActivityStack) {
			ActivityInfo activityInfo = new ActivityInfo();
			activityInfo.mRefActivity = new WeakReference<Activity>(activity);
			OnLayoutInterceptListener listener = addLayoutObserver(activity);
			activityInfo.mRefLayoutListener = new WeakReference<OnLayoutInterceptListener>(listener);
			mActivityStack.push(activityInfo);
		}
	}
		
	/**
	 * when we add an activity to the stack, we also add a layout observer, which can signal to instrumentation
	 * that a layout has completed.  Handy for stuff like waiting for the keyboard to appear/disappear
	 * @param a
	 * @return
	 */
	protected OnLayoutInterceptListener addLayoutObserver(Activity a) {
		Window w = a.getWindow();
	    View v = w.getDecorView().findViewById(android.R.id.content);
	        
	    // good stroke of luck, we can listen to layout events on the root view.
	    ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
	    OnLayoutInterceptListener listener = new OnLayoutInterceptListener();
	    viewTreeObserver.addOnGlobalLayoutListener(listener);
	    return listener;
	}

	
	/**
	 * remove the specified activity from the stack (not necessarily popping it)
	 * @param activity activity to be removed.
	 */
	protected boolean removeActivityFromStack(Activity activity) {
		ActivityInfo activityInfo = null;
		synchronized(mActivityStack) {
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
			}
			return false;
		}
	}

	// sleep utility
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
			
		}
	}
}
