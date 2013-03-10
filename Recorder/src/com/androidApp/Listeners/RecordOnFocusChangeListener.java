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
 * wrapper class to intercept and record dialog cancel events.
 * We have an exception case for dialogs created by spinners, so the emitter can generate
 * spinner-specific code.
 * @author Matthew
 *
 */
public class RecordOnFocusChangeListener extends RecordListener implements View.OnFocusChangeListener {
	protected View.OnFocusChangeListener 	mOriginalOnFocusChangeListener;
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, View view) {
		super(eventRecorder);
		try {
			mOriginalOnFocusChangeListener = view.getOnFocusChangeListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, View.OnFocusChangeListener originalFocusChangeListener) {
		super(eventRecorder);
		mOriginalOnFocusChangeListener = originalFocusChangeListener;
	}

	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		try {
			if (hasFocus) {
				// save the view for IME event detection
				mEventRecorder.setFocusedView(v);
				mEventRecorder.writeRecord(Constants.EventTags.GET_FOCUS, v);
			} else {
				mEventRecorder.writeRecord(Constants.EventTags.LOSE_FOCUS, v);
			}
		} catch (Exception ex) {
			mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v);
			ex.printStackTrace();
		}
		if (mOriginalOnFocusChangeListener != null) {
			mOriginalOnFocusChangeListener.onFocusChange(v, hasFocus);
		}		
	}
}
