package com.androidApp.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * scan the source project.properties file, mostly so we can get the android target
 * @author Matthew
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
}
