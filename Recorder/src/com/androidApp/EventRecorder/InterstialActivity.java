package com.androidApp.EventRecorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * read the list of interstitial activities.
 * @author matt2
 *
 */
public class InterstialActivity {
	public static List<Class<? extends Activity>> readActivityClassList(InputStream is) throws IOException, ClassNotFoundException {
		List<Class<? extends Activity>> activityClassList = new ArrayList<Class<? extends Activity>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = br.readLine()) != null) {
			Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(line);
			activityClassList.add(activityClass);			
		}
		return activityClassList;
	}
}
