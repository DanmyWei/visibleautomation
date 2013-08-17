package com.androidApp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.androidApp.util.Constants;

/**
 * utilities to execute shell commands
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class Exec {
	
	/**
	 *  execute an adb command and return the output in an array of strings
	 * @param androidSDK location of the android sdk command, either pulled from the eclipse preferences
	 * or the ANDROID_HOME directory specified by the user.
	 * @param adbCommand command for adb to execute
	 * @return array of strings, null on failure
	 */
	public static String[] getAdbCommandOutput(String androidSDK, String adbCommand) {

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
	 * @param androidSDK location of the android sdk command, either pulled from the eclipse preferences
	 * or the ANDROID_HOME directory specified by the user.
	 * @param adbCommand command for adb to execute
	 * @return true for success, false on failure
	 */
	public static boolean executeAdbCommand(String androidSDK, String adbCommand) {
		Process proc = null;
		try {
    	   	String adbPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS + File.separator + Constants.Executables.ADB;
    	   	String cmd = adbPath + " " + adbCommand;
            proc = Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
            System.err.println("failed to execute adb command " + adbCommand + " " + e.getMessage());
            return false;
        }
		int result = -1;
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing adb command " + adbCommand + " " + e.getMessage());
            return false;
        }	
        return result == 0;
 	}
	
	/**
	 * execute a shell command
	 * @param cmd command to run in the shell
	 * @return true on success, false on failure
	 */
	public static int executeShellCommand(String cmd) {
		Process proc = null;
		try {
            proc = Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            return -1;
        }
		int result = -1;
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
            return -1;
        }	
        return result;
 	}
	
	/**
	 * is code in results?
	 * @param code
	 * @param results
	 * @return
	 */
	public static boolean resultCodeIn(int code, int[] results) {
		for (int result : results) {
			if (code == result) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * execute a shell command as a list of strings (useful for running commands through the shell and such)
	 * @param cmd
	 * @return true if the command executed successfully with a 0 exit code.
	 */
	public static boolean executeShellCommand(String[] cmd) {
		Process proc = null;
		try {
            proc = Runtime.getRuntime().exec(cmd);
         } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
            return false;
        }
		int result = -1;
		try {
             result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
            return false;
        }	
        System.out.println("execute " + cmd + " exit code = " + result);
       return result == 0;
 	}
	
	/**
	 * execute a shell command from the specified directory
	 * @param dir directory to execute from
	 * @param cmd varargs command and parameters
	 * @return
	 * @throws IOException
	 */
	public static boolean executeShellCommand(String dir, String ... cmd) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.directory(new File(dir));
		Process proc = processBuilder.start();
		int result = -1;
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
            return false;
        }	
        System.out.println("execute " + cmd + " exit code = " + result);
        return result == 0;
		
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
        }
		return null;
 	}
	
	/**
	 * run a shell command and stdout as an inputstream
	 * @param cmd command to execute
	 * @return hopefully an input stream
	 */
	public static InputStream getShellCommandInputStream(String cmd) {
		Process proc = null;
		InputStream procStream = null;
		try {
            proc = Runtime.getRuntime().exec(cmd);
  		  	procStream = proc.getInputStream();
  		  	return procStream;
        } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
        }
		return null;
 	}
	
	/**
	 * run an adb command and stdout as an inputstream
	 * @param cmd command to execute
	 * @return hopefully an input stream
	 */
	
	public static InputStream getAdbCommandInputStream(String androidSDK, String cmd) {
		String adbPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS + File.separator + Constants.Executables.ADB;
		return getShellCommandInputStream(adbPath + " " + cmd);
	}
}
