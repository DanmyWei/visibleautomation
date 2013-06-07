package com.androidApp.Test;

import java.io.IOException;
import android.app.Activity;
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
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public abstract class RecordTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
	private static final String 			TAG = "RecordTest";
	protected SetupListeners					mSetupListeners;
	protected static Class<? extends Activity>	sActivityClass;
	
	
    public RecordTest(Class<T> activityClass) throws IOException {
        super(activityClass);
        sActivityClass = activityClass;
    }

	public void setUp() throws Exception { 
		super.setUp();
		mSetupListeners = new SetupListeners(getInstrumentation(), sActivityClass);
	}

    public abstract void initializeResources();

    // add the resource id references for id's and strings.
    public void addRdotID(Object rdotid) {
        mSetupListeners.getRecorder().addRdotID(rdotid);
    }

    public void addRdotString(Object rdotstring) {
    	mSetupListeners.getRecorder().addRdotString(rdotstring);
    }

	public void tearDown() throws Exception {
		Log.i(TAG, "tear down");
	}
	
	public void testRecord() {
		mSetupListeners.testRecord();
	}
}
