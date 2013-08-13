package com.androidApp.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Intercept.CopyTextRunnable;
import com.androidApp.Intercept.InsertRecordWindowCallbackRunnable;
import com.androidApp.Intercept.MagicOverlay;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;


/**
 * class for intercepting activity transitions
 * @author mattrey
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ActivityInterceptor {
	protected static final String 	TAG = "ActivityInterceptor";
	protected static final long		ACTIVITY_WAIT_MSEC = 30000;
	protected EventRecorder			mEventRecorder;						// reference to the event record
	protected ViewInterceptor		mViewInterceptor;
	protected Instrumentation		mInstrumentation;					// to run things synchronized on the UI thread
	protected ActivityMonitor		mActivityMonitor;					// returns on activity event
	protected Stack<ActivityState>	mActivityStack;						// stack of activities and state
	private Thread					mActivityThread;					// to track the activity monitor thread
	protected boolean				mfHasStarted;						// the first activity has been observed.
	protected boolean				mfHasFinished;						// last activity was finished()
	protected boolean				mfPastFirstActivity;				// first activity might finish() immediately because it's a splash screen
	/**
	 * retain the activity information along with the activity.
	 * @author Matthew
	 *
	 */
	public class ActivityState {
		protected WeakReference<Activity> 		mActivityRef;			// so we don't hold onto activity when it's destroyed.
		protected int							mRotation;				// to track orientation
		protected Class<? extends Activity>		mClass;					// activity class
		protected String						mActivityName;			// activity name
		protected List<MagicOverlay>			mMagicOverlayList;		// list of overlays used in this activity
		
		public ActivityState(Activity activity) {
			mActivityRef = new WeakReference<Activity>(activity);
			Display display = activity.getWindowManager().getDefaultDisplay();
			mRotation = display.getRotation();
			mClass = activity.getClass();
			mActivityName = activity.toString();
			mMagicOverlayList = new ArrayList<MagicOverlay>();
		}
		
		public Activity getActivity() {
			return mActivityRef.get();
		}
		
		
		public void addMagicOverlay(MagicOverlay magicOverlay) {
			mMagicOverlayList.add(magicOverlay);
		}
		
		public List<MagicOverlay> getMagicOverlayList() {
			return mMagicOverlayList;
		}
		
		// NOTE: this isn't the best test in the world.  Sometimes activities get finished, and the WeakReference it nulled out. 
		// we n
		public boolean same(Activity activity) {
			if (mActivityRef.get() != null) {
				return activity == mActivityRef.get();
			} else {
				return false;
			}
		}
		
		public Class getActivityClass() {
			return mClass;
		}
	
		public String getActivityUniqueName() {
			return mActivityName;
		}
	}
	
	public ActivityInterceptor(EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mEventRecorder = recorder;
		mViewInterceptor = viewInterceptor;
		mfHasStarted = false;
		mfHasFinished = false;
		mfPastFirstActivity = false;
	}
	
	public EventRecorder getRecorder() {
		return mEventRecorder;
	}
	
	public ViewInterceptor getViewInterceptor() {
		return mViewInterceptor;
	}
	
	public boolean hasStarted() {
		return mfHasStarted;
	}
	
	public void setStarted(boolean f) {
		mfHasStarted = f;
	}
	
	public void setFinished(boolean f) {
		mfHasFinished = f;
	}
	
	public boolean hasFinished() {
		return mfHasFinished;
	}
	
	public void setupActivityStackListener(Instrumentation instrumentation) {
		IntentFilter filter = null;
		
		// Still doesn't handle the case of forward, rotate, go back to rotated pane.
		// need to initialize this before the runnable, so we don't miss the first activity
		// the activity monitor fires twice in the transition case, once in the back from last activity case, and three times
		// in the rotation case.
		mInstrumentation = instrumentation;
		mActivityMonitor = instrumentation.addMonitor(filter, null, false);
		mActivityStack = new Stack<ActivityState>();
		Runnable runnable = new Runnable() {
			public void run() {
				boolean fStart = true;
				boolean fPastFirstActivity = false;
				int currentRotation = Surface.ROTATION_0;
				while (fStart || !fPastFirstActivity || !ActivityInterceptor.this.isActivityStackEmpty()) {
					try {
						Activity activity = ActivityInterceptor.this.mActivityMonitor.waitForActivity();
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
							handleStartActivity(activity);
							fStart = false;
						} else if (fFinishing) {
							copyTextValues(activity);
							// finishing..remove from the stack.  if it's the last activity, then write that
							removeActivityFromStack(activity);
							if (isActivityStackEmpty()) {
								handleLastActivityFinish(activity);
							}
						} else {
								// if the activity is in the stack, and we're resuming it, then we write "goBack()" to the activity
							if (inActivityStack(activity)  && fResumed) {	
								
								// sometimes we get 2 starts, so if it's already on the activty stack, we ignore it.
								if (!isOnTopOfActivityStack(activity)) {
									handleActivityBack(activity);
								}
							} else if (isManualRotation(activity, currentRotation)) {
								
								// manual rotation can fire 2 or 3 events: one for the destruction, and 1 or 2 for the creation
								Log.i(TAG, "manual rotation " + activity);
								handleManualRotation(activity, currentRotation);
							} else if (fResumed) {
								handleNewActivity(activity);
								
								// splash screen case, where the first activity is destroyed, emptying stack
								if (activityStackDepth() > 1) {
									fPastFirstActivity = true;
								}
							}
							// ignore this activity.
						}
						
						// remove finished or nulled activities.
						cleanupActivityStack();
					} catch (Exception ex) {
						Log.e(TAG, "exception thrown in activity interceptor");
						ex.printStackTrace();
					}
				}
				mfHasFinished = true;
			}
		};
		mActivityThread = new Thread(runnable, "activityMonitorThread");
		mActivityThread.start();
	}
	
	public void handleNewActivity(Activity activity) {
		String logMsg =  activity.getClass().getName() + "," + activity.toString();
		mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
		pushActivityOnStack(activity);
		if (!MagicFrame.isAlreadyInserted(activity)) {
			MagicFrame.insertMagicFrame(mInstrumentation, peekActivityOnStack().getActivity(), mEventRecorder, mViewInterceptor);			
			mInstrumentation.runOnMainSync(new InterceptActivityRunnable(activity));
			mInstrumentation.runOnMainSync(new InsertRecordWindowCallbackRunnable(activity.getWindow(), mEventRecorder, mViewInterceptor));
			mInstrumentation.runOnMainSync(new CopyTextRunnable(mEventRecorder, activity));
		}
	}
	
	public void handleManualRotation(Activity activity, int newRotation) {
		mEventRecorder.writeRotation(activity, newRotation);
		replaceLastActivity(activity);
		if (!MagicFrame.isAlreadyInserted(activity)) {
			MagicFrame.insertMagicFrame(mInstrumentation, peekActivityOnStack().getActivity(), mEventRecorder, mViewInterceptor);			
			mInstrumentation.runOnMainSync(new InterceptActivityRunnable(activity));
			mInstrumentation.runOnMainSync(new InsertRecordWindowCallbackRunnable(activity.getWindow(), mEventRecorder, mViewInterceptor));
		}
	}
	
	public void handleStartActivity(Activity activity) {
		Log.i(TAG, "start case activity = " + activity);
		// tell instrumentation that it can go ahead and start the first activity.  We're ready for anything.
		synchronized (this) {
			this.notify();
		}
		handleNewActivity(activity);
		ActivityInterceptor.this.setStarted(true);
		// write out the package so we can do the correct import for the application
		// initialize the view reference with an activity so we can read the appropriate whitelist
		// for the target application's SDK
		try {
			mEventRecorder.getViewReference().initializeWithActivity(activity, mInstrumentation);
			String packageName = getPackageName(activity);
			mEventRecorder.writeRecord(Constants.EventTags.PACKAGE, packageName);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "getting package name");
		}
	}
	
	public void handleActivityBack(Activity activity) {
		if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
			mViewInterceptor.setLastKeyAction(-1);
			String logMsg = activity.getClass().getName() + "," + activity;
			mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK_KEY, logMsg);
		} else {
			String logMsg = activity.getClass().getName() + "," + activity;
			mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK, logMsg);
		}
	}
	
	
	public void handleLastActivityFinish(Activity activity) {
        long time = SystemClock.uptimeMillis();
		if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
			mViewInterceptor.setLastKeyAction(-1);
 			mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK_KEY + ":" + time);
		} else {
			String logMsg = activity.getClass().getName() + "," + "no previous activity";
			mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK + ":" + time);
		}
	}
	
	/**
	 * has the application been rotated manually, causing the activity to be destroyed and re-created?
	 * @param activity
	 * @param currentOrientation
	 * @return
	 */
	public boolean isManualRotation(Activity activity, int currentOrientation) {
		ActivityState topActivityState = peekActivityOnStack();
		if (topActivityState != null) {
			Activity topActivity = topActivityState.getActivity();
			if (topActivity.getClass() == activity.getClass()) {
				return (currentOrientation != topActivityState.mRotation) && !activityRequestedOrientation(activity, currentOrientation);
			}
		}
		return false;
	}
	
	/**
	 * see if the activity actually requested this orientation (note that we have rotations from the screen
	 * Note that the newRotation is 0,1,2,3 (0 and 2 are portrait, 1 and 3 are landscape), and the activity 
	 * orientation is the Activity.SCREEN_ORIENTATION enumeration.
	 * NOTE: we also need to check configChanges in the activity, since if they are handled through
	 * onConfigurationChanged(), then the activity is not destroyed
	 * @param activity
	 * @param newRotation
	 * @return
	 */
	private static boolean activityRequestedOrientation(Activity activity, int newRotation) {
		int orientation = activity.getRequestedOrientation();
		return (((orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) && (newRotation == 1)) ||
				((orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) && (newRotation == 0)) || 
				((orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) && (newRotation == 3)) ||
				((orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) && (newRotation == 2)));
	}

	/**
	 * replace the current activity in the stack. Since we're probably doing this because of rotation,
	 * save the rotation.
	 * @param activity
	 */
	public synchronized void replaceLastActivity(Activity activity) {
		ActivityState lastActivity = mActivityStack.pop();
		pushActivityOnStack(activity);
	}
	
	public synchronized boolean removeActivityFromStack(Activity activity) {
		ActivityState targetState = null;
		for (ActivityState cand : mActivityStack) {
			if (cand.getActivity() == activity) {
				targetState = cand;
			}
		}
		if (targetState != null) {
			mActivityStack.remove(targetState);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * push the activity onto the stack of WeakReferences. We use weak reference so we don't actually hold onto the activity
	 * after it's been finished.
	 * @param activity activity to add to the stack
	 */
	public synchronized void pushActivityOnStack(Activity activity) {
		mActivityStack.push(new ActivityState(activity));
	}
	
	/**
	 * pop the activity stack, and return the activity that was referenced by the top WeakReference
	 * @return activity from the top of the stack
	 */
	public synchronized ActivityState popActivityFromStack() {
		return mActivityStack.pop();
	}
	
	/**
	 * unroll the stack to the passed activity
	 * @param activity the current activity
	 */
	public void unwindStackPastActivity(Activity activity) {
		ActivityState activityState = mActivityStack.peek();
		while (!mActivityStack.empty()) {
			activityState = mActivityStack.pop();
			if (activityState.getActivity() == activity) {
				break;
			}
		}
	}
	
	public synchronized boolean isOnTopOfActivityStack(Activity a) {
		if (mActivityStack.isEmpty()) {
			return false;
		} else {
			return mActivityStack.peek().getActivity() == a;
		}
	}

	/**
	 * return the activity that's on the top of the stack
	 * @return top activity
	 */
	public synchronized ActivityState peekActivityOnStack() {
		if (mActivityStack.isEmpty()) {
			return null;
		} else {
			return mActivityStack.peek();
		}
	}
	

	public ActivityState activityClassInStack(Class<? extends Activity> activityClass) {
		for (ActivityState cand : mActivityStack) {
			if ((cand.getActivity() != null) && (cand.getActivity().getClass() == activityClass)) {
				return cand;
			}
		}
		return null;
	}
	
	/**
	 * the weak references go null after the activities are finished
	 */
	public void cleanupActivityStack() {
		List<ActivityState> removeList = new ArrayList<ActivityState>();
		int iPos = mActivityStack.size() - 1;
		for (ActivityState cand : mActivityStack) {
			if ((cand.getActivity() == null) || (cand.getActivity().isFinishing())) {
				removeList.add(cand);
				Log.i(TAG, "removing activity " + cand.getActivityUniqueName() + " at position " + iPos);
			}
			iPos--;
		}
		for (ActivityState remove : removeList) {
			mActivityStack.remove(remove);
		}
	}
	
	
	/***
	 * scan the activity stack to determine if an activity is in it.  
	 * @param activity activity to search for
	 * @return true if the activity is found
	 */
	public synchronized boolean inActivityStack(Activity activity) {
		for (ActivityState activityState : mActivityStack) {
			if (activityState.same(activity)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * is the activity stack empty?
	 * @return
	 */
	public synchronized boolean isActivityStackEmpty() {
		return mActivityStack.isEmpty();
	}
	
	/**
	 * what is the depth of the activity stack
	 * @return activity stack depth
	 */
	public synchronized int activityStackDepth() {
		return mActivityStack.size();
	}
	
	/**
	 * get the current activity returned from the activity monitor
	 * @return current activity
	 * TODO: we shouldn't check for null, we should ensure that the activity stack exists before trying to 
	 * pull the current activity.
	 */
	public synchronized Activity getCurrentActivity() {
		if ((mActivityStack != null) && !mActivityStack.isEmpty()) {
			return mActivityStack.peek().getActivity();
		} else {
			return null;
		}
	}
	
	/**
	 * get the package name for this activity.
	 * @param activity
	 * @return package name 
	 * @throws NameNotFoundException 
	 */
	private String getPackageName(Activity activity) throws NameNotFoundException {
        PackageManager pm = activity.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(activity.getPackageName(), 0);
        return packageInfo.packageName;
	}
	
	protected boolean isStopped(Activity activity) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(activity, Activity.class, Constants.Fields.STOPPED);
	}
	
	protected boolean isResumed(Activity activity) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(activity, Activity.class, Constants.Fields.RESUMED);
	}

	/**
	 * when the activity is added to the stack, walk through the view hierarchy and intercept the listeners for each view.
	 * @author mattrey
	 *
	 */
	public class InterceptActivityRunnable implements Runnable {
		protected Activity mActivity;
		
		public InterceptActivityRunnable(Activity activity) {
			mActivity = activity;
		}
		
		public void run() {
			ActivityInterceptor.this.getViewInterceptor().findMotionEventViews(mActivity);
			ActivityInterceptor.this.getViewInterceptor().intercept(mActivity);
		}
	}
	
	/**
	 * copy text values on activity finish
	 * @param activity
	 */
	public void copyTextValues(Activity activity) {
		mInstrumentation.runOnMainSync(new CopyTextRunnable(mEventRecorder, activity));
	}
}

