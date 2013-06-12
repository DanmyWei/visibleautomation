package com.androidApp.EventRecorder;

import java.io.IOException;

import com.androidApp.Utility.Constants;
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
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class EventRecorder extends EventRecorderInterface {
	protected static final String	TAG = "EventRecorder";
	public static final float		GUESS_IME_HEIGHT = 0.25F;						// guess that IME takes up this amount of the screen.
	protected int 					mHashCode = 0x0;								// for fast tracking of view tree changes
	protected ViewReference			mViewReference;
	protected boolean				mfVisualDebug = true;							// enable visual debugging.
	protected Context				mContext;										// to send requests to service
	// constructor which opens the recording file, which is stashed somewhere on the sdcard.
	public EventRecorder(Instrumentation instrumentation, Context context, String recordFileName) throws IOException {
		super(context, recordFileName);
		mContext = context;
		mViewReference = new ViewReference(instrumentation);
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
	
	public ViewReference getViewReference() {
		return mViewReference;
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
		
	// write a record to the output
	public synchronized void writeRecord(String s)  {
		writeLog(mRecordFileName, s);
	}
		
	
	// wrapper for wrapper to write a record with an event, time view description, and message to the system	
	public void writeRecord(String event, View v, String message) {
		long time = SystemClock.uptimeMillis();
		try {
			writeRecord(event + ":" + time + "," + getViewReference().getReference(v) + "," + message);
		} catch (Exception ex) {
			writeRecord(Constants.EventTags.EXCEPTION, "while getting reference for view in event " + event + " " + message);
		}
	}
	
	// yet another wrapper with just a view to be described.
	public void writeRecord(String event, View v) {
		long time = SystemClock.uptimeMillis();
		try {
			writeRecord(event + ":" + time + "," + getViewReference().getReference(v));
		} catch (Exception ex) {
			writeRecord(Constants.EventTags.EXCEPTION, "while getting reference for view in event " + event);
		}
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