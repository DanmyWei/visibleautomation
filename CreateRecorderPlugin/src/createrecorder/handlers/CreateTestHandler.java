package createrecorder.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
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

import com.androidApp.codedefinition.CodeDefinition;
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

import createproject.GenerateRobotiumTestCode;
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
 * eclipse plugin handler to create the test project for binary APKs
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
		String packagePath = testClassDialog.getTestClassDialog(shell, RecorderConstants.VISIBLE_AUTOMATION, "Enter classpath of APK to test");
		if (packagePath != null) {
			String apkFileName = PackageUtils.getPackageName(testClassDialog.mPackagePath);
			
			// pull the APK file from the device, so we can re-install it when we run on another device
		    String[] pullResults = EclipseExec.getAdbCommandOutput("pull /data/app" + apkFileName);
		    EclipseUtility.printConsole(pullResults);
		    if (StringUtils.containedInStringArray(Constants.Errors.DOES_NOT_EXIST, pullResults)) {
		    	MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "failed to pull APK from device");
		    	return false;
		    }
			
			// get the android sdk directory from the eclipse ADT preferences
			IPreferencesService service = Platform.getPreferencesService();
			String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
			File aapt = EclipseUtility.findAAPT(androidSDK);
			if (aapt == null) {
				MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "could not find aapt in " + androidSDK);
			} else {
				try {
					// use aapt to pull the manifest and the badging information, so we can do the best we can to get the target SDK,
					// application name, start activity, and application package.
					String aaptPath = aapt.getAbsolutePath();
					String[] manifestLines = Exec.getShellCommandOutput(aaptPath + " dump --values xmltree " + PackageUtils.getPackageName(packagePath) + " AndroidManifest.xml");
					ManifestInformation manifestInformation = new ManifestInformation(manifestLines);
					String[] aaptBadgingLines = Exec.getShellCommandOutput(aaptPath + " dump --values badging " +  PackageUtils.getPackageName(packagePath));
					AAPTBadgingValues aaptBadgingValues = new AAPTBadgingValues(aaptBadgingLines);
					ProjectInformation projectInformation = new ProjectInformation();
					if (projectInformation.init(shell, aaptBadgingValues, manifestInformation)) {
						projectInformation.getProjectInformation(apkFileName, Constants.Extensions.TEST);
					// and create the project from the badging information
						createProject(shell, projectInformation, apkFileName);
						if (projectInformation.isNewProject()) {
							EclipseUtility.copyFileToProjectDirectory(projectInformation.getTestProject(), apkFileName, apkFileName);
						}
					} else {
						MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "failed to initialize project information");
					}
				} catch (Exception ex) {
					MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION, "an exception as thrown creating the project");	
					ex.printStackTrace();
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
	public void createProject(Shell shell, ProjectInformation projectInformation, String apkFileName) {
		try {
			
			// binary code project setup
			GenerateRobotiumTestCodeBinary codeGenerator = new GenerateRobotiumTestCodeBinary();
			
			// binary code generator
			EmitRobotiumCodeBinary emitter = new EmitRobotiumCodeBinary();
			List<MotionEventList> motionEvents = new ArrayList<MotionEventList>();
			Hashtable<CodeDefinition, List<LineAndTokens>> outputCode = new Hashtable<CodeDefinition, List<LineAndTokens>>();
			readHandlers(projectInformation.getTestProject(), outputCode);
			emitter.generateTestCode(projectInformation.getEventsFileName(), outputCode, motionEvents);
			if (projectInformation.isNewProject()) {
				codeGenerator.createProject(emitter, RecorderConstants.MANIFEST_TEMPLATE_BINARY_TEST, projectInformation);
			}
			codeGenerator.writeTheCode(emitter, outputCode, motionEvents, projectInformation, Constants.Templates.BINARY_TEST_FUNCTION);
			writeHandlers(projectInformation.getTestProject(), outputCode);
			GenerateRobotiumTestCode.copyViewDirectives(projectInformation.getTestProjectName());
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
	 * write the conditional handlers for dialogs and activities.  This overwrites the current handlers
	 * @param testProject
	 * @param outputCode
	 * @throws CoreException
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static void writeHandlers(IProject 										testProject,
			 						 Hashtable<CodeDefinition, List<LineAndTokens>> outputCode) throws IOException, EmitterException, CoreException {
		IFolder handlersFolder = testProject.getFolder(Constants.Dirs.HANDLERS);
		// write the conditional code, by activity name and unique indexed
		for (Entry<CodeDefinition, List<LineAndTokens>> entry : outputCode.entrySet()) {
			CodeDefinition codeDefCall = entry.getKey();
			List<LineAndTokens> linesCall = entry.getValue();
			if (!codeDefCall.getActivityName().equals(Constants.MAIN) && codeDefCall.getCodeType().equals(Constants.FUNCTION_CALL)) {
				CodeDefinition codeDefDefinition = CodeDefinition.findFunctionDefinition(codeDefCall, outputCode);
				List<LineAndTokens> linesDefinition = outputCode.get(codeDefDefinition);
				String templateFileName = StringUtils.getNameFromClassPath(codeDefCall.getActivityName()) + "." + Constants.Extensions.TEXT;
				int uniqueFileIndex = EclipseUtility.uniqueFileIndex(handlersFolder, templateFileName);
				if (uniqueFileIndex != 0) {
					templateFileName += Integer.toString(uniqueFileIndex);
				}
				String handlerCode = EmitRobotiumCode.createConditionalCode(codeDefCall, linesCall, codeDefDefinition, linesDefinition);
				EclipseUtility.writeString(handlersFolder, templateFileName, handlerCode);
			}
		}
	}
}
