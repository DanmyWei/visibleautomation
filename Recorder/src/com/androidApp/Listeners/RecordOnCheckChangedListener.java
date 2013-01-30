package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;

public class RecordOnCheckChangedListener implements CompoundButton.OnCheckedChangeListener {
	protected CompoundButton.OnCheckedChangeListener 	mOriginalOnCheckedChangeListener;
	protected EventRecorder								mEventRecorder;
	
	public RecordOnCheckChangedListener(EventRecorder eventRecorder, CompoundButton v) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnCheckedChangeListener = ListenerIntercept.getCheckedChangeListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnCheckChangedListener(EventRecorder eventRecorder, CompoundButton.OnCheckedChangeListener originalOnCheckedChangeListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnCheckedChangeListener = originalOnCheckedChangeListener;
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.CHECKED + ":" + time + "," + isChecked + "," + mEventRecorder.getViewReference().getReference(buttonView);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnCheckedChangeListener != null) {
			mOriginalOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
		} 
	}
}
