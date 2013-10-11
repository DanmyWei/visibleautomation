package createrecorder.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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

import com.androidApp.emitter.EmitterException;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;

import createproject.CreateRobotiumRecorder;
import createproject.CreateRobotiumRecorderBinary;
import createproject.ProjectInformation;
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
 * this handles the binary instrumentation recorder case
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
		String packagePath = testClassDialog.getTestClassDialog(shell, RecorderConstants.VISIBLE_AUTOMATION, "Enter classpath of APK to record");
		String apkFileName = PackageUtils.getPackageName(testClassDialog.mPackagePath);
		if (packagePath != null) {	
			try {
				String os = Platform.getOS();
				// TODO: TEST THIS for WIN32
				if (os.equals(Platform.OS_WIN32)) {	
					ResignAPK.resignWin32(shell, testClassDialog.mMatchingClass, apkFileName);
				} else {
					ResignAPK.resign(shell, testClassDialog.mMatchingClass, apkFileName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			File aapt = EclipseUtility.findAAPT(androidSDK);
			// aapt has moved from android-sdks/platform-tools to android-sdks/build-tools/17.0.0 in version 21.
			if (aapt == null) {
				MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "could not find aapt in " + androidSDK);
			} else {
				String aaptPath = aapt.getAbsolutePath();
				// use aapt to pull the manifest and the badging information, so we can do the best we can to get the target SDK,
				// application name, start activity, and application package.
				String[] manifestLines = Exec.getShellCommandOutput(aaptPath + " dump --values xmltree " + PackageUtils.getPackageName(packagePath) + " AndroidManifest.xml");
				ManifestInformation manifestInformation = new ManifestInformation(manifestLines);
				String[] aaptBadgingLines = Exec.getShellCommandOutput(aaptPath + " dump --values badging " +  PackageUtils.getPackageName(packagePath));
				AAPTBadgingValues aaptBadgingValues = new AAPTBadgingValues(aaptBadgingLines);
				ProjectInformation projectInformation = new ProjectInformation();
				if (projectInformation.init(shell, aaptBadgingValues, manifestInformation)) {
					try {
						projectInformation.getProjectInformation(apkFileName, Constants.Extensions.RECORDER);
						if (projectInformation.isNewProject()) {
							EclipseUtility.copyFileToProjectDirectory(projectInformation.getTestProject(), apkFileName, apkFileName);
						}
					} catch (Exception ex) {
						MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,	
								  "There was an exception obtaining information about the project");
						ex.printStackTrace();
						
					}
					createProject(shell, projectInformation, apkFileName);
				}
			}
		}
		return null;
	}

	/**
	 * create the project from the extracted manifest information
	 * @param shell
	 * @param aaptBadgingValues extracted data from aapt dump badging APK
	 * @param manifestInformation extracted data from aapt dump --values xmltree APK AndroidManifest.xml
	 * @param apkFileName name of the actual apk file name
	 * 
	 */
	public void createProject(Shell shell, ProjectInformation projectInformation, String apkFileName) {
		try {
			String projectName = projectInformation.getApplicationName();
			boolean fUseSupportLibraries = true;

			// create the new project
			String newProjectName = projectName + RecorderConstants.RECORDER_SUFFIX;
			IProject testProject = EclipseUtility.createBaseProject(newProjectName);
			CreateRobotiumRecorderBinary createRecorder = new CreateRobotiumRecorderBinary();
			
			// create the .classpath, AndroidManifest.xml, .project, and project.properties files
			createRecorder.createProjectProperties(testProject, projectInformation.getSDKVersion());
			createRecorder.createProject(testProject, projectInformation.getApplicationName());
			createRecorder.createManifest(testProject,
										  projectInformation.getPackageName(), 
										  projectInformation.getMinSDKVersion(),
										  projectInformation.getSDKVersion(), 
									      RecorderConstants.MANIFEST_TEMPLATE_BINARY_RECORDER);

			// create the java project, the test class output, and the "AllTests" driver which
			// iterates over all the test cases in the folder, so we can run with a single-click
			IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
			CreateRobotiumRecorder.createFolders(testProject);
			CreateRobotiumRecorder.createEclipseSettings(testProject);
			createRecorder.createRecorderTestClass(testProject, javaProject, projectInformation.getPackageName(), 
												   projectInformation.getStartActivity(), fUseSupportLibraries);
			createRecorder.createAllTests(testProject, javaProject, projectInformation.getPackageName());
			
			// copy the .apk that we're testing against to the project directory, so we can install it if needed
			EclipseUtility.copyFileToProjectDirectory(testProject, apkFileName, apkFileName);
			// pass the support libraries, because we also need to copy them
			String apkFilePath = testProject.getLocation().toString() + File.separator + apkFileName;
			createRecorder.createClasspath(testProject, projectInformation.getApplicationName(), projectInformation.getMinSDKVersion(), 
										   projectInformation.getSupportLibraries());
			createRecorder.addLibraries(testProject, projectInformation.getSupportLibraries(),  projectInformation.getMinSDKVersion());
			
			// check to see if the keyboard and logservice are installed.
			if (!InstallRecorderHandler.isPackageInstalled(RecorderConstants.KEYBOARD_PACKAGE)) {
				if (MessageDialog.openConfirm(shell, RecorderConstants.VISIBLE_AUTOMATION,
											  "The custom keyboard is not installed, do you wish to install it now?")) {
					if (InstallRecorderHandler.installAPKFromTemplate(shell, RecorderConstants.KEYBOARD_APK, RecorderConstants.KEYBOARD_PACKAGE)) {
						MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,	
													  "Please set the keyboard as the default input method from settings on your device");
					} 
				}
			}
			if (!InstallRecorderHandler.isPackageInstalled(RecorderConstants.LOGSERVICE_PACKAGE)) {
				if (MessageDialog.openConfirm(shell, RecorderConstants.VISIBLE_AUTOMATION,
											  "The Log Service is not installed. Do you want to install it now?")) {
					InstallRecorderHandler.installAPKFromTemplate(shell, RecorderConstants.KEYBOARD_APK, RecorderConstants.KEYBOARD_PACKAGE);
				}
			}
		} catch (Exception ex) {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
										  "There was an exception creating the test project " + ex.getMessage());
			ex.printStackTrace();		
		}
	}		
	   
	/**
     * retrieve the list of packages from the device
     * @return
     */
    String[] getDevicePackages() {
        return EclipseExec.getAdbCommandOutput("shell pm list packages");
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
