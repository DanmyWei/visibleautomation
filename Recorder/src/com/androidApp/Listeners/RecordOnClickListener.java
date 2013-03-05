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
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.CLICK + ":" + time + "," + 
							   mEventRecorder.getViewReference().getReference(v) + "," + getDescription(v);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnClickListener != null) {
			 mOriginalOnClickListener.onClick(v);
		} 
	}
}
