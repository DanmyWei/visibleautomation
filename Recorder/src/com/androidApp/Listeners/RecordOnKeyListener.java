package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

// recorder for view key events.
public class RecordOnKeyListener extends RecordListener implements View.OnKeyListener {
	protected View.OnKeyListener 	mOriginalOnKeyListener;
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnKeyListener = ListenerIntercept.getKeyListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View.OnKeyListener originalKeyListener) {
		super(eventRecorder);
		mOriginalOnKeyListener = originalKeyListener;
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
		boolean fConsumeEvent = false;
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				String action = Constants.Action.UNKNOWN;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					action = Constants.Action.DOWN;
				} else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
					action = Constants.Action.UP;
				}
				mEventRecorder.writeRecord(Constants.EventTags.KEY, keyCode + "," + action + "," +
										   mEventRecorder.getViewReference().getReference(v) + "," + getDescription(v));
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, " key event");
				ex.printStackTrace();
			}
			if (mOriginalOnKeyListener != null) {
				 fConsumeEvent = mOriginalOnKeyListener.onKey(v, keyCode, keyEvent);
			} 
			mfReentryBlock = false;
		}
		return fConsumeEvent;
	}
}
