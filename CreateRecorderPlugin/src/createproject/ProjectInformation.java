package createproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.androidApp.codedefinition.CodeDefinition;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.emitter.MotionEventList;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;

import createrecorder.util.AAPTBadgingValues;
import createrecorder.util.EclipseExec;
import createrecorder.util.EclipseUtility;
import createrecorder.util.ManifestInformation;
import createrecorder.util.RecorderConstants;


/**
 * merge the information from aapt dump --values badging and aapt dump --values xmltree AndroidManifest.xml
 * @author matt2
 *
 */
public class ProjectInformation {
	public String 			mLaunchableActivity;
	public int	  			mSDKVersion;
	public String 			mPackageName;
	public String 			mApplicationName;
	public String 			mProjectName;					// name of the source project (may be null)
	protected String 		mAndroidSDK;
	protected String 		mNewProjectName;
	protected String 		mTestClassPath;
	protected String 		mTestClassName;
	protected String 		mEventsFileName;
	protected String 		mTestPackagePath;
	protected int			mTargetSDKVersion;
	protected boolean		mfNewProject;
	protected List<String> 	mSupportLibraries;
	protected IProject 		mTestProject;
	
	
	public ProjectInformation() {
		mProjectName = null;
	}
	
	public ProjectInformation(String projectName) {
		mProjectName = projectName;
	}
	/**
	 * generate consistent project information for a binary application, from running aapt badging and
	 * aapt xmltree AndroidManifest.xml
	 * @param shell eclipse shell
	 * @param aaptBadgingValues results of aapt badging
	 * @param manifestInformation results of aapt dump xmlvales AndroidManifest.xml
	 * @return
	 */
	public boolean init(Shell shell, AAPTBadgingValues aaptBadgingValues, ManifestInformation manifestInformation) {
		// launching activity;
		if (aaptBadgingValues.getLaunchableActivity() != null) {
			mLaunchableActivity = aaptBadgingValues.getLaunchableActivity();
		} else if (manifestInformation.getStartActivityName() != null) {
			mLaunchableActivity = manifestInformation.getStartActivityName();
		} else {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
					  					  "Unable to extract the launch activity from the manifest or APK badging information");
			return false;
		}
		// SDK version (default to version 10 (2.3)
		if (aaptBadgingValues.getSDKVersion() != 0) {
			mSDKVersion = aaptBadgingValues.getSDKVersion();
		} else if (manifestInformation.getSDKVersion() != 0) {
			mSDKVersion = manifestInformation.getSDKVersion();
		} 
		if (mSDKVersion < Constants.AndroidVersions.GINGERBREAD_MR1) {
			mSDKVersion = Constants.AndroidVersions.GINGERBREAD_MR1;
		}
		// application name
		if (aaptBadgingValues.getApplicationLabel() != null) {
			mApplicationName = aaptBadgingValues.getApplicationLabel();
		} else if (manifestInformation.getApplicationName() != null) {
			mApplicationName = manifestInformation.getApplicationName();
		} else {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
					  "Unable to extract the application name from the manifest or APK badging information");
			return false;
		}
		// application package
		if (aaptBadgingValues.getPackage() != null) {
			mPackageName = aaptBadgingValues.getPackage();
		} else if (manifestInformation.getPackageName() != null) {
			mPackageName = manifestInformation.getPackageName();
		} else {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
					  "Unable to extract the package name from the manifest or APK badging information");
			return false;
		}
		return true;
	}
	
	/**
	 *  project information from source files AndroidManifest.xml, project.properties
	 * @param shell
	 * @param projectPropertiesScan
	 * @param manifestParser
	 * @return
	 */
	public boolean init(Shell shell, ManifestParser manifestParser, ProjectPropertiesScan projectPropertiesScan) {
		manifestParser.getStartActivity();
		// launching activity;
		if (manifestParser.getStartActivity() != null) {
			mLaunchableActivity = manifestParser.getStartActivity();
		} else {
			MessageDialog.openInformation(shell, RecorderConstants.VISIBLE_AUTOMATION,
					  					  "Unable to extract the launch activity from the manifest or APK badging information");
			return false;
		}
		if (projectPropertiesScan.getTargetSDK() != 0) {
			mSDKVersion = projectPropertiesScan.getTargetSDK();
		} else if (manifestParser.getMinSDKVersion() != 0) {
			mSDKVersion = manifestParser.getMinSDKVersion();
		} else {
			mSDKVersion = Constants.AndroidVersions.GINGERBREAD_MR1;
		}
		if (mSDKVersion < Constants.AndroidVersions.GINGERBREAD_MR1) {
			mSDKVersion = Constants.AndroidVersions.GINGERBREAD_MR1;
		}
		mApplicationName = manifestParser.getApplication();
		mPackageName = manifestParser.getPackage();
		return true;
	}
	
	// accessors
	public String getStartActivity() {
		return mLaunchableActivity;
	}
	
	public String getStartActivityName() {
		int ichDot = mLaunchableActivity.lastIndexOf(".");
		if (ichDot != -1) {
			return mLaunchableActivity.substring(ichDot + 1);
		} else {
			return mLaunchableActivity;
		}
	}
	
	public int getSDKVersion() {
		return mSDKVersion;
	}
	
	public int getMinSDKVersion() {
		return mSDKVersion;
	}
	
	public String getPackageName() {
		return mPackageName;
	}
	
	public String getApplicationName() {
		return mApplicationName;
	}
	
	public String getSourceProjectName() {
		return mProjectName;
	}
	
	
	public boolean isNewProject() {
		return mfNewProject;
	}
	
	public IProject getTestProject() {
		return mTestProject;
	}
	
	public int getTargetSDK() {
		return mTargetSDKVersion;
	}
	
	public String getAndroidSDK() {
		return mAndroidSDK;
	}
	
	public String getTestProjectName() {
		return mNewProjectName;
	}

	public String getTestClassName() {
		return mTestClassName;
	}
	
	public String getTestClassPath() {
		return mTestClassPath;
	}
	
	public String getEventsFileName() {
		return mEventsFileName;
	}
	
	public String getTestPackagePath() {
		return mTestPackagePath;
	}
	
	public List<String> getSupportLibraries() {
		return mSupportLibraries;
	}
	
	public void getProjectInformation(String apkFileName, String projectExtension) throws CoreException, IOException {
		// get the android SDK directory so we can execute adb
		IPreferencesService service = Platform.getPreferencesService();
		String mAndroidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		String projectName = this.getApplicationName();

		// create the new project: NOTE: should we check for existence?
		mNewProjectName = projectName + projectExtension;
		mTestProject = EclipseUtility.createBaseProject(mNewProjectName);
		
		// binary code generator
		EmitRobotiumCodeBinary emitter = new EmitRobotiumCodeBinary();
		mEventsFileName = GenerateRobotiumTestCode.getEventsFile(mAndroidSDK, Constants.Names.DEVICE);
		
		// test class path and test class name generated by emitter.
		// TODO: change this to get the information from the projectInformation, not the emitter.
		// am instrumentation requires the .test extension.
		mTestPackagePath = this.getPackageName() + ".test";
		mTestClassPath = mTestPackagePath + projectExtension;
		mTestClassName = this.getStartActivityName() + projectExtension;
		
		
		// scan to see if there are any unit tests in the source folder. If so, then we create a unique index.
		IFolder srcFolder = mTestProject.getFolder(Constants.Dirs.SRC);
		mTargetSDKVersion = EclipseUtility.getBestAndroidSDKLevel(this.getSDKVersion());
		if (srcFolder.exists()) {
			IFolder projectFolder = srcFolder.getFolder(FileUtility.sourceDirectoryFromClassName(mTestPackagePath));
			String templateFileName = mTestClassName + "." + Constants.Extensions.JAVA;
			int uniqueFileIndex = EclipseUtility.uniqueFileIndex(projectFolder, templateFileName);
			if (uniqueFileIndex != 0) {
				mTestClassPath += Integer.toString(uniqueFileIndex);
				mTestClassName += Integer.toString(uniqueFileIndex);
				
			}
			mfNewProject = false;
		}  else {
			mfNewProject = true;
		}
		mSupportLibraries = EclipseUtility.getSupportLibraries(apkFileName);
	} 
}
