package com.androidApp.SupportTest;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.SupportIntercept.InterceptSupport;
import com.androidApp.Test.IRecordTest;
import com.androidApp.Test.SetupListeners;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FileUtils;
import com.androidApp.Utility.SaveState;
import com.androidApp.randomtest.RandTest;

import android.app.Activity;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;


/**
 * record events in an activity. In short, be awesome.
 * @author matthew
 *
 * @param <T> activity being subjected to recording
 * This uses a thread which waits on events from an activity monitor to track activity forward and back events.  
 * When we navigate forward to an activity, we add intercept listeners to the events on the view hierarchy.  The interceptRunnable
 * also sets up a view hierarchy (Layout listener) listener which re-traverses the view hierarchy and adds record listeners
 * for newly created views.
 * Since dialogs can be popped up at any time, and they aren't picked up by the layout listener, we had to create a timer task
 * which polls for newly created dialogs in the current activity.  Unfortunately, the event handlers are member functions of
 * activity, so we can't intercept them, except with methods that are highly intrusive.
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public abstract class RecordTestBinary extends ActivityInstrumentationTestCase2 implements IRecordTest {
	private static final String 				TAG = "RecordTestBinary";
	protected SetupListeners					mSetupListeners;
	protected List<UserDefinedViewReference>	mMotionEventViewReferences = null;	// user-defined references to listen for motion events
	protected List<String>						mInterstitialActivities = null;		// user-defined list of activities which start by random, such as ads & stuff
	
	public RecordTestBinary(String activityName) throws IOException {
		super(null);
 	}
		
	public void initialize(String activityName) throws Exception { 
		// read the view specifications that should listen to motion events
		
        try {
        	InputStream isMotionEvents  = getInstrumentation().getContext().getAssets().open(Constants.Asset.USER_MOTION_EVENT_VIEWS);
        	mMotionEventViewReferences = UserDefinedViewReference.readViewReferences(isMotionEvents);
        } catch (Exception ex) {
        	Log.i(TAG, "did not read any user-defined motion event files");
        }
        try {
        	InputStream isInterstitialActivities = getInstrumentation().getContext().getAssets().open(Constants.Asset.INTERSTITIAL_ACTIVITIES);
        	mInterstitialActivities = FileUtils.readLines(isInterstitialActivities);
        } catch (Exception ex) {
        	Log.i(TAG, "did not read any user-defined interstitial activities");
        }
        InterceptSupport interceptSupport = new InterceptSupport();
       
		mSetupListeners = new SetupListeners(getInstrumentation(), interceptSupport, activityName, this, true);
		SaveState.backupDatabases(getInstrumentation().getTargetContext());
	}


	public abstract void initializeResources();
	

	public void tearDown() throws Exception {
		Log.i(TAG, "tear down");
	}
	
	public void testRecord() {
		mSetupListeners.testRecord();
	}
	public List<UserDefinedViewReference> getMotionEventViewReferences() {
		return mMotionEventViewReferences;
	}
	
	public List<String> getInterstitialActivityNames() {
		return mInterstitialActivities;
	}

}

