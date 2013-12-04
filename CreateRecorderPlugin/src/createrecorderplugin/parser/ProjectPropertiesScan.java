package createrecorderplugin.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/** 
 * scan the source project.properties file, so we can get the android target
 * TODO: get this from the manifest instead.
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ProjectPropertiesScan {
	protected final String TARGET = "target";
	protected String mTarget;
	
	public ProjectPropertiesScan(File projectProperties) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(projectProperties));
		while (true) {
			String s = br.readLine();
			if (s == null) {
				 break;
			}
			if (s.startsWith(TARGET)) {
				mTarget = s;
				break;
			}
		}
	}
	
	public String getTarget() {
		return mTarget;
	}
	
	// the project properties scan SDK target is returned as target=android-18, so we need to extract it and
	// parse it into an integer.
	
	public int getTargetSDK() {
		int ichDash = mTarget.lastIndexOf('-');
		String SDKString = mTarget.substring(ichDash + 1);
		return Integer.parseInt(SDKString);
	}
}
