package createproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;

import com.androidApp.codedefinition.CodeDefinition;
import com.androidApp.emitter.EmitRobotiumCodeBinary;
import com.androidApp.emitter.EmitRobotiumCodeSource;
import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.SetupRobotiumProject;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;

import createrecorder.util.EclipseUtility;
import createrecorder.util.EclipseExec;
import createrecorder.util.RecorderConstants;

/**
 * extract the events file from the device, and either create a new project, or add a test class to an
 * existing junit project which plays back the recording
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class GenerateRobotiumTestCodeBinary extends GenerateRobotiumTestCode {
	
	/**
	 * generate the .classpath file for building the project.  Just use the robotum jar, since we
	 * pick up the binary application name
	 * for eclipse/ant, and the robotium jar in the libs directory.
	 * @param projectName name of the target project
	 * @param name of the robotium-solo-X.XX.jar
	 * @throws IOException if the file can't be written
	 */
	@Override
	public void writeClasspath(IProject 	project, 
							   String 		projectName, 
							   String 		robotiumJar,
							   int			targetSDK,
							   List<String> supportLibraries) throws IOException, CoreException {
		String classpath = SetupRobotiumProject.createClasspathBinary(robotiumJar, targetSDK, supportLibraries);
		EclipseUtility.writeString(project, Constants.Filenames.CLASSPATH, classpath);
	}
}
