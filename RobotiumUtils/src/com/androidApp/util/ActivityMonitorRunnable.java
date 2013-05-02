package com.androidApp.util;

import java.lang.ref.WeakReference;
import java.util.Stack;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;

/**
 * This runnable runs in a background thread and maintains a stack of activities, which can be 
 * waited for and queried from the testing thread.  We use WeakReference<> so we don't hold onto 
 * the activity, which would interfere with the application under test
 * @author Matthew
 *
 */
public class ActivityMonitorRunnable implements Runnable {
	 private static final String 				TAG = "ActivityMonitorRunnable";
	 public static final int 					MINISLEEP = 100;			
	 protected Instrumentation.ActivityMonitor	mActivityMonitor;				// activity monitor
	 protected Stack<WeakReference<Activity>>	mActivityStack;					// stack of activities.
	
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
		mActivityStack = new Stack<WeakReference<Activity>>();
		IntentFilter intentFilter = null;
		mActivityMonitor = new Instrumentation.ActivityMonitor(intentFilter, null, false);
		instrumentation.addMonitor(mActivityMonitor);
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
			Activity activityB = null;
			if (!mActivityStack.isEmpty()) {
				activityB = mActivityMonitor.waitForActivity();
			}
			if (!inActivityStack(activityA)) {
				addActivityToStack(activityA);
			} else if (!inActivityStack(activityB)) {
				addActivityToStack(activityB);
			} else {
				mActivityStack.pop();
				if (mActivityStack.empty()) {
					break;
				}
			}
		}
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
				WeakReference<Activity> ref = mActivityStack.peek();
				if (ref != null) {
					return ref.get();
				}
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
		synchronized(mActivityStack) {
			for (WeakReference<Activity> ref : mActivityStack) {
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
			WeakReference<Activity> ref = new WeakReference<Activity>(activity);
			mActivityStack.push(ref);
		}
	}
	
	/**
	 * remove the specified activity from the stack (not necessarily popping it)
	 * @param activity activity to be removed.
	 */
	protected void removeActivityFromStack(Activity activity) {
		WeakReference<Activity> ref = null;
		synchronized(mActivityStack) {
			for (WeakReference<Activity> candRef : mActivityStack) {
				if (candRef.get() == activity) {
					ref = candRef;
					break;
				}
			}
			if (ref != null) {
				mActivityStack.remove(ref);
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
