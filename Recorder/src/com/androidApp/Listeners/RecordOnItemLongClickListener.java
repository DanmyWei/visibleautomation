package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

/**
 *  record long click listener
 *  TODO: this will handle the UI for waitForText() and extended record interface
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */ 
public class RecordOnItemLongClickListener extends RecordListener implements AdapterView.OnItemLongClickListener, IOriginalListener  {
	protected AdapterView.OnItemLongClickListener	mOriginalItemLongClickListener;
	
	public RecordOnItemLongClickListener(String activityName, EventRecorder eventRecorder, AdapterView<?> adapterView) {
		super(activityName, eventRecorder);
		mOriginalItemLongClickListener = adapterView.getOnItemLongClickListener();
		adapterView.setOnItemLongClickListener(this);
	}
	
	public Object getOriginalListener() {
		return mOriginalItemLongClickListener;
	}

	/**
	 * wrapper for the onItemLongClick() event.
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.ITEM_LONG_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "item long click");
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
