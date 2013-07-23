package createrecorder.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.MotionEventList;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;

import createproject.GenerateRobotiumTestCodeBinary;
import createrecorder.util.EclipseUtility;
import createrecorder.util.EclipseExec;
import createrecorder.util.ManifestInformation;
import createrecorder.util.PackageUtils;
import createrecorder.util.RecorderConstants;
import createrecorder.util.TestClassDialog;

/**
 * eclipse plugin handler to create the test project.
 * @author Matthew
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class CreateTestHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		
		// prompt the user for the classpath of the APK file to test
		TestClassDialog testClassDialog = new TestClassDialog();
		String packagePath = testClassDialog.getTestClassDialog(shell, "Visible Automation", "Enter classpath of APK to test");
		if (packagePath != null) {
			
			// get the android sdk directory from the eclipse ADT preferences
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
						"CreateTestHandler",
						"could not find aapt in " + aaptPath);
				
			} else {
				// pull the aapt parsed manifest
				String[] manifestLines = Exec.getShellCommandOutput(aaptPath + " dump --values xmltree " + PackageUtils.getPackageName(packagePath) + " AndroidManifest.xml");
				ManifestInformation manifestInformation = new ManifestInformation(manifestLines);
				if (!manifestInformation.verify()) {
					MessageDialog.openInformation(
							shell,
							"CreateTestHandler",
							"failed to parse AndroidManifest.xml " + manifestInformation.errorMessage());
				} else {
					
					// and creat the project from the manifest information
					createProject(shell, manifestInformation);
				}
			}
		}
		return null;
	}
	
	/**
	 * create the test project from the manifest information
	 * @param shell eclipse shell
	 * @param manifestInformation parsed aapt manifest information
	 */
	public void createProject(Shell shell, ManifestInformation manifestInformation) {
		String eventsFileName = Constants.Names.DEVICE;
		String outputCodeFileName = Constants.Filenames.OUTPUT;
		try {
			// get the android SDK directory so we can execute adb
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			
			// sometimes the manifest application name is .Application or com.foo.bar.Application.  We just want "Application"
			String projectName = manifestInformation.mApplicationName;
			int ichLastDot = manifestInformation.mApplicationName.lastIndexOf('.');
			if (ichLastDot != -1) {
				projectName = projectName.substring(ichLastDot + 1);
			}
			
			// binary code project setup
			GenerateRobotiumTestCodeBinary codeGenerator = new GenerateRobotiumTestCodeBinary();

			// create the new project: NOTE: should we check for existence?
			String newProjectName = projectName + Constants.Extensions.TEST;
			IProject testProject = EclipseUtility.createBaseProject(newProjectName);
			
			// binary code generator
			EmitRobotiumCodeBinary emitter = new EmitRobotiumCodeBinary();
			eventsFileName = codeGenerator.getEventsFile(androidSDK, Constants.Names.DEVICE);
			List<MotionEventList> motionEvents = new ArrayList<MotionEventList>();
			Hashtable<String, List<LineAndTokens>> outputCode = emitter.generateTestCode(emitter, eventsFileName, motionEvents);
			List<LineAndTokens> mainCode = outputCode.get(Constants.MAIN);
			
			// test class path and test class name generated by emitter.
			String testClassPath = emitter.getApplicationClassPath() + Constants.Extensions.TEST;
			String testClassName = emitter.getApplicationClassName() + Constants.Extensions.TEST;
			String templateFileName = testClassName + "." + Constants.Extensions.JAVA;
			String packagePath = manifestInformation.mPackage +  Constants.Extensions.TEST;
			
			// scan to see if there are any unit tests in the source folder, and if not, the robotium jar file
			// hasn't been selected, so we have to prompt the user.
			IFolder srcFolder = testProject.getFolder(Constants.Dirs.SRC);
			int uniqueFileIndex = 0;
			if (srcFolder.exists()) {
				IFolder projectFolder = srcFolder.getFolder(FileUtility.sourceDirectoryFromClassName(packagePath));
				uniqueFileIndex = EclipseUtility.uniqueFileIndex(projectFolder, templateFileName);
			}
			
			// TODO: not the best test for an existing project
			if (uniqueFileIndex != 0) {
				testClassPath += Integer.toString(uniqueFileIndex);
				testClassName += Integer.toString(uniqueFileIndex) ;
			} else {

				// create the java project and write the test driver out into it
				IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
				codeGenerator.createFolders(testProject);
				String target = "target=android-" + Integer.toString(manifestInformation.mTargetSDKVersion);
				codeGenerator.createProjectProperties(testProject, target, manifestInformation.mApplicationName);
				codeGenerator.createProject(testProject, manifestInformation.mApplicationName);

				// FIRST TIME ONLY:create the buil.xml AndroidManifest.xml, and resource files.
				codeGenerator.writeBuildXML(testProject, emitter.getApplicationClassPath());
				codeGenerator.writeManifest(testProject, testClassName, testClassPath, emitter.getApplicationPackage());
				codeGenerator.writeResources(testProject);				
				codeGenerator.writeClasspath(testProject, null, Constants.Filenames.ROBOTIUM_JAR);
				codeGenerator.copyJarToLibs(testProject, Constants.Filenames.UTILITY_JAR);
				codeGenerator.copyJarToLibs(testProject, Constants.Filenames.ROBOTIUM_JAR);

				// create the package for the test code, then write the AllTests.java driver which executes all of the unit tests
				IPackageFragment pack = javaProject.getPackageFragmentRoot(srcFolder).createPackageFragment(packagePath, false, null);
				pack.open(null);
				codeGenerator.copyTestDriverFile(pack, packagePath, emitter.getApplicationClassPath());
				pack.close();
			}
			// write out the test code, first to a temporary file, then to the actual project file.
			codeGenerator.writeTestCode(emitter, mainCode, packagePath, testClassName, outputCodeFileName);
			IJavaProject javaProject = JavaCore.create(testProject);
			IPackageFragmentRoot packRoot = javaProject.getPackageFragmentRoot(srcFolder);
			packRoot.open(null);
			IPackageFragment pack = packRoot.getPackageFragment(packagePath);
			String testCode = FileUtility.readToString(new FileInputStream(outputCodeFileName));
			String projectFileOutput = testClassName + "." + Constants.Extensions.JAVA;
			ICompilationUnit classFile = pack.createCompilationUnit(projectFileOutput, testCode, true, null);	
			codeGenerator.writeInterstitalHandlers(pack, emitter, outputCode, testClassPath);
			codeGenerator.writeMotionEvents(testProject, testClassName, motionEvents);
			codeGenerator.saveStateFiles(Constants.Dirs.EXTERNAL_STORAGE, testClassName, manifestInformation.mPackage, testProject);
			packRoot.close();
		} catch (Exception ex) {
			MessageDialog.openInformation(
					shell,
					"GenerateRobotiumTestCode",
					"There was an exception creating the test project " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}
