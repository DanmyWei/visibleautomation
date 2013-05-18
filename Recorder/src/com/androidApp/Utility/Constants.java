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
		public static final String POPUP_VIEW_CONTAINER = "android.widget.PopupWindow.PopupViewContainer";
		public static final String POPUP_VIEW_CONTAINER_CREATECLASS = "android.widget.PopupWindow$PopupViewContainer";
		public static final String DIALOG_TITLE_SIMPLE_NAME = "DialogTitle";
		public static final String DIALOG_TITLE = "com.android.internal.widget.DialogTitle";
		public static final String PHONE_WINDOW = "com.android.internal.policy.impl.PhoneWindow";
		public static final String WINDOW_MANAGER = "android.view.WindowManagerImpl";
		public static final String SPINNER_DIALOG_POPUP = "android.widget.Spinner$DialogPopup";
		public static final String AUTOCOMPLETE_DIALOG_POPUP = "android.widget.PopupWindow$PopupViewContainer";
		public static final String LIST_WINDOW_POPUP = "android.widget.ListPopupWindow";
		public static final String LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW = "android.widget.ListPopupWindow.DropDownListView";
		public static final String THIS = "this$0";
		public static final String ACTION_BAR_IMPL = "com.android.internal.app.ActionBarImpl";
		public static final String DROPDOWN_LISTVIEW = "android.widget.ListPopupWindow$DropDownListView";
		public static final String MENU_POPUP_HELPER = "com.android.internal.view.menu.MenuPopupHelper";
		public static final String MENU_ITEM_IMPL = "com.android.internal.view.menu.MenuItemImpl";
		public static final String ACTION_MENU_PRESENTER = "com.android.internal.view.menu.ActionMenuPresenter$OverflowPopup";
		public static final String EXPANDED_MENU_VIEW = "com.android.internal.view.menu.ExpandedMenuView";	
		public static final String ACTIONBAR_CONTAINER = "com.android.internal.widget.ActionBarContainer";
		public static final String LOCAL_WINDOW_MANAGER = "android.view.Window$LocalWindowManager";
		public static final String WINDOW_MANAGER_COMPAT_MODE = "android.view.WindowManagerImpl$CompatModeWrapper";
		public static final String WINDOW_MANAGER_IMPL = "android.view.WindowManagerImpl";
		public static final String ACTION_BAR_CONTAINER = "com.android.internal.widget.ActionBarContainer";
		public static final String LIST_MENU_ITEM_VIEW = "com.android.internal.view.menu.ListMenuItemView";
		public static final String MENU_BUILDER = "com.android.internal.view.menu.MenuBuilder";
		public static final String ACTION_BAR_IMPL_TAB_IMPL = "com.android.internal.app.ActionBarImpl$TabImpl";
		public static final String SPINNER_ADAPTER = "android.widget.Spinner$DropDownAdapter";;
	}
	
	// listener field names
	public static class Fields {
		public static final String TEXT_WATCHER_LIST = "mListeners";
		public static final String TOUCH_LISTENER = "mOnTouchListener";
		public static final String CLICK_LISTENER = "mOnClickListener";
		public static final String DRAG_LISTENER = "mOnDragListener";
		public static final String FOCUS_CHANGE_LISTENER = "mOnFocusChangeListener";
		public static final String ONITEM_CLICK_LISTENER = "mOnItemClickListener";
		public static final String ITEM_CLICK_LISTENER = "mItemClickListener";
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
		public static final String PARAMS = "mParams";
		public static final String DECOR = "mDecor";
		public static final String POPUP_WINDOW_ON_DISMISS_LISTENER = "mOnDismissListener";
		public static final String WINDOW_MANAGER_FIELD = "mWindowManager";
		public static final String WINDOW_MANAGER_FIELD_STATIC = "sWindowManager";
		public static final String CONTENT_VIEW = "mContentView";
		public static final String ACTION_VIEW = "mActionView";
		public static final String CONTAINER_VIEW = "mContainerView";
		public static final String CANCEL_AND_DISMISS_TAKEN = "mCancelAndDismissTaken";	
		public static final String PRESENTER_CALLBACK = "mPresenterCallback";
		public static final String MENU_ITEM_CLICK_LISTENER = "mMenuItemClickListener";
		public static final String MENU = "mMenu";
		public static final String SINGLE_LINE = "mSingleLine";	
		public static final String ENCLOSING_CLASS = "this$0";
		public static final String IS_SHOWING = "mIsShowing";
		public static final String POPUP_VIEW = "mPopupView";
		public static final String WINDOW_MANAGER = "mWindowManager";
		public static final String LAYOUT_CHANGE_LISTENERS = "mOnLayoutChangeListeners";
		public static final String ATTACH_STATE_CHANGE_LISTENERS = "mOnAttachStateChangeListeners";
		public static final String HOVER_LISTENER = "mOnHoverListener";
		public static final String GENERIC_MOTION_LISTENER = "mOnGenericMotionListener";
		public static final String SYSTEMUI_VISIBILITY_CHANGE_LISTENER = "mOnSystemUiVisibilityChangeListener";
		public static final String ITEM_DATA = "mItemData";
		public static final String TABS = "mTabs";
		public static final String ANCHOR = "mAnchor";
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
		public static final String DISMISS_DIALOG_BACK_KEY = "dismiss_dialog_back_key";
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
		public static final String GET_FOCUS = "get_focus";
		public static final String LOSE_FOCUS = "lose_focus";
		public static final String SHOW_IME = "show_ime";
		public static final String HIDE_IME = "hide_ime";
		public static final String HIDE_IME_BACK_KEY = "hide_ime_back_key";
		public static final String LONG_CLICK = "long_click";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN = "dismiss_autocomplete_dropdown";
		public static final String DISMISS_POPUP_WINDOW = "dismiss_popup_window";
		public static final String DISMISS_POPUP_WINDOW_BACK_KEY = "dismiss_popup_window_back_key";
		public static final String CREATE_POPUP_WINDOW = "create_popup_window";
		public static final String CREATE_SPINNER_POPUP_WINDOW = "create_spinner_popup_window";
		public static final String ROTATION = "rotation";
		public static final String MENU_ITEM_CLICK = "menu_item_click";
		public static final String POPUP_MENU_ITEM_CLICK = "popup_menu_item_click";
		public static final String OPEN_ACTION_MENU = "open_action_menu";
		public static final String OPEN_ACTION_MENU_KEY = "open_action_menu_key";
		public static final String MENU_BACK_KEY = "menu_back_key";
		public static final String MENU_MENU_KEY = "menu_menu_key";
		public static final String ACTIVITY_MENU_KEY = "activity_menu_key";
		public static final String ACTIVITY_BACK_KEY = "activity_back_key";
		public static final String KEY_BACK = "key_back";
		public static final String KEY_MENU = "key_menu";
		public static final String KEY_HOME = "key_home";
		public static final String CLOSE_OPTIONS_MENU = "close_options_menu";
		public static final String SELECT_ACTIONBAR_TAB = "select_actionbar_tab";
		public static final String DISMISS_SPINNER_DIALOG_BACK_KEY = "dismiss_spinner_dialog_back_key";
	}
	
	// description strings 
	public static class Description {
		public static final String IMAGE_VIEW = "ImageView";
		public static final String CLICK_ON = "Click on";
		public static final String UNTITLED_DIALOG = "Untitled Dialog";
		public static final String EMPTY_TEXT = "Empty Text View";
	}
	
	// key actions
	public static class Action {
		public static final String UNKNOWN = "unknown";
		public static final String UP = "up";
		public static final String DOWN = "down";
	}
}
