package com.androidApp.Utility;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * copy the database,shared_prefs and files directory contents back into the application directory
 * before the application is started under test
 * @author matt2
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class SaveState {
	protected static String TAG = "SaveState";
	
	/**
	 * back up the databases referenced by this application to the sdcard
	 * @param context target context
	 * @throws IOException
	 */
	public static void backupDatabases(Context context) throws IOException { 
		String[] databases = context.databaseList();
		String appDatabaseDirName = "/data/data/" + context.getPackageName() + File.separator + "databases";
		File appDatabaseDir = new File(appDatabaseDirName);
		String backupDatabaseDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "databases";
		File backupDatabaseDir = new File(backupDatabaseDirName);
		if (!backupDatabaseDir.mkdirs()) {
			Log.e(TAG, "failed to create backup database directory " + backupDatabaseDirName);
		} else {
			FileUtils.copyFileList(appDatabaseDir, backupDatabaseDir, databases);
		}
	}
	
	/**
	 * restore databases from sdcard
	 * @param context
	 * @throws IOException
	 */
	public static void restoreDatabases(Context context) throws IOException { 
		String appDatabaseDirName = "/data/data/" + context.getPackageName() + File.separator + "databases";
		File appDatabaseDir = new File(appDatabaseDirName);
		String backupDatabaseDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "databases";
		File backupDatabaseDir = new File(backupDatabaseDirName);
		String[] databases = backupDatabaseDir.list();
		FileUtils.copyFileList(backupDatabaseDir, appDatabaseDir, databases);
	}
	
	/**
	 * back up the preferences referenced by this application to the sdcard
	 * @param context target context
	 * @throws IOException
	 */
	public static void backupPreferences(Context context) throws IOException {
		String appPrefsDirName = "/data/data/" + context.getPackageName() + File.separator + "shared_prefs";
		File appPrefsDir = new File(appPrefsDirName);
		String backupPrefsDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "shared_prefs";
		File backupPrefsDir = new File(backupPrefsDirName);
		backupPrefsDir.mkdirs();
		if (!backupPrefsDir.mkdirs()) {
			Log.e(TAG, "failed to create backup database directory " + backupPrefsDirName);
		} else {
			String[] prefsFileList = appPrefsDir.list();
			FileUtils.copyFileList(appPrefsDir, backupPrefsDir, prefsFileList);
		}
	}
	
	public static void restorePreferences(Context context, String testName) throws IOException {
		String appPrefsDirName = "/data/data/" + context.getPackageName() + File.separator + "shared_prefs";
		File appPrefsDir = new File(appPrefsDirName);
		String backupPrefsDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "shared_prefs";
		File backupPrefsDir = new File(backupPrefsDirName);
		String[] prefsFileList = backupPrefsDir.list();
		FileUtils.copyFileList(backupPrefsDir, appPrefsDir, prefsFileList);
	}
	
	/**
	 * back up the files referenced by this application to the sdcard
	 * @param context target context
	 * @throws IOException
	 */
	public static void backupLocalFiles(Context context) throws IOException {
		String appFilesDirName = "/data/data/" + context.getPackageName() + File.separator + "files";
		File appFilesDir = new File(appFilesDirName);
		String backupFilesDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "files";
		File backupFilesDir = new File(backupFilesDirName);
		backupFilesDir.mkdirs();
		if (!backupFilesDir.mkdirs()) {
			Log.e(TAG, "failed to create backup database directory " + backupFilesDirName);
		} else {
			String[] filesFileList = appFilesDir.list();
			FileUtils.copyFileList(appFilesDir, backupFilesDir, filesFileList);
		}
	}
	
	public static void restoreLocalFiles(Context context, String testName) throws IOException {	
		String appFilesDirName = "/data/data/" + context.getPackageName() + File.separator + "files";
		File appFilesDir = new File(appFilesDirName);
		String backupFilesDirName = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + File.separator + "files";
		File backupFilesDir = new File(backupFilesDirName);
		String[] filesFileList = backupFilesDir.list();
		FileUtils.copyFileList(backupFilesDir, appFilesDir, filesFileList);
	}
}

