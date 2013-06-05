package com.androidApp.Test;


import java.io.IOException;

import android.app.Activity;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;


/**
 * record events in an activity. In short, be awesome.
 * @author matreyno
 *
 * @param <T> activity being subjected to recording
 * This uses a thread which waits on events from an activity monitor to track activity forward and back events.  
 * When we navigate forward to an activity, we add intercept listeners to the events on the view hierarchy.  The interceptRunnable
 * also sets up a view hierarchy (Layout listener) listener which re-traverses the view hierarchy and adds record listeners
 * for newly created views.
 * Since dialogs can be popped up at any time, and they aren't picked up by the layout listener, we had to create a timer task
 * which polls for newly created dialogs in the current activity.  Unfortunately, the event handlers are member functions of
 * activity, so we can't intercept them, except with methods that are highly intrusive.
 */
public abstract class RecordTestBinary extends ActivityInstrumentationTestCase2 {
	private static final String 				TAG = "RecordTestBinary";
	protected SetupListeners					mSetupListeners;
	protected static Class<? extends Activity>	sActivityClass;
	
	public RecordTestBinary(String activityName) throws IOException {
		super(sActivityClass);
	}
	
	public RecordTestBinary() throws IOException {
		super(sActivityClass);
	}
	
	public void setUp() throws Exception { 
		super.setUp();
		mSetupListeners = new SetupListeners(getInstrumentation(), sActivityClass);
	}


	public abstract void initializeResources();
	

	public void tearDown() throws Exception {
		Log.i(TAG, "tear down");
	}
	
	public void testRecord() {
		mSetupListeners.testRecord();
	}
}
