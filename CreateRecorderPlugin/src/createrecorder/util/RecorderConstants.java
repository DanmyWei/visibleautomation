package createrecorder.util;

/**
 * Constants for the plugin event recorder (TODO: these need to be broken into included classes)
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecorderConstants {
	public static final String ALLTESTS_FILE = "AllTests.java";
	public static final String CLASSPATH_TEMPLATE = "classpath_template_createrecorder.txt";
	public static final String MANIFEST_TEMPLATE = "manifest_template_createrecorder.txt";
	public static final String PROJECT_PROPERTIES_TEMPLATE = "project_properties_createrecorder.txt";
	public static final String PROJECT_TEMPLATE = "project_template_createrecorder.txt";
	public static final String TESTCLASS_TEMPLATE = "testclass_template_createrecorder.txt";
	public static final String ALLTESTS_TEMPLATE = "AllTests.txt";
	public static final String RECORDER_SUFFIX = "Recorder";
	public static final String RECORDER_JAR = "recorder.jar";
	public static final String TEST_EXTENSION = ".test";
	public static final String RECORDER_EXTENSION = "Recorder";
	public static final String ECLIPSE_ADT = "com.android.ide.eclipse.adt";
	public static final String ANDROID_SDK = ECLIPSE_ADT + ".sdk";
	public static final String ADB = "adb";
	public static final String DEVICE_EVENTS = "/sdcard/events";
	public static final String ALL_TESTS_CREATETEST = "AllTests_createtest.txt";
	public static final String BINARY_TESTCLASS_TEMPLATE = "binary_testclass_template_createrecorder.txt";
	public static final String TEST_SUFFIX = "Test";
	public static final String KEYBOARD_APK = "ImePreferences.apk";
	public static final String KEYBOARD_PACKAGE = "com.example.android.softkeyboard";
	public static final String LOGSERVICE_APK = "LogService.apk";
	public static final String LOGSERVICE_PACKAGE = "com.androidApp.logService";
	public static final String EVENTRECORDERINTERFACE_JAR = "eventrecorderinterface.jar";
	public static final String NO_SUCH_FILE_OR_DIRECTORY = "No such file or directory";
	public static final String VISIBLE_AUTOMATION = "Visible Automation";
	public static final String VERIFIED = "verified";
	public static final String BUILD_TOOLS = "build-tools";
	public static final String AAPT = "aapt";
	public static final String AAPT_WIN32 = "aapt.exe";
	
	// eclipse preference keys.
	public static class Preferences {
		public static final String RESIGN_APK = "resign_apk";
	}
	
	public static class EnvironmentVariables {
		public static final String ANDROID_HOME = "ANDROID_HOME";
		public static final String HOME = "HOME";
	}
}
