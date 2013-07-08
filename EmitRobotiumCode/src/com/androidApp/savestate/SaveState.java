package com.androidApp.savestate;

import java.io.File;

import com.androidApp.util.Constants;
import com.androidApp.util.Exec;

/**
 * after the run, the sdcard will contain the backups for the application local files, preferences, and SQL databases,
 * stored before the application was run, so we can restore its state.
 * @author matt2
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class SaveState {
	/**
	 * save files from the sdcard directory to a local directory
	 * @param androidSDK ANDROID_HOME environment variable from the shell, from preferences from eclipse
	 * @param destDir destination directory
	 * @param extDir name of the external storage directory on the device, hopefully /sdcard
	 * @param srcDir source directory on the device under /sdcarc containing the fies.
	 * @throws SaveStateException if one of the files is not copied correcl
	 */
	public static void saveFiles(String androidSDK, String destDir, String extDir, String srcDir) throws SaveStateException {
		String srcPath = extDir + File.separator + srcDir;
		String adbLsCommand = "shell ls " + srcPath;
		String[] files = Exec.getAdbCommandOutput(androidSDK, adbLsCommand);
		
		// if the directory is empty, we get "No such file or directory"
		if ((files.length == 1) && files[0].contains(Constants.Messages.NO_SUCH_FILE_OR_DIRECTORY)) {
			return;
		} else {
			for (String file : files) {
				String deviceFile = srcPath + File.separator + file;
				String adbPullCommand = "pull " + deviceFile + " " + destDir;
				Exec.executeAdbCommand(androidSDK, adbPullCommand);
				File checkFile = new File(destDir + File.separator + file);
				if (!checkFile.exists()) {
					throw new SaveStateException("failed to import " + file + " from " + deviceFile);
				}
			}
		}
	}
	
	/**
	 * save the state files from the device back to the host
	 * @param androidSDK ANDROID_HOME environment variable from the shell, from preferences from eclipse
	 * @param destDir destination directory
	 * @param extDir name of the external storage directory on the device, hopefully /sdcard
	 * @param appName files are stored under /scard/appName/<databases|shared_prefs|files>
	 * @throws SaveStateException
	 */
	public static void saveStateFiles(String androidSDK, String destDir, String extDir, String appName) throws SaveStateException {
		saveFiles(androidSDK, destDir + File.separator + Constants.Dirs.DATABASES, extDir, appName + File.separator + Constants.Dirs.DATABASES);
		saveFiles(androidSDK, destDir + File.separator + Constants.Dirs.SHARED_PREFS, extDir, appName + File.separator + Constants.Dirs.SHARED_PREFS);
		saveFiles(androidSDK, destDir + File.separator + Constants.Dirs.FILES, extDir, appName + File.separator + Constants.Dirs.FILES);
	}
}
