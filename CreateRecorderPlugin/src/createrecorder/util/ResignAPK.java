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
	public static void resign(Shell shell, String packageName, String apkFile) throws IOException {
	    String zipfile = replaceExtension(apkFile, Constants.Extensions.ZIP);
	    String[] pullResults = EclipseExec.getAdbCommandOutput("pull /data/app" + apkFile);
	    if (StringUtils.containedInStringArray(Constants.Errors.DOES_NOT_EXIST, pullResults)) {
	    	MessageDialog.openInformation(shell, "Resign APK", "failed to pull APK from device");
	    }
	    File apkFileRef = new File(apkFile);
	    if (!apkFileRef.exists()) {
			MessageDialog.openInformation(shell,"Resign APK","failed to pull /data/app/" + apkFile + " from device");
	    }
	    if (!Exec.resultCodeIn(Exec.executeShellCommand("zip -d " + apkFile + " META-INF/\\*"), new int[] { 0, 12 })) {
			MessageDialog.openInformation(shell,"Resign APK", "failed to remove META-INF directory");
	    }
	    String home = System.getenv(RecorderConstants.EnvironmentVariables.HOME);
	    String jarsignerCmd = "/usr/bin/jarsigner -keystore " + home + "/.android/debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey";
	    if (Exec.executeShellCommand(jarsignerCmd) != 0) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to sign the new APK");	    	  	
	    }
	    //Exec.executeShellCommand(androidHome + "/tools/zipalign 4 " + apkFile + " tempfile");
	    //Exec.executeShellCommand("mv tempfile " + apkFile);
	    String[] uninstallResults = EclipseExec.getAdbCommandOutput("uninstall " + packageName);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, uninstallResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to uninstall APK");	    	  	
	    }
	    String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, installResults)) {
	 		MessageDialog.openInformation(shell,"Resign APK", "failed to install APK");	    	  	
	    }
	}
	/**
	 * variant for win32
	 * @param packageName
	 * @param apkFile
	 * @throws IOException
	 */
	
	public static void resignWin32(Shell shell, String packageName, String apkFile) throws IOException {
	    String zipfile = replaceExtension(apkFile, Constants.Extensions.ZIP);
	    String[] pullResults = EclipseExec.getAdbCommandOutput("pull " + apkFile);
	    File apkFileRef = new File(apkFile);
	    if (!apkFileRef.exists()) {
			MessageDialog.openInformation(
					shell,
					"Resign APK",
					"failed to pull /data/app/" + apkFile + " from device");

	    }
	    Exec.executeShellCommand("del /s temp");
	    Exec.executeShellCommand("mkdir temp");
	    Exec.executeShellCommand("copy " + apkFile + " temp\\" + zipfile);
	    Exec.executeShellCommand("unzip temp\\" + zipfile + " -d temp");
	    Exec.executeShellCommand("rmdir /s temp/META-INF");
	    Exec.executeShellCommand("del temp\\" + zipfile);
	    Exec.executeShellCommand("temp", "zip", zipfile, "-r", ".");
	    Exec.executeShellCommand("move temp\\" + zipfile + " " + apkFile);
	    String home = System.getenv(RecorderConstants.EnvironmentVariables.HOME);
	    Exec.executeShellCommand("jarsigner -keystore " + home + "\\.android\\debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey");
	    //Exec.executeShellCommand(androidHome + "/tools/zipalign 4 " + apkFile + " tempfile");
	    //Exec.executeShellCommand("mv tempfile " + apkFile);
	    String[] uninstallResults = EclipseExec.getAdbCommandOutput("uninstall " + packageName);
	    String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    Exec.executeShellCommand("del /s temp");
	}


}
