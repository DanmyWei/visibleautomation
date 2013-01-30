package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public class RecordOnLongClickListener implements View.OnLongClickListener {
	protected View.OnLongClickListener 	mOriginalOnLongClickListener;
	protected EventRecorder				mEventRecorder;
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View v) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnLongClickListener = ListenerIntercept.getLongClickListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View.OnLongClickListener originalLongClickListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnLongClickListener = originalLongClickListener;
	}
	
	public boolean onLongClick(View v) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.CLICK + ":" + time + "," + mEventRecorder.getViewReference().getReference(v);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnLongClickListener != null) {
			return mOriginalOnLongClickListener.onLongClick(v);
		} else {
			return false;
		}
	}
}
