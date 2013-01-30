package com.androidApp.emitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;
import com.androidApp.util.SuperTokenizer;

public class EmitRobotiumCode {
	protected int mVariableIndex = 0;						// incremented for unique variable names
	protected static String sTargetClassPath = null;		// class path of initial activity.	
	protected static String	sTargetClassName = null;		// class name of initial activity
	protected static String sTestClassPath = null;			// class path of the test code
	protected static String sTestClassName = null;
	protected static String sRobotiumJar = null;			// path to the robotium jar file
	protected static String sTargetPackage = null;			// package name that the recorder pulled from the app
	private static String sSrcDir;							// src directory
	private static String sPackageDir;						// src/com/application/test dir
	private static String sLibDir;							// lib
	private static String sResDir;							// res
	private static String sDrawableDir;						// res/drawable
	/**
	 * take an input file spit out by the event recorder, and generate robotium code
	 * @param args 4 strings: input file (events.txt), output file test.java, target project name and the robotiumJar file.
	 * the caller has to specify the project name because the .apk file name can be changed, and it actually the name of the 
	 * build project, i.e. made from unobtainium
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, EmitterException {
		String eventsFileName = args[0];
		String outputCodeFileName = args[1];
		String targetProject = args[2];
		String robotiumJarPath = args[3];

		File robotiumFile = new File(robotiumJarPath);
		List<String> lines = new ArrayList<String>();
		sRobotiumJar = robotiumFile.getName();
		BufferedReader br = new BufferedReader(new FileReader(eventsFileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputCodeFileName));
		EmitRobotiumCode emitter = new EmitRobotiumCode();
		emitter.emit(br, lines);
		writeHeader(sTargetClassPath, sTargetClassName, bw);
		String testFunction = FileUtility.readTemplate(Constants.Templates.TEST_FUNCTION);
		bw.write(testFunction);
		FileUtility.writeLines(bw, lines);
		writeTrailer(bw);
		bw.close();
		br.close();
		if ((sTargetClassName != null) && (sTargetClassPath != null)) {
			sTestClassPath = sTargetClassPath + Constants.Extensions.TEST;
			sTestClassName = sTargetClassName + Constants.Extensions.TEST;
			String srcdirname = sTestClassName + File.separator + Constants.Dirs.SRC;
			String packagePath = srcdirname + File.separator + sTestClassPath;
			createDirectories(sTestClassName, packagePath);
			generateBuildXML(sTestClassName);
			copyBuildFiles(sTestClassName);
			generateManifest(sTestClassName);
			writeResources(sTestClassName);
			writeClasspath(sTestClassName, targetProject);
			FileUtility.copyFile(robotiumFile.getPath(), sTestClassName + File.separator + Constants.Dirs.LIBS + File.separator + sRobotiumJar);
			moveOutputCodeToPackage(outputCodeFileName, packagePath);

		} else {
			System.err.println("no activity class specified");
		}
	}
	
	public EmitRobotiumCode() {
	}
	
	/**
	 * create the directories required by the test project
	 * src - source directory
	 * src/path/to/test/java/package - directory that the java file is actually written into
	 * res - resources directory
	 * res/drawable - directory for icons and stuff
	 * res/values - directory for strings and stuff
	 * libs - directory for libraries (specifically the robotium jar)
	 * @param dirname
	 * @param packageName
	 */
	public static boolean createDirectories(String dirname, String packageName) throws IOException {
		boolean fOK = true;
		sSrcDir = dirname + File.separator + Constants.Dirs.SRC;
		sPackageDir = FileUtility.sourceDirectoryFromClassName(sSrcDir + File.separator + sTestClassPath);
		File packageDir = new File(sPackageDir);
		fOK = packageDir.mkdirs();
		sLibDir = dirname + File.separator + Constants.Dirs.LIBS;
		File libdir = new File(sLibDir);
		fOK &= libdir.mkdirs();
		sResDir = dirname + File.separator + Constants.Dirs.RES;
		File resdir = new File(sResDir);
		fOK &= resdir.mkdir();
		sDrawableDir = resdir.getPath() + File.separator + Constants.Dirs.DRAWABLE;
		File drawabledir = new File(sDrawableDir);
		fOK &= drawabledir.mkdir();
		return fOK;

	}
	
