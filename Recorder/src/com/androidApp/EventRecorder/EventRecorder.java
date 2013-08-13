package com.androidApp.EventRecorder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;
import com.androidApp.recorderInterface.EventRecorderInterface;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.View;

/**
 * interface to the log service, since instrumentation can't do its own permissions
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class EventRecorder extends EventRecorderInterface {
	protected static final String				TAG = "EventRecorder";
	protected int 								mHashCode = 0x0;					// for fast tracking of view tree changes
	protected ViewReference						mViewReference;
	protected boolean							mfVisualDebug = true;				// enable visual debugging.
	protected Context							mContext;							// to send requests to service
	protected Instrumentation					mInstrumentation;					// handy to get references to the test code
	protected List<ViewDirective>				mViewDirectiveList;					// list of "directives" to apply on record
	protected List<Class <? extends Activity>> 	mInterstitialActivityList;			// list of random popup activities, for ads and such
	protected Hashtable<String,String> 			mVariableTable;						// variable hashtable
	protected boolean							mEventWasRecorded;					// indicate that click/key event was recorded (or not)
	/**
	 * constructor which opens the recording file, which is stashed somewhere on the sdcard.
	 * @param instrumentation instrumentation handle
	 * @param context context of application being tested
	 * @param recordFileName output filename on the sdcard
	 * @param directiveFileName name of directive file
	 * @param fBinary target application is binary: Object references must be resolved to android public classes
	 * @throws IOException
	 */
	public EventRecorder(Instrumentation 	instrumentation, 
					     Context 			context, 
						 String 			recordFileName, 
						 String 			directiveFileName, 
						 boolean 			fBinary) throws ReferenceException, ClassNotFoundException, IOException {
		super(context, recordFileName, directiveFileName);
		mContext = context;
		mInstrumentation = instrumentation;
		mViewReference = new ViewReference(instrumentation, fBinary);
		mVariableTable = new Hashtable<String,String>();
		mViewDirectiveList = new ArrayList<ViewDirective>();
		try {
			InputStream isViewDirective  = instrumentation.getContext().getAssets().open(Constants.Asset.VIEW_DIRECTIVES);
			if (isViewDirective != null) {
				mViewDirectiveList = ViewDirective.readViewDirectiveList(isViewDirective);
			}
		} catch (FileNotFoundException fnfex) {
			Log.i(TAG, "no view directives were specified");
		}
		try {
			InputStream isInterstitialActivityList  = instrumentation.getContext().getAssets().open(Constants.Asset.INTERSTITIAL_ACTIVITY_LIST);
			mInterstitialActivityList = InterstialActivity.readActivityClassList(isInterstitialActivityList);
		} catch (FileNotFoundException fnfex) {
			Log.i(TAG, "no interstitial activities were specified");
		}
	}
	
	/**
	 * wrapper functions to add references for id's and strings.
	 * @param rdotid
	 */
	public void addRdotID(Object rdotid) {
		mViewReference.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mViewReference.addRdotID(rdotstring);
	}
	
	/**
	 * return the handle to the view reference generator 
	 * @return the view reference generator for this event recorder
	 */
	public ViewReference getViewReference() {
		return mViewReference;
	}
	
	public Instrumentation getInstrumentation() {
		return mInstrumentation;
	}
	
	/**
	 * enable/disable visual debugging
	 * @return
	 */
	public boolean getVisualDebug() {
		return mfVisualDebug;
	}

	public void setVisualDebug(boolean f) {
		mfVisualDebug = f;
	}

	/**
	 * When interacting with the low-level event interceptors, we listen to key and click events, and set a flag
	 * saying  "this event hasn't been recorded yet", when the event recorder writes the event to the log file,
	 * we unset the flag, but if the event wasn't recorded in a "short period of time", or by when the next event
	 * comes in, we bring up a little toast informing the user of this unfortunate situation.
	 * @return
	 */
	public boolean eventWasRecorded() {
		return mEventWasRecorded;
	}
		
	public void setEventRecorded(boolean f) {
		mEventWasRecorded = f;
	}
	
	// write a record to the output
	public synchronized void writeRecord(String s)  {
		mEventWasRecorded = true;
		writeLog(mRecordFileName, s);
	}
		
	/**
	 * write an event with time in milliseconds <event>:<time>
	 * @param event event to write out (from Constants.EventTags)
	 */
	public void writeRecordTime(String event) {
		mEventWasRecorded = true;
		long time = SystemClock.uptimeMillis();
		writeLog(mRecordFileName, event + ":" + time);
	}
	
	// wrapper to write a record with an event, time and message to the system	
	public void writeRecord(String event, String message) {
		mEventWasRecorded = true;
		long time = SystemClock.uptimeMillis();
		writeLog(mRecordFileName, event + ":" + time + "," + message);
	}
	
	// for copy and paste (from DirectiveIDalogs)
	public String getVariableValue(String var) {
		return mVariableTable.get(var);
	}
	
	public void setVariableValue(String var, String value) {
		mVariableTable.put(var, value);
	}
	
	// add a view directive to a a view (like clear the text, enter text key by key
	public void addViewDirective(ViewDirective viewDirective) {
		mViewDirectiveList.add(viewDirective);
		writeLog(mDirectiveFileName, viewDirective.toString());
		
	}
	
	// "this is an interstitial activity.  It doesn't come up all the time, because we need to annoy our
	// users with popup advertisements.
	public void addInterstitialActivity(Activity activity) {
		if (mInterstitialActivityList == null) {
			mInterstitialActivityList = new ArrayList<Class<? extends Activity>>();
		}
		mInterstitialActivityList.add(activity.getClass());
	}
	
	// don't want to double-write interstitial activities.
	public boolean isInterstitialActivity(Activity activity) {
		if (mInterstitialActivityList == null) {
			return false;
		} else {
			return mInterstitialActivityList.contains(activity.getClass());
		}
	}
	
	/**
	 * get the list of view directives for this activity, and this phase of execution.
	 * @param activity activity to filter on
	 * @param when activity_start, activity_finish, or value_changed (that'll be tricky
	 * @return filtered list of view directives.
	 */
	public List<ViewDirective> getMatchingViewDirectives(Activity activity, ViewDirective.When when) {
		List<ViewDirective> filteredList = new ArrayList<ViewDirective>();
		for (ViewDirective viewDirective : mViewDirectiveList) {
			UserDefinedViewReference reference = viewDirective.getReference();
			if (reference.matchActivity(activity) && (viewDirective.mWhen == when)) {
				filteredList.add(viewDirective);
			}
		}
		return filteredList;
	}	
	
	/**
	 * does this view, its preorder index, and the operation. match a directive in the list of view directives?
	 * @param view view to match
	 * @param viewIndex index of the view in a preorder traversal of the view hierarchy 
	 * @param operation view directive operation
	 * @param viewDirectiveList list of directives for this activity (filtered)
	 * @return
	 */
	public static boolean matchViewDirective(View 					      	view, 
											 int						  	viewIndex,
											 ViewDirective.ViewOperation 	operation,
											 ViewDirective.When				when,
											 List<ViewDirective> 		  	viewDirectiveList) {
		for (ViewDirective viewDirective : viewDirectiveList) {
			if (viewDirective.match(view, viewIndex, operation, when)) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * match against the internal list
	 * @param view view to match
	 * @param viewIndex its index within the view hierarchy: NOTE: BE CAREFUL TO NOT USE THE SAME VIEW MATCHER AS EVENTS
	 * The Robotium stuff only indexes views based on whats visible, not on the entire view hierarchy
	 * @param operation operaton to perform
	 * @param when start, end, or always
	 * @return true/false
	 */
	public boolean matchViewDirective(View 					      	view, 
									  int						  	viewIndex,
									  ViewDirective.ViewOperation 	operation,
									  ViewDirective.When			when) {
		for (ViewDirective viewDirective : mViewDirectiveList) {
			if (viewDirective.match(view, viewIndex, operation, when)) {
				return true;
			}
		}
		return false;
	}
	
	// wrapper for wrapper to write a record with an event, time view description, and message to the system	
	public void writeRecord(String event, View v, String message) {
		try {
			long time = SystemClock.uptimeMillis();
			int viewIndex = TestUtils.classIndex(v.getRootView(), v);
			if (!matchViewDirective(v, viewIndex, ViewDirective.ViewOperation.IGNORE_EVENTS, ViewDirective.When.ALWAYS)) {
				writeRecord(event + ":" + time + "," + getViewReference().getReference(v) + "," + message);
			}
			mEventWasRecorded = true;
		} catch (Exception ex) {
			// TEMPORARY: the IME keeps sending events long after we have died died died,
			// so we suppress the exceptions
			if (!event.equals(Constants.EventTags.SHOW_IME) && 
				!event.equals(Constants.EventTags.HIDE_IME) &&
				!event.equals(Constants.EventTags.HIDE_IME_BACK_KEY)) {
				writeRecord(Constants.EventTags.EXCEPTION, "while getting reference for view in event " + event + " " + message);

			}
		}
	}
	
	// yet another wrapper with just a view to be described.
	public void writeRecord(String event, View v) {
		try {
			long time = SystemClock.uptimeMillis();
			int viewIndex = TestUtils.classIndex(v.getRootView(), v);
			if (!matchViewDirective(v, viewIndex, ViewDirective.ViewOperation.IGNORE_EVENTS, ViewDirective.When.ALWAYS)) {
				writeRecord(event + ":" + time + "," + getViewReference().getReference(v));
			}
			mEventWasRecorded = true;
		} catch (Exception ex) {
			writeException(ex,  "while getting reference for view in event " + event);
		}
	}
	
	/**
	 * to enforce consistent exception logging
	 * @param ex the offending exception
	 * @param message descriptive message
	 */
	public void writeException(Exception ex, String message) {
		writeRecord(Constants.EventTags.EXCEPTION, ex.getMessage() + ": " + message);
		ex.printStackTrace();
	}
	
	/**
	 * write exception: (view parameter variant) all exceptions should go through this interface
	 * @param ex exception to write
	 * @param v view to generate reference from
	 * @param message error message in addition to exception message
	 */
	public void writeException(Exception ex, View v, String message) {
		try {
			writeRecord(Constants.EventTags.EXCEPTION, ex.getMessage() + ": " + getViewReference().getReference(v) + ": " + message);
		} catch (Exception ex3) {
			writeException(ex3,  "while getting reference for view " + v);
		}
		ex.printStackTrace();
	}
	/**
	 * write out the rotation expression: rotation:<time>,<rotation:0,90,180,270>,activity,activity_description
	 * @param recorder event recorder reference
	 * @param activity
	 * @param rotation
	 */
	public void writeRotation(Activity activity, int rotation) {
		int rotationValue = 0;
		if (rotation == Surface.ROTATION_0) {
			rotationValue = 0;
		} else if (rotation == Surface.ROTATION_90) {
			rotationValue = 90;
		} else if (rotation == Surface.ROTATION_180) {
			rotationValue = 180;
		} else if (rotation == Surface.ROTATION_270) {
			rotationValue = 270;
		}
		String logMsg = Integer.toString(rotationValue) + "," + activity.getClass().getName() + "," + activity.toString();
		writeRecord(Constants.EventTags.ROTATION, logMsg);
	}
}