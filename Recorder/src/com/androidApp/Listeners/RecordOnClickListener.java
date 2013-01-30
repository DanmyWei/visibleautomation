package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public class RecordOnClickListener implements View.OnClickListener {
	protected View.OnClickListener 	mOriginalOnClickListener;
	protected EventRecorder			mEventRecorder;
	
	public RecordOnClickListener(EventRecorder eventRecorder, View v) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, View.OnClickListener originalTouchListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnClickListener = originalTouchListener;
	}
	
	public void onClick(View v) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.CLICK + ":" + time + "," + mEventRecorder.getViewReference().getReference(v);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnClickListener != null) {
			 mOriginalOnClickListener.onClick(v);
		} 
	}
}
