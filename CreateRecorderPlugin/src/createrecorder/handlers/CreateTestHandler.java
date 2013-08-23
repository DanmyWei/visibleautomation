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
import createproject.ProjectInformation;
import createrecorder.util.AAPTBadgingValues;
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
			File aapt = EclipseUtility.findAAPT(androidSDK);
			if (aapt == null) {
				MessageDialog.openInformation(shell, "CreateTestHandler", "could not find aapt in " + androidSDK);
			} else {
				// use aapt to pull the manifest and the badging information, so we can do the best we can to get the target SDK,
				// application name, start activity, and application package.
				String aaptPath = aapt.getAbsolutePath();
				String[] manifestLines = Exec.getShellCommandOutput(aaptPath + " dump --values xmltree " + PackageUtils.getPackageName(packagePath) + " AndroidManifest.xml");
				ManifestInformation manifestInformation = new ManifestInformation(manifestLines);
				String[] aaptBadgingLines = Exec.getShellCommandOutput(aaptPath + " dump --values badging " +  PackageUtils.getPackageName(packagePath));
				AAPTBadgingValues aaptBadgingValues = new AAPTBadgingValues(aaptBadgingLines);
				ProjectInformation projectInformation = new ProjectInformation();
				if (projectInformation.init(shell, aaptBadgingValues, manifestInformation)) {
					// and create the project from the badging information
					createProject(shell, projectInformation);
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
	public void createProject(Shell shell, ProjectInformation projectInformation) {
		String eventsFileName = Constants.Names.DEVICE;
		try {
			// get the android SDK directory so we can execute adb
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			String projectName = projectInformation.getApplicationName();
			
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
			// TODO: change this to get the information from the projectInformation, not the emitter.
			String testClassPath = projectInformation.getStartActivity() + Constants.Extensions.TEST;
			String testClassName = projectInformation.getStartActivityName() + Constants.Extensions.TEST;
			String packagePath = projectInformation.getPackageName() +  Constants.Extensions.TEST;
			
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
				String androidTarget = "target=android-" + Integer.toString(projectInformation.getSDKVersion());
				codeGenerator.createProject(testProject, emitter, androidTarget, newProjectName, packagePath, testClassPath, testClassName);
			}
			codeGenerator.writeTheCode(emitter, outputCode, motionEvents, testProject, packagePath, projectInformation.getPackageName(),
									   testClassPath, testClassName);
			writeHandlers(testProject, outputCode);
			// copy the view_directives.txt file back to the recorder
			Exec.executeShellCommand("rm /sdcard/events.txt");
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
					RecorderConstants.VISIBLE_AUTOMATION,
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
