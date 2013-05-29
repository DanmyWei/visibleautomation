package createrecorder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.androidApp.util.Constants;

/**
 * utilities to execute shell commands
 * @author Matthew
 *
 */
public class Exec {
	
	/**
	 *  execute an adb command and return the output in an array of strins
	 * @param adbCommand command for adb to execute
	 * @return array of strings, null on failure
	 */
	public static String[] getAdbCommandOutput(String adbCommand) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);

		Process proc = null;
		InputStream procStream = null;
		List<String> lines = new ArrayList<String>();
   	   	String adbPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS + File.separator + Constants.Executables.ADB;
	   	String cmd = adbPath + " " + adbCommand;
		try {
            proc = Runtime.getRuntime().exec(cmd);
  		  	BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
  		  	while ((line = input.readLine()) != null) {
  		  		lines.add(line);
  		  	}
  		  	input.close();
  		  	String[] array = new String[lines.size()];
  		  	lines.toArray(array);
  		  	return array;
        } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            System.exit(-1);
        }
       return null;
 	}
	
	/**
	 * execute an adb command
	 * @param adbCommand command for adb to execute
	 * @return true for success, false on failure
	 */
	public static boolean executeAdbCommand(String adbCommand) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);

		Process proc = null;
		String cmd = RecorderConstants.ADB + " " + adbCommand;
		try {
    	   	String adbPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS + File.separator + Constants.Executables.ADB;
    	   	cmd = adbPath + " " + adbCommand;
            proc = Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            return false;
        }
        try {
            int result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
            return false;
        }	
        return true;
 	}
	
	/**
	 * execute a shell command
	 * @param cmd command to run in the shell
	 * @return true on success, false on failure
	 */
	public static boolean executeShellCommand(String cmd) {
		Process proc = null;
		try {
            proc = Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            return false;
        }
        try {
            int result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
            return false;
        }	
        return true;
 	}
	
	/**
	 * run a shell command and return the output as an array of strings
	 * @param cmd command to execute
	 * @return array of strings from the output, or null on failure
	 */
	public static String[] getShellCommandOutput(String cmd) {
		Process proc = null;
		InputStream procStream = null;
		List<String> lines = new ArrayList<String>();
		try {
            proc = Runtime.getRuntime().exec(cmd);
  		  	BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
  		  	while ((line = input.readLine()) != null) {
  		  		lines.add(line);
  		  	}
  		  	input.close();
  		  	String[] array = new String[lines.size()];
  		  	lines.toArray(array);
  		  	return array;
        } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            System.exit(-1);
        }
       return null;
 	}

}
