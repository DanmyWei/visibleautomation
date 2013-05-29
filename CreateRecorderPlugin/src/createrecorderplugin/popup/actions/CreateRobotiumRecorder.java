package createrecorderplugin.popup.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

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

import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;

/**
 * given an eclipse project, create the unit test project which will record events for it.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class CreateRobotiumRecorder implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public CreateRobotiumRecorder() {
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
			try {
				IProject project = (IProject) mSelection.getFirstElement();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				
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
				// create the new project
				String newProjectName = project.getName() + RecorderConstants.RECORDER_SUFFIX;
				IProject testProject = EclipseUtility.createBaseProject(newProjectName);
				
				// create the .classpath, AndroidManifest.xml, .project, and project.properties files
				createProjectProperties(testProject, projectPropertiesScan.getTarget());
				createProject(testProject,  projectParser.getProjectName());
				createClasspath(testProject, projectParser.getProjectName());
				createManifest(testProject, projectPropertiesScan.getTarget(), manifestParser.getPackage(), manifestParser.getMinSDKVersion());

				// create the java project, the test class output, and the "AllTests" driver which
				// iterates over all the test cases in the folder, so we can run with a single-click
				IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
				createFolders(testProject);
				createTestClass(testProject, javaProject, manifestParser.getPackage(), manifestParser.getStartActivity());
				createAllTests(testProject, javaProject, manifestParser.getPackage());
				addLibrary(testProject);
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"CreateRecorderPlugin",
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
	
	/**
	 * add the project under test to the template .classpath file.
	 * @param testProject selected android project that we want to create a recording for
	 * @param projectParser parser for .project file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createClasspath(IProject testProject, String projectName) throws CoreException, IOException {
		String classpath = FileUtility.readTemplate(RecorderConstants.CLASSPATH_TEMPLATE);
		classpath = classpath.replace(Constants.VariableNames.CLASSNAME, projectName);
		IFile file = testProject.getFile(Constants.Filenames.CLASSPATH);
		InputStream is = new StringBufferInputStream(classpath);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * add the project under test to the template .classpath file (binary APK version)
	 * @param testProject selected android project that we want to create a recording for
	 * @param projectParser parser for .project file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createBinaryClasspath(IProject testProject, String projectName) throws CoreException, IOException {
		String classpath = FileUtility.readTemplate(Constants.Templates.BINARY_CLASSPATH_CREATERECORDER);
		classpath = classpath.replace(Constants.VariableNames.CLASSNAME, projectName);
		IFile file = testProject.getFile(Constants.Filenames.CLASSPATH);
		InputStream is = new StringBufferInputStream(classpath);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * populate the template for the AndroidManifest.xml file
	 * @param testProject android project under test.
	 * @param android target
	 * @param packageName from the manifest
	 * @param minSDKVersion min-sdk-version from the manifest
	 * @throws CoreException
	 * @throws IOException
	 * TODO: add target-sdk-version into manifest
	 */
	public static void createManifest(IProject testProject, String target, String packageName, String minSDKVersion) throws CoreException, IOException, EmitterException {
		String manifest = FileUtility.readTemplate(RecorderConstants.MANIFEST_TEMPLATE);
		manifest = manifest.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		if (minSDKVersion != null) {
			manifest = manifest.replace(Constants.VariableNames.MIN_SDK_VERSION, minSDKVersion);
		} else {
			int ichDash = target.indexOf('-');
			if (ichDash == -1) {
				throw new EmitterException("failed to find SDK target in project.properties or AndroidManifest.xml");
			}
			String sdkVersion = target.substring(ichDash + 1);
			if (!StringUtils.isNumber(sdkVersion)) {
				throw new EmitterException("found target in project.properties, but it's ill-formed");
			}
			manifest = manifest.replace(Constants.VariableNames.MIN_SDK_VERSION, sdkVersion);
		}
		IFile file = testProject.getFile(Constants.Filenames.ANDROID_MANIFEST_XML);
		InputStream is = new StringBufferInputStream(manifest);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * popupate the .project file.
	 * @param testProject  android project under test.
	 * @param projectParser parser for the .project file under test.
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createProject(IProject testProject, String projectName) throws CoreException, IOException {
		String project = FileUtility.readTemplate(RecorderConstants.PROJECT_TEMPLATE);
		project = project.replace(Constants.VariableNames.CLASSNAME, projectName);
		project = project.replace(Constants.VariableNames.MODE, Constants.Names.RECORDER);
		IFile file = testProject.getFile(Constants.Filenames.PROJECT_FILENAME);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(project);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * create the project.properties file
	 * @param testProject reference to the project
	 * @param target platform from project.properties
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createProjectProperties(IProject testProject, String target) throws CoreException, IOException {
		String projectProperties = FileUtility.readTemplate(RecorderConstants.PROJECT_PROPERTIES_TEMPLATE);
		projectProperties = projectProperties.replace(Constants.VariableNames.TARGET, target);
		EclipseUtility.writeString(testProject, Constants.Filenames.PROJECT_PROPERTIES_FILENAME, projectProperties);
	}
	
	
	/**
	 * create the AllTests.java file, which iterates through the test files and runs them
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param manifestParser parsed data from AndroidManifest.xml file
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createAllTests(IProject 			testProject,
								      IJavaProject 		javaProject, 
								      String 			packageName) throws CoreException, IOException {
		String allTests = FileUtility.readTemplate(RecorderConstants.ALLTESTS_TEMPLATE);
		allTests = allTests.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = packageName + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		ICompilationUnit classFile = pack.createCompilationUnit(RecorderConstants.ALLTESTS_FILE, allTests, true, null);	
	}
	
	/**
	 * create the src, gen, and res folders
	 * @param testProject
	 */
	public static void createFolders(IProject testProject) {
		IFolder sourceFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.SRC);
		IFolder resFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.RES);
	}
	
	/**
	 * create the java file which drives the test
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param packageName package name from manifest
	 * @param startActivity start activity from manifest
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createTestClass(IProject 		testProject, 
									   IJavaProject 	javaProject, 
									   String			packageName,
									   String			startActivity) throws CoreException, IOException {
		String testClass = FileUtility.readTemplate(RecorderConstants.TESTCLASS_TEMPLATE);
		testClass = testClass.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		testClass = testClass.replace(Constants.VariableNames.CLASSNAME, startActivity);
		// write to the fully qualified path
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = packageName + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String javaFile = startActivity + RecorderConstants.RECORDER_SUFFIX + "." + Constants.Extensions.JAVA;
		ICompilationUnit classFile = pack.createCompilationUnit(javaFile, testClass, true, null);
 	}
	/**
	 * create the java file which drives the test
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param packageName package name from manifest
	 * @param startActivity start activity from manifest
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createBinaryTestClass(IProject 		testProject, 
										     IJavaProject 	javaProject, 
										     String			packageName,
										     String			startActivity) throws CoreException, IOException {
		String testClass = FileUtility.readTemplate(RecorderConstants.BINARY_TESTCLASS_TEMPLATE);
		testClass = testClass.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		testClass = testClass.replace(Constants.VariableNames.CLASSNAME, startActivity);
		// write to the fully qualified path
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = packageName + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String javaFile = startActivity + RecorderConstants.RECORDER_SUFFIX + "." + Constants.Extensions.JAVA;
		ICompilationUnit classFile = pack.createCompilationUnit(javaFile, testClass, true, null);
 	}
	
	
	/**
	 * create the libs directory and put the recorder.jar file in it
	 * @param testProject
	 * @throws CoreException
	 */
	public static void addLibrary(IProject testProject) throws CoreException {
		IFolder libFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.LIBS);
		IFile file = libFolder.getFile(RecorderConstants.RECORDER_JAR);
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + RecorderConstants.RECORDER_JAR);
		file.create(fis, IFile.FORCE, null);	
	}
}
