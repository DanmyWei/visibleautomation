package createrecorderplugin.popup.actions;

import java.io.BufferedReader;
import java.io.File;
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

import createrecorder.util.EclipseExec;
import createrecorder.util.EclipseUtility;

/**
 * when we record a session with the device, we save the files, database, and shared_prefs file into the eclipse
 * workspace. This copies those files back to the /sdcard, and the test driver copies the files into the application's
 * private data directory before running the test, thus restoring the state of the application before playback
 * @author matt2
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordTestAction  implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public RecordTestAction() {
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
				String testPackage = manifestParser.getPackage();
				if (!testPackage.endsWith("test")) {
					testPackage += ".test";
				}
				String uninstallCommand = "uninstall " + testPackage;
				EclipseExec.execADBBackgroundConsoleOutput(uninstallCommand);
				String installCommand = "install " + projectParser.getProjectName() + ".apk";
				EclipseExec.execADBBackgroundConsoleOutput(installCommand);
				String adbCommand = "shell am instrument -w " + testPackage + "/android.test.InstrumentationTestRunner";
				EclipseExec.execADBBackgroundConsoleOutput(adbCommand);
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"Record test",
						"There was an exception recording the project " + ex.getMessage());
				ex.printStackTrace();				
			}
		}
	}
	}
