package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

/**
 * listen to scroll events and record them.  But only record ones from the user.
 * NOTE: This is specific to ListView, ScrollViews will have to be handled differently, unfortunately we'll have to 
 * handle the touch events, and scroll by hand, since there's no OnScrollListener for scroll views.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnScrollListener extends RecordListener implements AbsListView.OnScrollListener, IOriginalListener  {
	private static final String 			TAG = "RecordOnScrollListener";
	protected AbsListView.OnScrollListener 	mOriginalOnScrollListener;
	protected int							mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
	
	public RecordOnScrollListener(String activityName, EventRecorder eventRecorder, AbsListView.OnScrollListener originalOnScrollListener) {
		super(activityName, eventRecorder);
		mOriginalOnScrollListener = originalOnScrollListener;
	}
	
	public RecordOnScrollListener(String activityName, EventRecorder eventRecorder, AbsListView listView) {
		super(activityName, eventRecorder);
		try {
			mOriginalOnScrollListener = ListenerIntercept.getScrollListener(listView);
			listView.setOnScrollListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Object getOriginalListener() {
		return mOriginalOnScrollListener;
	}

	/**
	 * record the scroll event
	 * scroll:<time>,first_visible_item,visible_item_count,total_item_count,<reference>,<description>
	 * @param view list view being intercepted
	 * @param firstVisibleItem first visible item
	 * @param visibleItemCount number of visible items
	 * @param totalItemCount total # of items in the list view.
	 */
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (mScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// list views only use class-index references because robotium doesn't support
					// scrolling with a ScrollView Specified.
					String description = getDescriptionByClassIndex(view);
					String logString = firstVisibleItem + "," + visibleItemCount + "," + 
					   					totalItemCount + "," + ViewReference.getClassIndexReference(view) + "," + description;
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.SCROLL, logString);
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, " on scroll");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnScrollListener != null) {
				mOriginalOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			} 
			
		}
		setEventBlock(false);
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
			Log.i(TAG, "fling");
		} else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			Log.i(TAG, "touch");
		} else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			Log.i(TAG, "idle");
		}
	}
}