	/**
	 * copy the output file to the package, since we don't know the package name until we've read the first activity
	 * @param outputCodeFileName argv[1]
	 * @param packagePath package name of the first activity encountered in the script.
	 * @throws EmitterException if the file can't be moved
	 */
	public static void moveOutputCodeToPackage(String outputCodeFileName, String packagePath) throws EmitterException {
		File sourceFile = new File(outputCodeFileName);
		String destinationPath = sPackageDir + File.separator + sTestClassName + "." + Constants.Extensions.JAVA;
		if (!sourceFile.renameTo(new File(destinationPath))) {
			throw new EmitterException("failed to rename " + sourceFile.getPath() + " to " + destinationPath);
		}

	}
	
	/**
	 * generate the .classpath file for building the project.  We  add the target project name
	 * for eclipse/ant, and the robotium jar in the libs directory.
	 * @param dirname directory that the .classpath file is written into
	 * @param projectName name of the tarrget project
	 * @throws IOException if the file can't be written
	 */
	public static void writeClasspath(String dirname, String projectName) throws IOException {
		String classpath = FileUtility.readTemplate(Constants.Templates.CLASSPATH);
		classpath = classpath.replace(Constants.VariableNames.TARGET_PROJECT, projectName);
		classpath = classpath.replace(Constants.VariableNames.ROBOTIUM_JAR, sRobotiumJar);
		String path = dirname + File.separator + Constants.Filenames.CLASSPATH;
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write(classpath);
		bw.close();
	}
	
	/** 
	 * write various resources used by the test application.  We write the icon, but we probably don't have to
	 * @param dirname test project directory
	 * @return true if the resources were written correctly.
	 * @throws IOException
	 */
	public static void writeResources(String dirname) throws IOException {
		FileUtility.writeResource(Constants.Filenames.LAUNCHER_PNG, sDrawableDir);
	}
	
	/**
	 * write the AndroidManifest.xml
	 * @param dirname
	 * @throws IOException
	 */
	public static void generateManifest(String dirname)throws IOException {
		String manifest = FileUtility.readTemplate(Constants.Templates.ANDROID_MANIFEST_XML); 
		manifest = manifest.replace(Constants.VariableNames.CLASSPATH, sTestClassPath);
		manifest = manifest.replace(Constants.VariableNames.TARGETPACKAGE, sTargetPackage);
		manifest = manifest.replace(Constants.VariableNames.CLASSNAME, sTestClassName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(dirname + File.separator + Constants.Filenames.ANDROID_MANIFEST_XML));
		bw.write(manifest);
		bw.close();
	}
	
	/**
	 * replace the variables in the build.xml template and write out the file
	 * @throws IOException
	 */
	public static void generateBuildXML(String dirname) throws IOException {
		String buildXML = FileUtility.readTemplate(Constants.Templates.BUILD_XML);
		String className = StringUtils.getNameFromClassPath(sTargetClassPath);
		buildXML = buildXML.replace(Constants.VariableNames.CLASSNAME, className);
		BufferedWriter bw = new BufferedWriter(new FileWriter(dirname + File.separator + Constants.Filenames.BUILD_XML));
		bw.write(buildXML);
		bw.close();
	}
	
