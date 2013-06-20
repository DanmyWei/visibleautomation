package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

// recorder for toggle buttons OnCheckedChangeListener (onClick really does this)
// NOTE: currently unused
// Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
public class RecordOnCheckChangedListener extends RecordListener implements CompoundButton.OnCheckedChangeListener, IOriginalListener  {
	protected CompoundButton.OnCheckedChangeListener 	mOriginalOnCheckedChangeListener;
	
	public RecordOnCheckChangedListener(EventRecorder eventRecorder, CompoundButton v) {
		super(eventRecorder);
		try {
			mOriginalOnCheckedChangeListener = ListenerIntercept.getCheckedChangeListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnCheckChangedListener(EventRecorder eventRecorder, CompoundButton.OnCheckedChangeListener originalOnCheckedChangeListener) {
		super(eventRecorder);
		mOriginalOnCheckedChangeListener = originalOnCheckedChangeListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnCheckedChangeListener;
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String fullDescription = isChecked + "," + mEventRecorder.getViewReference().getReference(buttonView);
				mEventRecorder.writeRecord(Constants.EventTags.CHECKED, fullDescription);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, buttonView, " on check changed");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnCheckedChangeListener != null) {
				mOriginalOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
			}	
		}
		setEventBlock(false);
	}
}
