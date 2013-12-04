package com.androidApp.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Debug;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsSpinner;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.androidApp.EventRecorder.ClassCount;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ReferenceException;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Intercept.MagicFramePopup;
import com.androidApp.Listeners.FinishTextChangedListener;
import com.androidApp.Listeners.InterceptOnHierarchyChangeListener;
import com.androidApp.Listeners.OnLayoutInterceptListener;
import com.androidApp.Listeners.RecordAutoCompleteDropdownOnDismissListener;
import com.androidApp.Listeners.RecordDialogOnCancelListener;
import com.androidApp.Listeners.RecordDialogOnDismissListener;
import com.androidApp.Listeners.RecordExpandedMenuViewOnItemClickListener;
import com.androidApp.Listeners.RecordFloatingWindowOnDismissListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnChildClickListener;
import com.androidApp.Listeners.RecordOnClickListener;
import com.androidApp.Listeners.RecordOnFocusChangeListener;
import com.androidApp.Listeners.RecordOnGroupClickListener;
import com.androidApp.Listeners.RecordOnItemClickListener;
import com.androidApp.Listeners.RecordOnItemSelectedListener;
import com.androidApp.Listeners.RecordOnLongClickListener;
import com.androidApp.Listeners.RecordOnMenuItemClickListener;
import com.androidApp.Listeners.RecordOnScrollListener;
import com.androidApp.Listeners.RecordOnTabChangeListener;
import com.androidApp.Listeners.RecordOnTouchListener;

import com.androidApp.Listeners.RecordPopupWindowOnDismissListener;
import com.androidApp.Listeners.RecordSeekBarChangeListener;
import com.androidApp.Listeners.RecordSpinnerDialogOnDismissListener;
import com.androidApp.Listeners.RecordSpinnerPopupWindowOnDismissListener;
import com.androidApp.Listeners.RecordTextChangedListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.DialogUtils;
import com.androidApp.Utility.FileUtils;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;
import com.androidApp.Utility.ViewType;

