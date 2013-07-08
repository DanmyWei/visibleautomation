package createrecorder.util;

import java.io.IOException;

import com.androidApp.util.Constants;
import com.androidApp.util.Exec;

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
	public static void resign(String packageName, String apkFile) throws IOException {
		String androidHome = System.getenv(RecorderConstants.EnvironmentVariables.ANDROID_HOME);
	    String zipfile = replaceExtension(apkFile, Constants.Extensions.ZIP);
	    String[] pullResults = EclipseExec.getAdbCommandOutput("pull " + apkFile);
	    Exec.executeShellCommand("rm -fr temp");
	    Exec.executeShellCommand("mkdir temp");
	    Exec.executeShellCommand("mv " + apkFile + " temp/" + zipfile);
	    Exec.executeShellCommand("unzip temp/" + zipfile + " -d temp");
	    Exec.executeShellCommand("rm -fr temp/META-INF");
	    Exec.executeShellCommand("rm temp/" + zipfile);
	    Exec.executeShellCommand("temp", "/usr/bin/zip", zipfile, "-r", ".");
	    Exec.executeShellCommand("mv temp/" + zipfile + " " + apkFile);
	    String home = System.getenv(RecorderConstants.EnvironmentVariables.HOME);
	    Exec.executeShellCommand("/usr/bin/jarsigner -keystore " + home + "/.android/debug.keystore -storepass android -keypass android " + apkFile + " androiddebugkey");
	    //Exec.executeShellCommand(androidHome + "/tools/zipalign 4 " + apkFile + " tempfile");
	    //Exec.executeShellCommand("mv tempfile " + apkFile);
	    String[] uninstallResults = EclipseExec.getAdbCommandOutput("uninstall " + packageName);
	    String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    Exec.executeShellCommand("rm -fr temp");
	}


}
