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
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createClasspath(IProject testProject, String projectName) throws CoreException, IOException {
		String classpath = FileUtility.readTemplate(RecorderConstants.CLASSPATH_TEMPLATE);
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
	public void createManifest(IProject testProject, String target, String packageName, String minSDKVersion) throws CoreException, IOException, EmitterException {
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
	 * @param target platform from project.properties
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProjectProperties(IProject testProject, String target) throws CoreException, IOException {
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
	public void createFolders(IProject testProject) {
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
	public void createTestClass(IProject 		testProject, 
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
	 * create the libs directory and put the recorder.jar file in it
	 * @param testProject
	 * @throws CoreException
	 */
	public void addLibraries(IProject testProject) throws CoreException, IOException {
		IFolder libFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.LIBS);
		IFile file = libFolder.getFile(RecorderConstants.RECORDER_JAR);
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + RecorderConstants.RECORDER_JAR);
		file.create(fis, IFile.FORCE, null);	
		fis.close();
		IFile file2 = libFolder.getFile(RecorderConstants.EVENTRECORDERINTERFACE_JAR);
		InputStream fis2 = EmitRobotiumCode.class.getResourceAsStream("/" + RecorderConstants.EVENTRECORDERINTERFACE_JAR);
		file2.create(fis2, IFile.FORCE, null);	
		fis.close();
	}
}
