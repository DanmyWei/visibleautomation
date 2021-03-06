package com.androidApp.util;

import java.io.File;

/**
 * Constants used in the robotium code emitter
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class Constants {
	// view listener references.
	public static final String UNKNOWN = "unknown";
	public static final String ACTIVITY = "activity";
	public static final String ANDROID_VIEW = "android.view";
	public static final String ANDROID_WIDGET = "android.widget";
	public static final String CLASS_INDEX_ID = "class_id";
	public static final String VIEW = "view";
	// view/widget reference types
	// TODO: create a class for this
	public static final String ID = "id";
	public static final String LIST_INDEX_ID = "list_index_id";
	public static final String TEXT = "text";
	public static final String TEXT_ID = "text_id";
	public static final String CLASS_INDEX = "class_index";
	public static final String INTERNAL_CLASS_INDEX = "internal_class_index";
	public static final String MAIN = "main";
	public static final String FUNCTION_CALL = "function_call";
	public static final String FUNCTION_DEF = "function_def";
	public static final String INTERSTITIAL_ACTIVITY_HANDLER = "InterstitialActivityHandler";
	public static final String DIALOG_HANDLER = "DialogHandler";	
	public static final String SUCCESS = "Success";
	public static final int ANDROID_40 = 14;
	
	
	// environment variables
	public static class Env {
		public static final String ANDROID_HOME = "ANDROID_HOME";
	}
	
	// events which were initiated by the user
	public enum UserEvent {
		BEFORE_TEXT("before_text"),
		BEFORE_TEXT_KEY("before_text_key"),
		AFTER_TEXT("after_text"),
		AFTER_TEXT_KEY("after_text_key"),
		CLICK("click"),
		TOUCH_UP("touch_up"),
		TOUCH_DOWN("touch_down"),
		TOUCH_MOVE("touch_move"),
		SCROLL("scroll"),
		SEEKBAR_CHANGE("seekbar_change"),
		ITEM_CLICK("item_click"),
		ITEM_CLICK_BY_TEXT("item_click_by_text"),
		ITEM_LONG_CLICK("item_long_click"),		
		DISMISS_DIALOG_BACK_KEY("dismiss_dialog_back_key"),
		CANCEL_DIALOG("cancel_dialog"),
		ITEM_SELECTED("item_selected"),
		SPINNER_ITEM_SELECTED("spinner_item_selected"),
		DISMISS_POPUP_WINDOW_BACK_KEY("dismiss_popup_window_back_key"),
		MENU_ITEM_CLICK("menu_item_click"),
		POPUP_MENU_ITEM_CLICK("popup_menu_item_click"),
		SELECT_ACTIONBAR_TAB("select_actionbar_tab"),
		DISMISS_SPINNER_DIALOG_BACK_KEY("dismiss_spinner_dialog_back_key"),
		DISMISS_SPINNER_POPUP_BACK_KEY("dismiss_spinner_popup_window_back_key"),
		SELECT_TAB("select_tab"),
		CHILD_CLICK("child_click"),
		GROUP_CLICK("group_click"),
		HIDE_IME_BACK_KEY("hide_ime_back_key"),	
		HIDE_IME("hide_ime"),	
		ACTIVITY_BACK_KEY("activity_back_key"), 
		GET_FOCUS("get_focus"),
		INTERSTITIAL_DIALOG_TITLE_ID("interstitial_dialog_title_id"),
		INTERSTITIAL_DIALOG_TITLE_TEXT("interstitial_dialog_title_text"),
		INTERSTITIAL_DIALOG_CONTENTS_ID("interstitial_dialog_contents_id"),
		INTERSTITIAL_DIALOG_CONTENTS_TEXT("interstitial_dialog_contents_text"),
		PAGE_SELECTED("page_selected"),
		COPY_TEXT("copy_text"),
		PASTE_TEXT("paste_text"),
		SELECT_BY_TEXT("select_by_text"), 
		CLICK_WORKAROUND("click_workaround"), 
		INTERSTITIAL_VIEW("interstitial_view"), 
		KEY_BACK("key_back"), 
		LONG_CLICK("long_click");
		
		public final String mEventName;		
		private UserEvent(String s) {
			mEventName = s;
		}
		
		public boolean equals(String s) {
			return mEventName.equals(s);
		}
		
		public static boolean isUserEvent(String s) {
			for (UserEvent event : UserEvent.values()) {
				if (event.equals(s)) {
					return true;
				}
			}
			return false;
		}
		
		public static boolean isInterstitialDialogEvent(String s) {
			String[] dialogEvents = new String[] {INTERSTITIAL_DIALOG_TITLE_ID.mEventName,
													INTERSTITIAL_DIALOG_TITLE_TEXT.mEventName,
													INTERSTITIAL_DIALOG_CONTENTS_ID.mEventName,
													INTERSTITIAL_DIALOG_CONTENTS_TEXT.mEventName };
			for (String event : dialogEvents) {
				if (s.equals(event)) {
					return true;
				}
			}
			return false;
		}
	}
	
	// activity transition events
	public enum ActivityEvent {
		PACKAGE("package"),
		ACTIVITY_FORWARD("activity_forward"),
		ACTIVITY_BACK("activity_back"), 
		INTERSTITIAL_ACTIVITY("interstitial_activity"),
		ACTIVITY_FINISH("activity_finish"), 
		INTERSTITIAL_VIEW("interstitial_view");
		
		public final String mEventName;		
		private ActivityEvent(String s) {
			mEventName = s;
		}
		
		public boolean equals(String s) {
			return mEventName.equals(s);
		}
		
		public static boolean isActivityEvent(String s) {
			for (ActivityEvent event : ActivityEvent.values()) {
				if (event.equals(s)) {
					return true;
				}
			}
			return false;
		}
	}	
	
	// events which are sent by the system
	public enum SystemEvent {
		DISMISS_DIALOG("dismiss_dialog"),
		SHOW_IME("show_ime"),	
		HIDE_IME("hide_ime"),	
		DISMISS_AUTOCOMPLETE_DROPDOWN("dismiss_autocomplete_dropdown"),
		CREATE_POPUP_WINDOW("create_popup_window"),
		DISMISS_POPUP_WINDOW("dismiss_popup_window"),
		ROTATION("rotation"),
		EXCEPTION("exception"),
		CREATE_SPINNER_POPUP_WINDOW("create_spinner_popup_window"),
		ON_PAGE_FINISHED("on_page_finished"),
		CREATE_DIALOG("create_dialog"),
		CREATE_SPINNER_POPUP_DIALOG("create_spinner_popup_dialog"), 
		AFTER_SET_TEXT("after_set_text");
				
		public final String mEventName;		
		private SystemEvent(String s) {
			mEventName = s;
		}
		
		public boolean equals(String s) {
			return mEventName.equals(s);
		}
		
		public static boolean isSystemEvent(String s) {
			for (SystemEvent event : SystemEvent.values()) {
				if (event.equals(s)) {
					return true;
				}
			}
			return false;
		}
	}
		
	public static class Templates {

		// template file names
		public static final String HEADER = "header.txt";
		public static final String BINARY_HEADER = "binary_header.txt";
		public static final String TEST_FUNCTION = "test_function.txt";
		public static final String BINARY_TEST_FUNCTION = "binary_test_function.txt";
		public static final String WAIT_FOR_ACTIVITY = "wait_for_activity.txt";
		public static final String BINARY_WAIT_FOR_ACTIVITY = "binary_wait_for_activity.txt";;
		public static final String CLICK_IN_LIST = "click_in_list.txt";
		public static final String SCROLL_LIST = "scroll_list.txt";
		public static final String DIALOG_CLOSE_TEMPLATE = "dialog_close_template.txt";
		public static final String DIALOG_BACK_KEY_CLOSE_TEMPLATE = "dialog_back_key_close_template.txt";
		public static final String CLICK_LIST_ITEM = "click_list_item.txt";
		public static final String CLICK_IN_VIEW_ID = "click_in_view_id.txt";
		public static final String CLICK_IN_VIEW_CLASS_INDEX = "click_in_view_class_index.txt";
		public static final String CLICK_IN_VIEW_INTERNAL_CLASS_INDEX = "click_in_view_internal_class_index.txt";
		public static final String LONG_CLICK_IN_VIEW_ID = "long_click_in_view_id.txt";
		public static final String LONG_CLICK_IN_VIEW_CLASS_INDEX = "long_click_in_view_class_index.txt";
		public static final String LONG_CLICK_IN_VIEW_INTERNAL_CLASS_INDEX = "long_click_in_view_internal_class_index.txt";
		public static final String CLICK_WORKAROUND_IN_VIEW_ID = "click_workaround_in_view_id.txt";
		public static final String CLICK_WORKAROUND_IN_VIEW_CLASS_INDEX = "click_workaround_in_view_class_index.txt";
		public static final String CLICK_WORKAROUND_IN_VIEW_INTERNAL_CLASS_INDEX = "click_workaround_in_view_internal_class_index.txt";
		public static final String TRAILER = "trailer.txt";
		public static final String BUILD_XML = "build.xml";
		public static final String PROJECT_PROPERTIES = "project.properties";
		public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
		public static final String CLASSPATH = "classpath.txt";
		public static final String CLASSPATH_SUPPORT = "classpath_support.txt";
		public static final String BINARY_CLASSPATH_CREATERECORDER = "binary_classpath_createrecorder.txt";
		public static final String BINARY_CLASSPATH_CREATERECORDER_SUPPORT = "binary_classpath_createrecorder_support.txt";
		public static final String GO_BACK = "go_back.txt";
		public static final String EDIT_TEXT_ID = "edit_text_id.txt";
		public static final String EDIT_TEXT_CLASS_INDEX = "edit_text_class_index.txt";
		public static final String EDIT_TEXT_INTERNAL_CLASS_INDEX = "edit_text_internal_class_index.txt";
		public static final String EDIT_TEXT_KEY_ID = "edit_text_key_id.txt";
		public static final String EDIT_TEXT_KEY_CLASS_INDEX = "edit_text_key_class_index.txt";
		public static final String EDIT_TEXT_KEY_INTERNAL_CLASS_INDEX = "edit_text_key_internal_class_index.txt";
		public static final String WAIT_FOR_VIEW_ID = "wait_for_view_id.txt";
		public static final String WAIT_FOR_VIEW_CLASS_INDEX = "wait_for_view_class_index.txt";
		public static final String WAIT_FOR_VIEW_INTERNAL_CLASS_INDEX = "wait_for_view_internal_class_index.txt";
		public static final String FUNCTION_HEADER = "function_header.txt";
		public static final String FUNCTION_TRAILER = "function_trailer.txt";
		public static final String SELECT_SPINNER_ITEM = "select_spinner_item.txt";
		public static final String FUNCTION_CALL = "function_call.txt";
		public static final String CLASS_TRAILER = "class_trailer.txt";
		public static final String WAIT_FOR_LIST_ID_ITEM = "wait_for_list_id_item.txt";
		public static final String WAIT_FOR_LIST_CLASS_INDEX = "wait_for_list_class_index.txt";
		public static final String WAIT_FOR_LIST_INTERNAL_CLASS_INDEX = "wait_for_list_internal_class_index.txt";
		public static final String GET_CURRENT_ACTIVITY = "get_current_activity.txt";
		public static final String WAIT_FOR_NEW_ACTIVITY = "wait_for_new_activity.txt";
		public static final String BINARY_WAIT_FOR_NEW_ACTIVITY = "binary_wait_for_new_activity.txt";;
		public static final String ALL_TESTS = "AllTests.txt";
		public static final String GO_BACK_TO_MATCHING_ACTIVITY = "go_back_to_matching_activity.txt";
		public static final String BINARY_GO_BACK_TO_MATCHING_ACTIVITY = "binary_go_back_to_matching_activity.txt";
		public static final String WENT_BACK_TO_MATCHING_ACTIVITY = "went_back_to_matching_activity.txt";			
		public static final String BINARY_WENT_BACK_TO_MATCHING_ACTIVITY = "binary_went_back_to_matching_activity.txt";
		public static final String GO_BACK_WAIT_ACTIVITY = "go_back_wait_activity.txt";
		public static final String BINARY_GO_BACK_WAIT_ACTIVITY = "binary_go_back_wait_activity.txt";
		public static final String WAIT_ACTIVITY = "wait_activity.txt";
		public static final String SHOW_IME = "show_ime.txt";
		public static final String SHOW_IME_ID = "show_ime_id.txt";
		public static final String SHOW_IME_CLASS_INDEX = "show_ime_class_index.txt";
		public static final String SHOW_IME_INTERNAL_CLASS_INDEX = "show_ime_internal_class_index.txt";
		public static final String HIDE_IME_ID = "hide_ime_id.txt";
		public static final String HIDE_IME_CLASS_INDEX = "hide_ime_class_index.txt";
		public static final String HIDE_IME_INTERNAL_CLASS_INDEX = "hide_ime_internal_class_index.txt";
		public static final String HIDE_IME_BACK_KEY_ID = "hide_ime_back_key_id.txt";
		public static final String HIDE_IME_BACK_KEY_CLASS_INDEX = "hide_ime_back_key_class_index.txt";
		public static final String HIDE_IME_BACK_KEY_INTERNAL_CLASS_INDEX = "hide_ime_back_key_internal_class_index.txt";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_ID = "dismiss_autocomplete_dropdown_id.txt";
		public static final String DISMISS_POPUP_WINDOW = "dismiss_popup_window.txt";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_CLASS_INDEX = "dismiss_autocomplete_dropdown_class_index.txt";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_INTERNAL_CLASS_INDEX = "dismiss_autocomplete_dropdown_internal_class_index.txt";
		public static final String DISMISS_POPUP_WINDOW_BACK_KEY = "dismiss_popup_window_back_key.txt";
		public static final String ROTATE = "rotate.txt";
		public static final String MENU_ITEM_CLICK = "menu_item_click.txt";
		public static final String EXCEPTION = "exception.txt";
		public static final String ALL_TESTS_CREATETEST = "AllTests_createtest.txt";
		public static final String SELECT_ACTIONBAR_TAB = "select_actionbar_tab.txt";
		public static final String GET_PREVIOUS_ACTIVITY = "get_previous_activity.txt";
		public static final String WAIT_FOR_WEBVIEW_PAGE_CLASS_INDEX = "wait_for_webview_page_class_index.txt";
		public static final String WAIT_FOR_WEBVIEW_PAGE_INTERNAL_CLASS_INDEX = "wait_for_webview_page_internal_class_index.txt";
		public static final String WAIT_FOR_WEBVIEW_PAGE_ID = "wait_for_webview_page_id.txt";
		public static final String WAIT_FOR_DIALOG_TO_OPEN = "wait_for_dialog_to_open.txt";
		public static final String WAIT_FOR_DIALOG_TO_OPEN_CONDITIONAL = "wait_for_dialog_to_open_conditional.txt";
		public static final String CANCEL_DIALOG_TEMPLATE = "dialog_cancel_template.txt";
		public static final String BINARY_CLASSPATH = "binary_classpath.txt";
		public static final String BINARY_CLASSPATH_SUPPORT = "binary_classpath_support.txt";
		public static final String PLAYBACK_MOTION_EVENTS = "playback_motion_events.txt";
		public static final String PLAYBACK_MOTION_EVENTS_CLASS_INDEX = "playback_motion_events_class_index.txt";
		public static final String PLAYBACK_MOTION_EVENTS_INTERNAL_CLASS_INDEX = "playback_motion_events_internal_class_index.txt";
		public static final String SELECT_TAB_ID = "select_tab_id.txt";
		public static final String SELECT_TAB_CLASS_INDEX = "select_tab_class_index.txt";
		public static final String SELECT_TAB_INTERNAL_CLASS_INDEX = "select_tab_internal_class_index.txt";
		public static final String CLICK_EXPANDABLE_LIST_CHILD_CLASS_INDEX = "click_expandable_list_child_class_index.txt";
		public static final String CLICK_EXPANDABLE_LIST_INTERNAL_CHILD_CLASS_INDEX = "click_expandable_list_child_internal_class_index.txt";
		public static final String CLICK_EXPANDABLE_LIST_CHILD_ID = "click_expandable_list_child_id.txt";
		public static final String CLICK_EXPANDABLE_LIST_GROUP_CLASS_INDEX = "click_expandable_list_group_class_index.txt";
		public static final String CLICK_EXPANDABLE_LIST_GROUP_INTERNAL_CLASS_INDEX = "click_expandable_list_group_internal_class_index.txt";
		public static final String CLICK_EXPANDABLE_LIST_GROUP_ID = "click_expandable_list_group_id.txt";
		public static final String WAIT_FOR_TEXT_ID = "wait_for_text_id.txt";
		public static final String WAIT_FOR_TEXT_CLASS_INDEX = "wait_for_text_class_index.txt";
		public static final String WAIT_FOR_TEXT_INTERNAL_CLASS_INDEX = "wait_for_text_internal_class_index.txt";
		public static final String INTERSTITIAL_HEADER = "interstitial_header.txt";
		public static final String ACTIIVTY_HANDLER = "activity_handler.txt";
		public static final String ACTIIVTY_HANDLER_BINARY = "activity_handler_binary.txt";
		public static final String IMPORT = "import.txt";
		public static final String REQUEST_FOCUS_ID = "request_focus_id.txt";
		public static final String REQUEST_FOCUS_CLASS_INDEX = "request_focus_class_index.txt";
		public static final String REQUEST_FOCUS_INTERNAL_CLASS_INDEX = "request_focus_internal_class_index.txt";
		public static final String DIALOG_HEADER = "dialog_header.txt";
		public static final String DIALOG_HANDLER = "dialog_handler.txt";
		public static final String IMPORT_DIALOG_HANDLER = "import_dialog_handler.txt";
		public static final String DIALOG_CONDITION = "dialog_condition.txt";
		public static final String ACTIVITY_CONDITION = "activity_condition.txt";
		public static final String DIALOG_CONDITION_BINARY = "dialog_condition_binary.txt";
		public static final String ACTIVITY_CONDITION_BINARY = "activity_condition_binary.txt";
		public static final String DIALOG_FUNCTION = "dialog_function.txt";
		public static final String ACTIVITY_FUNCTION = "activity_function.txt";
		public static final String VIEW_FUNCTION = "view_function.txt";
		public static final String CLICK_IN_LIST_INTERNAL_CLASS_INDEX = "click_in_list_internal_class_index.txt";
		public static final String CLICK_IN_LIST_BY_TEXT = "click_in_list_by_text.txt";
		public static final String CLICK_IN_LIST_BY_TEXT_INTERNAL_CLASS_INDEX = "click_in_list_by_text_internal_class_index.txt";
		public static final String CLICK_LIST_ITEM_ID_BY_TEXT = "click_in_list_id_by_text.txt";
		public static final String ACTIVITY_FUNCTION_BINARY = "activity_function_binary.txt";
		public static final String PAGE_SELECTED_ID = "page_selected_id.txt";
		public static final String PAGE_SELECTED_CLASS_INDEX = "page_selected_class_index.txt";
		public static final String PAGE_SELECTED_INTERNAL_CLASS_INDEX = "page_selected_internal_class_index.txt";
		public static final String COPY_TEXT_CLASS_INDEX = "copy_text_class_index.txt";
		public static final String COPY_TEXT_INTERNAL_CLASS_INDEX = "copy_text_internal_class_index.txt";
		public static final String COPY_TEXT_ID = "copy_text_id.txt";
		public static final String PASTE_TEXT_CLASS_INDEX = "copy_text_class_index.txt";
		public static final String PASTE_TEXT_INTERNAL_CLASS_INDEX = "copy_text_internal_class_index.txt";
		public static final String PASTE_TEXT_ID = "copy_text_id.txt";
		public static final String CLASSPATH_TEMPLATE_CREATERECORDER = "classpath_template_createrecorder.txt";
		public static final String CLASSPATH_TEMPLATE_CREATERECORDER_SUPPORT = "classpath_template_createrecorder_support.txt";
		public static final String HIDE_IME = "hide_ime.txt";
		public static final String VIEW_CONDITION_ID = "view_condition_id.txt";
		public static final String VIEW_CONDITION_CLASS_INDEX = "view_condition_class_index.txt";
		public static final String VIEW_CONDITION_INTERNAL_CLASS_INDEX = "view_condition_internal_class_index.txt";		
	}
	
	// template %replace% variables
	public static class VariableNames {
		public static final String CLASSNAME = "%CLASSNAME%";
		public static final String CLASSPATH = "%CLASSPATH%";
		public static final String TARGETCLASSPATH = "%TARGETCLASSPATH%";
		public static final String CLASSPACKAGE = "%CLASSPACKAGE%";
		public static final String ACTIVITY_CLASS = "%ACTIVITY_CLASS%";
		public static final String VARIABLE_INDEX = "%VARIABLE_INDEX%";
		public static final String ACTIVITY_VARIABLE_INDEX = "%ACTIVITY_VARIABLE_INDEX%";
		public static final String MOTION_EVENT_VARIABLE_INDEX = "%MOTION_EVENT_VARIABLE_INDEX%";
		public static final String ITEM_INDEX = "%ITEM_INDEX%";
		public static final String VIEW_INDEX = "%VIEW_INDEX%";
		public static final String ID = "%ID%";
		public static final String ROBOTIUM_JAR = "%ROBOTIUM_JAR%";
		public static final String TARGET_PACKAGE = "%TARGET_PACKAGE%";
		public static final String TARGET_PROJECT = "%TARGET_PROJECT%";
		public static final String TEXT = "%TEXT%";
		public static final String FUNCTION_NAME = "%FUNCTION_NAME%";
		public static final String SPINNER_INDEX = "%SPINNER_INDEX%";
		public static final String DESCRIPTION = "%DESCRIPTION%";
		public static final String ACTIVITY = "%ACTIVITY%";
		public static final String TESTCLASSNAME = "%TESTCLASSNAME%";
		public static final String ORIENTATION = "%ORIENTATION%";
		public static final String MENU_ITEM_ID = "%MENU_ITEM_ID%";
		public static final String TARGET = "%TARGET%";
		public static final String MIN_SDK_VERSION = "%MIN_SDK_VERSION%";
		public static final String MODE = "%MODE%";
		public static final String TESTPACKAGE = "%TESTPACKAGE%";
		public static final String TAB_INDEX = "%TAB_INDEX%";
		public static final String URL = "%URL%";
		public static final String TAB_ID = "%TAB_ID%";
		public static final String UNIQUE_NAME = "%UNIQUE_NAME%";
		public static final String INTERNAL_CLASS = "%INTERNAL_CLASS%";
		public static final String CLASS_VARIABLE_INDEX = "%CLASS_VARIABLE_INDEX%";
		public static final String INSERT = "%INSERT%";
		public static final String ACTIVITY_HANDLERS = "%ACTIVITY_HANDLERS%";
		public static final String HANDLER = "%HANDLER%";
		public static final String HANDLER_IMPORTS = "%HANDLER_IMPORTS%";
		public static final String INSERTION_START = "%INSERTION_START%";
		public static final String INSERTION_END = "%INSERTION_END%";
		public static final String CODE_DEFINITION = "%CODE_DEFINITION%";
		public static final String DIALOG_HANDLERS = "%DIALOG_HANDLERS%";
		public static final String DIALOG_HANDLER_IMPORTS = "%DIALOG_HANDLER_IMPORTS%";
		public static final String ITEM_TEXT = "%ITEM_TEXT%";
		public static final String GROUP_POSITION = "%GROUP_POSITION%";
		public static final String CHILD_POSITION = "%CHILD_POSITION%";
		public static final String PAGE_ID = "%PAGE_ID%";
		public static final String VARIABLE_NAME = "%VARIABLE_NAME%";
		public static final String REAL_INDEX = "%REAL_INDEX%";
		public static final String TEST_PACKAGE = "%TEST_PACKAGE%";	
		public static final String SUPPORT_LIBRARIES = "%SUPPORT_LIBRARIES%";
		public static final String RECORDER_SUPPORT = "%RECORDER_SUPPORT%";	
		public static final String RECORDER_LIBRARY = "%RECORDER_LIBRARY%";	
		public static final String ROBOTIUMUTILS = "%ROBOTIUMUTILS%";
		public static final String ROBOTIUMTUILS_PACKAGE = "%ROBOTIUMUTILS_PACKAGE%";
		public static final String LIBRARIES = "%LIBRARIES%";		
	}
	
	// generic names
	public static class Names {
		public static final String ACTIVITY = "activity";
		public static final String DEVICE = "device";		
		public static final String TEST = "Test";
		public static final String RECORDER = "Recorder";		
	}
	
	// filenames used in the project
	public static class Filenames {
		public static final String BUILD_XML = "build.xml";
		public static final String PROJECT_PROPERTIES = "project_properties.txt";
		public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
		public static final String LAUNCHER_PNG = "ic_launcher.png";
		public static final String CLASSPATH = ".classpath";
		public static final String OUTPUT = "output.java";
		public static final String UTILITY_JAR = "robotiumutils.jar";
		public static final String ALL_TESTS = "AllTests.java";
		public static final String EVENTS = "events.txt";
		public static final String PROJECT_PROPERTIES_FILENAME = "project.properties";
		public static final String PROJECT_FILENAME = ".project";
		public static final String ROBOTIUM_JAR = "robotium-solo-4.1.1-SNAPSHOT.jar";
		public static final String ANDROID_SUPPORT_JAR = "android-support-v13.jar";
		public static final String TEMPORARY_FILE = "temp.txt";
		public static final String VIEW_DIRECTIVES = "view_directives.txt";
		public static final String NONE = "none";
		public static final String DIALOG_HANDLERS = "dialog_handlers.txt";
		public static final String ECLIPSE_JDT_PREFS = "org.eclipse.jdt.core.prefs";
		public static final String ROBOTIUMUTILS_JAR = "robotiumutils.jar";
		public static final String ROBOTIUMUTILS_30_JAR = "robotiumutils30.jar";
		public static final String ROBOTIUMUTILS_40_JAR = "robotiumutils40.jar";		
		public static final String ROBOTIUMUTILS_SUPPORTV4_JAR = "robotiumutilssupportv4.jar";
		public static final String ROBOTIUMUTILS_SUPPORTV13_JAR = "robotiumutilssupportv13.jar";
		public static final String SUPPORT_V4 = "android-support-v4.jar";
		public static final String SUPPORT_V7_APPCOMPAT = "android-support-v7-appcompat.jar";
		public static final String SUPPORT_V7_GRIDLAYOUT = "android-support-v7-gridlayout.jar";
		public static final String SUPPORT_V7_MEDIA = "android-support-v7-mediarouter.jar";
		public static final String SUPPORT_V13 = "android-support-v13.jar";
		public static final String RECORDER_SUPPORT_V4_JAR = "recordersupportv4.jar";
		public static final String RECORDER_SUPPORT_V13_JAR = "recordersupportv13.jar";
		public static final String RECORDER_23_JAR = "recorder23.jar";
		public static final String RECORDER_30_JAR = "recorder30.jar";
		public static final String RECORDER_40_JAR = "recorder40.jar";
	}
	
	// output directories
	public static class Dirs {
		public static final String SRC = "src";
		public static final String RES = "res";
		public static final String DRAWABLE = "drawable";
		public static final String LIBS = "libs";
		public static final String BIN = "bin";
		public static final String PLATFORM_TOOLS = "platform-tools";
		public static final String PLATFORM_TOOLS_22 = "build-tools/17.0.0";
		public static final String GEN = "gen";
		public static final String ASSETS = "assets";
		public static final String DATABASES = "databases";
		public static final String SHARED_PREFS = "shared_prefs";
		public static final String FILES = "files";
		public static final String SAVESTATE = "savestate";
		public static final String EXTERNAL_STORAGE = "/sdcard";
		public static final String HANDLERS = "handlers";
		public static final String DIALOGS = "dialogs";
		public static final String SETTINGS = ".settings";
		public static final String PLATFORMS = "platforms";
	}
	
	// executables
	public static class Executables {
		public static final String ADB = "adb";
		public static final String AAPT = "aapt";
		public static final String DEXDUMP = "dexdump";
	}
	// outfile file extension
	public static class Extensions {
		public static final String RECORDER = "Recorder";
		public static final String TEST = "Test";
		public static final String JAVA = "java";
		public static final String TEXT = "txt";
		public static final String ZIP = "zip";
		public static final String APK = "apk";
	}
	
	public static class SoloFunctions {
		public static final String WAIT_FOR_ACTIVITY = "waitForActivity";
		public static final String GO_BACK = "goBack";
	}
	
	// messages back from commands
	public static class Messages {
		public static final String NO_SUCH_FILE_OR_DIRECTORY = "No such file or directory";
	}
	
	public static class Errors {
		public static final String DOES_NOT_EXIST = "does not exist";
		public static final String FAILURE = "Failure";
	}
	
	
	// classes to search for in the dex output from the apk to determine which support jar files to link with
	public static class SupportClasses {
		public static final String SUPPORT_V4 = "android/support/v4";
		public static final String SUPPORT_V7_APPCOMPAT = "android/support/v7/appcompat";
		public static final String SUPPORT_V7_GRIDLAYOUT = "android/support/v7/gridlayout";
		public static final String SUPPORT_V7_MEDIA = "android/support/v7/media";
		public static final String SUPPORT_V13 = "android/support/v13";
	}

	public static class Packages {
		public static final String ROBOTIUMUTILS = "com.androidApp.util";
		public static final String ROBOTIUMUTILS_30 = "com.androidApp.util30";
		public static final String ROBOTIUMUTILS_40 = "com.androidApp.util40";
		public static final String ROBOTIUMUTILS_SUPPORT_V4 = "com.androidApp.utilSuportV4";
		public static final String ROBOTIUMUTILS_SUPPORT_V13 = "com.androidApp.utilSuportV13";
	}
	
	public static class Prefixes {
		public static final String ANDROID = "android";
	}
	
	public static class AndroidVersions {
		public static int GINGERBREAD_MR1 = 10;
		public static int HONEYCOMB = 11;
	}
}
