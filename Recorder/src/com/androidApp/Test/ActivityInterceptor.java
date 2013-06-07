package com.androidApp.Test;

import java.lang.ref.WeakReference;
import java.util.Stack;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Intercept.InsertRecordWindowCallbackRunnable;
import com.androidApp.Intercept.ViewInsertRecordWindowCallbackRunnable;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.IntentFilter;
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
 * class for intercepting activity events
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class ActivityInterceptor {
	protected static final String 	TAG = "ActivityInterceptor";
	protected EventRecorder			mEventRecorder;						// reference to the event record
	protected ViewInterceptor		mViewInterceptor;
	protected Instrumentation		mInstrumentation;					// to run things synchronized on the UI thread
	protected ActivityMonitor		mActivityMonitor;					// returns on activity event
	protected Stack<ActivityState>	mActivityStack;						// stack of activities and state
	private Thread					mActivityThread;					// to track the activity monitor thread
	protected boolean				mfHasStarted;						// the first activity has been observed.
	protected boolean				mfHasFinished;						// last activity was finished()
	/**
	 * retain the activity information along with the activity.
	 * @author Matthew
	 *
	 */
	protected class ActivityState {
		protected WeakReference<Activity> 		mActivityRef;			// so we don't hold onto activity when it's destroyed.
		protected int							mRotation;				// to track orientation
		protected Class<? extends Activity>		mClass;					// activity class
		protected String						mActivityName;			// activity name
		
		public ActivityState(Activity activity) {
			mActivityRef = new WeakReference<Activity>(activity);
			Display display = activity.getWindowManager().getDefaultDisplay();
			mRotation = display.getRotation();
			mClass = activity.getClass();
			mActivityName = activity.toString();
		}
		
		public Activity getActivity() {
			return mActivityRef.get();
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
	
	/**
	 * activity monitor hits always come in pairs.
	 * if the activities are the same (A, A), then it is going forward to that activity (A)
	 * if the activities are different (A, B) then it is going backwards from B to A 
	 */
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
				EventRecorder recorder = ActivityInterceptor.this.getRecorder();
				ViewInterceptor viewInterceptor = ActivityInterceptor.this.getViewInterceptor();
				boolean fStart = true;
				int currentRotation = Surface.ROTATION_0;
				while (fStart || !ActivityInterceptor.this.isActivityStackEmpty()) {
					Activity activityA = ActivityInterceptor.this.mActivityMonitor.waitForActivity();
					boolean fEventsHaveBeenFired = RecordListener.getEventLatch();
					// no events have been recorded yet for this activity.
					RecordListener.setEventLatch(false);
					if (fStart) {

						Log.i(TAG, "start case activity = " + activityA);
						// tell instrumentation that it can go ahead and start the first activity.  We're ready for anything.
						synchronized (this) {
							this.notify();
						}
						ActivityInterceptor.this.recordInitialActivity(recorder, viewInterceptor, activityA);
						ActivityInterceptor.this.setStarted(true);
						fStart = false;
						currentRotation  = activityA.getWindowManager().getDefaultDisplay().getRotation();
						mInstrumentation.runOnMainSync(new InsertRecordWindowCallbackRunnable(activityA.getWindow(), recorder, viewInterceptor));
						// write out the package so we can do the correct import for the application
						try {
							String packageName = getPackageName(activityA);
							ActivityInterceptor.this.getRecorder().writeRecord(Constants.EventTags.PACKAGE, packageName);
						} catch (Exception ex) {
							ActivityInterceptor.this.getRecorder().writeRecord(Constants.EventTags.EXCEPTION, ex.getMessage());
							ex.printStackTrace();
						}
					} else {
						if (!fEventsHaveBeenFired) {
							Log.i(TAG, "no UI recorded were fired to cause this activity transition");
						}
						int newRotation = activityA.getWindowManager().getDefaultDisplay().getRotation();
						if (isFinalActivityBack(activityA)) {
							Log.i(TAG, "first activity back case activity = " + activityA);
							ActivityInterceptor.this.recordFirstActivityBack(recorder);
							ActivityInterceptor.this.setFinished(true);
						} else {
							// the device has rotated in the activity transition, where one activity is destroyed, and another created
							// with the rotated layout.
							if (newRotation != currentRotation) {
								currentRotation = newRotation;
								Log.i(TAG, "rotation case activity = " + activityA);
								ActivityInterceptor.this.recordRotation(recorder, viewInterceptor, activityA, newRotation);
							} else {
								Activity activityB = ActivityInterceptor.this.mActivityMonitor.waitForActivity();
								// if the activity was in the stack, then we are going back to it, otherwise we're
								// going forward to a new activity.
								if (ActivityInterceptor.this.inActivityStack(activityA)  && ActivityInterceptor.this.inActivityStack(activityB)) {
									Log.i(TAG, "normal back case activity = " + activityA);
									ActivityInterceptor.this.recordActivityBack(recorder, activityA, activityB, currentRotation);
								} else {
									if (activityA.equals(activityB)) {
										Log.i(TAG, "we cannot go forward to the same activity, this shouldn't happen");
									}
									Activity newActivity = activityB;
									if (ActivityInterceptor.this.inActivityStack(activityB)) {
										newActivity = activityA;
									}
									if (!activityA.isFinishing() && !activityB.isFinishing()) {
										Log.i(TAG, "forward case activity = " + newActivity);
										ActivityInterceptor.this.recordActivityForward(recorder, viewInterceptor, newActivity);
										mInstrumentation.runOnMainSync(new InsertRecordWindowCallbackRunnable(newActivity.getWindow(), recorder, viewInterceptor));
									}
								}
							}
						}
					}
				}
			}
		};
		mActivityThread = new Thread(runnable, "activityMonitorThread");
		mActivityThread.start();
	}
	
	/**
	 * if the activity is finishing, and getChangingConfigurations() is zero, and there's only one activity in the stack, and this is
	 * that last activity, then we are hitting the back key for the last time
	 * @param activityA candidate activity
	 * @return true if it's backing out of the last activity in the stack.
	 */
	public boolean isFinalActivityBack(Activity activityA) {
		return activityA.isFinishing() && (activityA.getChangingConfigurations() == 0x0) && (activityStackSize() == 1) && inActivityStack(activityA);
	}
	
	/**
	 * record the initial activity start, which we mark as "activity_forward"
	 * @param recorder event recorder
	 * @param activityA first activity returned from the activity monitor
	 */
	public void recordInitialActivity(EventRecorder recorder, ViewInterceptor viewInterceptor, Activity activityA) {
		pushActivityOnStack(activityA);
		// intercept events on the newly created activity.
		mInstrumentation.runOnMainSync(new InterceptRunnable(activityA));
		String logMsg = activityA.getClass().getName() + "," + activityA.toString();
		recorder.writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
		
		// we get 2 fires on the first activity
		Activity activityB = mActivityMonitor.waitForActivity();
		if (activityB != activityA) {
			recorder.writeRecord(Constants.EventTags.EXCEPTION, "first activities did not match");
		} else {
			MagicFrame.insertMagicFrame(mInstrumentation, activityA, recorder, viewInterceptor);
		}
	}
	
	/**
	 * record a rotation, which shows up as the activity being destroyed, re-created, and the display rotation changing.
	 * This creates 3 activity events rather than the normal 2
	 * @param recorder event recorder
	 * @param activityA activity returned from first waitForActivity() call
	 * @param newRotation current rotation returned from display (Surface.ROTATION_0, etc)
	 */
	public void recordRotation(EventRecorder recorder, ViewInterceptor viewInterceptor, Activity activityA, int newRotation) {
		Activity activityB = mActivityMonitor.waitForActivity();
		recorder.writeRotation(activityB, newRotation);
		replaceLastActivity(activityB);
		Activity activityBAgain = mActivityMonitor.waitForActivity();
		if (activityBAgain != activityB) {
			recorder.writeRecord(Constants.EventTags.EXCEPTION, "rotated activities did not match");
		}
		MagicFrame.insertMagicFrame(mInstrumentation, activityB, recorder, viewInterceptor);
	}
	
	/**
	 * record the "back" from the first activity, so we don't write out the previous activity reference
	 * @param recorder event recorder.
	 */
	public void recordFirstActivityBack(EventRecorder recorder) {
		long time = SystemClock.uptimeMillis();
		
		// activity was finished by the user hitting the back key
		if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
			mViewInterceptor.setLastKeyAction(-1);
			String logMsg = Constants.EventTags.ACTIVITY_BACK_KEY + ":" + time;
			recorder.writeRecord(logMsg);
		} else {
			String logMsg = Constants.EventTags.ACTIVITY_BACK + ":" + time;
			recorder.writeRecord(logMsg);
		}
		// this will cause the calling loop to terminate due to an empty stack
		popActivityFromStack();
	}
	
	/**
	 * record the "back" event from the middle of the activity stack. There is a special case for rotation, where if the
	 * new activity is different from the old one, and the rotation has changed from the previous activity, then the old
	 * activity is destroyed, and replaced with the new rotated activity.  This causes 3 activity monitor events to fire
	 * instead of the normal 2
	 * @param recorder event recorder
	 * @param activityA activity returned from first waitForActivity() call
	 * @param activityB activity returned from the second waitForActivity() call
	 * @param currentRotation rotation polled from display
	 */

	public void recordActivityBack(EventRecorder recorder, Activity activityA, Activity activityB, int currentRotation) {
		popActivityFromStack();
		ActivityState previousActivityState = peekActivityOnStack();
		if ((activityA != activityB) && (previousActivityState != null) && (currentRotation != previousActivityState.mRotation)) {
			Activity activityBAgain = mActivityMonitor.waitForActivity();
			
			// I'm not sure exactly where this activity comes from.  I need to track it down and verify it
			Activity activityBAgainAgain = mActivityMonitor.waitForActivity();
			replaceLastActivity(activityBAgain);
			previousActivityState = peekActivityOnStack();
			Log.i(TAG, "rotated back case replacing activity " + activityBAgain);
		}
		// activity was finished by the user hitting the back key
		if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
			mViewInterceptor.setLastKeyAction(-1);
			if (previousActivityState != null) {
				String logMsg = previousActivityState.getActivityClass().getName() + "," + previousActivityState.getActivityUniqueName();
				recorder.writeRecord(Constants.EventTags.ACTIVITY_BACK_KEY, logMsg);
			} else {
				String logMsg = previousActivityState.getActivityClass().getName() + "," + "no previous activity";
				recorder.writeRecord(Constants.EventTags.ACTIVITY_BACK_KEY, logMsg);		
			}
		} else {
			// activity was finished by some other method
			if (previousActivityState != null) {
				String logMsg = previousActivityState.getActivityClass().getName() + "," + previousActivityState.getActivityUniqueName();
				recorder.writeRecord(Constants.EventTags.ACTIVITY_BACK, logMsg);
			} else {
				String logMsg = previousActivityState.getActivityClass().getName() + "," + "no previous activity";
				recorder.writeRecord(Constants.EventTags.ACTIVITY_BACK, logMsg);
			}
		}
	}
	
	/**
	 * if the activity isn't in the "back stack", then it is a new activity and should be pushed onto the stack
	 * @param recorder event recorder
	 * @param activityB activity returned from second waitForActivity() call
	 */
	public void recordActivityForward(EventRecorder recorder, ViewInterceptor viewInterceptor, Activity activityB) {
		pushActivityOnStack(activityB);
		// intercept events on the newly created activity.
		mInstrumentation.runOnMainSync(new InterceptRunnable(activityB));
		String logMsg =  activityB.getClass().getName() + "," + activityB.toString();
		recorder.writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
		MagicFrame.insertMagicFrame(mInstrumentation, activityB, recorder, viewInterceptor);
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

	/**
	 * when the activity is added to the stack, walk through the view hierarchy and intercept the listeners for each view.
	 * @author mattrey
	 *
	 */
	public class InterceptRunnable implements Runnable {
		protected Activity mActivity;
		
		public InterceptRunnable(Activity activity) {
			mActivity = activity;
		}
		
		public void run() {
			ActivityInterceptor.this.getViewInterceptor().intercept(mActivity);
		}
	}
}

