package createrecorderplugin.popup.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.emitter.EmitRobotiumCodeSource;
import com.androidApp.emitter.MotionEventList;
import com.androidApp.emitter.SetupRobotiumProject;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

import createproject.GenerateRobotiumTestCode;
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
				File manifestFile = new File(projectDir, Constants.Filenames.ANDROID_MANIFEST_XML);
				File projectFile = new File(projectDir,  Constants.Filenames.PROJECT_FILENAME);
				File projectPropertiesFile = new File(projectDir,  Constants.Filenames.PROJECT_PROPERTIES_FILENAME);
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
				GenerateRobotiumTestCode codeGenerator = new GenerateRobotiumTestCode();
				// create the new project
				String newProjectName = project.getName() + TEST_SUFFIX;
				IProject testProject = EclipseUtility.createBaseProject(newProjectName);
				EmitRobotiumCodeSource emitter = new EmitRobotiumCodeSource();
				IPreferencesService service = Platform.getPreferencesService();
				String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);

				eventsFileName = codeGenerator.getEventsFile(androidSDK, Constants.Names.DEVICE);
				List<MotionEventList> motionEvents = new ArrayList<MotionEventList>();
				Hashtable<String, List<LineAndTokens>> outputCode = emitter.generateTestCode(emitter, eventsFileName, motionEvents);
				List<LineAndTokens> mainCode = outputCode.get(Constants.MAIN);
				
				// test class path and test class name generated by emitter.
				String testClassPath = emitter.getApplicationClassPath() + Constants.Extensions.TEST;
				String testClassName = emitter.getApplicationClassName() + Constants.Extensions.TEST;
				String templateFileName = testClassName + "." + Constants.Extensions.JAVA;
				String packagePath = manifestParser.getPackage() +  Constants.Extensions.TEST;
				
				// scan to see if there are any unit tests in the source folder, and if not, the robotium jar file
				// hasn't been selected, so we have to prompt the user.
				IFolder srcFolder = testProject.getFolder(Constants.Dirs.SRC);
				int uniqueFileIndex = 0;
				if (srcFolder.exists()) {
					IFolder projectFolder = srcFolder.getFolder(FileUtility.sourceDirectoryFromClassName(packagePath));
					uniqueFileIndex = EclipseUtility.uniqueFileIndex(projectFolder, templateFileName);
				}
				if (uniqueFileIndex != 0) {
					testClassPath += Integer.toString(uniqueFileIndex);
					testClassName += Integer.toString(uniqueFileIndex) ;
				} else {

					// create the java project and write the test driver out into it
					IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
					codeGenerator.createFolders(testProject);
					codeGenerator.createProjectProperties(testProject, projectPropertiesScan.getTarget(), projectParser.getProjectName());
					codeGenerator.createProject(testProject, projectParser.getProjectName());

					// FIRST TIME ONLY:create the buil.xml AndroidManifest.xml, and resource files.
					codeGenerator.writeBuildXML(testProject, emitter.getApplicationClassPath());
					codeGenerator.writeManifest(testProject, testClassName, testClassPath, emitter.getApplicationPackage());
					codeGenerator.writeResources(testProject);				
					codeGenerator.writeClasspath(testProject,  projectParser.getProjectName(), Constants.Filenames.ROBOTIUM_JAR);
					codeGenerator.copyJarToLibs(testProject, Constants.Filenames.UTILITY_JAR);
					codeGenerator.copyJarToLibs(testProject, Constants.Filenames.ROBOTIUM_JAR);

					// create the package for the test code, then write the AllTests.java driver which executes all of the unit tests
					IPackageFragment pack = javaProject.getPackageFragmentRoot(srcFolder).createPackageFragment(packagePath, false, null);
					pack.open(null);
					codeGenerator.copyTestDriverFile(pack, packagePath, emitter.getApplicationClassPath());
					pack.close();
				}
				// write out the test code, first to a temporary file, then to the actual project file.
				codeGenerator.writeTestCode(emitter, mainCode, packagePath, testClassName, Constants.Filenames.OUTPUT);
				IJavaProject javaProject = JavaCore.create(testProject);
				IPackageFragmentRoot packRoot = javaProject.getPackageFragmentRoot(srcFolder);
				packRoot.open(null);
				IPackageFragment pack = packRoot.getPackageFragment(packagePath);
				String testCode = FileUtility.readToString(new FileInputStream(Constants.Filenames.OUTPUT));
				String projectFileOutput = testClassName + "." + Constants.Extensions.JAVA;
				ICompilationUnit classFile = pack.createCompilationUnit(projectFileOutput, testCode, true, null);	
				codeGenerator.writeInterstitalHandlers(pack, emitter, outputCode, testClassPath);
				codeGenerator.writeMotionEvents(testProject, testClassName, motionEvents);
				codeGenerator.saveStateFiles(Constants.Dirs.EXTERNAL_STORAGE, testClassName, manifestParser.getPackage(), testProject);

				packRoot.close();
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"GenerateRobotiumTestCode",
						"There was an exception creating the test project " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	/** 
	 * file dialog to prompt for the location of the robotium jar file
	 * @return
	 */
	public  FileDialog robotiumJarFileDialog() {
		String[] filerExtensions = new String[1];
		filerExtensions[0] = "jar";
		FileDialog fileDialog = new FileDialog(mShell);
		fileDialog.setText("locate robotium jar file");
		fileDialog.setFilterExtensions(filerExtensions);
		fileDialog.open();
		return fileDialog;
	}
}
