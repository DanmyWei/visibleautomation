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
	protected AdapterView.OnItemLongClickListener	mOriginalItemLongClickListener;
	
	public RecordOnItemLongClickListener(EventRecorder eventRecorder, AdapterView<?> adapterView) {
		super(eventRecorder);
		mOriginalItemLongClickListener = adapterView.getOnItemLongClickListener();
		adapterView.setOnItemLongClickListener(this);
	}
	
	/**
	 * we don't call super for intercept, since it fails on parent adapter views.
	 */
	public boolean shouldIntercept(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		AdapterView.OnItemLongClickListener originalOnItemLongClickListener = ((AdapterView) v).getOnItemLongClickListener();
		return !(originalOnItemLongClickListener instanceof RecordOnItemLongClickListener);
	}

	/**
	 * wrapper for the onItemLongClick() event.
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(Constants.EventTags.ITEM_LONG_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, view, "item long click");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalItemLongClickListener != null) {
				fConsumeEvent = mOriginalItemLongClickListener.onItemLongClick(parent, view, position, id);
			} 
			
		}
		setEventBlock(false);
		return fConsumeEvent;
	}
}
