package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

/**
 * listen to scroll events and record them.  But only record ones from the user.
 * @author Matthew
 *
 */
public class RecordOnScrollListener extends RecordListener implements AbsListView.OnScrollListener {
	private static final String 			TAG = "RecordOnScrollListener";
	protected AbsListView.OnScrollListener 	mOriginalOnScrollListener;
	protected int							mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
	
	public RecordOnScrollListener(EventRecorder eventRecorder, AbsListView.OnScrollListener originalOnScrollListener) {
		super(eventRecorder);
		mOriginalOnScrollListener = originalOnScrollListener;
	}
	
	public RecordOnScrollListener(EventRecorder eventRecorder, AbsListView listView) {
		super(eventRecorder);
		try {
			mOriginalOnScrollListener = ListenerIntercept.getScrollListener(listView);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				if (mScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// list views only use class-index references because robotium doesn't support
					// scrolling with a ScrollView Specified.
					String description = getDescriptionByClassIndex(view);
					String logString = firstVisibleItem + "," + visibleItemCount + "," + 
					   					totalItemCount + "," + ViewReference.getClassIndexReference(view) + "," + description;
					mEventRecorder.writeRecord(Constants.EventTags.SCROLL, logString);
					if (mOriginalOnScrollListener != null) {
						mOriginalOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					} 
				}
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, view, "on scroll");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
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