	/**
	 * write out the project.properties file
	 * @param dirname project directory
	 * @throws IOException
	 */
	public static void copyBuildFiles(String dirname) throws IOException {
		String projectProperties = FileUtility.readTemplate(Constants.Templates.PROJECT_PROPERTIES);
		BufferedWriter bw = new BufferedWriter(new FileWriter(dirname + File.separator + Constants.Filenames.PROJECT_PROPERTIES));
		bw.write(projectProperties);
		bw.close();
	}
	
	
	/**
	 * actually emit robotium code from a recorded file
	 * @param br BufferedReader on events.txt file
	 * @param lines output lines
	 * @throws IOException
	 * @throws EmitterException
	 */
	public void emit(BufferedReader br, List<String> lines) throws IOException, EmitterException {
		boolean firstActivityFound = false;
		boolean scrollsHaveHappened = false;
		int scrollFirstVisibleItem = 0;
		int scrollListIndex = 0;
		long startTime = -1;
		do {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			// syntax is event:time,arguments,separated,by,commas
			SuperTokenizer st = new SuperTokenizer(line, "\"", ":,", '\\');
			List<String> tokens = st.toList();
			
			// we don't actually write out time, but it's handy, you know.
			String action = tokens.get(0);
			String time = tokens.get(1);
			long timeMsec = Long.parseLong(time);
			if (startTime == -1) {
				startTime = timeMsec;
			}
			
			// when the recorder scrolls, it writes out a scroll message for each move, but
			// we just want robotium to issue a single scroll command, so once we read a scroll
			// command, we wait until a scrol happens on another listview, or a different event
			// has occurred, and we scroll to the last list item that was recorded.
			if (scrollsHaveHappened) {
				if (!action.equals(Constants.Events.SCROLL)) {
					writeScroll(scrollListIndex, scrollFirstVisibleItem, lines);
					scrollsHaveHappened = false;
				} else {
					// scroll:195758909,0,11,11,class_index,android.widget.ListView,1
					// command:time,firstVisible,visibleItemCount,totalItemCount,[view reference]
					int scrollListIndexTest = Integer.parseInt(tokens.get(7));
					if (scrollListIndexTest != scrollListIndex) {
						writeScroll(scrollListIndex, scrollFirstVisibleItem, lines);
						scrollsHaveHappened = false;
					}
				}
			}
			if (action.equals(Constants.Events.PACKAGE)) {
				sTargetPackage = tokens.get(2);
			} else if (action.equals(Constants.Events.ACTIVITY_FORWARD)) {
				if (!firstActivityFound) {
					firstActivityFound = true;
				} 
				sTargetClassPath =  tokens.get(2);
				sTargetClassName = StringUtils.getNameFromClassPath(sTargetClassPath);
				writeWaitForActivity(tokens, lines);
			} else if (action.equals(Constants.Events.ACTIVITY_BACK)) {
				writeGoBack(tokens, lines);
				writeWaitForActivity(tokens, lines);
			} else if (action.equals(Constants.Events.ITEM_CLICK)) {
				writeItemClick(tokens, lines);
			} else if (action.equals(Constants.Events.SCROLL)) {
				scrollFirstVisibleItem = Integer.parseInt(tokens.get(2));
				scrollsHaveHappened = true;
				scrollListIndex = Integer.parseInt(tokens.get(7));
			} else if (action.equals(Constants.Events.CLICK)) {
				writeClick(tokens, lines);
			} else if (action.equals(Constants.Events.DISMISS_DIALOG)) {
				writeDismissDialog(tokens, lines);
			} else if (action.equals(Constants.Events.CANCEL_DIALOG)) {
				writeCancelDialog(tokens, lines);
			}
		
		} while (true);
	}
	
	public void writeGoBack(List<String> tokens, List<String> lines) throws IOException {
		String goBackTemplate = FileUtility.readTemplate(Constants.Templates.GO_BACK);
		lines.add(goBackTemplate);
	}
	public void writeWaitForActivity(List<String> tokens, List<String> lines) throws IOException {
		String waitTemplate = FileUtility.readTemplate(Constants.Templates.WAIT_FOR_ACTIVITY);
		String classPath = tokens.get(2);
		waitTemplate = waitTemplate.replace(Constants.VariableNames.ACTIVITY_CLASS, classPath);
		lines.add(waitTemplate);
	}
	
	/**
	 * write the expression for waitForDialogToClose
	 * @param tokens
	 * @param bw
	 * @throws IOException
	 */
	public void writeDismissDialog(List<String> tokens, List<String> lines) throws IOException {
		String waitForDialogCloseTemplate = FileUtility.readTemplate(Constants.Templates.DIALOG_CLOSE_TEMPLATE);
		lines.add(waitForDialogCloseTemplate);
	}
	
	/**
	 * write the expression for goBack() for a dialog
	 * @param tokens
	 * @param bw
	 * @throws IOException
	 */
	public void writeCancelDialog(List<String> tokens, List<String> lines) throws IOException {
		String waitForDialogCloseTemplate = FileUtility.readTemplate(Constants.Templates.GO_BACK);
		lines.add(waitForDialogCloseTemplate);
	}
	
	/**
	 * write out the click command:
	 * click:195773901,id,com.example.android.apis.R$id.radio_button
	 * @param tokens
	 * @param bw
	 * @throws IOException
	 */
	public void writeClick(List<String> tokens, List<String> lines) throws IOException {
		ReferenceParser ref = new ReferenceParser(tokens, 2);
		if (ref.getReferenceType() == ReferenceParser.ReferenceType.ID) {
			String clickInViewTemplate = FileUtility.readTemplate(Constants.Templates.CLICK_IN_VIEW_ID);
			clickInViewTemplate = clickInViewTemplate.replace(Constants.VariableNames.ID, ref.getID());
			clickInViewTemplate = clickInViewTemplate.replace(Constants.VariableNames.VARIABLE_INDEX, Integer.toString(mVariableIndex));
			clickInViewTemplate = clickInViewTemplate.replace(Constants.VariableNames.CLASSPATH, ref.getClassName());
			lines.add(clickInViewTemplate);
		} else if (ref.getReferenceType() == ReferenceParser.ReferenceType.CLASS_INDEX) {
			String clickInClassIndexTemplate = FileUtility.readTemplate(Constants.Templates.CLICK_IN_VIEW_CLASS_INDEX);
			clickInClassIndexTemplate = clickInClassIndexTemplate.replace(Constants.VariableNames.VARIABLE_INDEX, Integer.toString(mVariableIndex));
			clickInClassIndexTemplate = clickInClassIndexTemplate.replace(Constants.VariableNames.CLASSPATH, ref.getClassName());
			clickInClassIndexTemplate = clickInClassIndexTemplate.replace(Constants.VariableNames.VIEW_INDEX, Integer.toString(ref.getIndex()));
			lines.add(clickInClassIndexTemplate);			
		}
		mVariableIndex++;
	}
	
