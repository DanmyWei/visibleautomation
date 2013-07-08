package createrecorder.util;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * dialog to prompt for the test class name.  This needs to be changed to a list dialog with the output from the package list
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class TestClassDialog {
	public String mPackagePath = null;
	public String mMatchingClass = null;
	
	public String getTestClassDialog(Shell shell, String title, String prompt) {
		InputDialog inputDialog = new InputDialog(shell, title, prompt, "", null); 
		int answer = inputDialog.open();
		if (answer == Window.OK) {
			String className = inputDialog.getValue();
			// get the package for the classname package:/data/app/com.example.android.apis-1.apk=com.example.android.apis
			String[] matchingClasses = EclipseExec.getAdbCommandOutput("shell pm list packages  -f -3");
			mMatchingClass = null;
			for (String candidateClass : matchingClasses) {
				if (candidateClass.endsWith(className)) {
					mMatchingClass = candidateClass;
				}
			}
			if (mMatchingClass == null) {
				MessageDialog.openInformation(
						shell,
						"CreateRecorderPlugin",
						"No matching packages for class " + className);
			} else {
				String packagePath = getPackagePath(mMatchingClass);
				if (!EclipseExec.executeAdbCommand("pull " + packagePath)) {
					MessageDialog.openInformation(
							shell,
							"CreateRecorderPlugin",
							"failed to pull the APK from the device for package " + packagePath);
				}
				int ichEquals = mMatchingClass.indexOf("=");
				if (ichEquals != -1) {
					mMatchingClass = mMatchingClass.substring(ichEquals + 1);
				}
				return packagePath;
			}
		}
		return null;
	}
	
	// return /data/app/com.example.android.apis.test-1.apk from package:/data/app/com.example.android.apis.test-1.apk=com.example.android.apis.test
	public static String getPackagePath(String packageAndClass) {
		int ichStart = packageAndClass.indexOf(':');
		int ichEnd = packageAndClass.indexOf('=');
		if ((ichStart != -1) && (ichEnd != -1)) {
			return packageAndClass.substring(ichStart + 1, ichEnd);
		}
		return null;
	}
}
