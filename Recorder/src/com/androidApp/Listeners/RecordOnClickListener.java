package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.DialogUtils;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;

/**
 * recorder for view click events. This is probably called more than anything else in the world
 * TODO: This may be the cause of errors with toggle buttons
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnClickListener extends RecordListener implements View.OnClickListener, IOriginalListener  {
	protected View.OnClickListener 	mOriginalOnClickListener;
	
	public RecordOnClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
			v.setOnClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, v, "create on click listener");
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, View.OnClickListener originalClicksListener) {
		super(eventRecorder);
		mOriginalOnClickListener = originalClicksListener;
	}
	
	// IOriginalListener implementation
	public Object getOriginalListener() {
		return mOriginalOnClickListener;
	}
		
	/**
	 * record the all-pervasive click event
	 * click:time,<view reference>,Click on <description>
	 */
	public void onClick(View v) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			View rootView = v.getRootView();
			boolean fIsInDialog = (DialogUtils.getDialog(rootView) != null);
			boolean fWorkaroundDirective = mEventRecorder.matchViewDirective(v, ViewDirective.ViewOperation.CLICK_WORKAROUND, ViewDirective.When.ALWAYS);
			try {
				if (fIsInDialog || fWorkaroundDirective) { 
					mEventRecorder.writeRecord(Constants.EventTags.CLICK_WORKAROUND, v, getDescription(v));
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.CLICK, v, getDescription(v));
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, v, "on click");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnClickListener != null) {
				 mOriginalOnClickListener.onClick(v);
			}
		}
		setEventBlock(false);
	}
}
