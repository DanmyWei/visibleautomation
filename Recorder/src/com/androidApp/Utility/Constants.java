package com.androidApp.Utility;

public class Constants {
	// view listener references.
	public static final String ACTIVITY = "activity";
	public static class Packages {
		public static final String ANDROID_VIEW = "android.view";
		public static final String ANDROID_WIDGET = "android.widget";
	}
	// view/widget reference types
	
	public static class Reference {
		public static final String UNKNOWN = "unknown";		
		public static final String CLASS_ID = "class_id";
		public static final String ID = "id";
		public static final String LIST_INDEX_ID = "list_index_id";
		public static final String TEXT = "text";
		public static final String TEXT_ID = "text_id";
		public static final String CLASS_INDEX = "class_index";
	}
	
	public static class Classes {
		public static final String LISTENER_INFO = "android.view.View$ListenerInfo";
		public static final String PHONE_DECOR_VIEW = "com.android.internal.policy.impl.PhoneWindow$DecorView";
		public static final String ENCLOSING_CLASS = "this$0";
		public static final String DIALOG_TITLE_SIMPLE_NAME = "DialogTitle";
		public static final String DIALOG_TITLE = "com.android.internal.widget.DialogTitle";
		public static final String PHONE_WINDOW = "com.android.internal.policy.impl.PhoneWindow";
		public static final String WINDOW_MANAGER = "android.view.WindowManagerImpl";
		public static final String SPINNER_DIALOG_POPUP = "android.widget.Spinner$DialogPopup";
	}
	
	// listener field names
	public static class Fields {
		public static final String TEXT_WATCHER_LIST = "mListeners";
		public static final String TOUCH_LISTENER = "mOnTouchListener";
		public static final String CLICK_LISTENER = "mOnClickListener";
		public static final String DRAG_LISTENER = "mOnDragListener";
		public static final String FOCUS_CHANGE_LISTENER = "mOnFocusChangeListener";
		public static final String ITEM_CLICK_LISTENER = "mOnItemClickListener";
		public static final String KEY_LISTENER = "mOnKeyListener";
		public static final String LISTENER_INFO = "mListenerInfo";
		public static final String LONG_CLICK_LISTENER = "mOnLongClickListener";
		public static final String SCROLL_LISTENER = "mOnScrollListener";
		public static final String SEEKBAR_CHANGE_LISTENER = "mOnSeekBarChangeListener";
		public static final String DIALOG_DISMISS_LISTENER = "mOnDialogDismissListener";
		public static final String DIALOG_DISMISS_MESSAGE = "mDismissMessage";
		public static final String DIALOG_SHOW_MESSAGE = "mShowMessage";
		public static final String CALLBACK = "mCallback";
		public static final String CHECKED_CHANGE_LISTENER = "mOnCheckedChangeListener";
		public static final String DIALOG_CLICK_MESSAGE = "mClickMessage";
		public static final String DIALOG_CANCEL_MESSAGE = "mCancelMessage";
		public static final String SELECTED_ITEM_LISTENER = "mOnItemSelectedListener";
		public static final String POPUP = "mPopup";
		public static final String TITLE = "mTitle";
		public static final String TEXT = "mText";
		public static final String VIEWS = "mViews";
		public static final String DECOR = "mDecor";
	}
	
	// possible derive methods which listen for events.
	public static class EventMethods {
		public static final String ON_CLICK = "onClick";
		public static final String ON_TOUCH = "onTouch";
		public static final String ON_LONG_CLICK = "onLongClick";
	}
	
	// event tags
	public static class EventTags {
		public static final String UNKNOWN = "unknown";
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
		public static final String PROGRESS_CHANGED = "progress_changed";
		public static final String START_TRACKING = "start_tracking";
		public static final String STOP_TRACKING = "stop_tracking";
		public static final String DISMISS_DIALOG = "dismiss_dialog";
		public static final String SHOW_DIALOG = "show_dialog";
		public static final String PACKAGE = "package";
		public static final String APPLICATION = "application";
		public static final String DIALOG_KEY = "dialog_key";
		public static final String DIALOG_CLICK = "dialog_click";
		public static final String CANCEL_DIALOG = "cancel_dialog";
		public static final String ITEM_SELECTED = "item_selected";
		public static final String KEY = "key";
		public static final String SPINNER_CLICK = "spinner_click";
		public static final String EXCEPTION = "exception";
		public static final String DISMISS_SPINNER_DIALOG = "dismiss_spinner_dialog";
		public static final String CANCEL_SPINNER_DIALOG = "cancel_spinner_dialog";
	}
	
	// description strings 
	public static class Description {
		public static final String IMAGE_VIEW = "ImageView";
		public static final String CLICK_ON = "Click on";
		public static final String UNTITLED_DIALOG = "Untitled Dialog";
	}
	
}
