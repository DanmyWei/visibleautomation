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
import com.androidApp.util.StringUtils;

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
	
	/**
	 * get the input stream from an adb command so we can stream it to the console.
	 * @param adbCommand
	 * @return
	 */
	public static InputStream getAdbCommandInputStream(String adbCommand) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		return Exec.getAdbCommandInputStream(androidSDK, adbCommand);
 	}
	
	// we don't like hanging eclipse.
	public class AdbConsoleOutputRunnable implements Runnable {
		protected String mADBCommand;
		
		public AdbConsoleOutputRunnable(String adbCommand) {
			mADBCommand = adbCommand;
		}
		
		public void run() {
			try {
				InputStream is = EclipseExec.getAdbCommandInputStream(mADBCommand);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = br.readLine();
				while (line != null) {
					EclipseUtility.printConsole(line);
					line = br.readLine();
				}
				br.close();
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
	
	public static void execADBBackgroundConsoleOutput(String adbCommand) {
		EclipseExec exec = new EclipseExec();
		Runnable runnable = exec.new AdbConsoleOutputRunnable(adbCommand);
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	/**
	 * is a device attached?
	 * adb devices
	 * List of devices attached 
	 * 0149947A13016004	device
	 * @param androidSDK
	 * @return
	 */
	
	public static boolean isDeviceAttached() {
		String[] adbResults = getAdbCommandOutput("devices");
		if (adbResults.length > 1) {
			if (adbResults[0].contains(Constants.Names.DEVICE)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return the device names of the attach devices, suitable for adb -s <device-name>
	 * @return list of device names.
	 */
	public static List<String> getDeviceNames() {
		String[] adbResults = getAdbCommandOutput("devices");
		List<String> devices = new ArrayList<String>();
		for (int i = 1; i < adbResults.length; i++) {
			if (adbResults[i].contains(Constants.Names.DEVICE)) {
				String devicename = StringUtils.extractMatch(adbResults[i], "[^ ]*");
				if (devicename != null) {
					devices.add(devicename);
				}
			}
		}
		return devices;		
	}
}
