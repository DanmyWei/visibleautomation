package createproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
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
import com.androidApp.emitter.MotionEventList;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.savestate.SaveStateException;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

import createrecorder.util.EclipseExec;
import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;

/**
 * given an eclipse project, create the unit test project which will record events for it.
 * @author mattrey
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class CreateRobotiumRecorder  {
	
	/**
	 * add the project under test to the template .classpath file.
	 * @param testProject selected android project that we want to create a recording for
	 * @param projectParser parser for .project file.
	 * @param targetSDK target SDK level
	 * @param supportLibraries list of support libraries (if they're used by the app)
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createClasspath(IProject 		testProject, 
								String 			projectName,
								int				targetSDK,
								List<String> 	supportLibraries) throws CoreException, IOException {
		String classpath;
		if (!supportLibraries.isEmpty()) {
			classpath = FileUtility.readTemplate(Constants.Templates.CLASSPATH_TEMPLATE_CREATERECORDER_SUPPORT);
			// these are only needed for the compilation of the recorder, not runtime. (I hope)
			// String supportLibrariesClasspathEntries = EclipseUtility.createJarClasspathEntries(supportLibraries);
			// classpath = classpath.replace(Constants.VariableNames.SUPPORT_LIBRARIES, supportLibrariesClasspathEntries);
			classpath = classpath.replace(Constants.VariableNames.SUPPORT_LIBRARIES, "");
			if (supportLibraries.contains(RecorderConstants.SupportLibraries.SUPPORT_V4)) {
				classpath = classpath.replace(Constants.VariableNames.RECORDER_SUPPORT, Constants.Filenames.RECORDER_SUPPORT_V4_JAR);
			} else if (supportLibraries.contains(RecorderConstants.SupportLibraries.SUPPORT_V13)) {
				classpath = classpath.replace(Constants.VariableNames.RECORDER_SUPPORT, Constants.Filenames.RECORDER_SUPPORT_V13_JAR);
			}
		} else {
			classpath = FileUtility.readTemplate(Constants.Templates.CLASSPATH_TEMPLATE_CREATERECORDER);
			String recorderLibrary = getRecorderLibraryFromTargetSDK(targetSDK);
			classpath = classpath.replace(Constants.VariableNames.RECORDER_LIBRARY, recorderLibrary);
		}
		classpath = classpath.replace(Constants.VariableNames.CLASSNAME, projectName);
		IFile file = testProject.getFile(Constants.Filenames.CLASSPATH);
		InputStream is = new StringBufferInputStream(classpath);
		file.create(is, IFile.FORCE, null);
	}
		
	/**
	 * populate the template for the AndroidManifest.xml file
	 * @param testProject android project under test.
	 * @param targetSDK integer android SDK level
	 * @param packageName from the manifest
	 * @param minSDKVersion min-sdk-version from the manifest
	 * @param manifestTemplate different for binary vs source (binary doesn't support debuggable=true)s
	 * @throws CoreException
	 * @throws IOException
	 * TODO: add target-sdk-version into manifest
	 */
	public void createManifest(IProject testProject, 
							   String 	packageName, 
							   int 		minSDKVersion,
							   int 		targetSDK, 
							   String 	manifestTemplate) throws CoreException, IOException, EmitterException {
		String manifest = FileUtility.readTemplate(manifestTemplate);
		manifest = manifest.replace(Constants.VariableNames.TARGET_PACKAGE, packageName);
		if (minSDKVersion != 0) {
			manifest = manifest.replace(Constants.VariableNames.MIN_SDK_VERSION, Integer.toString(minSDKVersion));
		} else {
			manifest = manifest.replace(Constants.VariableNames.MIN_SDK_VERSION, Integer.toString(targetSDK));
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
	public void createProject(IProject testProject, String projectName) throws CoreException, IOException {
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
	 * @param SDKVersion platform from project.properties
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProjectProperties(IProject testProject, int SDKVersion) throws CoreException, IOException {
		String target= null;
		
		int bestAvailableSDKLevel = EclipseUtility.getRecorderAndroidSDKLevel();
		EclipseUtility.printConsole("best available SDK level = " + bestAvailableSDKLevel);
		target = "target=android-" + bestAvailableSDKLevel;
		String projectProperties = FileUtility.readTemplate(RecorderConstants.PROJECT_PROPERTIES_TEMPLATE);
		projectProperties = projectProperties.replace(Constants.VariableNames.TARGET, target);
		EclipseUtility.writeString(testProject, Constants.Filenames.PROJECT_PROPERTIES_FILENAME, projectProperties);
	}
	
	
	/**
	 * copy the eclipse preferences file, so we don't have to fix android properties.
	 * @param testProject
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void createEclipseSettings(IProject testProject) throws CoreException, IOException {
		InputStream isEclipsePrefs = FileUtility.getTemplateStream(Constants.Filenames.ECLIPSE_JDT_PREFS);
		IFolder settingsFolder = testProject.getFolder(Constants.Dirs.SETTINGS);
		IFile file = settingsFolder.getFile(Constants.Filenames.ECLIPSE_JDT_PREFS);
		file.create(isEclipsePrefs, IFile.FORCE, null);	
		isEclipsePrefs.close();
	}
	
	
	/**
	 * create the AllTests.java file, which iterates through the test files and runs them
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param manifestParser parsed data from AndroidManifest.xml file
	 * @throws CoreException
	 * @throws IOException
	 */
	public  void createAllTests(IProject 			testProject,
						        IJavaProject 		javaProject, 
						        String 				packageName) throws CoreException, IOException {
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
		IFolder assetsFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.ASSETS);
		IFolder settingsFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.SETTINGS);
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
	public void createRecorderTestClass(IProject 		testProject, 
									    IJavaProject 	javaProject, 
									    String			packageName,
									    String			startActivity,
									    boolean			fUseSupportLibrary) throws CoreException, IOException {
		String testClass = FileUtility.readTemplate(RecorderConstants.TESTCLASS_TEMPLATE);
		testClass = testClass.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		testClass = testClass.replace(Constants.VariableNames.CLASSNAME, startActivity);
		if (fUseSupportLibrary) {
			testClass = testClass.replace(Constants.VariableNames.TEST_PACKAGE, RecorderConstants.SUPPORT_PACKAGE);
		} else {
			testClass = testClass.replace(Constants.VariableNames.TEST_PACKAGE, RecorderConstants.ADVANCED_PACKAGE);		
		}
		// write to the fully qualified path
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = packageName + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String javaFile = startActivity + RecorderConstants.RECORDER_SUFFIX + "." + Constants.Extensions.JAVA;
		ICompilationUnit classFile = pack.createCompilationUnit(javaFile, testClass, true, null);
 	}	
	
	/**
	 * create the libs directory and put the recorder.jar file in it
	 * @param testProject project to copy the library files into
	 * @param supportLibraries list of support libraries (if they're used by the app)
	 * @param sdkLevel application target SDK
	 * @throws CoreException
	 */
	public void addLibraries(IProject 		testProject, 
							 List<String> 	supportLibraries, 
							 int			sdkLevel) throws CoreException, IOException {
		IFolder libFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.LIBS);
		EclipseUtility.writeResource(libFolder, RecorderConstants.RECORDER_JAR);
		EclipseUtility.writeResource(libFolder, RecorderConstants.EVENTRECORDERINTERFACE_JAR);
		String recorderLibrary = getRecorderLibraryFromTargetSDK(sdkLevel);
		EclipseUtility.writeResource(libFolder, recorderLibrary);
	}
	
	/**
	 * given the SDK level of the app, return the appropriate recorder library
	 * @param sdkLevel
	 * @return
	 */
	public static String getRecorderLibraryFromTargetSDK(int sdkLevel) {
		return Constants.Filenames.RECORDER_40_JAR;
		/*
		if (sdkLevel >= 14) {
			return Constants.Filenames.RECORDER_40_JAR;
		} else if (sdkLevel >= 11) {
			return Constants.Filenames.RECORDER_30_JAR;
		} else {
			return Constants.Filenames.RECORDER_23_JAR;
		}
		*/
	}
}
