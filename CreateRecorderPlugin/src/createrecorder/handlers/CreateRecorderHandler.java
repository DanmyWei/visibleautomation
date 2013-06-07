package createrecorder.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;

import createproject.CreateRobotiumRecorderBinary;
import createrecorder.util.EclipseUtility;
import createrecorder.util.Exec;
import createrecorder.util.ManifestInformation;
import createrecorder.util.PackageUtils;
import createrecorder.util.Pair;
import createrecorder.util.RecorderConstants;
import createrecorder.util.TestClassDialog;
import createrecorderplugin.popup.actions.CreateRobotiumRecorderAction;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class CreateRecorderHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public CreateRecorderHandler() {
	}
	
	/**
	 * retrieve the list of packages from the device
	 * @return
	 */
	String[] getDevicePackages() {
		return Exec.getAdbCommandOutput("shell pm list packages -3");
	}
	
	/**
	 * handler to create the project
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		String packagePath = TestClassDialog.getTestClassDialog(shell, "Robotium Recorder", "Enter classpath of APK to record");
		if (packagePath != null) {	
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			
			// aapt has moved from android-sdks/platform-tools to android-sdks/build-tools/17.0.0 in version 21.
			String aaptPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS + File.separator + Constants.Executables.AAPT;
			File aaptFile = new File(aaptPath);
			if (!aaptFile.exists()) {
				aaptPath = androidSDK + File.separator + Constants.Dirs.PLATFORM_TOOLS_22 + File.separator + Constants.Executables.AAPT;
			}
			aaptFile = new File(aaptPath);
			if (!aaptFile.exists()) {
				MessageDialog.openInformation(
						shell,
						"CreateRecorderPlugin",
						"could not find aapt in " + aaptPath);
				
			} else {
				String[] manifestLines = Exec.getShellCommandOutput(aaptPath + " dump --values xmltree " + PackageUtils.getPackageName(packagePath) + " AndroidManifest.xml");
				ManifestInformation manifestInformation = new ManifestInformation(manifestLines);
				if (!manifestInformation.verify()) {
					MessageDialog.openInformation(
							shell,
							"CreateRecorderPlugin",
							"failed to parse AndroidManifest.xml " + manifestInformation.errorMessage());
				
				} else {
					// extract the packge so we can get the AndroidManifest.xml file.
					createProject(shell, manifestInformation);
				}
			}
		}
		return null;
	}
	
	/**
	 * create the project from the extracted manifest information
	 * @param shell
	 * @param manifestInformation
	 */
	public void createProject(Shell shell, ManifestInformation manifestInformation) {
		try {
			String projectName = manifestInformation.mApplicationName;
	
			// create the new project
			String newProjectName = projectName + RecorderConstants.RECORDER_SUFFIX;
			IProject testProject = EclipseUtility.createBaseProject(newProjectName);
			CreateRobotiumRecorderBinary createRecorder = new CreateRobotiumRecorderBinary();
			
			// create the .classpath, AndroidManifest.xml, .project, and project.properties files
			String target = "target=android-" + Integer.toString(manifestInformation.mTargetSDKVersion);
			createRecorder.createProjectProperties(testProject, target);
			createRecorder.createProject(testProject, manifestInformation.mApplicationName);
			createRecorder.createClasspath(testProject, manifestInformation.mApplicationName);
			createRecorder.createManifest(testProject, target, manifestInformation.mPackage, Integer.toString(manifestInformation.mMinSDKVersion));

			// create the java project, the test class output, and the "AllTests" driver which
			// iterates over all the test cases in the folder, so we can run with a single-click
			IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
			createRecorder.createFolders(testProject);
			createRecorder.createTestClass(testProject, javaProject, manifestInformation.mPackage, manifestInformation.mStartActivityName);
			createRecorder.createAllTests(testProject, javaProject, manifestInformation.mPackage);
			createRecorder.addLibrary(testProject);
		} catch (Exception ex) {
			MessageDialog.openInformation(
					shell,
					"CreateRecorderPlugin",
					"There was an exception creating the test project " + ex.getMessage());
			ex.printStackTrace();
		
		}
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
