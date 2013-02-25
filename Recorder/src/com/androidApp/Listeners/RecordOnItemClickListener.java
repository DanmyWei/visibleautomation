package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

// record item clicks for listviews
public class RecordOnItemClickListener extends RecordListener implements AdapterView.OnItemClickListener {
	protected EventRecorder						mEventRecorder;
	protected AdapterView<?>					mAdapterView;
	protected AdapterView.OnItemClickListener	mOriginalItemClickListener;
	
	public RecordOnItemClickListener(EventRecorder eventRecorder, AdapterView<?> adapterView) {
		mEventRecorder = eventRecorder;
		mAdapterView = adapterView;
		mOriginalItemClickListener = adapterView.getOnItemClickListener();
	}
		
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(view);
			String logString = Constants.EventTags.ITEM_CLICK + ":" + time + ", "+ position + "," + mEventRecorder.getViewReference().getClassIndexReference(parent) + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalItemClickListener != null) {
			mOriginalItemClickListener.onItemClick(parent, view, position, id);
		} 
	}
}
