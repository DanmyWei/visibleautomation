package com.androidApp.Utility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListPopupWindow;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.MenuItem;
/**
 * constants used in the recorder, categorized by use
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class Constants {
	// view listener references.
	public static final String ACTIVITY = "activity";
	public static final String UTF_8 = "UTF-8";
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
		public static final String INTERNAL_CLASS_INDEX = "internal_class_index";
		public static final String VIEW_BY_CLASS = "view_class";
		public static final String VIEW_BY_ACTIVITY_CLASS = "activity_view_class";
		public static final String VIEW_BY_ACTIVITY_CLASS_INDEX = "activity_view_class_index";
		public static final String VIEW_BY_ACTIVITY_ID = "activity_view_id";
		public static final String VIEW_BY_ACTIVITY_INTERNAL_CLASS = "activity_view_internal_class";
		public static final String VIEW_BY_ACTIVITY_INTERNAL_CLASS_INDEX = "activity_view_internal_class_index";
	}
	
	// classes used in reflection
	public static class Classes {
		public static final String LISTENER_INFO = "android.view.View$ListenerInfo";
		public static final String PHONE_DECOR_VIEW = "com.android.internal.policy.impl.PhoneWindow$DecorView";
		public static final String POPUP_VIEW_CONTAINER = "android.widget.PopupWindow$PopupViewContainer";
		public static final String DIALOG_TITLE_SIMPLE_NAME = "DialogTitle";
		public static final String DIALOG_TITLE = "com.android.internal.widget.DialogTitle";
		public static final String PHONE_WINDOW = "com.android.internal.policy.impl.PhoneWindow";
		public static final String WINDOW_MANAGER = "android.view.WindowManagerImpl";
		public static final String SPINNER_DIALOG_POPUP = "android.widget.Spinner$DialogPopup";
		public static final String AUTOCOMPLETE_DIALOG_POPUP = "android.widget.PopupWindow$PopupViewContainer";
		public static final String LIST_WINDOW_POPUP = "android.widget.ListPopupWindow";
		public static final String LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW = "android.widget.ListPopupWindow$DropDownListView";
		public static final String THIS = "this$0";
		public static final String ACTION_BAR_IMPL = "com.android.internal.app.ActionBarImpl";
		public static final String DROPDOWN_LISTVIEW = "android.widget.ListPopupWindow$DropDownListView";
		public static final String MENU_POPUP_HELPER = "com.android.internal.view.menu.MenuPopupHelper";
		public static final String MENU_ITEM_IMPL = "com.android.internal.view.menu.MenuItemImpl";
		public static final String SCROLLING_TAG_CONTAINER_TAB_VIEW = "com.android.internal.widget.ScrollingTabContainerView$TabView";
		public static final String EXPANDED_MENU_VIEW = "com.android.internal.view.menu.ExpandedMenuView";	
		public static final String ACTIONBAR_CONTAINER = "com.android.internal.widget.ActionBarContainer";
		public static final String LOCAL_WINDOW_MANAGER = "android.view.Window$LocalWindowManager";
		public static final String WINDOW_MANAGER_COMPAT_MODE = "android.view.WindowManagerImpl$CompatModeWrapper";
		public static final String WINDOW_MANAGER_IMPL = "android.view.WindowManagerImpl";
		public static final String ACTION_BAR_CONTAINER = "com.android.internal.widget.ActionBarContainer";
		public static final String LIST_MENU_ITEM_VIEW = "com.android.internal.view.menu.ListMenuItemView";
		public static final String MENU_BUILDER = "com.android.internal.view.menu.MenuBuilder";
		public static final String ACTION_BAR_IMPL_TAB_IMPL = "com.android.internal.app.ActionBarImpl$TabImpl";
		public static final String SPINNER_ADAPTER = "android.widget.Spinner$DropDownAdapter";
		public static final String WEBKIT_CALLBACK_PROXY = "android.webkit.CallbackProxy";
		public static final String ALERT_CONTROLLER = "com.android.internal.app.AlertController";
		public static final String SPINNER_DROPDOWN_ADAPTER = "android.widget.Spinner$DropDownAdapter";	
		public static final String SCROLLING_TAB_CONTAINER_VIEW = "com.android.internal.widget.ScrollingTabContainerView";
		public static final String HORIZONTAL_SCROLL_VIEW = "com.android.widget.HorizontalScrollView";
		public static final String ANDROID_WIDGET = "android.widget";	
		public static final String ANDROID_VIEW = "android.view";
		public static final String ANDROID_INTERNAL = "android.internal";
		public static final String COM_ANDROID_WIDGET = "com.android.widget";	
		public static final String COM_ANDROID_VIEW = "com.android.view";
		public static final String COM_ANDROID_INTERNAL = "com.android.internal";
		public static final String INPUT_CONTENT_TYPE = "android.widget.TextView$InputContentType";
		public static final String ACTION_MENU_PRESENTER = "com.android.internal.view.menu.ActionMenuPresenter";
		public static final String OVERFLOW_MENU_BUTTON = "com.android.internal.view.menu.ActionMenuPresenter$OverflowMenuButton";
		public static final String ACTION_BUTTON_SUBMENU = "com.android.internal.view.menu.ActionMenuPresenter$ActionButtonSubmenu";
		public static final String POPUP_PRESENTER_CALLBACK = "com.android.internal.view.menu.ActionMenuPresenter$PopupPresenterCallback";
		public static final String ALERT_CONTROLLER_ALERT_PARAMS = "com.android.internal.app.AlertController$AlertParams$3";
		public static final String MENU_DIALOG_HELPER = "MenuDialogHelper";
		public static final String SUBMENU_BUILDER = "com.android.internal.view.menu.SubMenuBuilder";	
		public static final String VIEW_ROOT_IMPL = "android.view.ViewRootImpl";
		public static final String ACTION_BAR_VIEW = "com.android.internal.widget.ActionBarView";
		public static final String ON_PAGE_CHANGE_LISTENER = "android.support.v4.view.ViewPager.OnPageChangeListener";
	}
	
	// of course, developers can run proguard on their software, which changes the field names of classes.  On the other
	// hand, it can't change the intrinsic types that they reference, so we provide a "backup" reflection method, where
	// we can scan fields by types, and reference fields by their type and index. Unfortunately for templatized types
	// like list, we can only test against the type, not the contained element.
	public enum Fields {
		TEXT_WATCHER_LIST("mListeners", ArrayList.class),
		TOUCH_LISTENER("mOnTouchListener", View.OnTouchListener.class),
		CLICK_LISTENER("mOnClickListener", View.OnClickListener.class),
		DRAG_LISTENER("mOnDragListener", View.OnDragListener.class),
		FOCUS_CHANGE_LISTENER("mOnFocusChangeListener", View.OnFocusChangeListener.class),
		ONITEM_CLICK_LISTENER("mOnItemClickListener", AdapterView.OnItemClickListener.class),
		ITEM_CLICK_LISTENER("mItemClickListener", AdapterView.OnItemClickListener.class), 	// from ListPopupWindow
		KEY_LISTENER("mOnKeyListener", View.OnKeyListener.class),
		LISTENER_INFO("mListenerInfo", Constants.Classes.LISTENER_INFO),
		LONG_CLICK_LISTENER("mOnLongClickListener", OnLongClickListener.class),
		SCROLL_LISTENER("mOnScrollListener", AbsListView.OnScrollListener.class ),
		SEEKBAR_CHANGE_LISTENER("mOnSeekBarChangeListener", SeekBar.OnSeekBarChangeListener.class),
		DIALOG_DISMISS_MESSAGE("mDismissMessage", android.os.Message.class, 1),
		DIALOG_SHOW_MESSAGE("mShowMessage", android.os.Message.class, 2),
		DIALOG_CANCEL_MESSAGE("mCancelMessage", android.os.Message.class, 0),
		CALLBACK("mCallback", ActionBar.TabListener.class),
		CHECKED_CHANGE_LISTENER("mOnCheckedChangeListener", CompoundButton.OnCheckedChangeListener.class),
		SELECTED_ITEM_LISTENER("mOnItemSelectedListener", AdapterView.OnItemSelectedListener.class ),
		POPUP("mPopup", ListPopupWindow.class),
		TITLE("mTitle", TextView.class),
		VIEWS("mViews", View[].class),
		POPUP_WINDOW_ON_DISMISS_LISTENER("mOnDismissListener", PopupWindow.OnDismissListener.class),
		WINDOW_MANAGER_FIELD("mWindowManager", String.class),
		WINDOW_MANAGER_FIELD_STATIC("sWindowManager", String.class),
		CONTENT_VIEW("mContentView", View.class ),
		ACTION_VIEW("mActionView", Constants.Classes.ACTION_BAR_VIEW),
		CONTAINER_VIEW("mContainerView", Constants.Classes.ACTION_BAR_IMPL),
		CANCEL_AND_DISMISS_TAKEN("mCancelAndDismissTaken", String.class),	
		PRESENTER_CALLBACK("mPresenterCallback", PopupMenu.class),
		MENU_ITEM_CLICK_LISTENER("mMenuItemClickListener", PopupMenu.OnMenuItemClickListener.class),
		// NOTE: we may need multiple classes for this
		MENU("mMenu", Menu.class),
		ENCLOSING_CLASS("this$0"),
		POPUP_VIEW("mPopupView", View.class, 1),
		ITEM_DATA("mItemData", Constants.Classes.LIST_MENU_ITEM_VIEW),
		ANCHOR("mAnchor",WeakReference.class),
		CALLBACK_PROXY("mCallbackProxy", Constants.Classes.WEBKIT_CALLBACK_PROXY),
		WEBVIEW_CLIENT("mWebViewClient", WebViewClient.class),
		ON_TAB_CHANGE_LISTENER("mOnTabChangeListener",TabHost.OnTabChangeListener.class ),
		ONGROUP_CLICK_LISTENER("mOnGroupClickListener", ExpandableListView.OnGroupClickListener.class ),
		ONCHILD_CLICK_LISTENER("mOnChildClickListener", ExpandableListView.OnGroupClickListener.class ),
		HORIZONTALLY_SCROLLING("mHorizontallyScrolling", boolean.class),
		ON_HIERARCHY_CHANGE_LISTENER("mOnHierarchyChangeListener", ViewGroup.OnHierarchyChangeListener.class),
		POPUP_PRESENTER_CALLBACK("mPopupPresenterCallback",Constants.Classes.POPUP_PRESENTER_CALLBACK ),
		MENU_CLICK_LISTENER("mClickListener", MenuItem.OnMenuItemClickListener.class),
		ONCLICK_LISTENER("mOnClickListener", View.OnClickListener.class),
		ITEMS("mItems",List.class),
		VIEW("mView", View.class),
		PARENT("mParent", View.class),
		ON_VALUE_CHANGE_LISTENER("mOnValueChangeListener", NumberPicker.OnValueChangeListener.class),
		STOPPED("mStopped", boolean.class ),
		RESUMED("mResumed", boolean.class),
		ON_PAGE_CHANGE_LISTENER("mOnPageChangeListener", Constants.Classes.ON_PAGE_CHANGE_LISTENER );

		protected final static String TAG = "Fields";
		public final String mName;
		public Class mCls;
		public final int mClassIndex;
		
		private Fields(String name, Class cls) {
		    mName = name;
		    mCls = cls;
		    mClassIndex  = 0;
		}

		private Fields(String name) {
		    mName = name;
		    mCls = null;
		    mClassIndex = 0;
		}
		
		private Fields(String name, String className) {
		    mName = name;
		    mClassIndex = 0;
		    try {	
		    	mCls = Class.forName(className);
		    } catch (ClassNotFoundException cnfex) {
				Log.e(TAG, "failed to resolve class for " + className);
				mCls = null;
		    }
		}

		private Fields(String name, String className, int classIndex) {
		    mName = name;
		    mClassIndex = classIndex;
		    try {	
		    	mCls = Class.forName(className);
		    } catch (ClassNotFoundException cnfex) {
				Log.e(TAG, "failed to resolve class for " + className);
				mCls = null;
		    }
		}

		private Fields(String name, Class cls, int classIndex) {
		    mName = name;
		    mCls = cls;
		    mClassIndex = classIndex;
		}
	
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
		public static final String BEFORE_TEXT_KEY = "before_text_key";
		public static final String AFTER_TEXT = "after_text";
		public static final String AFTER_TEXT_KEY = "after_text_key";
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
		public static final String CREATE_DIALOG = "create_dialog";
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
		public static final String DISMISS_SPINNER_POPUP_WINDOW = "dismiss_spinner_popup_window";
		public static final String DISMISS_POPUP_WINDOW_BACK_KEY = "dismiss_popup_window_back_key";
		public static final String DISMISS_SPINNER_POPUP_WINDOW_BACK_KEY = "dismiss_spinner_popup_window_back_key";
		public static final String CREATE_POPUP_WINDOW = "create_popup_window";
		public static final String CREATE_SPINNER_POPUP_WINDOW = "create_spinner_popup_window";
		public static final String CREATE_AUTOCOMPLETE_DROPDOWN = "create_autocomplete_dropdown";
		public static final String DISMISS_SPINNER_DIALOG_BACK_KEY = "dismiss_spinner_dialog_back_key";
		public static final String DISMISS_AUTOCOMPLETE_DROPDOWN_BACK_KEY = "dismiss_autocomplete_dropdown_back_key";
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
		public static final String ON_PAGE_FINISHED = "on_page_finished";
		public static final String ON_PAGE_STARTED = "on_page_started";
		public static final String ON_RECEIVED_ERROR = "on_received_error";
		public static final String ON_SCALE_CHANGED = "on_scale_changed";
		public static final String CREATE_SPINNER_POPUP_DIALOG = "create_spinner_popup_dialog";
		public static final String SELECT_TAB = "select_tab";
		public static final String GROUP_CLICK = "group_click";
		public static final String CHILD_CLICK = "child_click";
		public static final String CREATE_FLOATING_WINDOW = "create_floating_window";
		public static final String TOUCH_CANCEL = "touch_cancel";
		public static final String INTERSTITIAL_ACTIVITY = "interstitial_activity";
		public static final String IGNORE_EVENTS = "ignore_events";
		public static final String IGNORE_TEXT_EVENTS = "ignore_text_events";
		public static final String IGNORE_CLICK_EVENTS = "ignore_click_events";
		public static final String IGNORE_SCROLL_EVENTS = "ignore_scroll_events";
		public static final String IGNORE_LONG_CLICK_EVENTS = "ignore_long_click_events";
		public static final String IGNORE_FOCUS_EVENTS = "ignore_focus_events";
		public static final String IGNORE_ITEM_SELECTED_EVENTS = "ignore_item_selected_events";
		public static final String MOTION_EVENTS = "motion_events";
		public static final String COPY_TEXT = "copy_text";
		public static final String PASTE_TEXT = "paste_text";
		public static final String SELECT_BY_TEXT = "select_by_text";
		public static final String CHECK = "check";
		public static final String UNCHECK = "uncheck";
		public static final String ENTER_TEXT_BY_KEY = "enter_text_by_key";
		public static final String BEFORE_SET_TEXT = "before_set_text";
		public static final String AFTER_SET_TEXT = "after_set_text";
		public static final String CREATE_EXPANDED_MENU_VIEW = "create_expanded_menu_view";
		public static final String VALUE_CHANGE = "value_change";
		public static final String INTERSTITIAL_DIALOG_TITLE_ID = "interstitial_dialog_title_id";
		public static final String INTERSTITIAL_DIALOG_TITLE_TEXT = "interstitial_dialog_title_text";
		public static final String INTERSTITIAL_DIALOG_CONTENTS_ID = "interstitial_dialog_contents_id";
		public static final String INTERSTITIAL_DIALOG_CONTENTS_TEXT = "interstitial_dialog_contents_text";
		public static final String INTERSTITIAL_DIALOG_CONTENTS = "interstitial_dialog_contents";
		public static final String ITEM_CLICK_BY_TEXT = "item_click_by_text";
		public static final String CLICK_WORKAROUND = "click_workaround";
		public static final String SELECT_ITEM_WORKAROUND = "select_item_workaround";
		public static final String PAGE_SCROLL_STATE_CHANGED = "page_scroll_state_changed";
		public static final String PAGE_SCROLLED = "page_scrolled";
		public static final String PAGE_SELECTED = "page_selected";
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
	
	// internal asset file names
	public static class Asset {
		public static final String DICTIONARY = "dictionary.txt";	
		public static final String INTERNALCLASSES = "internalclasses.txt";
		public static final String WHITELIST = "whitelist-android";	
		public static final String USER_MOTION_EVENT_VIEWS = "user_motion_event_views.txt";
		public static final String INTERSTITIAL_ACTIVITIES = "interstitial_activities.txt";
		public static final String VIEW_DIRECTIVES = "view_directives.txt";
		public static final String INTERSTITIAL_ACTIVITY_LIST = "interstitial_activities.txt";
	}
	
	public static class Methods {
		public static final String IS_SCROLLING_CONTAINER = "isScrollingContainer";
		public static final String ON_CLICK = "onClick";
		public static final String ON_TOUCH = "onTouch";	
	}
	
	public static class Sizes {
		public static final int MAX_TEXT_LEN = 32;
		public static final int REALLY_MAX_TEXT_LEN = 64;
	}
	
	public static class Files {
		public static final String EVENTS = "events.txt";
		public static final String VIEW_DIRECTIVES = "view_directives.txt";
	}
	
	// TODO: this needs to be read from a resource file, but jar files cannot access android resource files
	public static class DisplayStrings {
		public static final String VISIBLE_AUTOMATION = "Visible Automation";
		public static final String INTERSTITIAL_DIALOG_TITLE = "Interstitial Dialog by Title";
		public static final String INTERSTITIAL_DIALOG_CONTENTS = "Interstitial Dialog by Contents";
		public static final String INTERSTITIAL_ACTIVITY = "Interstitial Activity";
		public static final String VIEW_SELECTION = "View Selection";

		public static final String IGNORE_EVENTS = "Ignore All Events";
		public static final String IGNORE_TEXT_EVENTS = "Ignore Text Events";
		public static final String IGNORE_TOUCH_EVENTS = "Ignore Touch Events";
		public static final String IGNORE_CLICK_EVENTS = "Ignore Click Events";
		public static final String IGNORE_LONG_CLICK_EVENTS = "Ignore Long Click Events";
		public static final String IGNORE_SCROLL_EVENTS = "Ignore Scroll Events";
		public static final String IGNORE_ITEM_SELECT_EVENTS = "Ignore Item Select Events";
		public static final String IGNORE_FOCUS_EVENTS = "Ignore Focus Events";

		public static final String COPY_TEXT = "Copy Text To Variable";
		public static final String PASTE_TEXT = "Paste Text From Variable";
		public static final String SELECT_BY_TEXT = "Select Items By Text";
		public static final String CHECK = "Check";
		public static final String UNCHECK = "Uncheck";
		public static final String MOTION_EVENTS = "Listen to Motion Events";
		public static final String OK = "OK";
		public static final String CANCEL = "Cancel";
		public static final String VARIABLE_NOT_FOUND = "Variable not found";
		public static final String VIEW_NOT_TEXT_VIEW = "The selected view is not a text view";
		public static final String VIEW_REFERENCE_FAILED = "Failed to generate view reference";
		public static final String INSERT_BY_CHARACTER = "Insert text by keys";
		public static final String NO_DIALOG_TITLE = "Unable to find dialog title";
		public static final String RESOURCE_ERROR = "resource access error";
		public static final String SELECT_ITEM_WORKAROUND = "select item workaround";
		public static final String CLICK_WORKAROUND = "click workaround";
		public static final String KEYBOARD_NOT_INSTALLED = "The custom keyboard was not installed.  Please go to settings and set it as the default keyboard";
	}
	
	// when ViewDirectives should be applied during record and playback.
	public static class When {
		public static final String ON_ACTIVITY_START = "on_activity_start";
		public static final String ON_ACTIVITY_END = "on_activity_end";
		public static final String ON_VALUE_CHANGE = "on_value_change";
		public static final String ALWAYS = "always";
	}
}