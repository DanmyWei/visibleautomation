package createrecorder.util;

/**
 * utility functions for processing package names and paths
 * @author Matthew
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class PackageUtils {
	
	// return /data/app/com.example.android.apis.test-1.apk from package:/data/app/com.example.android.apis.test-1.apk=com.example.android.apis.test
	public static String getPackagePath(String packageAndClass) {
		int ichStart = packageAndClass.indexOf(':');
		int ichEnd = packageAndClass.indexOf('=');
		if ((ichStart != -1) && (ichEnd != -1)) {
			return packageAndClass.substring(ichStart + 1, ichEnd);
		}
		return null;
	}
	
	/**
	 * get the package name from /the/package/path
	 * @param packagePath full package path
	 * @return package name
	 */
	public static String getPackageName(String packagePath) {
		int ichLastSlash = packagePath.lastIndexOf('/');
		if (ichLastSlash != -1) {
			return packagePath.substring(ichLastSlash + 1);
		}
		return packagePath;
	}
	
	/**
	 * pull the APK name from the package path
	 * @param packagePath
	 * @return
	 */
	public static String getAPKFromPackagePath(String packagePath) {
		int ichLastSlash = packagePath.lastIndexOf('/');
		if (ichLastSlash != -1) {
			return packagePath.substring(ichLastSlash + 1);
		}
		return packagePath;
	}
	

}
