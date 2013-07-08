package com.androidApp.logService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 *  from dianne hackborn
 * Correct, Instrumentation runs in the process of the app being tested, so
 * can only run with its permissions. You can't extend the app's permissions
 * without changing its manifest to declare them. I suppose you could put a
 * service in your instrumentation .apk that your instrumentation code can
 * connect with from the app to execute code that needs other permissions.
 * test service: top-level intent actions:
 * com.androidApp.test.log: write a log message to the specified file 
 * 		message: message to write
 * 		filename: filename to write to (on the /sdcard)
 * TODO: chances are that we will extend this service to provide other operations, such as 
 * enabling flight mode and stuff
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class RobotiumRecorderLogService extends IntentService {
	protected static String TAG = "LogService";
	public static final String INITIALIZE = "com.androidApp.LogService.initialize";
	public static final String FILENAME = "filename";
	public static final String LOG = "com.androidApp.LogService.log";
	public static final String MESSAGE = "message";

	public RobotiumRecorderLogService() {
		super("LogService");
	}
	
	public RobotiumRecorderLogService(String name) {
		super(name);
	}

	/**
	 * standard onHandleIntent implementation. This is to allow Robotium instrumentation code to 
	 * perform tasks without having to modify the manifest of the controlled application.
	 * @param intent action specifies service.  One of the following
	 * LOG_SERVICE: write a log to the log file
	 * 		MESSAGE: message to write to the log file (String)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		String requestType = intent.getAction();
		if (requestType.equals(INITIALIZE)) {
			final String logfileName = intent.getStringExtra(FILENAME);
			File extDir = Environment.getExternalStorageDirectory();
			File path = new File(extDir, logfileName);
			Log.d(TAG, "deleting " + path);
			path.delete();
		} else if (requestType.equals(LOG)) {
			final String message = intent.getStringExtra(MESSAGE);
			final String logfileName = intent.getStringExtra(FILENAME);
			writeLog(logfileName,message);
		} 
	}
	
	/**
	 * write a string to the current log file
	 * @param s string to write.
	 */
	public void writeLog(String file, String s) {
		FileWriter fw = null;
		try {
			File extDir = Environment.getExternalStorageDirectory();
			File path = new File(extDir, file);
			fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(s + "\n");
			bw.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {		
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception ex2) {			
				}
			}
		}
	}
}
	
