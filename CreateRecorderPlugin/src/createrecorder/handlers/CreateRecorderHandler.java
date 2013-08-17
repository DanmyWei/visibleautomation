package createrecorder.handlers;

import java.io.BufferedReader;
import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import com.androidApp.util.Constants;
import com.androidApp.util.Exec;

import createproject.CreateRobotiumRecorderBinary;
import createrecorder.util.AAPTBadgingValues;
import createrecorder.util.EclipseUtility;
import createrecorder.util.EclipseExec;
import createrecorder.util.ManifestInformation;
import createrecorder.util.PackageUtils;
import createrecorder.util.Pair;
import createrecorder.util.RecorderConstants;
import createrecorder.util.ResignAPK;
import createrecorder.util.TestClassDialog;
import createrecorderplugin.Activator;
import createrecorderplugin.popup.actions.CreateRobotiumRecorderAction;

/**
 * Our recorder handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class CreateRecorderHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public CreateRecorderHandler() {
	}
	
	/**
	 * handler to create the project
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();

		/*
		if (!Activator.verifyLicense()) {
			MessageDialog.openInformation(shell, "Visible Automation", "failed to validate license");
			return null;
		}
		*/

		TestClassDialog testClassDialog = new TestClassDialog();
		String packagePath = testClassDialog.getTestClassDialog(shell, "Visible Automation", "Enter classpath of APK to record");
		if (packagePath != null) {	
			try {
				String os = Platform.getOS();
				// TODO: TEST THIS
				if (os.equals(Platform.OS_WIN32)) {	
					ResignAPK.resignWin32(shell, testClassDialog.mMatchingClass, PackageUtils.getPackageName(testClassDialog.mPackagePath));
				} else {
					ResignAPK.resign(shell, testClassDialog.mMatchingClass, PackageUtils.getPackageName(testClassDialog.mPackagePath));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			File aapt = EclipseUtility.findAAPT(androidSDK);
			// aapt has moved from android-sdks/platform-tools to android-sdks/build-tools/17.0.0 in version 21.
			if (aapt == null) {
				MessageDialog.openInformation(shell, "Visible Automation", "could not find aapt in " + androidSDK);
			} else {
				String aaptPath = aapt.getAbsolutePath();
				String[] aaptBadgingLines = Exec.getShellCommandOutput(aaptPath + " dump --values badging " +  PackageUtils.getPackageName(packagePath));
				AAPTBadgingValues badgingValues = new AAPTBadgingValues(aaptBadgingLines);
				// extract the packge so we can get the AndroidManifest.xml file.
				createProject(shell, badgingValues);
			}
		}
		return null;
	}
	
	/**
	 * create the project from the extracted manifest information
	 * @param shell
	 * @param manifestInformation
	 */
	public void createProject(Shell shell, AAPTBadgingValues aaptBadgingValues) {
		try {
			String projectName = aaptBadgingValues.getApplicationLabel();
	
			// create the new project
			String newProjectName = projectName + RecorderConstants.RECORDER_SUFFIX;
			IProject testProject = EclipseUtility.createBaseProject(newProjectName);
			CreateRobotiumRecorderBinary createRecorder = new CreateRobotiumRecorderBinary();
			
			// create the .classpath, AndroidManifest.xml, .project, and project.properties files
			String target = "target=android-" + Integer.toString(aaptBadgingValues.getSDKVersion());
			createRecorder.createProjectProperties(testProject, target);
			createRecorder.createProject(testProject, aaptBadgingValues.getApplicationLabel());
			createRecorder.createClasspath(testProject, aaptBadgingValues.getApplicationLabel());
			createRecorder.createManifest(testProject, target, aaptBadgingValues.getPackage(), Integer.toString(aaptBadgingValues.getSDKVersion()));

			// create the java project, the test class output, and the "AllTests" driver which
			// iterates over all the test cases in the folder, so we can run with a single-click
			IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
			createRecorder.createFolders(testProject);
			createRecorder.createEclipseSettings(testProject);
			createRecorder.createTestClass(testProject, javaProject, aaptBadgingValues.getPackage(), aaptBadgingValues.getLaunchableActivity());
			createRecorder.createAllTests(testProject, javaProject, aaptBadgingValues.getPackage());
			createRecorder.addLibraries(testProject);
		} catch (Exception ex) {
			MessageDialog.openInformation(
					shell,
					"CreateRecorderPlugin",
					"There was an exception creating the test project " + ex.getMessage());
			ex.printStackTrace();
		
		}
	}		
	   
	/**
     * retrieve the list of packages from the device
     * @return
     */
    String[] getDevicePackages() {
        return EclipseExec.getAdbCommandOutput("shell pm list packages -3");
    }

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute_unused(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		ElementListSelectionDialog dialog =  new ElementListSelectionDialog(shell, new LabelProvider());
		String[] packages = getDevicePackages();
		if (packages != null) {
			dialog.setElements(packages);
			dialog.setTitle("select package from device");
			// User pressed cancel
			if (dialog.open() != Window.OK) {
			    return false;
			}
			Object[] result = dialog.getResult(); 	
			
		} else {
			MessageDialog.openInformation(
					window.getShell(),
					"Robotium Recorder",
					"failed to obtain package list from the device");
			return null;
		}
		return null;
	}


}