	/**
	 * write out the scroll command for robotium
	 * @param scrollListIndex
	 * @param scrollFirstVisibleItem
	 * @param bw buffered writer output stream
	 */
	public void writeScroll(int scrollListIndex, int scrollFirstVisibleItem, List<String> lines) throws IOException {
		String scrollListTemplate = FileUtility.readTemplate(Constants.Templates.SCROLL_LIST);
		scrollListTemplate = scrollListTemplate.replace(Constants.VariableNames.VARIABLE_INDEX, Integer.toString(mVariableIndex));
		scrollListTemplate = scrollListTemplate.replace(Constants.VariableNames.LIST_INDEX, Integer.toString(scrollListIndex));
		scrollListTemplate = scrollListTemplate.replace(Constants.VariableNames.ITEM_INDEX, Integer.toString(scrollFirstVisibleItem));
		lines.add(scrollListTemplate);
		mVariableIndex++;
	}
	
	/**
	 * write the item click event for a list item
	 * item_click:195768219, 2,class_index,android.widget.ListView,1
	 * command:time,item_index,[view reference]
	 * @param tokens
	 * @param bw
	 * @throws IOException
	 * @throws EmitterException
	 */
	public void writeItemClick(List<String> tokens, List<String> lines) throws IOException, EmitterException {
		int itemIndex = Integer.parseInt(tokens.get(2)) + 1;
		ReferenceParser ref = new ReferenceParser(tokens, 3);
		if (ref.getReferenceType() == ReferenceParser.ReferenceType.CLASS_INDEX) {
			String viewClass = ref.getClassName();
			int classIndex = ref.getIndex();
			String itemClickTemplate = FileUtility.readTemplate(Constants.Templates.CLICK_IN_LIST);
			itemClickTemplate = itemClickTemplate.replace(Constants.VariableNames.VARIABLE_INDEX, Integer.toString(mVariableIndex));
			itemClickTemplate = itemClickTemplate.replace(Constants.VariableNames.LIST_INDEX, Integer.toString(classIndex));
			itemClickTemplate = itemClickTemplate.replace(Constants.VariableNames.ITEM_INDEX, Integer.toString(itemIndex));
			lines.add(itemClickTemplate);
		} else if (ref.getReferenceType() == ReferenceParser.ReferenceType.ID) {
			String id = ref.getID();
			String clickListItemTemplate = FileUtility.readTemplate(Constants.Templates.CLICK_IN_LIST);
			clickListItemTemplate = clickListItemTemplate.replace(Constants.VariableNames.VARIABLE_INDEX,  Integer.toString(mVariableIndex));
			clickListItemTemplate = clickListItemTemplate.replace(Constants.VariableNames.ID, id);
			clickListItemTemplate = clickListItemTemplate.replace(Constants.VariableNames.ITEM_INDEX, Integer.toString(itemIndex));
			lines.add(clickListItemTemplate);
		}
		mVariableIndex++;	
	}
	
	
	/**
	 * write the header on the first activity
	 * @param tokens
	 * @param bw
	 * @throws IOException
	 */
	public static void writeHeader(String classPath, String className, BufferedWriter bw) throws IOException {
		String header = FileUtility.readTemplate(Constants.Templates.HEADER);
		header = header.replace(Constants.VariableNames.CLASSPATH, classPath);
		header = header.replace(Constants.VariableNames.CLASSPACKAGE, classPath);
		header = header.replace(Constants.VariableNames.CLASSNAME, className);
		bw.write(header);
	}
	
	/**
	 * write the trailer on completion.
	 * @param bw
	 * @throws IOException
	 */
	public static void writeTrailer(BufferedWriter bw) throws IOException {
		String trailer = FileUtility.readTemplate(Constants.Templates.TRAILER);
		bw.write(trailer);
	}
}
