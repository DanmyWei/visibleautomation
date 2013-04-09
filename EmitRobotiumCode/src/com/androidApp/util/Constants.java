package com.androidApp.util;

public class Constants {
	// view listener references.
	public static final String UNKNOWN = "unknown";
	public static final String ACTIVITY = "activity";
	public static final String ANDROID_VIEW = "android.view";
	public static final String ANDROID_WIDGET = "android.widget";
	public static final String CLASS_INDEX_ID = "class_id";
	// view/widget reference types
	public static final String ID = "id";
	public static final String LIST_INDEX_ID = "list_index_id";
	public static final String TEXT = "text";
	public static final String TEXT_ID = "text_id";
	public static final String CLASS_INDEX = "class_index";
	
	// event tags
	public static class Events {
		public static final String PACKAGE = "package";
		public static final String ENTER_TEXT = "enter_text";
		public static final String BEFORE_TEXT = "before_text";
		public static final String AFTER_TEXT = "after_text";
		public static final String CHECKED = "checked";
		public static final String CLICK = "click";
		public static final String TOUCH_UP = "touch_up";
		public static final String TOUCH_DOWN = "touch_down";
		public static final String TOUCH_MOVE = "touch_move";
		public static final String SCROLL = "scroll";
		public static final String SEEKBAR_CHANGE = "seekbar_change";
		public static final String ITEM_CLICK = "item_click";
		public static final String ITEM_LONG_CLICK = "item_long_click";
		public static final String ACTIVITY_FORWARD = "activity_forward";
		public static final String ACTIVITY_BACK = "activity_back";
		public static final String ACTIVITY_BACK_KEY = "activity_back_key";
		public static final String CLASS = "class";	
		public static final String DISMISS_DIALOG = "dismiss_dialog";
		public static final Object DISMISS_DIALOG_BACK_KEY = "dismiss_dialog_back_key";
		public static final String CANCEL_DIALOG = "cancel_dialog";
		public static final String ITEM_SELECTED = "item_selected";
		public static final String SHOW_IME = "show_ime";	
		public static final String HIDE_IME = "hide_ime";	
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN = "dismiss_autocomplete_dropdown";
		public static final String CREATE_POPUP_WINDOW = "create_popup_window";
		public static final String DISMISS_POPUP_WINDOW = "dismiss_popup_window";
		public static final Object DISMISS_POPUP_WINDOW_BACK_KEY = "dismiss_popup_window_back_key";
		public static final String ROTATION = "rotation";
		public static final Object MENU_ITEM_CLICK = "menu_item_click";
		public static final Object POPUP_MENU_ITEM_CLICK = "popup_menu_item_click";

	}
	public static class Templates {

		// template file names
		public static final String HEADER = "header.txt";
		public static final String TEST_FUNCTION = "test_function.txt";
		public static final String WAIT_FOR_ACTIVITY = "wait_for_activity.txt";
		public static final String CLICK_IN_LIST = "click_in_list.txt";
		public static final String SCROLL_LIST = "scroll_list.txt";
		public static final String DIALOG_CLOSE_TEMPLATE = "dialog_close_template.txt";
		public static final String DIALOG_BACK_KEY_CLOSE_TEMPLATE = "dialog_back_key_close_template.txt";
		public static final String CLICK_LIST_ITEM = "click_list_item.txt";
		public static final String CLICK_IN_VIEW_ID = "click_in_view_id.txt";
		public static final String CLICK_IN_VIEW_CLASS_INDEX = "click_in_view_class_index.txt";
		public static final String TRAILER = "trailer.txt";
		public static final String BUILD_XML = "build.xml";
		public static final String PROJECT_PROPERTIES = "project.properties";
		public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
		public static final String CLASSPATH = "classpath";
		public static final String GO_BACK = "go_back.txt";
		public static final String EDIT_TEXT_ID = "edit_text_id.txt";
		public static final String EDIT_TEXT_CLASS_INDEX = "edit_text_class_index.txt";
		public static final String WAIT_FOR_VIEW_ID = "wait_for_view_id.txt";
		public static final String WAIT_FOR_VIEW_CLASS_INDEX = "wait_for_view_class_index.txt";
		public static final String FUNCTION_HEADER = "function_header.txt";
		public static final String FUNCTION_TRAILER = "function_trailer.txt";
		public static final String SELECT_SPINNER_ITEM = "select_spinner_item.txt";
		public static final String FUNCTION_CALL = "function_call.txt";
		public static final String CLASS_TRAILER = "class_trailer.txt";
		public static final String WAIT_FOR_LIST_ID_ITEM = "wait_for_list_id_item.txt";
		public static final String WAIT_FOR_LIST_CLASS_INDEX = "wait_for_list_class_index.txt";
		public static final String GET_CURRENT_ACTIVITY = "get_current_activity.txt";
		public static final String WAIT_FOR_NEW_ACTIVITY = "wait_for_new_activity.txt";
		public static final String ALL_TESTS = "AllTests.txt";
		public static final String GO_BACK_TO_MATCHING_ACTIVITY = "go_back_to_matching_activity.txt";
		public static final String WENT_BACK_TO_MATCHING_ACTIVITY = "went_back_to_matching_activity.txt";			
		public static final String GO_BACK_WAIT_ACTIVITY = "go_back_wait_activity.txt";
		public static final String WAIT_ACTIVITY = "wait_activity.txt";
		public static final String SHOW_IME_ID = "show_ime_id.txt";
		public static final String SHOW_IME_CLASS_INDEX = "show_ime_class_index.txt";
		public static final String HIDE_IME_ID = "hide_ime_id.txt";
		public static final String HIDE_IME_CLASS_INDEX = "hide_ime_class_index.txt";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_ID = "dismiss_autocomplete_dropdown_id.txt";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_CLASS_INDEX = "dismiss_autocomplete_dropdown_class_index.txt";
		public static final String DISMISS_POPUP_WINDOW = "dismiss_popup_window.txt";
		public static final String DISMISS_POPUP_WINDOW_BACK_KEY = "dismiss_popup_window_back_key.txt";
		public static final String ROTATE = "rotate.txt";
	}
	
