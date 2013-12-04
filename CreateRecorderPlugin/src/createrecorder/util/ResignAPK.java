package createrecorder.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.StringUtils;

/*
 * utility functions to re-sign an .apk with the developer's debug signature.
 * NOTE: this only works on OSX
   * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ResignAPK {
	
	public static String replaceExtension(String file, String newExtension) {
	    int ichDot = file.lastIndexOf('.');
	    file = file.substring(0, ichDot + 1);
	    return file + newExtension;
	}

	/**
	 * NOTE: The androidHome variable should be picked up from eclipse preferenes, not the environment variable
	 * @param packageName
	 * @param apkFile
	 * @throws IOException
	 */
	public static boolean resign(Shell shell, String packageName, String apkFile) throws IOException {
	    String[] pullResults = EclipseExec.getAdbCommandOutput("pull /data/app" + apkFile);
	    EclipseUtility.printConsole(pullResults);
	    if (StringUtils.containedInStringArray(Constants.Errors.DOES_NOT_EXIST, pullResults)) {
	    	MessageDialog.openInformation(shell, "Resign APK", "failed to pull APK from device");
	    	return false;
	    }
	    File apkFileRef = new File(apkFile);
	    if (!apkFileRef.exists()) {
			MessageDialog.openInformation(shell,"Resign APK","failed to pull /data/app/" + apkFile + " from device");
	    	return false;
	    }
	    String zipCmd = "zip -d " + apkFile + " META-INF/*";
		String[] zipResults = Exec.getShellCommandOutput(zipCmd);
		EclipseUtility.printConsole(zipResults);

	    if (false) {
			MessageDialog.openInformation(shell,"Resign APK", "failed to remove META-INF directory");
	    	return false;
	    }
	    String home = System.getenv(RecorderConstants.EnvironmentVariables.HOME);
	    String jarsignerCmd = "/usr/bin/jarsigner -sigalg SHA1withRSA -digestalg SHA1 -keystore " + home + "/.android/debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey";
	    if (Exec.executeShellCommand(jarsignerCmd) != 0) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to sign the new APK");	    	  	
	    	return false;
	    }
	    String jarsignerVerifyCmd = "/usr/bin/jarsigner -verify -sigalg SHA1withRSA -digestalg SHA1 -keystore " + home + "/.android/debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey";
	    String[] jarsignerVerifyResults = Exec.getShellCommandOutput(jarsignerVerifyCmd);
	    if (!StringUtils.containedInStringArray(RecorderConstants.VERIFIED, jarsignerVerifyResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to sign the new APK");	    	  	
	    	return false;	    	
	    }
	    String[] uninstallResults = EclipseExec.getAdbCommandOutput("uninstall " + packageName);
	    EclipseUtility.printConsole(uninstallResults);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, uninstallResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to uninstall APK");	    	  	
	    	return false;
	    }
	    String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    EclipseUtility.printConsole(installResults);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, installResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to install APK");	    	  	
	    	return false;
	    }
	    return true;
	}
	/**
	 * variant for win32
	 * @param packageName
	 * @param apkFile
	 * @throws IOException
	 */
	
	public static boolean resignWin32(Shell shell, String packageName, String apkFile) throws IOException {
	    String[] pullResults = EclipseExec.getAdbCommandOutput("pull /data/app" + apkFile);
	    EclipseUtility.printConsole(pullResults);
	    if (StringUtils.containedInStringArray(Constants.Errors.DOES_NOT_EXIST, pullResults)) {
	    	MessageDialog.openInformation(shell, "Resign APK", "failed to pull APK from device");
	    	return false;
	    }
	    File apkFileRef = new File(apkFile);
	    if (!apkFileRef.exists()) {
			MessageDialog.openInformation(shell,"Resign APK","failed to pull /data/app/" + apkFile + " from device");
	    	return false;
	    }
	    String zipCmd = "zip -d " + apkFile + " META-INF/*";
		String[] zipResults = Exec.getShellCommandOutput(zipCmd);
	    EclipseUtility.printConsole(zipResults);

	    if (false) {
			MessageDialog.openInformation(shell,"Resign APK", "failed to remove META-INF directory");
	    	return false;
	    }
	    String home = System.getenv(RecorderConstants.EnvironmentVariables.HOME);
	    String jarsignerCmd = "jarsigner -sigalg SHA1withRSA -digestalg SHA1 -keystore " + home + "\\.android\\debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey";
	    if (Exec.executeShellCommand(jarsignerCmd) != 0) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to sign the new APK");	    	  	
	    	return false;
	    }
	    String[] uninstallResults = EclipseExec.getAdbCommandOutput("uninstall " + packageName);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, uninstallResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to uninstall APK");	    	  	
	    	return false;
	    }
	    String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, installResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to install APK");	    	  	
	    	return false;
	    }
	    return true;
	}


}
