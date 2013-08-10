package com.androidApp.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

/**
 * This runnable runs in a background thread and maintains a stack of activities, which can be 
 * waited for and queried from the testing thread.  We use WeakReference<> so we don't hold onto 
 * the activity, which would interfere with the application under test
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ActivityMonitorRunnable implements Runnable {
	 private static final String 										TAG = "ActivityMonitorRunnable";
	 public static final int 											MINISLEEP = 1000;
	 protected Instrumentation.ActivityMonitor							mActivityMonitor;				// activity monitor
	 protected Stack<ActivityInfo>										mActivityStack;					// stack of activities.
	 protected Instrumentation											mInstrumentation;				// so we can run stuff on the UI thread
	 /**
	  * so we can retain information associated with the activity
	  * @author mattrey
	  *
	  */
	 protected class ActivityInfo {
		 WeakReference<Activity> 					mRefActivity;
		 WeakReference<OnLayoutInterceptListener> 	mRefLayoutListener;
		 
		 public Activity getActivity() {
			 return mRefActivity.get();
		 }
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
	
	public void run() {
		boolean fStart = true;
		boolean fPastFirstActivity = false;
		int currentRotation = Surface.ROTATION_0;
		while (fStart || !fPastFirstActivity || !ActivityMonitorRunnable.this.isActivityStackEmpty()) {
			try {
				Activity activity = ActivityMonitorRunnable.this.mActivityMonitor.waitForActivity();
				// assign these right awway, since they are LIVE activities, and the state may change
				// it would be nice to have a mutex lock preventing any further execution on the application
				// thread, but I have no such luck
				boolean fStopped = isStopped(activity);
				boolean fResumed = isResumed(activity);
				boolean fFinishing = activity.isFinishing();
				Log.i(TAG, "activity = " + activity + 
						   " finished = " + fFinishing +
						   " stopped = " + fStopped + 
						   " resumed = " + fResumed + 
						   " flags = 0x" + Integer.toHexString(activity.getIntent().getFlags()));
				currentRotation  = activity.getWindowManager().getDefaultDisplay().getRotation();
				
				// first activity resumed..start activity
				if (fStart  && fResumed) {
					addActivityToStack(activity);
					fStart = false;
				} else if (fFinishing || fStopped) {
					
					// finishing..remove from the stack.  if it's the last activity, then write that
					removeActivityFromStack(activity);
				} else {
						// if the activity is in the stack, and we're resuming it, then we write "goBack()" to the activity
					if (!inActivityStack(activity)  && fResumed) {	
						addActivityToStack(activity);
					} 
				}
				
				// remove finished or nulled activities.
				cleanupActivityStack();
			} catch (Exception ex) {
				Log.e(TAG, "exception thrown in activity interceptor");
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * the weak references go null after the activities are finished
	 */
	public void cleanupActivityStack() {
		List<ActivityInfo> removeList = new ArrayList<ActivityInfo>();
		int iPos = mActivityStack.size() - 1;
		for (ActivityInfo cand : mActivityStack) {
			if ((cand.getActivity() == null) || (cand.getActivity().isFinishing())) {
				removeList.add(cand);
			}
			iPos--;
		}
		for (ActivityInfo remove : removeList) {
			mActivityStack.remove(remove);
		}
	}
	
	protected boolean isStopped(Activity activity) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(activity, Activity.class, Constants.Fields.STOPPED);
	}
	
	protected boolean isResumed(Activity activity) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(activity, Activity.class, Constants.Fields.RESUMED);
	}

	protected boolean isActivityStackEmpty() {
		return mActivityStack.isEmpty();
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
		
	
	/**
	 * Runnable (on the UI thread) to add a layout intercept listener, which allows us to wait for layout events.
	 * @author matt2
	 *
	 */
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

	

	// sleep utility
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
			
		}
	}
}
