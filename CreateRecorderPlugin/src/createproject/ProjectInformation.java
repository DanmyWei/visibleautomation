package createproject;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import createrecorder.util.AAPTBadgingValues;
import createrecorder.util.ManifestInformation;
import createrecorder.util.RecorderConstants;


/**
 * merge the information from aapt dump --values badging and aapt dump --values xmltree AndroidManifest.xml
 * @author matt2
 *
 */
public class ProjectInformation {
	public String mLaunchableActivity;
	public int	  mSDKVersion;
	public String mPackageName;
	public String mApplicationName;
	
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
		// SDK version
		if (aaptBadgingValues.getSDKVersion() != 0) {
			mSDKVersion = aaptBadgingValues.getSDKVersion();
		} else if (manifestInformation.getSDKVersion() != 0) {
			mSDKVersion = manifestInformation.getSDKVersion();
		} else {
			mSDKVersion = 8;
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
	
	public String getPackageName() {
		return mPackageName;
	}
	
	public String getApplicationName() {
		return mApplicationName;
	}

}
