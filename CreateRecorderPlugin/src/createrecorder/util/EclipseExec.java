package createrecorder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.runtime.Log;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.androidApp.util.Constants;
import com.androidApp.util.Exec;

/**
 * utilities to execute shell commands
 * variant which pulls the android sdk directory from eclipse preferences
 * @author Matthew
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class EclipseExec {
	
	/**
	 *  execute an adb command and return the output in an array of strins
	 * @param adbCommand command for adb to execute
	 * @return array of strings, null on failure
	 */
	public static String[] getAdbCommandOutput(String adbCommand) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		return Exec.getAdbCommandOutput(androidSDK, adbCommand);
 	}
	
	/**
	 * execute an adb command
	 * @param adbCommand command for adb to execute
	 * @return true for success, false on failure
	 */
	public static boolean executeAdbCommand(String adbCommand) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		return Exec.executeAdbCommand(androidSDK, adbCommand);
 	}
}
