package com.androidApp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidUtil {
	
	/**
	 * get the android SDK levels by iterating over the android-sdk/platforms directory and extracting
	 * the android levels
	 * @return vector of integers of the available android levels.
	 */
	public static int[] getAndroidSDKLevels(String androidSDK) {
		File platformsDir = new File(androidSDK + File.separator + Constants.Dirs.PLATFORMS);
		String[] platforms = platformsDir.list();
		List<String> androidPlatforms = new ArrayList<String>();
		for (String platform : platforms) {
			if (platform.startsWith(Constants.Prefixes.ANDROID)) {
				String level = platform.substring(Constants.Prefixes.ANDROID.length() + 1);
				androidPlatforms.add(level);
			}
		}
		int[] androidLevels = new int[androidPlatforms.size()];
		for (int i = 0; i < androidPlatforms.size(); i++) {
			androidLevels[i] = Integer.parseInt(androidPlatforms.get(i));
			
		}
		return androidLevels;
	}
	
	/**
	 * get the same or next higher android SDK, or the SDK if it wasn't found.
	 * @param level
	 * @return
	 */
	public static int getBestAndroidSDKLevel(String androidSDK, int level) {
		int[] availableLevels = getAndroidSDKLevels(androidSDK);
		Arrays.sort(availableLevels);
		for (int availableLevel : availableLevels) {
			if (availableLevel >= level) {
				return availableLevel;
			}
		}
		return level;
	}

}
