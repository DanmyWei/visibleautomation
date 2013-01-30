package com.androidApp.Test;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

/**
 * record click and key events in an activity. In short, be awesome.
 * @author matreyno
 *
 * @param <T>
 */
public abstract class RecordTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
	private static final String TAG = "RecordTest";
	private EventRecorder 					mRecorder;
	private ActivityMonitor					mActivityMonitor;					// track switches between activities	
	private Stack<WeakReference<Activity>> 	mActivityStack;						// current stack of activities, managed by TimerThread
	private static final int 				ACTIVITYSYNCTIME = 50;				// msec polling frequency
	private Timer 							mActivitySyncTimer;					// timer to run activity stack thread.
	private boolean							mfFirstActivityInitialized = false;	// if stack is empty, and this is true, then exit.
	private Dialog							mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	// initialize the event recorder
	public void initRecorder() {
		try {
			mRecorder = new EventRecorder("events.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordTest(Class<T> activityClass) {
		super(activityClass);
		initRecorder();
	}
	
	public abstract void initializeResources();
	
	public void addRdotID(Object rdotid) {
		mRecorder.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mRecorder.addRdotString(rdotstring);
	}

	/**
	 * initialize the event recorder, the activity monitor, the stack of activities, and the background thread that populates
	 * that stack.  Use instrumentation to launch the activity
	 */
	public void setUp() throws Exception { 
		super.setUp();
		
		initRecorder();
		initializeResources();
		setupActivityMonitor();
		mActivityStack = new Stack<WeakReference<Activity>>();
		setupActivityStackListener();

		Instrumentation instrumentation = getInstrumentation();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), getActivity().getClass().getName());			// so we can get the package name to write in the manifest and classpath
		String packageName = getPackageName(getActivity());
		long time = SystemClock.uptimeMillis();
		mRecorder.writeRecord(Constants.EventTags.PACKAGE + "," + time + "," + packageName);
		instrumentation.startActivitySync(intent);
	}


	public void tearDown() throws Exception {
	}
		
	/**
	* This is were the activityMonitor is set up. The monitor will keep check
	* for the currently active activity.
	*
	*/

	private void setupActivityMonitor() {

		try {
			IntentFilter filter = null;
			mActivityMonitor = getInstrumentation().addMonitor(filter, null, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// is the activity in the current activity stack?
	private boolean inActivityStack(Activity activity) {
		for (WeakReference<Activity> activityRef : RecordTest.this.mActivityStack) {
			if (activityRef.get().equals(activity)) {
				return true;
			}
		}
		return false;
	}
	/**
	* This is were the activityStack listener is set up. The listener will keep track of the
	* opened activities and their positions.
	*/

	private void setupActivityStackListener() {
		mActivitySyncTimer = new Timer();
		TimerTask activitySyncTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (RecordTest.this.mActivityMonitor != null) {
					Activity activity = RecordTest.this.mActivityMonitor.getLastActivity();
					Dialog dialog = TestUtils.findDialog(activity);
					if ((dialog != null) && (dialog != mCurrentDialog)) {
						mRecorder.interceptDialog(dialog);
						mCurrentDialog = dialog;
					}
					if (activity != null) {
						if (!RecordTest.this.mActivityStack.isEmpty()) {
							WeakReference<Activity> lastStackedActivity = RecordTest.this.mActivityStack.peek();
							if (lastStackedActivity.get() == null) {
								return;
							}
							if (lastStackedActivity.get().equals(activity)) {
								return;
							}
						}
						// if the activity matches the activity *previous* to the current activity, then we're going back to it
						int stackSize = RecordTest.this.mActivityStack.size();
						if (stackSize >= 2) {
							int i = 0;
							for (WeakReference<Activity> activityRef : RecordTest.this.mActivityStack) {
								Log.i(TAG, "activity[" + i +"] = " + activityRef.get().hashCode());
								if (activityRef.get().equals(activity)) {
									Log.i(TAG, "found previous activity index = " + i + " hashtag = " + activityRef.get().hashCode());
									removeActivityFromStack(activity);
									RecordTest.this.mActivityStack.pop();
									long time = SystemClock.uptimeMillis();
									mRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK + "," + time + "," + activity.getClass().getName());
									return;
								}
								i++;
							}
							Log.i(TAG, " currentActivity = " + activity.hashCode());
						} 
					
						if (activity.isFinishing()) {
							if (inActivityStack(activity)) {
								removeActivityFromStack(activity);
								long time = SystemClock.uptimeMillis();
								mRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK + "," + time + "," + activity.getClass().getName());
							}
						} else {
							addActivityToStack(activity);
							long time = SystemClock.uptimeMillis();
							mRecorder.writeRecord(Constants.EventTags.ACTIVITY_FORWARD + "," + time + "," + activity.getClass().getName());
						}
					}
				}
			}
		};
		mActivitySyncTimer.schedule(activitySyncTimerTask, 0, ACTIVITYSYNCTIME);
	}

	/**
	 * Removes a given activity from the activity stack
	 * 
	 * @param activity
	 *            the activity to remove
	 */
	private void removeActivityFromStack(Activity activity) {
		Iterator<WeakReference<Activity>> activityStackIterator = mActivityStack.iterator();
		while (activityStackIterator.hasNext()) {
			Activity activityFromWeakReference = activityStackIterator.next().get();
			if (activityFromWeakReference == null) {
				activityStackIterator.remove();
			}
			if (activity != null && activityFromWeakReference != null && activityFromWeakReference.equals(activity)) {
				activityStackIterator.remove();
			}
		}
	}

	/**
	* Adds an activity to the stack
	*
	* @param activity the activity to add
	*/

	private void addActivityToStack(Activity activity) {
		activity.runOnUiThread(new InterceptRunnable(activity));
		WeakReference<Activity> weakActivityReference = new WeakReference<Activity>(activity);
		
		if (mActivityStack.isEmpty()) {
			mfFirstActivityInitialized = true;
		}
		activity = null;
		mActivityStack.push(weakActivityReference);
	}
	
	/**
	 * get the package name for this activity.
	 * @param activity
	 * @return
	 */
	private String getPackageName(Activity activity) {
		try {
	        PackageManager pm = activity.getPackageManager();
	        PackageInfo packageInfo = pm.getPackageInfo(activity.getPackageName(), 0);
	        return packageInfo.packageName;
	    } catch (NameNotFoundException e) {
	    	return null;
	    }
	}

	/**
	 * when the activity is added to the stack, walk through the view hierarchy and intercept the listeners for each view.
	 * @author matreyno
	 *
	 */
	public class InterceptRunnable implements Runnable {
		protected Activity mActivity;
		
		public InterceptRunnable(Activity activity) {
			mActivity = activity;
		}
		
		public void run() {
			RecordTest.this.mRecorder.intercept(mActivity);
		}
	}
	public void testRecord() {

		try {
			do {
				Thread.sleep(100);
			} while (!mfFirstActivityInitialized && !mActivityStack.isEmpty());
		} catch (Exception ex) {
		}
		Log.i("foo", "foo");
	}
}
