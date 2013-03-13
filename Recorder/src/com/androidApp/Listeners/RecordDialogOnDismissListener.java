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
 * recorder wrapper to intercept dialog dismiss events
 * We have an exception case for dialogs created by spinners, so the emitter can generate
 * spinner-specific code.
 * @author Matthew
 *
 */

public class RecordDialogOnDismissListener extends RecordListener implements DialogInterface.OnDismissListener {
	protected DialogInterface.OnDismissListener mOriginalOnDismissListener;
	protected Spinner							mSpinner;					// if dialog was popped up from a spinner
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, Spinner spinner, Dialog dialog) {
		super(eventRecorder);
		mSpinner = spinner;
		try {
			mOriginalOnDismissListener = ListenerIntercept.getOnDismissListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, Spinner spinner, DialogInterface.OnDismissListener originalDismissListener) {
		super(eventRecorder);
		mSpinner = spinner;
		mOriginalOnDismissListener = originalDismissListener;
	}

	
	public void onDismiss(DialogInterface dialog) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				String description = getDescription(dialog);
				if (mSpinner != null) {
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_SPINNER_DIALOG, description);
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_DIALOG, description);
				}
				if (mOriginalOnDismissListener != null) {
					mOriginalOnDismissListener.onDismiss(dialog);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on dismiss dialog");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}
}
