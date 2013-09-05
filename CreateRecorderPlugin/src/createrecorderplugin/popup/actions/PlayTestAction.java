package createrecorderplugin.popup.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;

import createrecorder.handlers.InstallRecorderHandler;
import createrecorder.util.EclipseExec;
import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;

/**
 * when we record a session with the device, we save the files, database, and shared_prefs file into the eclipse
 * workspace. This copies those files back to the /sdcard, and the test driver copies the files into the application's
 * private data directory before running the test, thus restoring the state of the application before playback
 * @author matt2
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class PlayTestAction  implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public PlayTestAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		mShell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		mSelection = (StructuredSelection) selection;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 * record a robotium test application
	 */
	public void run(IAction action) {
		if (mSelection != null) {
			try {
				IProject project = (IProject) mSelection.getFirstElement();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				// parse AndroidManifest.xml .project and project.properties
				// grab the test class name from AndroidManifest.xml, it's under the tag
				File manifestFile = new File(projectDir, Constants.Filenames.ANDROID_MANIFEST_XML);
				ManifestParser manifestParser = null;
				File projectFile = new File(projectDir, Constants.Filenames.PROJECT_FILENAME);
				ProjectParser projectParser = null;
				try {
					manifestParser = new ManifestParser(manifestFile);
					projectParser = new ProjectParser(projectFile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				// TODO: a fun and nasty hack.  If the user selects the original package, we find
				// the package with the .test extension, so it'll work whether he selects the recorder
				// or the source code.  
				String testPackage = manifestParser.getTargetPackage();
				if (!testPackage.endsWith(Constants.Extensions.TEST)) {
					testPackage += ".test";
				}
				
				// uninstall and re-install the test driver
				// NOTE: we need to re-install the .APK if it's not installed on the device.
				String uninstallCommand = "adb uninstall " + testPackage;
				EclipseExec.execADBBackgroundConsoleOutput(uninstallCommand);
				String installCommand = "adb install " + projectParser.getProjectName() + ".apk";
				EclipseExec.execADBBackgroundConsoleOutput(installCommand);
				// NOTE: need to get the apkFileName from the root directory of the project.
				if (!InstallRecorderHandler.isPackageInstalled(manifestParser.getTargetPackage())) {
					if (MessageDialog.openConfirm(mShell, RecorderConstants.VISIBLE_AUTOMATION,
							  "The application is not installed on this device.  Do you wish to install it now?")) {
						 File[] apkFiles = FileUtility.getFilesByExtension(projectDir, "apk");
						 if ((apkFiles != null) && (apkFiles.length > 0)) {
							 String apkFileName = apkFiles[0].getName();
							 InstallRecorderHandler.installAPKFromFile(mShell, apkFileName);
						 } else {
								MessageDialog.openInformation(mShell, RecorderConstants.VISIBLE_AUTOMATION,	
										  "There is no APK in the project to install");
							 
						 }
					}		
				}
				// install the soft keyboard if neccessary
				if (!InstallRecorderHandler.isPackageInstalled(RecorderConstants.KEYBOARD_PACKAGE)) {
					if (MessageDialog.openConfirm(mShell, RecorderConstants.VISIBLE_AUTOMATION,
												  "The custom keyboard is not installed, do you wish to install it now?")) {
						if (InstallRecorderHandler.installAPKFromTemplate(mShell, RecorderConstants.KEYBOARD_APK, RecorderConstants.KEYBOARD_PACKAGE)) {
							MessageDialog.openInformation(mShell, RecorderConstants.VISIBLE_AUTOMATION,	
														  "Please set the keyboard as the default input method from settings on your device");
						} 
					}
				}

				// 
				String testPath = EclipseUtility.classNameToPath(testPackage);
				IFolder classFolder = project.getFolder(Constants.Dirs.SRC + File.separator + testPath);
				IResource[] packageContents = classFolder.members();
				if ((packageContents != null) && (packageContents.length > 0)) {
					IFolder saveStateFolder = project.getFolder(Constants.Dirs.SAVESTATE);
					
					// iterate through each class in the src folder, find the associated save state files for that
					// class and install them on the device, then run each class as a single test case
					// adb shell am instrument -w -e class com.android.foo.FooTest com.android.foo/android.test.InstrumentationTestRunner
					for (IResource srcFile : packageContents) {
						String fileName = srcFile.getName();
						if ((srcFile.getType() == IResource.FILE) && 
						    fileName.endsWith(Constants.Extensions.JAVA) &&
						    !fileName.equals(RecorderConstants.ALLTESTS_FILE)) {
							String className = testPackage + "." + fileName.substring(0, fileName.length() - 5);
							IResource saveStateClassResource = saveStateFolder.findMember(className);
							if ((saveStateClassResource != null) && (saveStateClassResource.getType() == IResource.FOLDER)) {
								IFolder stateStateClassFolder = (IFolder) saveStateClassResource;
								RecorderStateFilesAction.restoreStateFiles(stateStateClassFolder, stateStateClassFolder.getName(), Constants.Dirs.EXTERNAL_STORAGE);
							}
							String instrumentCommand = "shell am instrument -w -e class " + className + " " + 
													   testPackage + "/android.test.InstrumentationTestRunner";
							EclipseExec.execADBBackgroundConsoleOutput(instrumentCommand);
						}
					}
				} else {
					MessageDialog.openInformation(mShell, RecorderConstants.VISIBLE_AUTOMATION,
							"the source directory has no test packages");
				}
			} catch (Exception ex) {
				MessageDialog.openInformation(mShell, RecorderConstants.VISIBLE_AUTOMATION,
						"There was an exception recording the project " + ex.getMessage());
				ex.printStackTrace();				
			}
		}
	}
}
