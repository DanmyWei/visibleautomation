package com.androidApp.util;

import java.util.ArrayList;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.TestCase;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ScrollView;


public class RobotiumUtils {
	protected static int ACTIVITY_POLL_INTERVAL_MSEC = 1000;			// interval for activity existence polling
	protected static int VIEW_TIMEOUT_MSEC = 5000;						// time to wait for view to be visible
	protected static int VIEW_POLL_INTERVAL_MSEC = 1000;				// poll interval for view existence
	
	// get a list view item.  
	public static View getListViewItem(ListView lv, int itemIndex) {
		return lv.getChildAt((itemIndex - 1) - lv.getFirstVisiblePosition());
	}
	
	// wrapper for sleep
	static void sleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
			
		}
	}
	
	/**
	 * wait for the list, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param listViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return list of TextViews in the list
	 */
	public static ArrayList<android.widget.TextView> waitAndClickInListByClassIndex(Solo solo, int listViewIndex, int itemIndex) {
		boolean foundView = solo.waitForView(android.widget.ListView.class, listViewIndex + 1, VIEW_TIMEOUT_MSEC);
		if (!foundView) {
			Log.e("test", "test");
			TestCase.assertTrue(foundView);
		}
		android.widget.ListView listView = (android.widget.ListView) solo.getView(android.widget.ListView.class, listViewIndex);
		int waitMsec = VIEW_TIMEOUT_MSEC;
		android.view.View listViewItem = null; 
		while ((listViewItem == null) && (waitMsec > 0)) {
			listViewItem = RobotiumUtils.getListViewItem(listView, itemIndex);
			if (listViewItem != null) {
				if (solo.waitForView(listViewItem)) {
					break;
				}
			} 
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}	
		if (waitMsec > 0) {
			int visibleIndex = itemIndex - listView.getFirstVisiblePosition();
			ArrayList<android.widget.TextView> textViewList = solo.clickInList(visibleIndex, listViewIndex);
			return textViewList;
		} else {
			return null;
		}
	}
	/**
	 * wait for the list, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param listViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return list of TextViews in the list
	 */
	public static ArrayList<android.widget.TextView> waitAndClickInListById(Solo solo, int listViewId, int itemIndex) {
		android.widget.ListView listView = null;
		int waitMsec = VIEW_TIMEOUT_MSEC;
		while ((listView == null) && (waitMsec > 0)) {
			listView = (android.widget.ListView) solo.getView(listViewId);
			if (listView != null) {
				TestCase.assertTrue(solo.waitForView(listView));
				break;
			}
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}
		waitMsec = VIEW_TIMEOUT_MSEC;
		android.view.View listViewItem = null; 
		while ((listView == null) && (waitMsec > 0)) {
			listViewItem = RobotiumUtils.getListViewItem(listView, itemIndex);
			if (listViewItem != null) {
				if (solo.waitForView(listViewItem)) {
					break;
				}
			} 
			RobotiumUtils.sleep(VIEW_POLL_INTERVAL_MSEC);
			waitMsec -= VIEW_POLL_INTERVAL_MSEC;
		}
		if (waitMsec > 0) {
			// click item Seeking
			ArrayList<android.widget.TextView> textViewList = solo.clickInList(itemIndex, itemIndex - listView.getFirstVisiblePosition());
			return textViewList;
		} else {
			return null;
		}
	}

	/**
	 * sometimes, the application will navigate from one activity to another, but the activities have the same class.
	 * robotium only supports waitForActivity(className), which can fire on the current activity due to a race condition
	 * before the operation which navigates to a new activity, we save the activity, and test against it with the new
	 * activity. This was written to handle a special case in ApiDemos, where new activities had the same class as the
	 * current a
	 * @param solo robotium handle
	 * @param currentActivity activity before the event
	 * @param newActivityClass class of the new activity (which should match currentActivity.class, but we pass it anyway
	 * @param timeoutMsec timeout in millisecond
	 * @return true if a new activity of the specified class has been created
	 */
	public static boolean waitForNewActivity(Solo solo, Activity currentActivity, Class<? extends Activity> newActivityClass, int timeoutMsec) {
		Activity newActivity = null;
		while (timeoutMsec > 0) {
			boolean f = solo.waitForActivity(newActivityClass.getSimpleName(), ACTIVITY_POLL_INTERVAL_MSEC);
			newActivity = solo.getCurrentActivity();
			if (newActivity.getClass().equals(newActivityClass) && !newActivity.equals(currentActivity)) {
				return true;
			}
			timeoutMsec -= ACTIVITY_POLL_INTERVAL_MSEC;
			
			// if the activity class was the same, then waitForActivity returns immediately, so we do the sleep for it
			if (newActivity.getClass().equals(newActivityClass)) {
				try {
					Thread.sleep(ACTIVITY_POLL_INTERVAL_MSEC);
				} catch (InterruptedException iex) {}
			}
		}
		return false;		
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
			if (vp.getParent() == null) {
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
	public static void scrollToViewVisible(Solo solo, View v) {
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
				RobotiumUtils utils = new RobotiumUtils();
				Runnable runnable = utils.new ScrollViewRunnable(scrollingContainer, scrollHorizontal, scrollVertical);
				RobotiumUtils.blockOnRunOnUiThread(solo.getCurrentActivity(), runnable, VIEW_TIMEOUT_MSEC);
			}
		}
	}
	
	/**
	 * if we want to run anything in the application, we have to use Activity.runOnUiThread(), but it returns immediately, and we then
	 * probably send/expect something with the application immediately after, creating a recipe for a race condition.  Instead, we create
	 * a wrapper around the runnable, which does a wait object, then a notify when the runnable has completed
	 * @param activity to call runOnUiThread() 
	 * @param runnable our intended victim
	 * @param timeoutMsec time to wait before giving up
	 */
	public static void blockOnRunOnUiThread(Activity activity, Runnable runnable, long timeoutMsec) {
		Object waitObject = new Object();
		RobotiumUtils utils = new RobotiumUtils();
		Runnable waitRunnable = utils.new BlockOnRunnable(activity, runnable, waitObject);
		activity.runOnUiThread(waitRunnable);
		try {
			synchronized(waitObject) {
				waitObject.wait(timeoutMsec);
			}
		} catch (InterruptedException iex) {	
		}
	}
	
	/**
	 * wrapper for runnable which sends a notify() on completion
	 */
	public class BlockOnRunnable implements Runnable {
		protected Activity  mActivity;
		protected Runnable 	mRunnable;
		protected Object	mWaitObject;
		
		public BlockOnRunnable(Activity activity, Runnable runnable, Object waitObject) {
			mActivity = activity;
			mRunnable = runnable;
			mWaitObject = waitObject;
		}
		
		public void run() {
			mRunnable.run();
			synchronized(mWaitObject) {
				mWaitObject.notify();
			}
		}
	}
	
	/**
	 * unfortunately, views are often scrolled off the screen, so we often need to scroll them back to 
	 * click on them.
	 * @param solo handle to robotium
	 * @param v view to click on
	 */
	public static void clickOnViewAndScrollIfNeeded(Solo solo, View v) {
		RobotiumUtils.scrollToViewVisible(solo, v);
		solo.clickOnView(v, true);
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
		}
	}
}
