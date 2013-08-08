package createrecorder.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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

import com.androidApp.emitter.CodeDefinition;
import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.emitter.EmitterException;
import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.MotionEventList;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

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
			Hashtable<CodeDefinition, List<LineAndTokens>> outputCode = new Hashtable<CodeDefinition, List<LineAndTokens>>();
			readHandlers(testProject, outputCode);
			emitter.generateTestCode(eventsFileName, outputCode, motionEvents);
			List<LineAndTokens> mainCode = outputCode.get(new CodeDefinition(Constants.MAIN, null));
			
			// test class path and test class name generated by emitter.
			String testClassPath = emitter.getApplicationClassPath() + Constants.Extensions.TEST;
			String testClassName = emitter.getApplicationClassName() + Constants.Extensions.TEST;
			String packagePath = manifestInformation.mPackage +  Constants.Extensions.TEST;
			
			// scan to see if there are any unit tests in the source folder. If so, then we create a unique index.
			IFolder srcFolder = testProject.getFolder(Constants.Dirs.SRC);
			if (srcFolder.exists()) {
				IFolder projectFolder = srcFolder.getFolder(FileUtility.sourceDirectoryFromClassName(packagePath));
				String templateFileName = testClassName + "." + Constants.Extensions.JAVA;
				int uniqueFileIndex = EclipseUtility.uniqueFileIndex(projectFolder, templateFileName);
				if (uniqueFileIndex != 0) {
					testClassPath += Integer.toString(uniqueFileIndex);
					testClassName += Integer.toString(uniqueFileIndex);
				}
			} else {
				// create the project if the source folder doesn't exist.
				String androidTarget = "target=android-" + Integer.toString(manifestInformation.mTargetSDKVersion);
				codeGenerator.createProject(testProject, emitter, androidTarget, newProjectName, packagePath, testClassPath, testClassName);
			}
			codeGenerator.writeTheCode(emitter, outputCode, motionEvents, testProject, packagePath, 
									   manifestInformation.mPackage, testClassPath, testClassName);
			writeHandlers(testProject, outputCode);
			// copy the view_directives.txt file back to the recorder
			EclipseExec.executeAdbCommand("pull /sdcard/" + Constants.Filenames.VIEW_DIRECTIVES);
			String recorderProjectName = projectName + RecorderConstants.RECORDER_SUFFIX;
			IProject recorderProject = ResourcesPlugin.getWorkspace().getRoot().getProject(recorderProjectName);
			try {
				EclipseUtility.writeFile(recorderProject.getFolder(Constants.Dirs.ASSETS), Constants.Filenames.VIEW_DIRECTIVES, Constants.Filenames.VIEW_DIRECTIVES);
			} catch (Exception ex) {
				// no directives file to copy, but we should at least write something to the console
			}
		} catch (Exception ex) {
			MessageDialog.openInformation(
					shell,
					"GenerateRobotiumTestCode",
					"There was an exception creating the test project " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * read the conditional code from the handlers directory
	 * @param testProject
	 * @param outputCode
	 * @throws CoreException
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static void readHandlers(IProject 										testProject,
							 		Hashtable<CodeDefinition, List<LineAndTokens>> 	outputCode) throws CoreException, IOException, EmitterException {
		IFolder handlersFolder = testProject.getFolder(Constants.Dirs.HANDLERS);
		if (handlersFolder.exists()) {
			IResource[] handlerFiles = handlersFolder.members();
			for (IResource handler : handlerFiles) {
				if (handler.getType() == IResource.FILE) {
					IFile handlerFile = (IFile) handler;
					InputStream is = handlerFile.getContents();
					EmitRobotiumCode.readConditionalCode(is, outputCode);
				}
			}
		}
	}
	
	/**
	 * write the conditional handlers for dialogs and activities.
	 * @param testProject
	 * @param outputCode
	 * @throws CoreException
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static void writeHandlers(IProject 										testProject,
	 								 Hashtable<CodeDefinition, List<LineAndTokens>> outputCode) throws CoreException, IOException, EmitterException {
		IFolder handlersFolder = testProject.getFolder(Constants.Dirs.HANDLERS);
		for (Entry<CodeDefinition, List<LineAndTokens>> entry : outputCode.entrySet()) {
			CodeDefinition codeDef = entry.getKey();
			List<LineAndTokens> code = entry.getValue();
			if (!codeDef.getActivityName().equals(Constants.MAIN) && !codeDef.getActivityName().equals(Constants.FUNCTIONS)) {
				String templateFileName = StringUtils.getNameFromClassPath(codeDef.getActivityName());
				int uniqueFileIndex = EclipseUtility.uniqueFileIndex(handlersFolder, templateFileName + "." + Constants.Extensions.TEXT);
				if (uniqueFileIndex != 0) {
					templateFileName += Integer.toString(uniqueFileIndex);
				}
				templateFileName += "." + Constants.Extensions.TEXT;
				FileOutputStream fos = new FileOutputStream("handler");
				EmitRobotiumCode.writeConditionalCode(fos, codeDef, code);
				fos.close();
				FileInputStream fis = new FileInputStream("handler");
				IFile file = handlersFolder.getFile(templateFileName);
				file.delete(false, null);
				file.create(fis, IFile.FORCE, null);	
				fis.close();

			}
		}
}
}
