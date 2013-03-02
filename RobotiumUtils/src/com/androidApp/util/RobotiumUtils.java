package com.androidApp.util;

import java.util.ArrayList;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.TestCase;
import android.app.Activity;
import android.view.View;
import android.widget.ListView;


public class RobotiumUtils {
	protected static int ACTIVITY_TIMEOUT_MSEC = 1000;
	
	public static View getListViewItem(ListView lv, int itemIndex) {
		return lv.getChildAt((itemIndex - 1) - lv.getFirstVisiblePosition());
	}
	
	/**
	 * wait for the list, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param listViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return list of TextViews in the list
	 */
	public static ArrayList<android.widget.TextView> waitAndClickInListByClassIndex(Solo solo, int listViewIndex, int itemIndex) {
		android.widget.ListView listView = (android.widget.ListView) solo.getView(android.widget.ListView.class, listViewIndex);
		TestCase.assertTrue(solo.waitForView(listView));
		android.view.View listViewItem = RobotiumUtils.getListViewItem(listView, itemIndex);
		TestCase.assertTrue(solo.waitForView(listViewItem));
		
		// click item Seeking
		int visibleIndex = itemIndex - listView.getFirstVisiblePosition();
		ArrayList<android.widget.TextView> textViewList = solo.clickInList(visibleIndex, listViewIndex);
		return textViewList;
	}
	/**
	 * wait for the list, then wait for the specified view, then click on it.
	 * @param solo robotium handle
	 * @param listViewIndex index of the list view in the array of list views in the android layout
	 * @param itemIndex absolute index of the item 
	 * @return list of TextViews in the list
	 */
	public static ArrayList<android.widget.TextView> waitAndClickInListById(Solo solo, int listViewId, int itemIndex) {
		android.widget.ListView listView = (android.widget.ListView) solo.getView(listViewId);
		TestCase.assertTrue(solo.waitForView(listView));
		android.view.View listViewItem = RobotiumUtils.getListViewItem(listView, itemIndex);
		TestCase.assertTrue(solo.waitForView(listViewItem));
		
		// click item Seeking
		ArrayList<android.widget.TextView> textViewList = solo.clickInList(itemIndex, itemIndex - listView.getFirstVisiblePosition());
		return textViewList;
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
			boolean f = solo.waitForActivity(newActivityClass.getSimpleName(), ACTIVITY_TIMEOUT_MSEC);
			newActivity = solo.getCurrentActivity();
			if (newActivity.getClass().equals(newActivityClass) && !newActivity.equals(currentActivity)) {
				return true;
			}
			timeoutMsec -= ACTIVITY_TIMEOUT_MSEC;
			
			// if the activity class was the same, then waitForActivity returns immediately, so we do the sleep for it
			if (newActivity.getClass().equals(newActivityClass)) {
				try {
					Thread.sleep(ACTIVITY_TIMEOUT_MSEC);
				} catch (InterruptedException iex) {}
			}
		}
		return false;		
	}
}
