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
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
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
}
