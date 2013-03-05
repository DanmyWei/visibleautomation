package com.androidApp.util;

import java.util.ArrayList;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.TestCase;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;


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
}
