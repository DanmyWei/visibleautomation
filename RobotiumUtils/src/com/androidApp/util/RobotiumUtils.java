package com.androidApp.util;

import java.util.ArrayList;

import com.jayway.android.robotium.solo.Solo;

import junit.framework.TestCase;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;

/**
 * utility class for monitoring activities and sending events to views.
 * TODO: we need to change the wait routines so they're instantiated before the events that they depend on are fired.
 * All the wait objects need to be created on a background thread, which will fire a callback when the waitObject is 
 * notified.  The API thread will have an external wait call, which will return immediately if the callback has been 
 * fired, or wait for a notification if it hasn't been fired yet. This will improve the performance of the tests, and their 
 * roboustness.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RobotiumUtils {
	protected static int ACTIVITY_POLL_INTERVAL_MSEC = 1000;			// interval for activity existence polling
	protected static int VIEW_TIMEOUT_MSEC = 5000;						// time to wait for view to be visible
	protected static int VIEW_POLL_INTERVAL_MSEC = 1000;				// poll interval for view existence
	protected static int WAIT_INCREMENT_MSEC = 100;						// poll interval for wait timers.
	protected static int WAIT_SCROLL_MSEC = 2000;						// wait at most this long for a scroll to complete
	protected static ActivityMonitorRunnable	sActivityMonitorRunnable = null;
	protected Instrumentation					mInstrumentation;

	/**
	 * This has to be called before getActivity(), so it can intercept the first activity.
	 * @param instrumentation
	 */
	public RobotiumUtils(Instrumentation instrumentation) {
		mInstrumentation = instrumentation;
		sActivityMonitorRunnable = new ActivityMonitorRunnable(instrumentation);
		Thread activityMonitorThread = new Thread(sActivityMonitorRunnable);
		activityMonitorThread.start();
	}
	
	// get a list view item.  
	public static View getAdapterViewItem(AdapterView av, int itemIndex) {
		return av.getChildAt((itemIndex - 1) - av.getFirstVisiblePosition());
	}
	
	// wrapper for sleep
	static void sleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
			
		}
	}
	
	
	/**
	 * wait for a adapter view item to become visible.
	 * @param adpaterView list view containing item
	 * @param itemIndex index of item
	 * @param waitMsec max wait time in milliseconds
	 * @return
	 */
	public boolean waitForAdaperViewItem(Solo solo, AdapterView adapterView, int itemIndex, int waitMsec) {
		android.view.View adapterViewItem = null; 
		while ((adapterViewItem == null) && (waitMsec > 0)) {
			adapterViewItem = RobotiumUtils.getAdapterViewItem(adapterView, itemIndex);
			if (adapterViewItem != null) {
				if (solo.waitForView(adapterViewItem)) {
					return true;
				}
			} 
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}	
		return false;
	}
	/**
	 * wait for the adapter view, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param adapterViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return list of TextViews in the list
	 */
	public boolean waitAndClickInAdapterByClassIndex(Solo solo, int adapterViewIndex, int itemIndex) {
		boolean foundView = solo.waitForView(android.widget.AdapterView.class, adapterViewIndex + 1, VIEW_TIMEOUT_MSEC);
		if (!foundView) {
			return false;
		}
		android.widget.AdapterView adapterView = (android.widget.AdapterView) solo.getView(android.widget.AdapterView.class, adapterViewIndex);
		scrollToViewVisible(solo, adapterView);
		int waitMsec = VIEW_TIMEOUT_MSEC;
		android.view.View adapterViewItem = null; 
		while ((adapterViewItem == null) && (waitMsec > 0)) {
			adapterViewItem = RobotiumUtils.getAdapterViewItem(adapterView, itemIndex);
			if (adapterViewItem != null) {
				if (solo.waitForView(adapterViewItem)) {
					break;
				}
			} 
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}	
		if (waitMsec > 0) {
			int visibleIndex = itemIndex - adapterView.getFirstVisiblePosition();
			solo.clickOnView(adapterViewItem);
			return true;
		} else {
			return false;
		}
	}
	/**
	 * wait for the adapter, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param adapterViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return true if selected, false if timeout
	 */
	public static boolean waitAndClickInAdapterById(Solo solo, int adapterViewId, int itemIndex) {
		android.widget.AdapterView adapterView = null;
		int waitMsec = VIEW_TIMEOUT_MSEC;
		while ((adapterView == null) && (waitMsec > 0)) {
			adapterView = (android.widget.AdapterView) solo.getView(adapterViewId);
			if (adapterView != null) {
				TestCase.assertTrue(solo.waitForView(adapterView));
				break;
			}
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}
		waitMsec = VIEW_TIMEOUT_MSEC;
		android.view.View adapterViewItem = null; 
		while ((adapterViewItem == null) && (waitMsec > 0)) {
			adapterViewItem = RobotiumUtils.getAdapterViewItem(adapterView, itemIndex);
			if (adapterViewItem != null) {
				if (solo.waitForView(adapterViewItem)) {
					break;
				}
			} 
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}
		if (waitMsec > 0) {
			// click item Seeking
			solo.clickOnView(adapterViewItem);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * sometimes, the application will navigate from one activity to another, but the activities have the same class.
	 * robotium only supports waitForActivity(className), which can fire on the current activity due to a race condition
	 * before the operation which navigates to a new activity, we save the activity, and test against it with the new
	 * activity. This was written to handle a special case in ApiDemos, where new activities had the same class as the
	 * current activity
	 * @param currentActivity activity before the event
	 * @param newActivityClass class of the new activity (which should match currentActivity.class, but we pass it anyway
	 * @param timeoutMsec timeout in millisecond
	 * @return true if a new activity of the specified class has been created
	 */
	public static boolean waitForNewActivity(Activity currentActivity, Class<? extends Activity> newActivityClass, long timeoutMsec) {
		long startTimeMillis = SystemClock.uptimeMillis();
		long currentTimeMillis = startTimeMillis;
		do {
			Activity newActivity = sActivityMonitorRunnable.waitForActivity(newActivityClass, timeoutMsec);
			if ((newActivity != currentActivity) || (currentActivity == null)) {
				return true;
			}
			ActivityMonitorRunnable.sleep(ActivityMonitorRunnable.MINISLEEP);
			currentTimeMillis = SystemClock.uptimeMillis();
		} while (currentTimeMillis - startTimeMillis < timeoutMsec);
		return false;
	}
	
	/**
	 * public function to wait for an activity from the activity monitor background thread.
	 * @param newActivityClass activity class to match.
	 * @param timeoutMsec timeout in milliseconds
	 * @return true if matching activity was found, false otherwise.
	 */
	public static boolean waitForActivity(Class<? extends Activity> newActivityClass, long timeoutMsec) {
		return sActivityMonitorRunnable.waitForActivity(newActivityClass, timeoutMsec) != null;
	}

	/**
	 * public function to return the current activity from the activity monitor background thread.
	 * @return current activity
	 */
	public static Activity getCurrentActivity() {
		return sActivityMonitorRunnable.getCurrentActivity();
	}
	
	/**
	 * public function to return the previous activity from the activity monitor background thread
	 * @return previos activity
	 */
	public static Activity getPreviousActivity() {
		return sActivityMonitorRunnable.getPreviousActivity();
	}
	

	/**
	 * get the leastmost scrolling container for this view: NOTE: this may not be the container we're looking for,
	 * since the view may be off the screen horizontally, and the container may only scroll vertically.
	 * @param v
	 * @return
	 */
	public static ScrollView getScrollingContainer(View v) {
		ViewParent vp = v.getParent();
		while (vp != null) {
			if (vp instanceof ScrollView) {
				return (ScrollView) vp;
			}
			vp = vp.getParent();
			if ((vp == null) || (vp.getParent() == null)) {
				return null;
			}
		}
		return null;
	}
	
	/** 
	 * return the rectangle of a view relative to its ancestor
	 */
	public static Rect getRelativeRect(ScrollView svAncestor, View view) {
		Rect r = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		ViewParent vp = view.getParent();
		while (vp != svAncestor) {
			r.offset(((View) vp).getLeft(), ((View) vp).getTop());
			vp = vp.getParent();
		}
		r.offset(-svAncestor.getScrollX(), -svAncestor.getScrollY());
		return r;
	}
	
	/**
	 * call on the activity ui thread to scroll a view until it's visible, so we can click or do something to it
	 */
	public class ScrollViewRunnable implements Runnable {
		protected View mView;
		protected int mXScroll;
		protected int mYScroll;
		
		public ScrollViewRunnable(View view, int xScroll, int yScroll) {
			mView = view;
			mXScroll = xScroll;
			mYScroll = yScroll;
		}
		public void run() {
			mView.scrollBy(mXScroll, mYScroll);
		}
	}

	/**
	 * scroll the ancestor of the view until the view is fully visible.
	 * @param v view to scroll into view.
	 */
	public void scrollToViewVisible(Solo solo, View v) {
		ScrollView scrollingContainer = RobotiumUtils.getScrollingContainer(v);
		if (scrollingContainer != null) {
			Rect r = RobotiumUtils.getRelativeRect(scrollingContainer, v);
			int scrollVertical = 0;
			int scrollHorizontal = 0;
			if (r.top < 0) {
				scrollVertical = r.top;
			} else if (r.bottom > scrollingContainer.getMeasuredHeight()) {
				scrollVertical = r.bottom - scrollingContainer.getMeasuredHeight();
			}
			if (r.left < 0) {
				scrollHorizontal = r.left;
			} else if (r.right > scrollingContainer.getMeasuredWidth()) {
				scrollHorizontal = r.right - scrollingContainer.getMeasuredWidth();
			}
			if ((scrollVertical != 0) || (scrollHorizontal != 0)) {
				int originalScrollX = scrollingContainer.getScrollX();
				int originalScrollY = scrollingContainer.getScrollY();
				Runnable runnable = new ScrollViewRunnable(scrollingContainer, scrollHorizontal, scrollVertical);
				mInstrumentation.runOnMainSync(runnable);
				int timeout = WAIT_SCROLL_MSEC;
				while (timeout > 0) {
					int currentScrollX = scrollingContainer.getScrollX();
					int currentScrollY = scrollingContainer.getScrollY();
					if ((currentScrollX == originalScrollX + scrollHorizontal) && (currentScrollY == originalScrollY + scrollVertical)) {
						break;
					}
					sleep(WAIT_INCREMENT_MSEC);
					timeout -= WAIT_INCREMENT_MSEC;
				}
			}
		}
	}
	
	/**
	 * unfortunately, views are often scrolled off the screen, so we often need to scroll them back to 
	 * click on them.
	 * @param solo handle to robotium
	 * @param v view to click on
	 */
	public boolean clickOnViewAndScrollIfNeeded(Solo solo, View v) {
		scrollToViewVisible(solo, v);
		solo.clickOnView(v, true);
		return true;
	}
	
	/** 
	 * sending the back key while the IME is up hides the IME, rather than exiting the activity, so
	 * things have a tendency to get out of sync.  We detect show and hide IME events and perform
	 * them explicitly
	 * @param v view that the IME is being shown for.
	 */
	public static void showIME(View v) {
		if (v != null) {
	        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(v, 0);
	        // displaying and hiding the input method manager causes a re-layout that we need to wait for, since later events
	        // may get sent with view dimensions before the layout, but after the layout has occurred.
	        sActivityMonitorRunnable.getCurrentLayoutListener().waitForLayout(VIEW_TIMEOUT_MSEC);
		}
	}
	
	/**
	 * see comment above.
	 * @param v
	 */
	public static void hideIME(View v) {
		if (v != null) {
	        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	        sActivityMonitorRunnable.getCurrentLayoutListener().waitForLayout(VIEW_TIMEOUT_MSEC);
		}
	}
	
	
	/**
	 * wait for a layout
	 * @param timeoutMsec - time to wait for a layout.
	 * 
	 */
	public static void waitForLayout(long timeoutMsec) {
		OnLayoutInterceptListener currentLayoutListener = sActivityMonitorRunnable.getCurrentLayoutListener();
		currentLayoutListener.waitForLayout(timeoutMsec);
	}

	/**
	 * create a runnable which dismisses the auto complete dropdown
	 * @param solo
	 * @param autoCompleteTextView
	 */
	public void dismissAutoCompleteDialog(AutoCompleteTextView autoCompleteTextView) {
		mInstrumentation.runOnMainSync(new DismissAutoCompleteRunnable(autoCompleteTextView));
	}
	
	public class DismissAutoCompleteRunnable implements Runnable {
		public AutoCompleteTextView mAutoCompleteTextView;
		
		public DismissAutoCompleteRunnable(AutoCompleteTextView autoCompleteTextView) {
			mAutoCompleteTextView = autoCompleteTextView;
		}

		@Override
		public void run() {
			mAutoCompleteTextView.dismissDropDown();		
		}		
	}
	
	/**
	 * dismissing a popup has to be done on the UI thread via a runnable
	 * @author Matthew
	 *
	 */
	public class DismissPopupWindowRunnable implements Runnable {
		public PopupWindow mPopupWindow;
		
		public DismissPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		
		@Override
		public void run() {
			mPopupWindow.dismiss();
		}
	}
	
	public void dismissPopupWindow(Activity activity) throws TestException {
		dismissPopupWindow(activity, VIEW_TIMEOUT_MSEC);
	}
	
	public boolean waitForPopupWindow(Activity activity, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	/**
	 * dismiss a popup window, like a menu popup, autocomplete dropdown, etc.  I hope there is only one popup.
	 * @param activity
	 */
	public boolean dismissPopupWindow(Activity activity, long waitMsec) throws TestException {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				mInstrumentation.runOnMainSync(new DismissPopupWindowRunnable(popupWindow));
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	
	public static boolean verifyPopupWindowDimissed(Activity activity, PopupWindow dismissedPopupWindow) throws TestException {
		return verifyPopupWindowDismissed(activity, dismissedPopupWindow, VIEW_TIMEOUT_MSEC);
	}

	/**
	 * verify that there are no popups displayed.
	 * NOTE: What if another popup is displayed immediately?
	 * @param activity
	 * @param waitMsec
	 * @return
	 * @throws TestException
	 */
	public static boolean verifyPopupWindowDismissed(Activity activity, PopupWindow dismissedPopupWindow, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if ((popupWindow == null) || (dismissedPopupWindow != popupWindow)) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	
	/**
	 * select the indexed tab on the action bar
	 * @param activity activity to get the action bar from
	 * @param tabIndex index of the tab to select
	 * @throws TestException if there is no action bar to select a tab from
	 */
	public void selectActionBarTab(Activity activity, int tabIndex) throws TestException {
		ActionBar actionBar = activity.getActionBar();
		if (actionBar == null) {
			throw new TestException("selectActionBarTab: activity has no action bar");
		}
		mInstrumentation.runOnMainSync(new SetActionBarTabRunnable(actionBar, tabIndex));
	}
	
	/**
	 * runnable to select an action bar tab.
	 * @author Matthew
	 *
	 */
	public class SetActionBarTabRunnable implements Runnable {
		public ActionBar 	mActionBar;
		public int 			mIndex;
		
		public SetActionBarTabRunnable(ActionBar actionBar, int index) {
			mActionBar = actionBar;
			mIndex = index;
		}
		
		public void run() {
			ActionBar.Tab tab = mActionBar.getTabAt(mIndex);
			tab.select();
		}
	}
	
	/**
	 * extract the WebViewClient from WebView.mCallbackProxy.mWebViewClient
	 * @param webView
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	protected static WebViewClient getWebViewClient(WebView webView) throws NoSuchFieldException, SecurityException, IllegalAccessException, ClassNotFoundException {
		Class callbackProxyClass = Class.forName(Constants.Classes.WEBKIT_CALLBACK_PROXY);
		Object callbackProxy = ReflectionUtils.getFieldValue(webView, WebView.class, Constants.Fields.CALLBACK_PROXY);
		WebViewClient webViewClient = (WebViewClient) ReflectionUtils.getFieldValue(callbackProxy, callbackProxyClass, Constants.Fields.WEBVIEW_CLIENT);
		return webViewClient;
	}	
	
	public static void waitForPageToLoad(WebView webView, String url, long timeoutMsec) throws TestException {
		try {
			if (webView.getProgress() < 100) {
				WebViewClient originalWebViewClient = RobotiumUtils.getWebViewClient(webView);
				if (!(originalWebViewClient instanceof InterceptWebViewClient)) {
					InterceptWebViewClient interceptWebViewClient = new InterceptWebViewClient(originalWebViewClient);
					webView.setWebViewClient(interceptWebViewClient);
					interceptWebViewClient.waitForPageLoad(url, timeoutMsec);
				}
			}
		} catch (Exception ex) {
			throw new TestException(ex.getMessage());
		}
	}
	
	/** 
	 * wait for a dialog to appear.
	 * @param activity
	 * @param timeoutMsec
	 * @return
	 */
	public static Dialog waitForDialogToOpen(Activity activity, long timeoutMsec) {
		long startTimeMillis = SystemClock.uptimeMillis();
		long currentTimeMillis = startTimeMillis;
		while (timeoutMsec > 0) {
			Dialog dialog = ViewExtractor.findDialog(activity);
			if (dialog != null) {
				return dialog;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			timeoutMsec -= WAIT_INCREMENT_MSEC;
		}
		return null;
	}
}
