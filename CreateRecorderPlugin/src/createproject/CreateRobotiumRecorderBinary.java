package createproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
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

import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;

/**
 * given an eclipse project, create the unit test project which will record events for it. (binary version)
 * @author mattrey
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class CreateRobotiumRecorderBinary extends CreateRobotiumRecorder {
	
	/**
	 * add the project under test to the template .classpath file (binary APK version)
	 * @param testProject selected android project that we want to create a recording for
	 * @param projectParser parser for .project file.
	 * @param apkFilename .APK so we can run dexdump to find out which support libraries it uses.
	 * @param supportLibraries android support libraries
	 * @throws CoreException
	 * @throws IOException
	 */
	@Override
	public void createClasspath(IProject 		testProject, 
								String 			projectName, 
								int				targetSDK,
								List<String>	supportLibraries) throws CoreException, IOException {
		String classpath;
		String classpathEntries = "";
		List<String> jarFiles = new ArrayList<String>();
		String recorderLibrary = getRecorderLibraryFromTargetSDK(targetSDK);
		jarFiles.add(recorderLibrary);
		classpath = FileUtility.readTemplate(Constants.Templates.BINARY_CLASSPATH_CREATERECORDER);	
		classpathEntries = EclipseUtility.createJarClasspathEntries(jarFiles);
		classpath = classpath.replace(Constants.VariableNames.LIBRARIES, classpathEntries);
		classpath = classpath.replace(Constants.VariableNames.CLASSNAME, projectName);
		IFile file = testProject.getFile(Constants.Filenames.CLASSPATH);
		InputStream is = new StringBufferInputStream(classpath);
		file.create(is, IFile.FORCE, null);
	}
		
	/**
	 * create the java file which drives the test
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param packageName package name from manifest
	 * @param startActivity start activity from manifest
	 * @param fUseSupportLibraries use the support or advanced libraries
	 * @throws CoreException
	 * @throws IOException
	 */
	
	@Override
	public void createRecorderTestClass(IProject 		testProject, 
										IJavaProject 	javaProject, 
										String			packageName,
										String			startActivityClassPath,
										boolean			fUseSupportLibrary) throws CoreException, IOException {
		// strip the nasty dot they use in AndroidManifest.xml to prefix activities
		int ichLastDot = startActivityClassPath.lastIndexOf('.');
		String startActivityFileName = startActivityClassPath;
		if (ichLastDot != -1) {
			startActivityFileName = startActivityFileName.substring(ichLastDot + 1);
		}
		if (startActivityClassPath.charAt(0) == '.') {
			startActivityClassPath = packageName + startActivityClassPath;
		}
		String testClass = FileUtility.readTemplate(RecorderConstants.BINARY_TESTCLASS_TEMPLATE);
		testClass = testClass.replace(Constants.VariableNames.CLASSPACKAGE, packageName);
		testClass = testClass.replace(Constants.VariableNames.CLASSPATH, startActivityClassPath);
		testClass = testClass.replace(Constants.VariableNames.CLASSNAME, startActivityFileName);
		if (fUseSupportLibrary) {
			testClass = testClass.replace(Constants.VariableNames.TEST_PACKAGE, RecorderConstants.SUPPORT_PACKAGE);
		} else {
			testClass = testClass.replace(Constants.VariableNames.TEST_PACKAGE, RecorderConstants.ADVANCED_PACKAGE);		
		}
		// write to the fully qualified path
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = packageName + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String javaFile = startActivityFileName + RecorderConstants.RECORDER_SUFFIX + "." + Constants.Extensions.JAVA;
		ICompilationUnit classFile = pack.createCompilationUnit(javaFile, testClass, true, null);
 	}
}
