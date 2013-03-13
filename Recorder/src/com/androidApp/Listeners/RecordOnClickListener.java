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
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, View.OnClickListener originalTouchListener) {
		super(eventRecorder);
		mOriginalOnClickListener = originalTouchListener;
	}
	
	// click:time,<view reference>,Click on <description>
	public void onClick(View v) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.CLICK, v, getDescription(v));
				if (mOriginalOnClickListener != null) {
					 mOriginalOnClickListener.onClick(v);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on click");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}
}