	// template %replace% variables
	public static class VariableNames {
		public static final String CLASSNAME = "%CLASSNAME%";
		public static final String CLASSPATH = "%CLASSPATH%";
		public static final String TARGETCLASSPATH = "%TARGETCLASSPATH%";
		public static final CharSequence CLASSPACKAGE = "%CLASSPACKAGE%";
		public static final CharSequence ACTIVITY_CLASS = "%ACTIVITY_CLASS%";
		public static final CharSequence VARIABLE_INDEX = "%VARIABLE_INDEX%";
		public static final CharSequence ACTIVITY_VARIABLE_INDEX = "%ACTIVITY_VARIABLE_INDEX%";
		public static final CharSequence ITEM_INDEX = "%ITEM_INDEX%";
		public static final CharSequence VIEW_INDEX = "%VIEW_INDEX%";
		public static final CharSequence ID = "%ID%";
		public static final CharSequence ROBOTIUM_JAR = "%ROBOTIUM_JAR%";
		public static final CharSequence TARGETPACKAGE = "%TARGET_PACKAGE%";
		public static final CharSequence TARGET_PROJECT = "%TARGET_PROJECT%";
		public static final CharSequence TEXT = "%TEXT%";
		public static final CharSequence FUNCTION_NAME = "%FUNCTION_NAME%";
		public static final CharSequence SPINNER_INDEX = "%SPINNER_INDEX%";
		public static final CharSequence DESCRIPTION = "%DESCRIPTION%";
		public static final CharSequence ACTIVITY = "%ACTIVITY%";
		public static final CharSequence TESTCLASSNAME = "%TESTCLASSNAME%";
		public static final CharSequence ORIENTATION = "%ORIENTATION%";
	}
	
	// generic names
	public static class Names {
		public static final String ACTIVITY = "activity";
		public static final Object DEVICE = "device";		
	}
	
	// filenames used in the project
	public static class Filenames {
		public static final String BUILD_XML = "build.xml";
		public static final String PROJECT_PROPERTIES = "project.properties";
		public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
		public static final String LAUNCHER_PNG = "ic_launcher.png";
		public static final String CLASSPATH = ".classpath";
		public static final String OUTPUT = "output.java";
		public static final String UTILITY_JAR = "robotiumutils.jar";
		public static final String ALL_TESTS = "AllTests.java";
	}
	
	// output directories
	public static class Dirs {
		public static final String SRC = "src";
		public static final String RES = "res";
		public static final String DRAWABLE = "drawable";
		public static final String LIBS = "libs";
	}
	
	// outfile file extension
	public static class Extensions {
		public static final String TEST = "Test";
		public static final String JAVA = "java";
	}
	
	public static class SoloFunctions {
		public static final String WAIT_FOR_ACTIVITY = "waitForActivity";
		public static final String GO_BACK = "goBack";
	}
}
