package com.androidApp.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * actually, restore state.  Takes the files from the /sdcard/<TestName>/(databases/files/shared_prefs) directory
 * and copies them to /data/data/<application-package>)/(databases/files/shared_prefs) before the target
 * application is started in playback.
 * @author matt2
 *
 */
public class SaveState {
	protected static String TAG = "SaveState";
	
	/**
	 * restore databases from sdcard
	 * @param context application context (get this before the app runs, please)
	 * @param testName name of the test being run, to differentiate the backed state files 
	 * @throws IOException
	 */
	public static void restoreDatabases(Context context, String testName) throws IOException { 
		try {
			String appDatabaseDirName = "/data/data/" + context.getPackageName() + File.separator + "databases";
			File appDatabaseDir = new File(appDatabaseDirName);
			String backupDatabaseDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "databases";
			File backupDatabaseDir = new File(backupDatabaseDirName);
			String[] databases = backupDatabaseDir.list();
			FileUtils.copyFileList(backupDatabaseDir, appDatabaseDir, databases);
		} catch (Exception ex) {
			Log.i(TAG, "no databases to restore for " + testName);
		}
	}
	
	/**
	 * restore preferences from sdcard
	 * @param context application context (get this before the app runs, please)
	 * @param testName name of the test being run, to differentiate the backed state files 
	 * @throws IOException
	 */
	public static void restorePreferences(Context context, String testName) throws IOException {
		try {
			String appPrefsDirName = "/data/data/" + context.getPackageName() + File.separator + "shared_prefs";
			File appPrefsDir = new File(appPrefsDirName);
			String backupPrefsDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "shared_prefs";
			File backupPrefsDir = new File(backupPrefsDirName);
			String[] prefsFileList = backupPrefsDir.list();
			FileUtils.copyFileList(backupPrefsDir, appPrefsDir, prefsFileList);
		} catch (Exception ex) {
			Log.i(TAG, "no databases to restore for " + testName);
		}
	}
	
	/**
	 * restore local files from sdcard
	 * @param context application context (get this before the app runs, please)
	 * @param testName name of the test being run, to differentiate the backed state files 
	 * @throws IOException
	 */
	public static void restoreLocalFiles(Context context, String testName) throws IOException {	
		try {
			String appFilesDirName = "/data/data/" + context.getPackageName() + File.separator + "files";
			File appFilesDir = new File(appFilesDirName);
			String backupFilesDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "files";
			File backupFilesDir = new File(backupFilesDirName);
			String[] filesFileList = backupFilesDir.list();
			FileUtils.copyFileList(backupFilesDir, appFilesDir, filesFileList);
		} catch (Exception ex) {
			Log.i(TAG, "no databases to restore for " + testName);
		}
	}
}

