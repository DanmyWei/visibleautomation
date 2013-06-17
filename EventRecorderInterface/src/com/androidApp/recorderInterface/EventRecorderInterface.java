package com.androidApp.recorderInterface;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * interface to the log service, since instrumentation can't do its own permissions.  We also call this from
 * the keyboard service.
 * @author Matthew Reynolds
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class EventRecorderInterface {
	public static final String 		LOGSERVICE = "com.androidApp.logService";
	public static final String 		LOGSERVICE_INITIALIZE = "com.androidApp.LogService.initialize";
	public static final String 		LOGSERVICE_FILENAME = "filename";
	public static final String 		LOGSERVICE_LOG = "com.androidApp.LogService.log";
	public static final String 		LOGSERVICE_MESSAGE = "message";
	public static final String		LOGSERVICE_DEFAULT_FILE = "events.txt";
	protected static final String	TAG = "EventRecorder";
	protected Context				mContext;										// to send requests to service
	protected String				mRecordFileName;								// name of the file in the sdcard
	
	// constructor which opens the recording file, which is stashed somewhere on the sdcard.
	public EventRecorderInterface(Context context, String recordFileName) {	
		mContext = context;
		mRecordFileName = recordFileName;
        Intent i = new Intent(LOGSERVICE_INITIALIZE);
        i.putExtra(LOGSERVICE_FILENAME, mRecordFileName);
        mContext.startService(i);
	}	
	
	/**
	 * this can be called from multiple threads: the IME process, the layout threads, the popup and 
	 * dialog listeners, and little Red Riding Hood.
	 * @param filename
	 * @param s
	 */
    public synchronized void writeLog(String filename, String s) {
        if (mContext != null) {
            Intent i = new Intent(LOGSERVICE_LOG);
            i.putExtra(LOGSERVICE_FILENAME, filename);
            i.putExtra(LOGSERVICE_MESSAGE, s);
            mContext.startService(i);
        } else {
        	Log.e(TAG, "writeLog: context must be initialized");
        }
    }

	
	// write a record to the output
	public synchronized void writeRecord(String s)  {
		writeLog(mRecordFileName, s);
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
}