package createrecorderplugin.popup.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.emitter.EmitterException;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.FileUtility;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

import createproject.CreateRobotiumRecorder;
import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;
import createrecorderplugin.Activator;

/**
 * given an eclipse project, create the unit test project which will record events for it.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class CreateRobotiumRecorderAction implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public CreateRobotiumRecorderAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		mShell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 * create the robotium recorder project
	 */
	public void run(IAction action) {
		if (mSelection != null) {
			boolean fUseSupportLibraries = true;
			/*
			if (!Activator.verifyLicense()) {
				MessageDialog.openInformation(mShell, "Visible Automation", "failed to validate license");
				return;
			}
			*/
			try {
				IProject project = (IProject) mSelection.getFirstElement();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				// grab the .APK file path from the project dir
				IFolder binFolder = project.getFolder(Constants.Dirs.BIN);
				IFile apkFile = EclipseUtility.findFile(binFolder, project.getName() + ".*" + Constants.Extensions.APK);
				if (apkFile == null) {
					MessageDialog.openInformation(mShell, RecorderConstants.VISIBLE_AUTOMATION,
												  "failed to find APK file named " + project.getName());
					return;
				}
				String apkFilename = projectPath.toString() + File.separator + Constants.Dirs.BIN + File.separator + apkFile.getName();
			
				// parse AndroidManifest.xml .project and project.properties
				File manifestFile = new File(projectDir, Constants.Filenames.ANDROID_MANIFEST_XML);
				File projectFile = new File(projectDir, Constants.Filenames.PROJECT_FILENAME);
				File projectPropertiesFile = new File(projectDir, Constants.Filenames.PROJECT_PROPERTIES_FILENAME);
				ManifestParser manifestParser = null;
				ProjectParser projectParser = null;
				ProjectPropertiesScan projectPropertiesScan = null;
				try {
					manifestParser = new ManifestParser(manifestFile);
					projectParser = new ProjectParser(projectFile);
					projectPropertiesScan = new ProjectPropertiesScan(projectPropertiesFile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				CreateRobotiumRecorder createRecorder = new CreateRobotiumRecorder();
				// create the new project
				String newProjectName = project.getName() + RecorderConstants.RECORDER_SUFFIX;
				IProject testProject = EclipseUtility.createBaseProject(newProjectName);
				
				// create the .classpath, AndroidManifest.xml, .project, and project.properties files
				createRecorder.createProjectProperties(testProject, projectPropertiesScan.getTargetSDK());
				createRecorder.createProject(testProject,  projectParser.getProjectName());
				List<String> supportLibraries = EclipseUtility.getSupportLibraries(apkFilename);
				createRecorder.createClasspath(testProject, projectParser.getProjectName(), 
											   projectPropertiesScan.getTargetSDK(), supportLibraries);
				createRecorder.createManifest(testProject, manifestParser.getPackage(), 
											  projectPropertiesScan.getTargetSDK(),
											  manifestParser.getMinSDKVersion(),
											  RecorderConstants.MANIFEST_TEMPLATE_RECORDER);

				// create the java project, the test class output, and the "AllTests" driver which
				// iterates over all the test cases in the folder, so we can run with a single-click
				IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
				createRecorder.createFolders(testProject);
				createRecorder.createRecorderTestClass(testProject, javaProject, manifestParser.getPackage(), 
											   manifestParser.getStartActivity(), fUseSupportLibraries);
				createRecorder.createAllTests(testProject, javaProject, manifestParser.getPackage());
				createRecorder.addLibraries(testProject, supportLibraries, projectPropertiesScan.getTargetSDK());
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"Visible Automation",
						"There was an exception creating the test project " + ex.getMessage());
				ex.printStackTrace();
			
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		mSelection = (StructuredSelection) selection;
	}
}
