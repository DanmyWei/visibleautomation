package createrecorder.util;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * dialog to prompt for the test class name.  This needs to be changed to a list dialog with the output from the package list
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class TestClassDialog {
	public String mPackagePath = null;
	public String mMatchingClass = null;
	
	public String getTestClassDialog(Shell shell, String title, String prompt) {
		if (!EclipseExec.isDeviceAttached()) {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "No device attached");
			return null;
		}
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
		dialog.setTitle(RecorderConstants.VISIBLE_AUTOMATION);
		dialog.setMessage("Select a package from the device");
		String[] matchingClasses = EclipseExec.getAdbCommandOutput("shell pm list packages -f");
		String[] displayStrings = new String[matchingClasses.length];
		
		// strip out the display strings
		for (int i = 0; i < matchingClasses.length; i++) {
			String line = matchingClasses[i];
			int ichColon = line.indexOf('=');
			line = line.substring(ichColon + 1);
			displayStrings[i] = line;
		}
		dialog.setElements(displayStrings);
		dialog.setMultipleSelection(false);
		int answer = dialog.open();
		if (answer == Window.OK) {
			mMatchingClass = (String) dialog.getFirstResult();
			for (int iPackage = 0; iPackage < displayStrings.length; iPackage++) {
				int ichEquals = matchingClasses[iPackage].indexOf('=');
				if (matchingClasses[iPackage].substring(ichEquals + 1).equals(mMatchingClass)) {
					// get the package for the classname package:/data/app/com.example.android.apis-1.apk=com.example.android.apis
					mPackagePath = getPackagePath(matchingClasses[iPackage]);
					break;
				}
			}
			if (!EclipseExec.executeAdbCommand("pull " + mPackagePath)) {
				MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
											  "failed to pull the APK from the device for package " + mPackagePath);
				return null;
			}
			return mPackagePath;
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
