package createrecorderplugin.popup.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.androidApp.emitter.CodeDefinition;
import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.emitter.EmitRobotiumCodeSource;
import com.androidApp.emitter.EmitterException;
import com.androidApp.emitter.MotionEventList;
import com.androidApp.emitter.SetupRobotiumProject;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

import createproject.GenerateRobotiumTestCode;
import createproject.ProjectInformation;
import createrecorder.handlers.CreateTestHandler;
import createrecorder.util.EclipseUtility;
import createrecorder.util.EclipseExec;
import createrecorder.util.RecorderConstants;

/**
 * extract the events file from the device, and either create a new project, or add a test class to an
 * existing junit project which plays back the recording
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class GenerateRobotiumTestCodeAction implements IObjectActionDelegate {
	protected final String TEST_SUFFIX = "Test";

	private Shell mShell;
	private StructuredSelection mSelection;

	/**
	 * Constructor for Action1.
	 */
	public GenerateRobotiumTestCodeAction() {
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
	 * create the robotium recorder project
	 */
	public void run(IAction action) {
		String eventsFileName = "device";
	
		if (mSelection != null) {
			try {
				IProject project = (IProject) mSelection.getFirstElement();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				
				// read the manifest, the .project file, and the project.properties file from the tested application
				File manifestFile = new File(projectDir, Constants.Filenames.ANDROID_MANIFEST_XML);
				File projectPropertiesFile = new File(projectDir,  Constants.Filenames.PROJECT_PROPERTIES_FILENAME);
				ManifestParser manifestParser = null;
				ProjectPropertiesScan projectPropertiesScan = null;
				try {
					manifestParser = new ManifestParser(manifestFile);
					projectPropertiesScan = new ProjectPropertiesScan(projectPropertiesFile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//public boolean init(Shell shell, ManifestParser manifestParser, ProjectPropertiesScan projectPropertiesScan) {

				ProjectInformation projectInformation = new ProjectInformation();
			    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				projectInformation.init(shell, manifestParser, projectPropertiesScan);
				IFolder binFolder = project.getFolder(Constants.Dirs.BIN);
				IFile apkFile = EclipseUtility.findFile(binFolder, project.getName() + ".*" + Constants.Extensions.APK);
				projectInformation.getProjectInformation(apkFile.getName(), Constants.Extensions.RECORDER);

				// read the events file and generate the code into a hashtable, with code for the main test class
				// and the activity handler classes.
				GenerateRobotiumTestCode codeGenerator = new GenerateRobotiumTestCode();
				EmitRobotiumCodeSource emitter = new EmitRobotiumCodeSource();

				// generate the code into a hashtable with code for the test class and the activity handlers.
				List<MotionEventList> motionEvents = new ArrayList<MotionEventList>();
				Hashtable<CodeDefinition, List<LineAndTokens>> outputCode = new Hashtable<CodeDefinition, List<LineAndTokens>>();
				CreateTestHandler.readHandlers(projectInformation.getTestProject(), outputCode);
				emitter.generateTestCode(projectInformation.getEventsFileName(), outputCode, motionEvents);
				if (projectInformation.isNewProject()) {
					codeGenerator.createProject(emitter, RecorderConstants.MANIFEST_TEMPLATE_TEST, projectInformation);
				}
				codeGenerator.writeTheCode(emitter, outputCode, motionEvents, projectInformation, Constants.Templates.TEST_FUNCTION);
				CreateTestHandler.writeHandlers(projectInformation.getTestProject(), outputCode);
				GenerateRobotiumTestCode.copyViewDirectives(projectPath.toString());
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"GenerateRobotiumTestCode",
						"There was an exception creating the test project " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
}
