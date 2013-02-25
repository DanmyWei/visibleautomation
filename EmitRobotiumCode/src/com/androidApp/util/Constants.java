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
		public static final String CLASS = "class";	
		public static final String DISMISS_DIALOG = "dismiss_dialog";
		public static final Object CANCEL_DIALOG = "cancel_dialog";
		public static final Object ITEM_SELECTED = "item_selected";	
	}
	public static class Templates {

		// template file names
		public static final String HEADER = "header.txt";
		public static final String TEST_FUNCTION = "test_function.txt";
		public static final String WAIT_FOR_ACTIVITY = "wait_for_activity.txt";
		public static final String CLICK_IN_LIST = "click_in_list.txt";
		public static final String SCROLL_LIST = "scroll_list.txt";
		public static final String DIALOG_CLOSE_TEMPLATE = "dialog_close_template.txt";
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
			
	}
	// template %replace% variables
	public static class VariableNames {
		public static final String CLASSNAME = "%CLASSNAME%";
		public static final String CLASSPATH = "%CLASSPATH%";
		public static final String TARGETCLASSPATH = "%TARGETCLASSPATH%";
		public static final CharSequence CLASSPACKAGE = "%CLASSPACKAGE%";
		public static final CharSequence ACTIVITY_CLASS = "%ACTIVITY_CLASS%";
		public static final CharSequence VARIABLE_INDEX = "%VARIABLE_INDEX%";
		public static final CharSequence LIST_INDEX = "%LIST_INDEX%";
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
	}
	
	public static class Filenames {
		public static final String BUILD_XML = "build.xml";
		public static final String PROJECT_PROPERTIES = "project.properties";
		public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
		public static final String LAUNCHER_PNG = "ic_launcher.png";
		public static final String CLASSPATH = ".classpath";
		public static final String OUTPUT = "output.java";
	}
	
	public static class Dirs {
		public static final String SRC = "src";
		public static final String RES = "res";
		public static final String DRAWABLE = "drawable";
		public static final String LIBS = "libs";
	}
	
	public static class Extensions {
		public static final String TEST = "Test";
		public static final String JAVA = "java";
	}
	
	public static class SoloFunctions {
		public static final String WAIT_FOR_ACTIVITY = "waitForActivity";
		public static final String GO_BACK = "goBack";
	}
}
