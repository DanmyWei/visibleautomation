package createrecorder.util;

/**
 * Constants for the plugin event recorder (TODO: these need to be broken into included classes)
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecorderConstants {
	public static final String ALLTESTS_FILE = "AllTests.java";
	public static final String CLASSPATH_TEMPLATE = "classpath_template_createrecorder.txt";
	public static final String CLASSPATH_TEMPLATE_SUPPORT = "classpath_template_createrecorder_support.txt";
	public static final String MANIFEST_TEMPLATE_RECORDER = "manifest_template_createrecorder.txt";
	public static final String MANIFEST_TEMPLATE_BINARY_RECORDER = "manifest_template_createrecorder_binary.txt";
	public static final String MANIFEST_TEMPLATE_TEST = "manifest_template_createtest.txt";
	public static final String MANIFEST_TEMPLATE_BINARY_TEST = "manifest_template_createtest_binary.txt";
	public static final String PROJECT_PROPERTIES_TEMPLATE = "project_properties_createrecorder.txt";
	public static final String PROJECT_TEMPLATE = "project_template_createrecorder.txt";
	public static final String TESTCLASS_TEMPLATE = "testclass_template_createrecorder.txt";
	public static final String BINARY_TESTCLASS_TEMPLATE = "binary_testclass_template_createrecorder.txt";
	public static final String SUPPORT_PACKAGE = "com.androidApp.SupportTest";
	public static final String ADVANCED_PACKAGE = "com.androidApp.AdvancedTest";
	public static final String ALLTESTS_TEMPLATE = "AllTests.txt";
	public static final String RECORDER_SUFFIX = "Recorder";
	public static final String APK_SUFFIX = "apk";
	public static final String RECORDER_JAR = "recorder.jar";
	public static final String ANDROID_SUPPORT_V13_JAR = "android-support-v13.jar";
	public static final String ANDROID_SUPPORT_V7_APPCOMPAT = "android-support-v7-appcompat.jar";
	public static final String TEST_EXTENSION = ".test";
	public static final String RECORDER_EXTENSION = "Recorder";
	public static final String ECLIPSE_ADT = "com.android.ide.eclipse.adt";
	public static final String ANDROID_SDK = ECLIPSE_ADT + ".sdk";
	public static final String ADB = "adb";
	public static final String DEVICE_EVENTS = "/sdcard/events";
	public static final String ALL_TESTS_CREATETEST = "AllTests_createtest.txt";
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
	public static final String DEXDUMP = "dexdump";
	public static final String DEXDUMP_WIN32 = "dexdump.exe";
	public static final String PLATFORMS = "platforms";
	public static final String ANDROID = "android";
	
	// classes to search for in the dex output from the apk to determine which support jar files to link with
	public static class SupportClasses {
		public static final String SUPPORT_V4 = "android/support/v4";
		public static final String SUPPORT_V7_APPCOMPAT = "android/support/v7/appcompat";
		public static final String SUPPORT_V7_GRIDLAYOUT = "android/support/v7/gridlayout";
		public static final String SUPPORT_V7_MEDIA = "android/support/v7/media";
		public static final String SUPPORT_V13 = "android/support/v13";
	}
	
	// support jr files to link with.
	public static class SupportLibraries {
		public static final String SUPPORT_V4 = "android-support-v4.jar";
		public static final String SUPPORT_V7_APPCOMPAT = "android-support-v7-appcompat.jar";
		public static final String SUPPORT_V7_GRIDLAYOUT = "android-support-v7-gridlayout.jar";
		public static final String SUPPORT_V7_MEDIA = "android-support-v7-mediarouter.jar";
		public static final String SUPPORT_V13 = "android-support-v13.jar";
	}
	
	// eclipse preference keys.
	public static class Preferences {
		public static final String RESIGN_APK = "resign_apk";
	}
	
	public static class EnvironmentVariables {
		public static final String ANDROID_HOME = "ANDROID_HOME";
		public static final String HOME = "HOME";
	}
}
