package com.androidApp.emitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.emitter.EmitRobotiumCode.LineAndTokens;
import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

public class SetupRobotiumProject {
	protected static String sTargetClassPath = null;		// class path of initial activity.	
	protected static String sTargetPackage = null;			// package name that the recorder pulled from the app
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
	 */
	public class Dirs {
		public String mSrcDir = null;							// src
		public String mLibDir = null;							// lib
		public String mResDir = null;							// res
		public String mDrawableDir = null;						// res/drawable	
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
	 * create the directories required by the test project
	 * src - source directory
	 * src/path/to/test/java/package - directory that the java file is actually written into
	 * res - resources directory
	 * res/drawable - directory for icons and stuff
	 * res/values - directory for strings and stuff
	 * libs - directory for libraries (specifically the robotium jar)
	 * @param dirname name of the output test class (also the name of the project)
	 * @param testClassFilePath full path directory to output test class files
	 */
	public Dirs createDirectories(String dirname, String testClassFilePath) throws IOException {
		Dirs dirs = new Dirs();
		boolean fOK = true;
		dirs.mSrcDir = dirname + File.separator + Constants.Dirs.SRC;
		File packageDir = new File(testClassFilePath);
		fOK = packageDir.mkdirs();
		dirs.mLibDir = dirname + File.separator + Constants.Dirs.LIBS;
		File libdir = new File(dirs.mLibDir);
		fOK &= libdir.mkdirs();
		dirs.mResDir = dirname + File.separator + Constants.Dirs.RES;
		File resdir = new File(dirs.mResDir);
		fOK &= resdir.mkdir();
		dirs.mDrawableDir = resdir.getPath() + File.separator + Constants.Dirs.DRAWABLE;
		File drawabledir = new File(dirs.mDrawableDir);
		fOK &= drawabledir.mkdir();
		return dirs;
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
	
	public static String createClasspath(String projectName, String robotiumJar) throws IOException {
		String classpath = FileUtility.readTemplate(Constants.Templates.CLASSPATH);
		classpath = classpath.replace(Constants.VariableNames.TARGET_PROJECT, projectName);
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
		FileUtility.writeResource(Constants.Filenames.LAUNCHER_PNG, dirs.mDrawableDir);
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
	
	public static String createManifest(String testClassName, String testClassPath, String targetPackage)throws IOException {
		String manifest = FileUtility.readTemplate(Constants.Templates.ANDROID_MANIFEST_XML); 
		manifest = manifest.replace(Constants.VariableNames.CLASSPATH, testClassPath);
		manifest = manifest.replace(Constants.VariableNames.TARGETPACKAGE, sTargetPackage);
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
	 * copy support library to the output directory
	 * @param libraryDir libs directory
	 * @throws IOException if the template can't be found
	 */
	public static void copyLibrary(String libraryDir) throws IOException {
		byte[] jarData = FileUtility.readBinaryTemplate(Constants.Filenames.UTILITY_JAR);
		FileOutputStream fos = new FileOutputStream(libraryDir + File.separator + Constants.Filenames.UTILITY_JAR);
		fos.write(jarData, 0, jarData.length);
		fos.close();		
	}
	
	/**
	 * pull the events file from /sdcard/events.txt if eventsFileName is "device"
	 * @param eventsFileName
	 * @return
	 */
	public static String getEventsFile(String eventsFileName) {
		// if he specified device, use adb to pull the events file off the device.
		if (eventsFileName.equals(Constants.Names.DEVICE)) {
	       Process proc = null;
	       String cmd = "adb pull /sdcard/events.txt";
	       try {
	            proc = Runtime.getRuntime().exec(cmd);
	        } catch (IOException e) {
	            System.err.println("failed to execute " + cmd + " " + e.getMessage());
	            System.exit(-1);
	        }
	        try {
	            int result = proc.waitFor();
	        } catch (InterruptedException e) {
	            System.err.println("interrupted executing " + cmd + " " + e.getMessage());
	            System.exit(-1);
	        }        
		}
		return Constants.Filenames.EVENTS;
	}
}
