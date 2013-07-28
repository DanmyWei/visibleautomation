package com.androidApp.emitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;
/*
 * utilities to set up a robotium project in eclipse
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class SetupRobotiumProject {
	/**
	 * retains parsed options
	 * @author Matthew
	 *
	 */
	public class Options {
		public boolean 	mfWriteFunctions = false;			// write output as functions broken up by activity.
		public int		mMinLines = 5;						// min # of lines in a function		
	}
	
	/**
	 * retains directories
	 * create the directories required by the test project
	 * src - source directory
	 * src/path/to/test/java/package - directory that the java file is actually written into
	 * res - resources directory
	 * res/drawable - directory for icons and stuff
	 * res/values - directory for strings and stuff
	 * libs - directory for libraries (specifically the robotium jar)
	 * assets - directory for motion event files
	 * savestate - directory to retain files, shared preferences and databases.
	 * @param dirname name of the output test class (also the name of the project)
	 * @param testClassFilePath full path directory to output test class files
	 */
	public class Dirs {
		public File mSrcDir = null;							// src
		public File mLibDir = null;							// lib
		public File mResDir = null;							// res
		public File mAssetsDir = null;						// assets
		public File mDrawableDir = null;					// res/drawable	
		public File mSaveStateDir = null;					// savestate
		public File mPackageDir = null;						// src/path/to/testclass
		public File mHandlerDir = null;						// src/path/to/testclss/handlers
		
		public Dirs(String dirname, String testClassFilePath) {
			mSrcDir = new File(dirname + File.separator + Constants.Dirs.SRC);
			mPackageDir = new File(testClassFilePath);
			mHandlerDir = new File(mPackageDir + File.separator + Constants.Dirs.HANDLERS);
			mLibDir = new File(dirname + File.separator + Constants.Dirs.LIBS);
			mResDir = new File(dirname + File.separator + Constants.Dirs.RES);
			mDrawableDir = new File(mResDir + File.separator + Constants.Dirs.DRAWABLE);
			mAssetsDir = new File(dirname + File.separator + Constants.Dirs.ASSETS);
			mSaveStateDir = new File(dirname + File.separator + Constants.Dirs.SAVESTATE);
		}
		
		public boolean createDirectories() {
			boolean fOK = mPackageDir.mkdirs();
			fOK &= mHandlerDir.mkdirs();
			fOK &= mLibDir.mkdirs();
			fOK &= mResDir.mkdirs();
			fOK &= mDrawableDir.mkdirs();
			fOK &= mAssetsDir.mkdirs();
			fOK &= mSaveStateDir.mkdirs();
			return fOK;
		}
	}
	
	/**
	 * process the options into flags and values.
	 * @param args
	 */
	public Options processOptions(String[] args) {
		Options options = new Options();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--splitFunctions")) {
				options.mfWriteFunctions = true;
			} else if (args.equals("--minLines")) {
				options.mMinLines = Integer.parseInt(args[i + 1]);
			}
		}
		return options;
	}

	
	/**
	 * copy the output file to the package, since we don't know the package name until we've read the first activity
	 * @param packageDir output package directory src/com/foo/barTest
	 * @param outputCodeFileName argv[1]
	 * @param packagePath class name of the test class
	 * @throws EmitterException if the file can't be moved
	 */
	public static void moveOutputCodeToPackage(String packageDir, String outputCodeFileName, String testClassName) throws EmitterException {
		File sourceFile = new File(outputCodeFileName);
		String destinationPath = packageDir + File.separator + testClassName + "." + Constants.Extensions.JAVA;
		if (!sourceFile.renameTo(new File(destinationPath))) {
			throw new EmitterException("failed to rename " + sourceFile.getPath() + " to " + destinationPath);
		}
	}
	
	
	/**
	 * generate the .classpath file for building the project.  We  add the target project name
	 * for eclipse/ant, and the robotium jar in the libs directory.
	 * @param dirname directory that the .classpath file is written into
	 * @param projectName name of the target project
	 * @param name of the robotium-solo-X.XX.jar
	 * @throws IOException if the file can't be written
	 */
	protected static void writeClasspath(String dirname, String projectName, String robotiumJar) throws IOException {
		String classpath = createClasspath(projectName, robotiumJar);
		FileUtility.writeString(dirname + File.separator + Constants.Filenames.CLASSPATH, classpath);
	}
	/**
	 * generate the .classpath file for building the binary-only target project.  
	 * for eclipse/ant, and the robotium jar in the libs directory.
	 * @param dirname directory that the .classpath file is written into
	 * @param projectName name of the target project
	 * @param name of the robotium-solo-X.XX.jar
	 * @throws IOException if the file can't be written
	 */
	protected static void writeClasspathBinary(String dirname, String robotiumJar) throws IOException {
		String classpath = createClasspathBinary(robotiumJar);
		FileUtility.writeString(dirname + File.separator + Constants.Filenames.CLASSPATH, classpath);
	}
	
	/**
	 * create the classpath file with references to the target project and robotium jar file
	 * @param projectName target project
	 * @param robotiumJar robotium jar from templates folder
	 * @return
	 * @throws IOException
	 */
	public static String createClasspath(String projectName, String robotiumJar) throws IOException {
		String classpath = FileUtility.readTemplate(Constants.Templates.CLASSPATH);
		classpath = classpath.replace(Constants.VariableNames.TARGET_PROJECT, projectName);
		classpath = classpath.replace(Constants.VariableNames.ROBOTIUM_JAR, robotiumJar);
		return classpath;
	}
	
	/**
	 * binary-only projects: create the classpath file with references  robotium jar file
	 * @param robotiumJar robotium jar from templates folder
	 * @return
	 * @throws IOException
	 */
	public static String createClasspathBinary(String robotiumJar) throws IOException {
		String classpath = FileUtility.readTemplate(Constants.Templates.BINARY_CLASSPATH);
		classpath = classpath.replace(Constants.VariableNames.ROBOTIUM_JAR, robotiumJar);
		return classpath;
	}
	
	/** 
	 * write various resources used by the test application.  We write the icon, but we probably don't have to
	 * @param dirname test project directory
	 * @return true if the resources were written correctly.
	 * @throws IOException
	 */
	public static void writeResources(Dirs dirs, String dirname) throws IOException {
		FileUtility.writeResource(Constants.Filenames.LAUNCHER_PNG, dirs.mDrawableDir.getAbsolutePath());
	}
	
	/**
	 * write the AndroidManifest.xml
	 * @param dirname name of the top-level directory to write the manifest into
	 * @param testClassName name of the test class.
	 * @param testClassPath fully.qualified.testClass.path
	 * @param targetPackage package of test class
	 * @throws IOException
	 */
	public static void writeManifest(String dirname, String testClassName, String testClassPath, String targetPackage) throws IOException {
		String manifest = createManifest(testClassName, testClassPath, targetPackage); 
		FileUtility.writeString(dirname + File.separator + Constants.Filenames.ANDROID_MANIFEST_XML, manifest);
	}
	
	public static String createManifest(String testClassName, String testClassPath, String targetPackage) throws IOException {
		String manifest = FileUtility.readTemplate(Constants.Templates.ANDROID_MANIFEST_XML); 
		manifest = manifest.replace(Constants.VariableNames.CLASSPATH, testClassPath);
		manifest = manifest.replace(Constants.VariableNames.TARGETPACKAGE, targetPackage);
		manifest = manifest.replace(Constants.VariableNames.CLASSNAME, testClassName);
		return manifest;
	}
	
	/**
	 * replace the variables in the build.xml template and write out the file
	 * @param dirname target top-level directory
	 * @param targetClassPath full class name of application class being tested.
	 * @throws IOException
	 */
	public static void writeBuildXML(String dirname, String targetClassPath) throws IOException {
		String buildXML = createBuildXML(targetClassPath);
		FileUtility.writeString(dirname + File.separator + Constants.Filenames.BUILD_XML, buildXML);
	}
	
	public static String createBuildXML(String targetClassPath) throws IOException {
		String buildXML = FileUtility.readTemplate(Constants.Templates.BUILD_XML);
		String className = StringUtils.getNameFromClassPath(targetClassPath);
		buildXML = buildXML.replace(Constants.VariableNames.CLASSNAME, className);
		return buildXML;
	}
	
	/**
	 * write out the project.properties file
	 * @param dirname project directory
	 * @throws IOException
	 */
	public static void copyBuildFiles(String dirname, String target) throws IOException {
		String projectProperties = createProjectProperties(target);
		FileUtility.writeString(dirname + File.separator + Constants.Filenames.PROJECT_PROPERTIES_FILENAME, projectProperties);
	}
	
	public static String createProjectProperties(String target) throws IOException {
		String projectProperties = FileUtility.readTemplate(Constants.Templates.PROJECT_PROPERTIES);
		projectProperties = projectProperties.replace(Constants.VariableNames.TARGET, target);
		return projectProperties;
	}
	
	/**
	 * write out the AllTests.java to the output class directory src\foo\bar\path
	 * @packagePathName file/path/to/class/output/directory
	 * @param applicationClassPath fully.qualified.path.to.application.under.test
	 * @throws IOException if the template can't be found
	 */
	public static void copyTestDriverFile(String packagePathName, String applicationClassPath) throws IOException {
		String allTests = FileUtility.readTemplate(Constants.Templates.ALL_TESTS_CREATETEST);
		allTests = allTests.replace(Constants.VariableNames.CLASSPACKAGE, applicationClassPath);
		FileUtility.writeString(packagePathName + File.separator + Constants.Filenames.ALL_TESTS, allTests);
		
	}
	
	/**
	 * copy robotium jar and support library to the output directory
	 * @param libraryDir libs directory
	 * @throws IOException if the template can't be found
	 */
	public static void copyLibraries(File libraryDir) throws IOException {
		byte[] jarData = FileUtility.readBinaryTemplate(Constants.Filenames.UTILITY_JAR);
		FileOutputStream fos = new FileOutputStream(libraryDir + File.separator + Constants.Filenames.UTILITY_JAR);
		fos.write(jarData, 0, jarData.length);
		fos.close();		
		jarData = FileUtility.readBinaryTemplate(Constants.Filenames.ROBOTIUM_JAR);
		fos = new FileOutputStream(libraryDir + File.separator + Constants.Filenames.ROBOTIUM_JAR);
		fos.write(jarData, 0, jarData.length);
		fos.close();		
	}
	
	/**
	 * pull the events file from /sdcard/events.txt if eventsFileName is "device"
	 * @param eventsFileName
	 * @return "events.txt"
	 * @throws EmitterException if the adb command failed to pull the events file from the device.
	 */
	public static String getEventsFile(String eventsFileName) throws EmitterException {
		// if he specified device, use adb to pull the events file off the device.
		if (eventsFileName.equals(Constants.Names.DEVICE)) {
			String androidSDK = System.getenv(Constants.Env.ANDROID_HOME);
			String adbCmd = "pull /sdcard/events.txt";				// TODO: constant
			if (!Exec.executeAdbCommand(androidSDK, adbCmd)) {
				throw new EmitterException("failed to execute adb command " + adbCmd);
			}
		}
		return Constants.Filenames.EVENTS;
	}
	/**
	 * pull the viewDirectives file 
	 * @param eventsFileName
	 * @return "events.txt"
	 * @throws EmitterException if the adb command failed to pull the events file from the device.
	 */
	public static String getViewDirectivesFile(String viewDirectivesFileName) throws EmitterException {
		// if he specified device, use adb to pull the events file off the device.
		if (viewDirectivesFileName.equals(Constants.Names.DEVICE)) {
			String androidSDK = System.getenv(Constants.Env.ANDROID_HOME);
			String adbCmd = "pull /sdcard/view_directives.txt";			// TODO: constant
			if (!Exec.executeAdbCommand(androidSDK, adbCmd)) {
				return null;
			}
		}
		return Constants.Filenames.VIEW_DIRECTIVES;
	}
	
	/**
	 * write the motion events to files under the test class name. We need to use subdirectories to differentiate
	 * between files on each run
	 * @param assetDirName asset directory
	 * @param testClassName
	 * @param motionEvents
	 * @throws IOException if it can't write to files, EmitterException if it failed to create the motion events directory.
	 */
	public static void writeMotionEvents(String assetDirName, String testClassName, List<MotionEventList> motionEvents) throws IOException, EmitterException {
		if (!motionEvents.isEmpty()) {
			File motionPointDir = new File(assetDirName + File.separator + testClassName);
			if (!motionPointDir.mkdirs()) {
				throw new EmitterException("failed to create motion events directory " + motionPointDir.getAbsolutePath());
			}
		
			for (MotionEventList eventList : motionEvents) {
				String path = assetDirName + File.separator + testClassName + File.separator + eventList.getName() + "." + Constants.Extensions.TEXT;
				OutputStream os = new FileOutputStream(path);
				eventList.write(os);
				os.close();
			}
		}
	}
	
	/**
	 * return the list of handler names by stripping the .java extension.
	 * @param handlerDir directory containing handler .java files
	 * @return list of handler names.
	 */
	public static List<String> getHandlerNames(File handlerDir) {
		String[] files = handlerDir.list();
		List<String> functionNames = new ArrayList<String>();
		for (String file : files) {
			if (file.endsWith(Constants.Extensions.JAVA)) {
				String functionName = file.substring(0, file.length() - 5);
				functionNames.add(functionName);
			}
		}
		return functionNames;
	}
	
	/**
	 * return the list of activities that the handlers are registered for by stripping the .java extension
	 * and the InterstitialActivityHandler prefix.
	 * @param handlerDir
	 * @return
	 */
	public static List<String> getHandlerActivityNames(File handlerDir) {
		String[] files = handlerDir.list();
		List<String> functionNames = new ArrayList<String>();
		for (String file : files) {
			if (file.endsWith(Constants.Extensions.JAVA) && file.startsWith(Constants.INTERSTITIAL_ACTIVITY_HANDLER)) {
				int prefixLength = Constants.INTERSTITIAL_ACTIVITY_HANDLER.length();
				String activityName = file.substring(0, file.length() - 5).substring(prefixLength);
				functionNames.add(activityName);
			}
		}
		return functionNames;
	}
}
