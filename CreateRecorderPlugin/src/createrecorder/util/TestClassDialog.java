package createrecorder.util;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * dialog to prompt for the test class name.  This needs to be changed to a list dialog with the output from the package list
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class TestClassDialog {
	public static String getTestClassDialog(Shell shell, String title, String prompt) {
		InputDialog inputDialog = new InputDialog(shell, title, prompt, "", null); 
		int answer = inputDialog.open();
		if (answer == Window.OK) {
			String className = inputDialog.getValue();
			// get the package for the classname package:/data/app/com.example.android.apis-1.apk=com.example.android.apis
			String[] matchingClasses = Exec.getAdbCommandOutput("shell pm list packages  -f -3");
			String matchingClass = null;
			for (String candidateClass : matchingClasses) {
				if (candidateClass.endsWith(className)) {
					matchingClass = candidateClass;
				}
			}
			if (matchingClass == null) {
				MessageDialog.openInformation(
						shell,
						"CreateRecorderPlugin",
						"No matching packages for class " + className);
			} else {
				String packagePath = getPackagePath(matchingClass);
				if (!Exec.executeAdbCommand("pull " + packagePath)) {
					MessageDialog.openInformation(
							shell,
							"CreateRecorderPlugin",
							"failed to pull the APK from the device for package " + packagePath);
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
