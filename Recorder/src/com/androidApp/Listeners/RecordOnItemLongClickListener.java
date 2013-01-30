package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

public class RecordOnItemLongClickListener implements AdapterView.OnItemLongClickListener {
	protected EventRecorder							mEventRecorder;
	protected AdapterView<?>						mAdapterView;
	protected AdapterView.OnItemLongClickListener	mOriginalItemLongClickListener;
	
	public RecordOnItemLongClickListener(EventRecorder eventRecorder, AdapterView<?> adapterView) {
		mEventRecorder = eventRecorder;
		mAdapterView = adapterView;
		mOriginalItemLongClickListener = adapterView.getOnItemLongClickListener();
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.ITEM_LONG_CLICK + ":" + time + ", "+ position + "," + mEventRecorder.getViewReference().getClassIndexReference(parent);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalItemLongClickListener != null) {
			return mOriginalItemLongClickListener.onItemLongClick(parent, view, position, id);
		} else {
			return false;
		}
	}
}
