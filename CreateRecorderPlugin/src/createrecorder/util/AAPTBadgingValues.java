package createrecorder.util;

import com.androidApp.util.StringUtils;

/**
 * parse the output from aapt dump --values badging com.DanielBach.FrequenSee-1.apk 
 * package: name='com.DanielBach.FrequenSee' versionCode='11' versionName='1.2.3'
 * sdkVersion:'9'
 * uses-gl-es:'0x20000'
 * uses-permission:'android.permission.RECORD_AUDIO'
 * uses-permission:'android.permission.INTERNET'
 * uses-permission:'android.permission.ACCESS_NETWORK_STATE'
 * application-label:'FrequenSee'
 * application-icon-120:'res/drawable-ldpi/icon.png'
 * application-icon-160:'res/drawable-mdpi/icon.png'
 * application-icon-240:'res/drawable-hdpi/icon.png'
 * application: label='FrequenSee' icon='res/drawable-mdpi/icon.png'
 * launchable-activity: name='com.DanielBach.FrequenSee.FrequenSeeActivity'  label='FrequenSee' icon=''
 * uses-feature:'android.hardware.microphone'
 * uses-implied-feature:'android.hardware.microphone','requested android.permission.RECORD_AUDIO permission'
 * uses-feature:'android.hardware.touchscreen'
 * uses-implied-feature:'android.hardware.touchscreen','assumed you require a touch screen unless explicitly made optional'
 * uses-feature:'android.hardware.screen.landscape'
 * uses-implied-feature:'android.hardware.screen.landscape','one or more activities have specified a landscape orientation'
 * main
 * other-activities
 * supports-screens: 'small' 'normal' 'large' 'xlarge'
 * supports-any-density: 'true'
 * locales: '--_--'
 * densities: '120' '160' '240'
 * native-code: 'armeabi' 'armeabi-v7a'
 * @author matt2
 *
 */
public class AAPTBadgingValues {
	protected String PACKAGE = "package";
	protected String SDK_VERSION = "sdkVersion";
	protected String APPLICATION_LABEL = "application-label";
	protected String LAUNCHABLE_ACTIVITY = "launchable-activity";
	protected int mSDKVersion = 8;
	protected String mPackage;
	protected String mApplicationLabel;
	protected String mLaunchableActivity;
	
	public AAPTBadgingValues(String[] aaptOutput) {
		if (aaptOutput != null) {
			for (String line : aaptOutput) {
				String[] values = line.split(":");
				if (values[0].equals(PACKAGE)) {
					String pattern="name='[A-Za-z0-9\\.]*'";
					String packageName = StringUtils.extractMatch(values[1], pattern);
					mPackage = StringUtils.stripFrontBack(packageName, "name='", "'");		
				} else if (values[0].equals(SDK_VERSION)) {
					mSDKVersion = Integer.parseInt(StringUtils.stripFrontBack(values[1], "'", "'"));
				} else if (values[0].equals(APPLICATION_LABEL)) {
					mApplicationLabel = StringUtils.stripFrontBack(values[1], "'", "'");
				} else if (values[0].equals(LAUNCHABLE_ACTIVITY)) {
					String pattern="name='[A-Za-z0-9\\.\\$]*'";
					String launchableActivity = StringUtils.extractMatch(values[1], pattern);
					mLaunchableActivity = StringUtils.stripFrontBack(launchableActivity, "name='", "'");		
				}
			} 
		}
	}
	
	public int getSDKVersion() {
		return mSDKVersion;
	}
	
	public String getPackage() {
		return mPackage;
	}
	
	public String getApplicationLabel() {
		return mApplicationLabel;
	}
	
	public String getLaunchableActivity() {
		return mLaunchableActivity;
	}
}