/**
 * class to install the interceptors in the view event listeners
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ViewInterceptor {
	protected static final String			TAG = "ViewInterceptor";
	public static final float				GUESS_IME_HEIGHT = 0.25F;			// guess that IME takes up this amount of the screen.
	protected EventRecorder 				mEventRecorder;
	protected int							mHashCode;							// for quick view hierarchy comparison
	protected boolean						mIMEWasDisplayed = false;			// IME was displayed in the last layout
	protected View							mViewFocus;
	protected int							mLastKeyAction;						// so we can track dialog/popup/ime/activity dismiss from back/menu key					
	private Dialog							mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	private PopupWindow						mCurrentPopupWindow = null;			// current popup window, which is like the current dialog, but different
	private Object							mCurrentFloatingWindow = null;		// for windows that are not popup windows
	private View							mCurrentOptionsMenuView = null;		// current action menu window
	private List<UserDefinedViewReference>	mMotionEventViewRefs;				// references to views which receive motion events
	protected InterceptInterface			mInterceptInterface;				// intercept interface for the support library
	protected  boolean						mfDoneKeyEntered = false;			// user entered done key, triggering focus
	protected View							mNextFocusedView;					// next focused view found by finder.

	
	public ViewInterceptor(EventRecorder 		eventRecorder, 
						   IRecordTest 			recordTest,
						   InterceptInterface	interceptInterface) throws IOException, ClassNotFoundException, ReferenceException {
		mEventRecorder = eventRecorder;
		mLastKeyAction = -1;
		mInterceptInterface = interceptInterface;
		try {
			InputStream is = ViewInterceptor.class.getResourceAsStream("/raw/motion_event_views.txt");
			mMotionEventViewRefs = UserDefinedViewReference.readViewReferences(is);
			List<UserDefinedViewReference> addedMotionEventViewRefs = recordTest.getMotionEventViewReferences();
			if (addedMotionEventViewRefs != null) {
				mMotionEventViewRefs.addAll(addedMotionEventViewRefs);
			}
		} catch (Exception ex) {
			Log.i(TAG, "no view references were specified to accept motion events");
		}
	}
	
	public InterceptInterface getInterceptInterface() {
		return mInterceptInterface;
	}
	
	// accessors/mutator for focused view for IME display/remove event
	public View getFocusedView() {
		if (mViewFocus != null) {
			Log.i(TAG, "set focused view preset " + mViewFocus);
			return mViewFocus;
		} else {
			View[] decorViews = ViewExtractor.getWindowDecorViews();
			if (decorViews != null) {
				for (int i = 0; i < decorViews.length; i++) {
					View focusedView = TestUtils.findFocusedView(decorViews[i]);
					if (focusedView != null) {
						Log.i(TAG, "set focused view found " + mViewFocus);
						return focusedView;
					}
				}
			} else {
				Log.e(TAG, "failed to find any decor views");
			}
			return null;
		}
	}
	
	public void setFocusedView(View v) {
		Log.i(TAG, "set focused view " + v);
		mViewFocus = v;
	}
	
	public void setDoneKeyEntered(boolean f) {
		mfDoneKeyEntered = f;
	}
	
	public boolean getDoneKeyEntered() {
		return mfDoneKeyEntered;
	}
	
	public void setNextFocusedView(View v) {
		mNextFocusedView = v;
	}
	
	public View getNextFocusedView() {
		return mNextFocusedView;
	}
	
	/**
	 * set the current dialog
	 * @param dialog
	 */
	public void setCurrentDialog(Dialog dialog) {
		mCurrentDialog = dialog;
	}
	
	/**
	 * get the current dialog
	 * @return dialog
	 */
	public Dialog getCurrentDialog() {
		return mCurrentDialog;
	}
	
	/**
	 * get the current popup window
	 * @return popup window
	 */
	public PopupWindow getCurrentPopupWindow() {
		return mCurrentPopupWindow;
	}
	
	/**
	 * set the current popup window
	 * @param popupWindow
	 */
	public void setCurrentPopupWindow(PopupWindow popupWindow) {
		mCurrentPopupWindow = popupWindow;
	}
	
	/**
	 * get and set the current floating window
	 * @return
	 */
	public Object getCurrentFloatingWindow() {
		return mCurrentFloatingWindow;
	}

	public void setCurrentFloatingWindow(Object window) {
		mCurrentFloatingWindow = window;
	}
	
	public View getCurrentOptionsMenuView() {
		return mCurrentOptionsMenuView;
	}
	
	public void setCurrentOptionsMenuView(View view) {
		mCurrentOptionsMenuView = view;
	}
			
	
	/**
	 * retrieve the last key action, so when something happens, we can tell if the user did it.
	 * @return
	 */
	public int getLastKeyAction() {
		return mLastKeyAction;
	}

	public void setLastKeyAction(int keyAction) {
		mLastKeyAction = keyAction;
	}
	
	/**
	 * get the hashcode for the view tree.  We want to see if the view tree has changed, and unfortunately, I haven't 
	 * found an event saying that a window has been added or changed
	 * @param v
	 * @return
	 */
	static public int viewTreeHashCode(View v) {
		if (v instanceof ViewGroup) {
			int hashCode = 0x0;
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				hashCode ^= vChild.hashCode();
			}
			return hashCode;
		} else {
			return v.hashCode();
		}
	}
		
	/**
	 * replace the listeners in the view with wrapping listeners that record the events
	 * TODO: have this return a bool to indicate events were listened to.
	 * @param activity.toString() of current activity, so we can write activity uniquely
	 * @param v view to record events for.
	 * @param classTable hashtable of counters for classes for fast indexing into the viewDirectives table
	 * @param viewDirectives filtered list of directives for this activity on start.
	 * @param fAncestorListenedToTouch ancestor has listened to a touch event.
	 */
	public void replaceListeners(Activity				activity,
								 String					activityName,
								 View 					v, 
							     List<ViewDirective> 	viewDirectives,
							     boolean				fAncestorListenedToTouch) {
		if (ViewType.isVisibleAutomationView(v)) {
			return;
		}
		if (ViewDirective.match(v, ViewDirective.ViewOperation.IGNORE_EVENTS, 
							    ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			return;
		}
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				replaceHierarchyChangeListener(activity, vg);
			}
			if (v instanceof WebView) {
				mInterceptInterface.replaceWebViewListeners(mEventRecorder, v);
			} else {
				if (!ViewType.shouldBeIgnored(v) && !ViewType.isInTabControl(v)) {
					replaceViewListeners(activityName, v, viewDirectives);
				}
			}
			
			// specific handlers for seekbars/progress bars/etc.
			if (v instanceof SeekBar) {
				SeekBar seekBar = (SeekBar) v;
				replaceSeekBarListeners(activityName, mEventRecorder, seekBar);
			}
			if (v instanceof TabHost) {
				TabHost tabHost = (TabHost) v;
				replaceTabHostListeners(activityName, mEventRecorder, tabHost);
			}
			
			// adapter view cases
			if (v instanceof AdapterView) {
				AdapterView adapterView = (AdapterView) v;
				if (v instanceof AbsSpinner) {
					AbsSpinner spinner = (AbsSpinner) v;
					replaceSpinnerListeners(activityName, mEventRecorder, spinner);
				} else {
					if (!isSpinnerDropdownList(adapterView)) {
						replaceAdapterViewListeners(activityName, mEventRecorder, adapterView, viewDirectives);
					}
					// expandable list views are a special case.
					if (adapterView instanceof ExpandableListView) {
						ExpandableListView expandableListView = (ExpandableListView) adapterView;
						replaceExpandableListViewListeners(activityName, mEventRecorder, expandableListView);
					}
				}
			} else {
				
				// motion event case.  Note that adapters don't listen to raw motion events, but rather
				// scroll events
				boolean fDirectiveMatch = ViewDirective.match(v, ViewDirective.ViewOperation.MOTION_EVENTS, ViewDirective.When.ALWAYS, viewDirectives);
				boolean fOverridden = ViewType.hasOverriddenAndroidTouchMethod(v);
				boolean fListen = ViewType.listenMotionEvents(v);
				if ((fDirectiveMatch || fOverridden || fListen) && !fAncestorListenedToTouch) {
					replaceTouchListener(activityName, mEventRecorder, v);
				}
			}
			// text for focus and text listeners.
			if (v instanceof EditText) {
				TextView tv = (TextView) v;
				if (tv.hasFocus()) {
					setFocusedView(tv);
				}
				replaceEditTextListeners(activityName, tv, viewDirectives);
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeException(activityName, ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(activityName, ex, "unknown description");
			}
		}
	}
	
	/**
	 * replace the touch listener for views that listen for motion events.
	 * @param v view to replace listener for 
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public static boolean replaceTouchListener(String activityName, EventRecorder eventRecorder, View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		View.OnTouchListener originalTouchListener = ListenerIntercept.getTouchListener(v);
		if (!(originalTouchListener instanceof RecordOnTouchListener)) {
			RecordOnTouchListener recordTouchListener = new RecordOnTouchListener(activityName, eventRecorder, originalTouchListener);
			v.setOnTouchListener(recordTouchListener);
			return true;
		}
		return false;
	}
	
	/**
	 * replace this view's click listener
	 * @param v view to replace listener for 
	 * @return true if a recording listener was set
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public boolean replaceClickListener(String activityName, EventRecorder eventRecorder, View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		View.OnClickListener originalClickListener = ListenerIntercept.getClickListener(v);
		if (ViewType.listenClickEvents(v) || (originalClickListener != null)) {
			if (!(originalClickListener instanceof RecordOnClickListener)) {
				RecordOnClickListener recordClickListener = new RecordOnClickListener(activityName, eventRecorder, this, originalClickListener);
				v.setOnClickListener(recordClickListener);
				Log.i(TAG, "setting click listener for " + v + " " + RecordListener.getDescription(v));
				return true;
			}
		}
		return false;
	}

	/**
	 * replace the view's long click listener
	 * @param v
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public static boolean replaceLongClickListener(String activityName, EventRecorder eventRecorder, View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {	
		// if an ancestor has a click listener, then this view should not have one.
		if (!RecordOnLongClickListener.hasAncestorListenedToLongClick(v)) {
			View.OnLongClickListener originalLongClickListener = ListenerIntercept.getLongClickListener(v);
			
			// we do a null check here, because LongClick isn't handled by an overridden view method,
			// only with a listener class
			if ((originalLongClickListener != null) && !(originalLongClickListener instanceof RecordOnLongClickListener)) {
				RecordOnLongClickListener recordLongClickListener = new RecordOnLongClickListener(activityName, eventRecorder, originalLongClickListener);
				v.setOnLongClickListener(recordLongClickListener);
				return true;
			}
		}
		return false;		
	}

	/**
	 * since adapters don't fire the layout listener when views are added or recycled, we have to intercept
	 * their view hierarchy change
	 * @param vg
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public boolean replaceHierarchyChangeListener(Activity activity, ViewGroup vg) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {	
		ViewGroup.OnHierarchyChangeListener originalHierarchyChangeListener = ListenerIntercept.getOnHierarchyChangeListener(vg);
		if (!(originalHierarchyChangeListener instanceof InterceptOnHierarchyChangeListener)) {
			InterceptOnHierarchyChangeListener interceptHierarchyChangeListener = new InterceptOnHierarchyChangeListener(activity, mEventRecorder, this, originalHierarchyChangeListener);
			vg.setOnHierarchyChangeListener(interceptHierarchyChangeListener);
			return true;
		}
		return false;
	}

	/**
	 * the spinner popup is actually a list view, but we've already intercepted the item selected event for the spinner
	 * and we don't want to also get the list item click
	 * @param absListView list view to test
	 * @return true if the list view's adapter is a spinner adapter.
	 */
	public static boolean isSpinnerDropdownList(AdapterView adapterView) throws ClassNotFoundException {
		Adapter adapter = adapterView.getAdapter();
		Class spinnerAdapterClass = Class.forName(Constants.Classes.SPINNER_ADAPTER);
		return ((adapter != null) && (adapter.getClass() == spinnerAdapterClass));
	}
	
	/**
	 * replace the listeners in an atomic view (like a button or a text view, and stuff like that)
	 * or if there's a view directive for motion events.
	 * @param v view to intercept
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceViewListeners(String activityName, View v, List<ViewDirective> viewDirectives) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		if (!ViewDirective.match(v, ViewDirective.ViewOperation.IGNORE_CLICK_EVENTS, 
			    				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			replaceClickListener(activityName, mEventRecorder,  v);
		}
		if (!ViewDirective.match(v, ViewDirective.ViewOperation.IGNORE_LONG_CLICK_EVENTS, 
				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			replaceLongClickListener(activityName, mEventRecorder, v);
		}
	}
		
	/**
	 * same, but for overflow menu listeners
	 * TODO: CAN WE REMOVE THIS?
	 * @param eventRecorder
	 * @param v
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	/*
	public static void replaceOverflowMenuListeners(EventRecorder eventRecorder, View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		PopupMenu.OnMenuItemClickListener originalMenuItemClickListener = ListenerIntercept.getOverflowMenuOnMenuItemClickListener(v);
		if (!(originalMenuItemClickListener instanceof RecordPopupMenuOnMenuItemClickListener)) {
			ListenerIntercept.setPopupMenuOnMenuItemClickListener(v, new RecordPopupMenuOnMenuItemClickListener(eventRecorder, v)); 
		}
	}
	*/

	/**
	 * replace the listeners in a seekbar
	 * @param seekBar seekbar/progress listener, etc
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public static void replaceSeekBarListeners(String activityName, EventRecorder eventRecorder, SeekBar seekBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		SeekBar.OnSeekBarChangeListener originalSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekBar);
		if ((originalSeekBarChangeListener != null) && !(originalSeekBarChangeListener instanceof RecordSeekBarChangeListener)) {
			RecordSeekBarChangeListener recordSeekbarChangeListener = new RecordSeekBarChangeListener(activityName, eventRecorder, seekBar);
			seekBar.setOnSeekBarChangeListener(recordSeekbarChangeListener);
		}
	}
	
	/**
	 * replace the listeners for click and item selected for a spinner.
	 * @param spinner
	 */
	public static void replaceSpinnerListeners(String activityName, EventRecorder eventRecorder, AbsSpinner spinner) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		AdapterView.OnItemSelectedListener originalSelectedItemListener = ListenerIntercept.getItemSelectedListener(spinner);
		if (!(originalSelectedItemListener instanceof RecordOnItemSelectedListener)) {
			RecordOnItemSelectedListener recordItemSelectedListener = new RecordOnItemSelectedListener(activityName, eventRecorder, originalSelectedItemListener);
			spinner.setOnItemSelectedListener(recordItemSelectedListener);
		}
	}
	
	/**
	 * replace the listeners for tab chanages
	 * @param eventRecorder
	 * @param tabHost
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static void replaceTabHostListeners(String activityName, EventRecorder eventRecorder, TabHost tabHost) throws IllegalAccessException, NoSuchFieldException {
		TabHost.OnTabChangeListener originalTabChangeListener = ListenerIntercept.getTabChangeListener(tabHost);
		if (!(originalTabChangeListener instanceof RecordOnTabChangeListener)) {
			RecordOnTabChangeListener recordOnTabChangeListener = new RecordOnTabChangeListener(activityName, eventRecorder, originalTabChangeListener, tabHost);
			tabHost.setOnTabChangedListener(recordOnTabChangeListener);
		}
	}
	
	/**
	 * on one of the Android handsets, rather than bringing up an extension to the options menu, they display a popup window with a RecycleListView which
	 * displays the menu options and the AdapterView.OnItemClickListener() calls the menu item action.  
	 * @param originalItemClickListener
	 * @return
	 */
	public static boolean isExpandedMenuViewItemClickListener(AdapterView.OnItemClickListener originalItemClickListener) {
		try {
			Class alertControllerAlertParamsClass = Class.forName(Constants.Classes.ALERT_CONTROLLER_ALERT_PARAMS); 
			if ((originalItemClickListener != null) && alertControllerAlertParamsClass.isAssignableFrom(originalItemClickListener.getClass())) {
				Object clickListener = ReflectionUtils.getFieldValue(originalItemClickListener, alertControllerAlertParamsClass, Constants.Fields.ONCLICK_LISTENER);
				Class menuDialogHelperClass = Class.forName(Constants.Classes.MENU_DIALOG_HELPER);
				if (menuDialogHelperClass.isAssignableFrom(clickListener.getClass())) {
					return true;
				}
			}
		} catch (Exception ex) {
			Log.i(TAG, "exception tryin to get expanded menu view item click listener (probably a platform version problem)");
		}
		return false;

	}
	/**
	 * replace the listeners for item click, list scroll, and item select if its a spinner
	 * @param absListView list view to intercept
	 * @throws IllegalAccessException ReflectionUtils exceptions
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundExceptions
	 */
	public static void replaceAdapterViewListeners(String 				activityName, 
												   EventRecorder 		eventRecorder, 
												   AdapterView 			adapterView,
												   List<ViewDirective> 	viewDirectives) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		if (!ViewDirective.match(adapterView, ViewDirective.ViewOperation.IGNORE_ITEM_SELECT_EVENTS, 
				 				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			AdapterView.OnItemClickListener originalItemClickListener = adapterView.getOnItemClickListener();
			if (!(originalItemClickListener instanceof RecordOnItemClickListener)) {
				if (isExpandedMenuViewItemClickListener(originalItemClickListener)) {
					RecordExpandedMenuViewOnItemClickListener recordItemClickListener = new RecordExpandedMenuViewOnItemClickListener(activityName, eventRecorder, originalItemClickListener);
					adapterView.setOnItemClickListener(recordItemClickListener);	
				} else {
					RecordOnItemClickListener recordItemClickListener = new RecordOnItemClickListener(activityName, eventRecorder, originalItemClickListener);
					adapterView.setOnItemClickListener(recordItemClickListener);
				}
			}	
		}
		if (!ViewDirective.match(adapterView, ViewDirective.ViewOperation.IGNORE_SCROLL_EVENTS, 
				 				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			if (adapterView instanceof AbsListView) {
				AbsListView absListView = (AbsListView) adapterView;
				AbsListView.OnScrollListener originalScrollListener = ListenerIntercept.getScrollListener(absListView);
				if (!(originalScrollListener instanceof RecordOnScrollListener)) {
					RecordOnScrollListener recordScrollListener = new RecordOnScrollListener(activityName, eventRecorder, originalScrollListener);
					absListView.setOnScrollListener(recordScrollListener);
				}
			}
		}
	}
	
	/**
	 * ExpandableListViews are different from list views. They ignore onItemClickListener, but they have an onGroupClickListener
	 * and an onChildClickListener
	 */
	public static void replaceExpandableListViewListeners(String activityName, EventRecorder eventRecorder, ExpandableListView expandableListView) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		ExpandableListView.OnChildClickListener onChildClickListener = ListenerIntercept.getOnChildClickListener(expandableListView);
		if (!(onChildClickListener instanceof RecordOnChildClickListener)) {
			RecordOnChildClickListener recordOnChildClickListner = new RecordOnChildClickListener(activityName, eventRecorder, expandableListView);
			expandableListView.setOnChildClickListener(recordOnChildClickListner);
		}
		ExpandableListView.OnGroupClickListener onGroupClickListener = ListenerIntercept.getOnGroupClickListener(expandableListView);
		if (!(onGroupClickListener instanceof RecordOnGroupClickListener)) {
			RecordOnGroupClickListener recordOnGroupClickListener = new RecordOnGroupClickListener(activityName, eventRecorder, expandableListView);
			expandableListView.setOnGroupClickListener(recordOnGroupClickListener);
		}
	}
		
		
	/**
	 * replace the text watcher and focus change listeners for a text view (actually EditText, but we're not picky)
	 * can't be static, because the this has to be passed ot the FocusChangeListener 
	 * @param tv TextView
	 * @param viewClassIndex we need this for the ViewDirective, where the text view has to be resolved
	 * by the class and view index for event suppression and key by key entry, because it doesn't use the
	 * standard eventRecorder interface, and uses control logic for key listening. TODO: Instead, we should split
	 * the RecordTextChangedListener() to a setText and enter key by key version when this is instantiated.
	 * @throws IllegalAccessException ReflectionUtils Exceptions
	 * @throws NoSuchFieldException
	 */
	public void replaceEditTextListeners(String activityName, TextView tv, List<ViewDirective> viewDirectives) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		if (!ViewDirective.match(tv, ViewDirective.ViewOperation.IGNORE_TEXT_EVENTS, 
				 				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			ArrayList<TextWatcher> textWatcherList = ListenerIntercept.getTextWatcherList(tv);
			if (textWatcherList == null) {
				textWatcherList = new ArrayList<TextWatcher>();
			}
			// make sure that we haven't already added the intercepting text watcher.
			if (!ListenerIntercept.containsTextWatcher(textWatcherList, RecordTextChangedListener.class)) {
				textWatcherList.add(0, new RecordTextChangedListener(activityName, mEventRecorder, tv));
				ListenerIntercept.setTextWatcherList(tv, textWatcherList);
			}
		}
		if (!ViewDirective.match(tv, ViewDirective.ViewOperation.IGNORE_FOCUS_EVENTS, 
				 				 ViewDirective.When.ON_ACTIVITY_START, viewDirectives)) {
			// add listener for focus
			View.OnFocusChangeListener originalFocusChangeListener = tv.getOnFocusChangeListener();
			if (!(originalFocusChangeListener instanceof RecordOnFocusChangeListener)) {
				View.OnFocusChangeListener recordFocusChangeListener = new RecordOnFocusChangeListener(activityName, mEventRecorder, this, originalFocusChangeListener);
				tv.setOnFocusChangeListener(recordFocusChangeListener);
			}
		}
	}

	/**
	 * intercept a list of views extracted from the view hierarchy
	 * @param a
	 * @param viewList
	 * @throws NoSuchFieldException 
	 */
	public void interceptList(Activity activity, String activityName, List<View> viewList) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<ViewDirective> viewDirectives = mEventRecorder.getMatchingViewDirectives(activity, ViewDirective.When.ON_ACTIVITY_START);
		boolean fParentListenedToTouchEvent = false;
		for (View v : viewList) {
			replaceListeners(activity, activityName, v, viewDirectives, fParentListenedToTouchEvent);
			fParentListenedToTouchEvent = RecordOnTouchListener.hasListenenedToTouch(v);
		}
	}
	/**
	 * recursively set the intercepting listeners for touch/key/textwatcher events through the view tree
	 * @param v view to recurse from.
	 */
	public void intercept(Activity activity, String activityName, View v, boolean fParentListenedToTouchEvent) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<ViewDirective> viewDirectives = mEventRecorder.getMatchingViewDirectives(activity, ViewDirective.When.ON_ACTIVITY_START);
		replaceListeners(activity, activityName, v, viewDirectives, false);
		fParentListenedToTouchEvent = RecordOnTouchListener.hasListenenedToTouch(v);
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
					View vChild = vg.getChildAt(iChild);
					intercept(activity, activityName, vChild, fParentListenedToTouchEvent);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeException(activityName, ex, description);
				ex.printStackTrace();
			} catch (Exception exlog) {
				mEventRecorder.writeException(activityName, exlog , " unknown description");
			}
		}
	}
		
	/**
	 * install recorders in the listeners for the views contained in an activity
	 * @param a
	 */
	public void intercept(Activity a) {
		
		// get the content view for this activity
		Window w = a.getWindow();
        View v = w.getDecorView().findViewById(android.R.id.content);
        setFocusedView(null);
        setDoneKeyEntered(false);
        setNextFocusedView(null);
        try {
        	intercept(a, a.toString(), v, false); 
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        /*
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a, this, mEventRecorder));
        */
    	mInterceptInterface.interceptActionBar(a, this, mEventRecorder);
	}

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptPopupWindow(Activity activity, String activityName, EventRecorder eventRecorder, PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
				RecordPopupWindowOnDismissListener recordOnDismissListener = new RecordPopupWindowOnDismissListener(activityName, eventRecorder, this, null, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
			View contentView = popupWindow.getContentView();
			
			// we have to intercept even if we've already inserted the magic frame.
			if (contentView instanceof MagicFramePopup) {
				contentView = ((MagicFramePopup) contentView).getChildAt(0);
			}
			if (mInterceptInterface.isPopupMenu(contentView)) {
				mInterceptInterface.replacePopupMenuListeners(activityName, eventRecorder, contentView);
			} else {
		        setFocusedView(null);
		        setDoneKeyEntered(false);
		        setNextFocusedView(null);
		        try {
		        	intercept(activity, activityName, contentView, true);
		        } catch (Exception ex) {
		        	ex.printStackTrace();
		        }
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(activityName, ex, "Intercepting popup window");
		}
	}

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptSpinnerPopupWindow(String activityName, PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
				View anchorView = DialogUtils.getPopupWindowAnchor(popupWindow);
				RecordSpinnerPopupWindowOnDismissListener recordOnDismissListener = new RecordSpinnerPopupWindowOnDismissListener(activityName, mEventRecorder, this, anchorView, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(activityName, ex, "Intercepting popup window");
		}
	}
	
	

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptAutocompleteDropdown(Activity activity, String activityName, PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordAutoCompleteDropdownOnDismissListener.class)) {
				View anchorView = DialogUtils.getPopupWindowAnchor(popupWindow);
				RecordAutoCompleteDropdownOnDismissListener recordOnDismissListener = new RecordAutoCompleteDropdownOnDismissListener(activityName, mEventRecorder, this, anchorView, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
			View contentView = popupWindow.getContentView();
			intercept(activity, activityName, contentView, false);
		} catch (Exception ex) {
			mEventRecorder.writeException(activityName, ex, "Intercepting popup window");
		}
	}
	/**
	 * intercept the contents of a dialog.
	 * @param dialog
	 */
	public void interceptDialog(Activity activity, String activityName, Dialog dialog) {
		try {
			boolean fCancelAndDismissTaken = ListenerIntercept.isCancelAndDismissTaken(dialog);
	           DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
	            if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
	                // TODO: ?
	                RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(activityName, mEventRecorder, this, originalDismissListener);
	                if (fCancelAndDismissTaken) {
	                    ListenerIntercept.setOnDismissListener(dialog, recordOnDismissListener);
	                } else {
	                    dialog.setOnDismissListener(recordOnDismissListener);
	                }
	            }
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(activityName, mEventRecorder, originalCancelListener);
				// TODO: ?
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnCancelListener(dialog, recordOnCancelListener);
				} else {
					dialog.setOnCancelListener(recordOnCancelListener);
				}
			}
			Window window = dialog.getWindow();
	        setFocusedView(null);
	        setDoneKeyEntered(false);
	        setNextFocusedView(null);
			intercept(activity, activityName, window.getDecorView(), false);
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				mEventRecorder.writeException(activityName, ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(activityName, ex, " unknown description");
			}
		}
	}
	
	/**
	 * intercept a spinner dialog, which is different from a normal dialog, in that we want to record a different
	 * event in the dismiss case.  Note we also have to pick up the key event from the last magic frame, so we
	 * can tell if the dialog was dismissed by the "back" key or from a selection in the spinner (in which case
	 * it's ignored)
	 * @param dialog dialog containing a spinner adapter.
	 */
	public void interceptSpinnerDialog(String activityName, Dialog dialog) {
		try {
			boolean fCancelAndDismissTaken = ListenerIntercept.isCancelAndDismissTaken(dialog);
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				// TODO: ?
				RecordSpinnerDialogOnDismissListener recordSpinnerOnDismissListener = new RecordSpinnerDialogOnDismissListener(activityName, mEventRecorder, this, originalDismissListener);
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnDismissListener(dialog, recordSpinnerOnDismissListener);
				} else {
					dialog.setOnDismissListener(recordSpinnerOnDismissListener);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				mEventRecorder.writeException(activityName, ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(activityName, ex, "unknown description");
			}
		}
	}
	
	/**
	 * intercept the OnMenuItemClickListeners for the menuItems in a menu
	 * @param menu a list of menuitems and other stuff.
	 */
	public void interceptMenu(String activityName, Menu menu) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		for (int iItem = 0; iItem < menu.size(); iItem++) {
			MenuItem menuItem = menu.getItem(iItem);
			MenuItem.OnMenuItemClickListener originalClickListener = ListenerIntercept.getOnMenuItemClickListener(menuItem);
			if (!(originalClickListener instanceof RecordOnMenuItemClickListener)) {
				RecordOnMenuItemClickListener recordOnMenuItemClickListener = new RecordOnMenuItemClickListener(activityName, mEventRecorder, originalClickListener);
				menuItem.setOnMenuItemClickListener(recordOnMenuItemClickListener);
			}
		}
	}
	
	
	/**
	 * handy runnable to intercept listeners on views.  Sometimes, we intercept an event, such as a child
	 * added to a listview, but we actually want to perform the interception later, so 
	 * @author matt2
	 *
	 */
	public class InterceptViewRunnable implements Runnable {
		protected View 		mView;
		protected String 	mActivityName;
		protected Activity	mActivity;
		
		public InterceptViewRunnable(Activity activity, String activityName, View v) {
			mView = v;
			mActivityName = activityName;
			mActivity = activity;
		}
		
		public void run() {
			try {
				ViewInterceptor.this.intercept(mActivity, mActivityName, mView, false);
			} catch (Exception ex) {
				ViewInterceptor.this.mEventRecorder.writeException(mActivityName, ex, "while trying to intercept view");
			}
		}
	}
	
	/**
	 * for arbitrary floating windows that we can't clossify of figure ou their callback
	 * @param activityName
	 * @param eventRecorder
	 * @param floatingWindow
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void interceptFloatingWindow(String activityName, EventRecorder eventRecorder, Object floatingWindow) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getFloatingWindowOnDismissListener(floatingWindow);
		if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
			RecordFloatingWindowOnDismissListener recordOnDismissListener = new RecordFloatingWindowOnDismissListener(activityName, eventRecorder, this, originalDismissListener);
			ListenerIntercept.setFloatingWindowOnDismissListener(floatingWindow, recordOnDismissListener);
		}
	}
	
	public class RunUIQueuedRunnable implements Runnable {
		protected Activity	mActivity;
		protected Runnable 	mTargetRunnable;
		
		public RunUIQueuedRunnable(Activity activity, Runnable targetRunnable) {
			mActivity = activity;
			mTargetRunnable = targetRunnable;
		}
		
		public void run() {
			mActivity.runOnUiThread(mTargetRunnable);
		}
	}
	
	/**
	 * create a background thread just to force the UI to run this in its run queue as opposed to immediately
	 * if we're calling from the UI thread.
	 * not quite the same as runOnUiThread, because we want the UI to run it AFTER it's completed it's current
	 * call (like adding a view in a list).
	 * @param runnable
	 */
	public void runDeferred(Activity activity, Runnable runnable) {
		Thread backgroundThread = new Thread(new RunUIQueuedRunnable(activity, runnable));
		backgroundThread.start();
	}
 }
