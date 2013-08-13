package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.Utility.Constants;

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
	protected int			mViewIndex;
	
	public RecordOnClickListener(EventRecorder eventRecorder, int viewIndex, View v) {
		super(eventRecorder);
		mViewIndex = viewIndex;
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
			v.setOnClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, v, "create on click listener");
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, int viewIndex, View.OnClickListener originalTouchListener) {
		super(eventRecorder);
		mViewIndex = viewIndex;
		mOriginalOnClickListener = originalTouchListener;
	}
	
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
			try {
				if (mEventRecorder.matchViewDirective(v, mViewIndex, ViewDirective.ViewOperation.CLICK_WORKAROUND,
					   	   						  ViewDirective.When.ALWAYS)) { 
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
