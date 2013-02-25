package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

// recorder for view click events.
public class RecordOnKeyListener extends RecordListener implements View.OnKeyListener {
	protected View.OnKeyListener 	mOriginalOnKeyListener;
	protected EventRecorder			mEventRecorder;
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View v) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnKeyListener = ListenerIntercept.getKeyListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View.OnKeyListener originalKeyListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnKeyListener = originalKeyListener;
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
		long time = SystemClock.uptimeMillis();
		try {
			String action = "unknown";
			if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				action = "down";
			} else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
				action = "up";
			}
			String logString = Constants.EventTags.KEY + ":" + time + "," + keyCode + "," + action + "," +
							   mEventRecorder.getViewReference().getReference(v) + "," + getDescription(v);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnKeyListener != null) {
			 return mOriginalOnKeyListener.onKey(v, keyCode, keyEvent);
		} 
		return false;
	}
}
