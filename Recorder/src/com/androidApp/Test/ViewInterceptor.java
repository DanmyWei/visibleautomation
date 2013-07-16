package com.androidApp.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ReferenceException;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.Intercept.InterceptActionBar;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Listeners.FinishTextChangedListener;
import com.androidApp.Listeners.InterceptOnHierarchyChangeListener;
import com.androidApp.Listeners.RecordAutoCompleteDropdownOnDismissListener;
import com.androidApp.Listeners.RecordDialogOnCancelListener;
import com.androidApp.Listeners.RecordDialogOnDismissListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnChildClickListener;
import com.androidApp.Listeners.RecordOnClickListener;
import com.androidApp.Listeners.RecordOnFocusChangeListener;
import com.androidApp.Listeners.RecordOnGroupClickListener;
import com.androidApp.Listeners.RecordOnItemClickListener;
import com.androidApp.Listeners.RecordOnItemSelectedListener;
import com.androidApp.Listeners.RecordOnKeyListener;
import com.androidApp.Listeners.RecordOnLongClickListener;
import com.androidApp.Listeners.RecordOnScrollListener;
import com.androidApp.Listeners.RecordOnTabChangeListener;
import com.androidApp.Listeners.RecordOnTouchListener;
import com.androidApp.Listeners.RecordPopupMenuOnMenuItemClickListener;
import com.androidApp.Listeners.RecordPopupWindowOnDismissListener;
import com.androidApp.Listeners.RecordSeekBarChangeListener;
import com.androidApp.Listeners.RecordSpinnerDialogOnDismissListener;
import com.androidApp.Listeners.RecordSpinnerPopupWindowOnDismissListener;
import com.androidApp.Listeners.RecordTextChangedListener;
import com.androidApp.Listeners.RecordWebViewClient;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FileUtils;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

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
	private List<View>						mMotionEventViews;
	
	private class InterceptInfo {
		public View			mClickView;			// this view had its onClickListener set (or overrode onClick()), so don't bother anymore
		public View			mTouchView;			// this view had its onTouchListener set (or overrode onTouch()), so don't bother anymore
		public View			mLongClickView;		// this view had its onLongClickListener set
		public AdapterView	mAdapterView;		// this adapter was found, so any child is now a child of an adapter.
		public boolean		mfAdapterListens;	// the adapter listens to item click and item selected events.	
		
		public InterceptInfo() {
			mClickView = null;
			mTouchView = null;
			mLongClickView = null;
			mAdapterView = null;
			mfAdapterListens = false;
		}
	}
	
	public ViewInterceptor(EventRecorder eventRecorder, IRecordTest recordTest) throws IOException, ClassNotFoundException, ReferenceException {
		mEventRecorder = eventRecorder;
		mLastKeyAction = -1;
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
	
	/**
	 * given an activity, recurse its views to find any that we should listen
	 * @param a
	 */
	public void findMotionEventViews(Activity a) {
		if (mMotionEventViewRefs != null) {
			List<UserDefinedViewReference> activityReferences = UserDefinedViewReference.filterReferencesInActivity(a, mMotionEventViewRefs);
			mMotionEventViews = UserDefinedViewReference.getMatchingViews(a, activityReferences);
		}
	}
	
	// accessors/mutator for focused view for IME display/remove event
	public View getFocusedView() {
		return mViewFocus;
	}
	
	public void setFocusedView(View v) {
		mViewFocus = v;
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
	 * @param v view to record events for.
	 */
	public void replaceListeners(View v) {
		try {
			if (v instanceof WebView) {
				replaceWebViewListeners(v);
			} else {
				if (!TestUtils.shouldBeIgnored(v) &&
					!TestUtils.isInTabControl(v)) {
					replaceViewListeners(v);
				}
			}
			
			// specific handlers for seekbars/progress bars/etc.
			if (v instanceof SeekBar) {
				SeekBar seekBar = (SeekBar) v;
				replaceSeekBarListeners(seekBar);
			}
			if (v instanceof TabHost) {
				TabHost tabHost = (TabHost) v;
				replaceTabHostListeners(tabHost);
			}
			
			// adapter view cases
			if (v instanceof AdapterView) {
				AdapterView adapterView = (AdapterView) v;
				replaceHierarchyChangeListener(adapterView);
				if (v instanceof Spinner) {
					Spinner spinner = (Spinner) v;
					replaceSpinnerListeners(spinner);
				} else {
					if (!isSpinnerDropdownList(adapterView)) {
						replaceAdapterViewListeners(adapterView);
					}
					// expandable list views are a special case.
					if (adapterView instanceof ExpandableListView) {
						ExpandableListView expandableListView = (ExpandableListView) adapterView;
						replaceExpandableListVewListeners(expandableListView);
					}
					if (TestUtils.listenMotionEvents(mMotionEventViews, v)) {
						replaceTouchListener(v);
					}
				}
			}
			if (v instanceof EditText) {
				TextView tv = (TextView) v;
				if (tv.hasFocus()) {
					setFocusedView(tv);
				}
				replaceEditTextListeners(tv);
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeException(ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(ex, "unknown description");
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
	public boolean replaceTouchListener(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		
		// if an ancestor has a click listener, then this view should not have one.
		if (!RecordOnTouchListener.hasAncestorListenedToTouch(v)) {
			View.OnTouchListener originalTouchListener = ListenerIntercept.getTouchListener(v);
			
			// only install the click listener if the view has overridden the onTouch() method, or has an OnTouchListener()
			// or it's a primitive widget.
			if (RecordOnTouchListener.hasOverriddenOnTouchMethod(v) || (originalTouchListener != null) || (!(v instanceof ViewGroup))) {
				if (!(originalTouchListener instanceof RecordOnTouchListener)) {
					RecordOnTouchListener recordTouchListener = new RecordOnTouchListener(mEventRecorder, originalTouchListener);
					v.setOnTouchListener(recordTouchListener);
					return true;
				}
			}
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
	public boolean replaceClickListener(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		View.OnClickListener originalClickListener = ListenerIntercept.getClickListener(v);
		
		// if the view has a click listener, or if it's a primitive widget and none of its ancestors have defined
		// a click listener, and it is not a child of an adapter, or the adapter doesn't have any listeners of
		// its own, then we can record the click
		AdapterView adapterView = (AdapterView) TestUtils.getDescendantOfClass(v, AdapterView.class);
		if ((originalClickListener != null) || 
			(!(v instanceof ViewGroup) && !RecordOnClickListener.hasAncestorListenedToClick(v))) {
			if ((adapterView == null) || !TestUtils.adapterHasListeners(adapterView)) {
				if (!(originalClickListener instanceof RecordOnClickListener)) {
					RecordOnClickListener recordClickListener = new RecordOnClickListener(mEventRecorder, originalClickListener);
					v.setOnClickListener(recordClickListener);
					Log.i(TAG, "setting click listener for " + v + " " + RecordListener.getDescription(v));
					return true;
				}
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
	public boolean replaceLongClickListener(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {	
		// if an ancestor has a click listener, then this view should not have one.
		if (!RecordOnLongClickListener.hasAncestorListenedToLongClick(v)) {
			View.OnLongClickListener originalLongClickListener = ListenerIntercept.getLongClickListener(v);
			
			// we do a null check here, because LongClick isn't handled by an overridden view method,
			// only with a listener class
			if ((originalLongClickListener != null) && !(originalLongClickListener instanceof RecordOnLongClickListener)) {
				RecordOnLongClickListener recordLongClickListener = new RecordOnLongClickListener(mEventRecorder, originalLongClickListener);
				v.setOnLongClickListener(recordLongClickListener);
				return true;
			}
		}
		return false;		
	}

	/**
	 * replace the key listener for this view (NOTE: is this just the hardware key?
	 * 
	 * @param v
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public boolean replaceKeyListener(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {	
		View.OnKeyListener originalKeyListener = ListenerIntercept.getKeyListener(v);
		if ((originalKeyListener != null) && !(originalKeyListener instanceof RecordOnKeyListener)) {
			RecordOnKeyListener recordKeyListener = new RecordOnKeyListener(mEventRecorder, originalKeyListener);
			v.setOnKeyListener(recordKeyListener);
			return true;
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
	public boolean replaceHierarchyChangeListener(ViewGroup vg) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {	
		ViewGroup.OnHierarchyChangeListener originalHierarchyChangeListener = ListenerIntercept.getOnHierarchyChangeListener(vg);
		if (!(originalHierarchyChangeListener instanceof InterceptOnHierarchyChangeListener)) {
			InterceptOnHierarchyChangeListener interceptHierarchyChangeListener = new InterceptOnHierarchyChangeListener(mEventRecorder, this, originalHierarchyChangeListener);
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
	 * replace the webView client with our recorder
	 * @param v a webview, actually
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceWebViewListeners(View v) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		WebView webView = (WebView) v;
		WebViewClient originalWebViewClient = ListenerIntercept.getWebViewClient(webView);
		if (!(originalWebViewClient instanceof RecordWebViewClient)) {
			RecordWebViewClient recordWebViewClient = new RecordWebViewClient(mEventRecorder, originalWebViewClient);
			webView.setWebViewClient(recordWebViewClient);
		}
	}
	
	/**
	 * replace the listeners in an atomic view (like a button or a text view, and stuff like that)
	 * @param v view to intercept
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceViewListeners(View v) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		replaceClickListener(v);
		replaceLongClickListener(v);
		if (TestUtils.listenMotionEvents(mMotionEventViews, v)) {
			replaceTouchListener(v);
		}
		// replaceKeyListener(v);
	}
	
	/**
	 * replace the listeners in a PopupMenu
	 * @param v popup menu
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replacePopupMenuListeners(View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		PopupMenu.OnMenuItemClickListener originalMenuItemClickListener = ListenerIntercept.getPopupMenuOnMenuItemClickListener(v);
		if (!(originalMenuItemClickListener instanceof RecordPopupMenuOnMenuItemClickListener)) {
			ListenerIntercept.setPopupMenuOnMenuItemClickListener(v, new RecordPopupMenuOnMenuItemClickListener(mEventRecorder, v)); 
		}
	}

	/**
	 * replace the listeners in a seekbar
	 * @param seekBar seekbar/progress listener, etc
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceSeekBarListeners(SeekBar seekBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		SeekBar.OnSeekBarChangeListener originalSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekBar);
		if ((originalSeekBarChangeListener != null) && !(originalSeekBarChangeListener instanceof RecordSeekBarChangeListener)) {
			RecordSeekBarChangeListener recordSeekbarChangeListener = new RecordSeekBarChangeListener(mEventRecorder, seekBar);
			seekBar.setOnSeekBarChangeListener(recordSeekbarChangeListener);
		}
	}
	
	/**
	 * replace the listeners for click and item selected for a spinner.
	 * @param spinner
	 */
	public void replaceSpinnerListeners(Spinner spinner) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		AdapterView.OnItemSelectedListener originalSelectedItemListener = ListenerIntercept.getItemSelectedListener(spinner);
		if (!(originalSelectedItemListener instanceof RecordOnItemSelectedListener)) {
			RecordOnItemSelectedListener recordItemSelectedListener = new RecordOnItemSelectedListener(mEventRecorder, originalSelectedItemListener);
			spinner.setOnItemSelectedListener(recordItemSelectedListener);
		}
	}
	
	public void replaceTabHostListeners(TabHost tabHost) throws IllegalAccessException, NoSuchFieldException {
		TabHost.OnTabChangeListener originalTabChangeListener = ListenerIntercept.getTabChangeListener(tabHost);
		if (!(originalTabChangeListener instanceof RecordOnTabChangeListener)) {
			RecordOnTabChangeListener recordOnTabChangeListener = new RecordOnTabChangeListener(mEventRecorder, originalTabChangeListener, tabHost);
			tabHost.setOnTabChangedListener(recordOnTabChangeListener);
		}
	}
	/**
	 * replace the listeners for item click, list scroll, and item select if its a spinner
	 * @param absListView list view to intercept
	 * @throws IllegalAccessException ReflectionUtils exceptions
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundExceptions
	 */
	public void replaceAdapterViewListeners(AdapterView adapterView) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		AdapterView.OnItemClickListener itemClickListener = adapterView.getOnItemClickListener();
		if (!(itemClickListener instanceof RecordOnItemClickListener)) {
			RecordOnItemClickListener recordItemClickListener = new RecordOnItemClickListener(mEventRecorder, adapterView);
			adapterView.setOnItemClickListener(recordItemClickListener);		
		}	
		if (adapterView instanceof AbsListView) {
			AbsListView absListView = (AbsListView) adapterView;
			AbsListView.OnScrollListener originalScrollListener = ListenerIntercept.getScrollListener(absListView);
			if (!(originalScrollListener instanceof RecordOnScrollListener)) {
				RecordOnScrollListener recordScrollListener = new RecordOnScrollListener(mEventRecorder, originalScrollListener);
				absListView.setOnScrollListener(recordScrollListener);
			}
		}
	}
	
	/**
	 * ExpandableListViews are different from list views. They ignore onItemClickListener, but they have an onGroupClickListener
	 * and an onChildClickListener
	 */
	public void replaceExpandableListVewListeners(ExpandableListView expandableListView) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		ExpandableListView.OnChildClickListener onChildClickListener = ListenerIntercept.getOnChildClickListener(expandableListView);
		if (!(onChildClickListener instanceof RecordOnChildClickListener)) {
			RecordOnChildClickListener recordOnChildClickListner = new RecordOnChildClickListener(mEventRecorder, expandableListView);
			expandableListView.setOnChildClickListener(recordOnChildClickListner);
		}
		ExpandableListView.OnGroupClickListener onGroupClickListener = ListenerIntercept.getOnGroupClickListener(expandableListView);
		if (!(onGroupClickListener instanceof RecordOnGroupClickListener)) {
			RecordOnGroupClickListener recordOnGroupClickListener = new RecordOnGroupClickListener(mEventRecorder, expandableListView);
			expandableListView.setOnGroupClickListener(recordOnGroupClickListener);
		}
	}
		
		
	/**
	 * replace the text watcher and focus change listeners for a text view (actually EditText, but we're not picky)
	 * @param tv TextView
	 * @throws IllegalAccessException ReflectionUtils Exceptions
	 * @throws NoSuchFieldException
	 */
	public void replaceEditTextListeners(TextView tv) throws IllegalAccessException, NoSuchFieldException {
		ArrayList<TextWatcher> textWatcherList = ListenerIntercept.getTextWatcherList(tv);
		if (textWatcherList == null) {
			textWatcherList = new ArrayList<TextWatcher>();
		}
		// make sure that we haven't already added the intercepting text watcher.
		if (!ListenerIntercept.containsTextWatcher(textWatcherList, RecordTextChangedListener.class)) {
			textWatcherList.add(0, new RecordTextChangedListener(mEventRecorder, tv));
			textWatcherList.add(new FinishTextChangedListener(mEventRecorder));
			ListenerIntercept.setTextWatcherList(tv, textWatcherList);
		}
		// add listener for focus
		View.OnFocusChangeListener originalFocusChangeListener = tv.getOnFocusChangeListener();
		if (!(originalFocusChangeListener instanceof RecordOnFocusChangeListener)) {
			View.OnFocusChangeListener recordFocusChangeListener = new RecordOnFocusChangeListener(mEventRecorder, this, originalFocusChangeListener);
			tv.setOnFocusChangeListener(recordFocusChangeListener);
		}
	}
	
	/**
	 * recursively set the intercepting listeners for touch/key/textwatcher events through the view tree
	 * @param v view to recurse from.
	 */
	public void intercept(View v) {
		// TEMPORARY
		replaceListeners(v);
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
					View vChild = vg.getChildAt(iChild);
					intercept(vChild);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeException(ex, description);
				ex.printStackTrace();
			} catch (Exception exlog) {
				mEventRecorder.writeException(exlog , " unknown description");
			}
		}
	}
	
	/**
	 * the action bar is not in the activity contentView, so it has to be handled separately
	 * @param activity activity to intercept
	 * @param actionBar actionBar
	 */
	public void intercept(Activity activity, ActionBar actionBar) {
        if (actionBar != null) {
        	try {
	        	View contentView = null;
	        	try {
	        		Class actionBarImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL);
	        		contentView = (View) ReflectionUtils.getFieldValue(actionBar, actionBarImplClass, Constants.Fields.CONTAINER_VIEW);
	        	} catch (Exception ex) {
	        		mEventRecorder.writeException(ex, "while intercepting the action bar for " + activity.getClass().getName());
	        	}
	        	if (contentView != null) {
	            	intercept(contentView);
	            }
	       		InterceptActionBar.interceptActionBarTabListeners(mEventRecorder, actionBar);
		       	if (actionBar.getCustomView() != null) {
		        	intercept(actionBar.getCustomView());
		        }
		       	intercept(InterceptActionBar.getActionBarView(actionBar));
        	} catch (Exception ex) {
        		mEventRecorder.writeException(ex, "while intercepting action bar");
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
        
        // create a hashcode which we can quickly test later, since we don't want to insert recorders just when
        // the layout has changed, but only when views have been added or removed from the view hierarchy
        mHashCode = viewTreeHashCode(v);
        
        // good stroke of luck, we can listen to layout events on the root view.
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a));
        setFocusedView(null);
        intercept(v);     
        ActionBar actionBar = a.getActionBar();
        if (actionBar != null) {
        	try {
	        	View actionBarView = InterceptActionBar.getActionBarView(actionBar);
	            ViewTreeObserver viewTreeObserverActionBar = actionBarView.getViewTreeObserver();
	            viewTreeObserverActionBar.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a));
	            View customView = actionBar.getCustomView();
	            if (customView != null) {
		            ViewTreeObserver viewTreeObserverActionBarCustomView = customView.getViewTreeObserver();
		            viewTreeObserverActionBarCustomView.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a));	            	
	            }
        	} catch (Exception ex) {
        		Log.d(TAG, "failed to intercept action bar");
        	}
        	intercept(a, actionBar);
        }
       
 	}
	
	/**
	 * when we receive a layout event, set up the record listeners on the view hierarchy.
	 */
	public class OnLayoutInterceptListener implements ViewTreeObserver.OnGlobalLayoutListener {
		protected Activity mActivity;
		protected int mCurrentRotation;
		
		public OnLayoutInterceptListener(Activity activity) {
			mActivity = activity;
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			mCurrentRotation = display.getRotation();
		}
		
		public void onGlobalLayout() {
			// this actually returns our magic frame, which doesn't resize when the IME is displayed
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			int newRotation = display.getRotation();
			if (newRotation != mCurrentRotation) {
				mEventRecorder.writeRotation(mActivity, newRotation);
				mCurrentRotation = newRotation;
			}
	        
	        // recursively generate the hashcode for this view hierarchy, and re-intercept if it's changed.
			int hashCode = viewTreeHashCode(mActivity.getWindow().getDecorView());
			if (hashCode != mHashCode) {
				intercept(mActivity.getWindow().getDecorView());
				mHashCode = hashCode;
			}
			
			// do the action bar, since it doesn't seem to get populated until after the activity was created/resumed

			ActionBar actionBar = mActivity.getActionBar();
	        intercept(mActivity, actionBar);   
		}
	}

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptPopupWindow(PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
				RecordPopupWindowOnDismissListener recordOnDismissListener = new RecordPopupWindowOnDismissListener(mEventRecorder, this, null, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
			View contentView = popupWindow.getContentView();
			if (ListenerIntercept.isPopupMenu(contentView)) {
				replacePopupMenuListeners(contentView);
			} else {
				// we are assuming this is a spinner, which is probably not the best thing.
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "Intercepting popup window");
		}
	}

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptSpinnerPopupWindow(PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
				View anchorView = TestUtils.getPopupWindowAnchor(popupWindow);
				RecordSpinnerPopupWindowOnDismissListener recordOnDismissListener = new RecordSpinnerPopupWindowOnDismissListener(mEventRecorder, this, anchorView, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "Intercepting popup window");
		}
	}
	
	

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptAutocompleteDropdown(PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordAutoCompleteDropdownOnDismissListener.class)) {
				View anchorView = TestUtils.getPopupWindowAnchor(popupWindow);
				RecordAutoCompleteDropdownOnDismissListener recordOnDismissListener = new RecordAutoCompleteDropdownOnDismissListener(mEventRecorder, this, anchorView, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "Intercepting popup window");
		}
	}
	/**
	 * intercept the contents of a dialog.
	 * @param dialog
	 */
	public void interceptDialog(Dialog dialog) {
		try {
			boolean fCancelAndDismissTaken = ListenerIntercept.isCancelAndDismissTaken(dialog);
	           DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
	            if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
	                // TODO: ?
	                RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(mEventRecorder, this, originalDismissListener);
	                if (fCancelAndDismissTaken) {
	                    ListenerIntercept.setOnDismissListener(dialog, recordOnDismissListener);
	                } else {
	                    dialog.setOnDismissListener(recordOnDismissListener);
	                }
	            }
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(mEventRecorder, originalCancelListener);
				// TODO: ?
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnCancelListener(dialog, recordOnCancelListener);
				} else {
					dialog.setOnCancelListener(recordOnCancelListener);
				}
			}
			Window window = dialog.getWindow();
			intercept(window.getDecorView());
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				mEventRecorder.writeException(ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(ex, " unknown description");
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
	public void interceptSpinnerDialog(Dialog dialog) {
		try {
			boolean fCancelAndDismissTaken = ListenerIntercept.isCancelAndDismissTaken(dialog);
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				// TODO: ?
				RecordSpinnerDialogOnDismissListener recordSpinnerOnDismissListener = new RecordSpinnerDialogOnDismissListener(mEventRecorder, this, originalDismissListener);
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnDismissListener(dialog, recordSpinnerOnDismissListener);
				} else {
					dialog.setOnDismissListener(recordSpinnerOnDismissListener);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				mEventRecorder.writeException(ex, description);
			} catch (Exception exlog) {
				mEventRecorder.writeException(ex, "unknown description");
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
		protected View mView;
		
		public InterceptViewRunnable(View v) {
			mView = v;
		}
		
		public void run() {
			ViewInterceptor.this.intercept(mView);
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
	 * if we're calling from the UI thread
	 * @param runnable
	 */
	public void runDeferred(Activity activity, Runnable runnable) {
		Thread backgroundThread = new Thread(new RunUIQueuedRunnable(activity, runnable));
		backgroundThread.start();
	}
	
	/**
	 * variant which takes a view, rather than an activity, since an activity may not be in scope.s
	 * @param v
	 * @param runnable
	 */
	public boolean runDeferred(View v, Runnable runnable) {
		Context c = v.getContext();
		if (c instanceof Activity) {
			Activity a = (Activity) c;
			runDeferred(a, runnable);
			return true;
		} else {
			Log.e(TAG, "run deferred called from view not part of an activity " + v);
			return false;
		}
	}
 }
