package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

// recorder for toggle buttons OnCheckedChangeListener (onClick really does this)
// NOTE: currently unused
// Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
public class RecordOnCheckChangedListener extends RecordListener implements CompoundButton.OnCheckedChangeListener, IOriginalListener  {
	protected CompoundButton.OnCheckedChangeListener 	mOriginalOnCheckedChangeListener;
	
	public RecordOnCheckChangedListener(String activityName, EventRecorder eventRecorder, CompoundButton v) {
		super(activityName, eventRecorder);
		try {
			mOriginalOnCheckedChangeListener = ListenerIntercept.getCheckedChangeListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnCheckChangedListener(String activityName, EventRecorder eventRecorder, CompoundButton.OnCheckedChangeListener originalOnCheckedChangeListener) {
		super(activityName, eventRecorder);
		mOriginalOnCheckedChangeListener = originalOnCheckedChangeListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnCheckedChangeListener;
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			setEventBlock(true);
			mEventRecorder.setTouchedDown(false);
			try {
				String fullDescription = isChecked + "," + mEventRecorder.getViewReference().getReference(buttonView);
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.CHECKED, fullDescription);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, buttonView, " on check changed");
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
