package com.androidApp.EventRecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.androidApp.Utility.Constants;
import android.app.Activity;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.View;

/**
 * install the interception listeners in the view tree using some pretty cool reflection trickery.
 * @author matreyno
 *
 */
public class EventRecorder {
	protected static final String			TAG = "EventRecorder";
	public static final float				GUESS_IME_HEIGHT = 0.25F;			// guess that IME takes up this amount of the screen.
	protected BufferedWriter 				mRecordWriter;						// to write the events to file
	protected int 							mHashCode = 0x0;					// for fast tracking of view tree changes
	protected ViewReference					mViewReference;
	protected boolean						mIMEWasDisplayed = false;			// IME was displayed in the last layout
	protected boolean						mfVisualDebug = true;				// enable visual debugging.

	// constructor which opens the recording file, which is stashed somewhere on the sdcard.
	public EventRecorder(String recordFileName) throws IOException {				
		File extDir = Environment.getExternalStorageDirectory();
		File path = new File(extDir, recordFileName);
		path.delete();
		FileWriter fw = new FileWriter(path, true);
		mRecordWriter = new BufferedWriter(fw);
		mViewReference = new ViewReference();
	}
	
	public void addRdotID(Object rdotid) {
		mViewReference.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mViewReference.addRdotID(rdotstring);
	}
	
	public ViewReference getViewReference() {
		return mViewReference;
	}
	
	public boolean getVisualDebug() {
		return mfVisualDebug;
	}

	public void setVisualDebug(boolean f) {
		mfVisualDebug = f;
	}
	
	// write a record to the output
	public synchronized void writeRecord(String s)  {
		try {
			if (mRecordWriter != null) {
				mRecordWriter.write(s);
				mRecordWriter.newLine();
				mRecordWriter.flush();
			} else {
				Log.e(TAG, "record writer closed writing " + s);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	/**
	 * close the record writer.
	 */
	public synchronized void close() {
		try {
			mRecordWriter.close();
			mRecordWriter = null;
		} catch (IOException ioex) {
			
		}
	}
	
	/**
	 * write an event with time in milliseconds <event>:<time>
	 * @param event event to write out (from Constants.EventTags)
	 */
	public void writeRecordTime(String event) {
		long time = SystemClock.uptimeMillis();
		writeRecord(event + ":" + time);
	}
	
	// wrapper to write a record with an event, time and message to the system	
	public void writeRecord(String event, String message) {
		long time = SystemClock.uptimeMillis();
		writeRecord(event + ":" + time + "," + message);
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