package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;

/**
 * wrapper class to receive focus change events.
 * @author Matthew
 *
 */
public class RecordOnFocusChangeListener extends RecordListener implements View.OnFocusChangeListener {
	protected View.OnFocusChangeListener 	mOriginalOnFocusChangeListener;
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, View view) {
		super(eventRecorder);
		try {
			mOriginalOnFocusChangeListener = view.getOnFocusChangeListener();
			view.setOnFocusChangeListener(mOriginalOnFocusChangeListener);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, View.OnFocusChangeListener originalFocusChangeListener) {
		super(eventRecorder);
		mOriginalOnFocusChangeListener = originalFocusChangeListener;
	}
	
	/**
	 * we shouldn't intercept if we're already recording the focus change listener.
	 */
	public boolean shouldIntercept(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		if (super.shouldIntercept(v)) {
			View.OnFocusChangeListener originalOnFocusChangeListener = v.getOnFocusChangeListener();
			return !(originalOnFocusChangeListener instanceof RecordOnFocusChangeListener);
		}
		return false;
	}

	/**
	 * actual focus change record point.
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (hasFocus) {
					// save the view for IME event detection
					mEventRecorder.setFocusedView(v);
					mEventRecorder.writeRecord(Constants.EventTags.GET_FOCUS, v);
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.LOSE_FOCUS, v);
				}
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on focus change " + hasFocus);
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnFocusChangeListener != null) {
				mOriginalOnFocusChangeListener.onFocusChange(v, hasFocus);
			}		
		}
		setEventBlock(false);
	}
}
