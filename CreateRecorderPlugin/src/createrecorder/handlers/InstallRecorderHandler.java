package createrecorder.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

import createrecorder.util.EclipseExec;
import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;

/** menu handler to install the log service and keyboard apks to support the recorder.
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class InstallRecorderHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		installAPKFromTemplate(shell, RecorderConstants.LOGSERVICE_APK, RecorderConstants.LOGSERVICE_PACKAGE);
		installAPKFromTemplate(shell, RecorderConstants.KEYBOARD_APK, RecorderConstants.KEYBOARD_PACKAGE);
		return null;
	}
	
	/**
	 * check to see if a package is installed using shell pm list packages
	 * @param packageName name.of.the.package.to.check
	 * @return
	 */
	public static boolean isPackageInstalled(String packageName) {
		String[] results = EclipseExec.getAdbCommandOutput("shell pm list packages -3 " + packageName);
		return results.length == 1;
	}

	/**
	 * check to see if a.package.is.installed and install the associated .APK file
	 * @param templateFile
	 * @param packageName
	 * @throws IOException
	 */
	public static boolean installAPKFromTemplate(Shell shell, String templateFile, String packageName) {
		if (!isPackageInstalled(packageName)) {
			try {
				FileUtility.writeResource(templateFile);
			} catch (IOException ioex) {
				MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, 
											  "failed to read the the package " + packageName + " from the plugin");
				return false;
			}
			return installAPKFromFile(shell, templateFile);
		}
		return true;
	}
	
	/**
	 * install an APK from a file
	 * @param shell
	 * @param apkFile
	 * @return
	 */
	public static boolean installAPKFromFile(Shell shell, String apkFile) {
		String[] installResults = EclipseExec.getAdbCommandOutput("install " + apkFile);
	    EclipseUtility.printConsole(installResults);
	    if (StringUtils.containedInStringArray(Constants.Errors.FAILURE, installResults)) {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, 
										  "failed to install the APK file " + apkFile + 
										  "onto your device. Check the eclipse log for details");
	    	return false;
	    }
	    return true;
	}
}
