package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public class RecordOnLongClickListener extends RecordListener implements View.OnLongClickListener {
	protected View.OnLongClickListener 	mOriginalOnLongClickListener;
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnLongClickListener = ListenerIntercept.getLongClickListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View.OnLongClickListener originalLongClickListener) {
		super(eventRecorder);
		mOriginalOnLongClickListener = originalLongClickListener;
	}
	
	public boolean onLongClick(View v) {
		boolean fConsumeEvent = false;
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				String description = getDescription(v);
				mEventRecorder.writeRecord(Constants.EventTags.LONG_CLICK, v, description);
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "long click");
				ex.printStackTrace();
			}
			if (mOriginalOnLongClickListener != null) {
				fConsumeEvent = mOriginalOnLongClickListener.onLongClick(v);
			} 
			mfReentryBlock = false;
		}
		return fConsumeEvent;
	}
}
