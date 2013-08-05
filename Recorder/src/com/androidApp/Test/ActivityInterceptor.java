package com.androidApp.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Intercept.InsertRecordWindowCallbackRunnable;
import com.androidApp.Intercept.MagicOverlay;
import com.androidApp.Intercept.ViewInsertRecordWindowCallbackRunnable;
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
	
	public boolean getFinished() {
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
				boolean fPreviousActivityFinished = false;
				boolean fPastFirstActivity = false;
				int currentRotation = Surface.ROTATION_0;
				WeakReference<Activity> activityRefBackedTo = null;

				while (fStart || !fPastFirstActivity || !ActivityInterceptor.this.isActivityStackEmpty()) {
					try {
						Activity activity = ActivityInterceptor.this.mActivityMonitor.waitForActivity();
						Log.i(TAG, "activity = " + activity + 
								   " finished = " + activity.isFinishing() +
								   " stopped = " + isStopped(activity) + 
								   " resumed = " + isResumed(activity) + 
								   " flags = 0x" + Integer.toHexString(activity.getIntent().getFlags()));
						cleanupActivityStack();
						currentRotation  = activity.getWindowManager().getDefaultDisplay().getRotation();
						if (fStart) {
							handleStartActivity(activity);
							fStart = false;
						} else if (activity.isFinishing()) {
							removeActivityFromStack(activity);
						} else {
							if (inActivityStack(activity)  && isResumed(activity)) {	
								handleActivityBack(activity);
							} else if (isManualRotation(activity, currentRotation)) {
								Log.i(TAG, "manual rotation " + activity);
								handleManualRotation(activity, currentRotation);
							} else if (isResumed(activity)) {
								handleNewActivity(activity);
							}
						}
					} catch (Exception ex) {
						Log.e(TAG, "exception thrown in activity interceptor");
						ex.printStackTrace();
					}
				}
			}
		};
		mActivityThread = new Thread(runnable, "activityMonitorThread");
		mActivityThread.start();
	}
		
	public void handleClearTop(Activity activity) {
		if (!isOnTopOfActivityStack(activity)) {
			if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
				mViewInterceptor.setLastKeyAction(-1);
				String logMsg = activity.getClass().getName() + "," + activity;
				mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK_KEY, logMsg);
			} else {
				String logMsg = activity.getClass().getName() + "," + activity;
				mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_BACK, logMsg);
			}
			if (!MagicFrame.isAlreadyInserted(activity)) {
				MagicFrame.insertMagicFrame(mInstrumentation, activity, mEventRecorder, mViewInterceptor);			
				mInstrumentation.runOnMainSync(new InterceptActivityRunnable(activity));
			}
		}
	}
	
	public void handleNewActivity(Activity activity) {
		String logMsg =  activity.getClass().getName() + "," + activity.toString();
		mEventRecorder.writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
		pushActivityOnStack(activity);
		if (!MagicFrame.isAlreadyInserted(activity)) {
			MagicFrame.insertMagicFrame(mInstrumentation, peekActivityOnStack().getActivity(), mEventRecorder, mViewInterceptor);			
			mInstrumentation.runOnMainSync(new InterceptActivityRunnable(activity));
			mInstrumentation.runOnMainSync(new InsertRecordWindowCallbackRunnable(activity.getWindow(), mEventRecorder, mViewInterceptor));
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
	
	public boolean isManualRotation(Activity activity, int currentOrientation) {
		ActivityState topActivityState = peekActivityOnStack();
		Activity topActivity = topActivityState.getActivity();
		if (topActivity.getClass() == activity.getClass()) {
			return (currentOrientation != topActivityState.mRotation) && !activityRequestedOrientation(activity, currentOrientation);
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
	 * so, how big is it, really?  
	 * @return
	 */
	public synchronized int activityStackSize() {
		return mActivityStack.size();
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
	/**
	 * return the activity that's below the top of the stack
	 * @return top activity
	 */
	public synchronized ActivityState peekPreviousActivityOnStack() {
		if (mActivityStack.size() < 2) {
			return null;
		} else {
			return mActivityStack.get(mActivityStack.size() - 2);
		}
	}
	
	/**
	 * rather than popping the stack, find the activity, for certain
	 * @param a
	 * @return
	 */
	public synchronized ActivityState findPreviousActivityOnStack(Activity a) {
		for (ActivityState cand : mActivityStack) {
			if (cand.getActivity() == a) {
				return cand;
			}
		}
		return null;
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
			if (cand.getActivity() == null) {
				removeList.add(cand);
				Log.i(TAG, "removing activity " + cand.getActivityUniqueName() + " at position " + iPos);
			}
			iPos--;
		}
		for (ActivityState remove : removeList) {
			mActivityStack.remove(remove);
		}
	}
	
	
	/**
	 * activities can be returned from the activity monitor out of order. We need to know what order they are
	 * actually in
	 * @param a first activity returned from ActivityMonitor
	 * @param b second activity returned from ActivityMonitor
	 * @return
	 */
	public synchronized ActivityState findEarliestActivityOnStack(Activity a, Activity b) {
		int stackDepthA = -1, stackDepthB = -1;
		ActivityState stateA = null, stateB = null;
		for (int stackDepth = 0; stackDepth < mActivityStack.size(); stackDepth++) {
			// list: stacks top is at end of list.
			ActivityState cand = mActivityStack.get(mActivityStack.size() - 1 - stackDepth);
			if (cand.getActivity() == a) {
				stackDepthA = stackDepth;
				stateA = cand;
			}
			if (cand.getActivity() == b) {
				stackDepthB = stackDepth;
				stateB = cand;
			}
		}
		if (stackDepthA > stackDepthB) {
			return stateA;
		} else {
			return stateB;
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
	 * when the intent flag FLAG_ACTIVITY_CLEAR_TOP is passed to the activity when startActivity is called,
	 * then if the activty is in the stack, then all the activities between it and the activity are cleared
	 * so we unroll the stack to the matching activity CLASS, because unless onNewIntent() is defined for that
	 * activity, it will launch a new activity and replace the old activity
	 */

	protected boolean isClearTop(Activity activity, Activity activityJustFinished) {
		Intent intent = activity.getIntent();
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0x0) {
			return (activityClassInStack(activity.getClass()) != null) || 
					((activityJustFinished != null) && (activity.getClass() == activityJustFinished.getClass()));
		}
		return false;
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
}

