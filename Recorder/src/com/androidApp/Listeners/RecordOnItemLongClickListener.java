package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

// record long click listener
// TODO: this will handle the UI for waitForText() and extended record interface
public class RecordOnItemLongClickListener extends RecordListener implements AdapterView.OnItemLongClickListener {
	protected AdapterView<?>						mAdapterView;
	protected AdapterView.OnItemLongClickListener	mOriginalItemLongClickListener;
	
	public RecordOnItemLongClickListener(EventRecorder eventRecorder, AdapterView<?> adapterView) {
		super(eventRecorder);
		mAdapterView = adapterView;
		mOriginalItemLongClickListener = adapterView.getOnItemLongClickListener();
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		boolean fConsumeEvent = false;
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.ITEM_LONG_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
				if (mOriginalItemLongClickListener != null) {
					fConsumeEvent = mOriginalItemLongClickListener.onItemLongClick(parent, view, position, id);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, view, "item long click");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
		return fConsumeEvent;
	}
}
