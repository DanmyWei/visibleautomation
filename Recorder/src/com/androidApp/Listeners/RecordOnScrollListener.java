package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

/**
 * listen to scroll events and record them
 * @author Matthew
 *
 */
public class RecordOnScrollListener implements AbsListView.OnScrollListener {
	protected AbsListView.OnScrollListener 	mOriginalOnScrollListener;
	protected EventRecorder					mEventRecorder;
	
	public RecordOnScrollListener(EventRecorder eventRecorder, AbsListView.OnScrollListener originalOnScrollListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnScrollListener = originalOnScrollListener;
	}
	
	public RecordOnScrollListener(EventRecorder eventRecorder, AbsListView listView) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnScrollListener = ListenerIntercept.getScrollListener(listView);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.SCROLL + ":" + time + "," + firstVisibleItem + "," + visibleItemCount + "," + 
			   					totalItemCount + "," + mEventRecorder.getViewReference().getClassIndexReference(view);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnScrollListener != null) {
			mOriginalOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		} 
		
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
}
