package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

// recorder for view click events.
public class RecordOnClickListener extends RecordListener implements View.OnClickListener {
	protected View.OnClickListener 	mOriginalOnClickListener;
	
	public RecordOnClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
			v.setOnClickListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, View.OnClickListener originalTouchListener) {
		super(eventRecorder);
		mOriginalOnClickListener = originalTouchListener;
	}
	
	/**
	 * we shouldn't intercept if we're already recording the click listener.
	 */
	public boolean shouldIntercept(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		if (super.shouldIntercept(v)) {
			View.OnClickListener originalOnClickListener = ListenerIntercept.getClickListener(v);
			return !(originalOnClickListener instanceof RecordOnClickListener);
		}
		return false;
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
				mEventRecorder.writeRecord(Constants.EventTags.CLICK, v, getDescription(v));
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on click");
				ex.printStackTrace();
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
