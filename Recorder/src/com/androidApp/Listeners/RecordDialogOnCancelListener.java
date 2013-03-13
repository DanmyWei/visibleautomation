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
public class RecordDialogOnCancelListener extends RecordListener implements DialogInterface.OnCancelListener {
	protected DialogInterface.OnCancelListener 	mOriginalOnCancelListener;
	protected Spinner							mSpinner;					// if dialog was launched from spinner.
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, Spinner spinner, Dialog dialog) {
		super(eventRecorder);
		try {
			mOriginalOnCancelListener = ListenerIntercept.getOnCancelListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, Spinner spinner, DialogInterface.OnCancelListener originalCancelListener) {
		super(eventRecorder);
		mSpinner = spinner;
		mOriginalOnCancelListener = originalCancelListener;
	}

	
	public void onCancel(DialogInterface dialog) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				String description = getDescription(dialog);
				if (mSpinner != null) {
					mEventRecorder.writeRecord(Constants.EventTags.CANCEL_SPINNER_DIALOG, description);
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.CANCEL_DIALOG, description);
				}
				if (mOriginalOnCancelListener != null) {
					mOriginalOnCancelListener.onCancel(dialog);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on cancel dialog");
				ex.printStackTrace();
			} 
			mfReentryBlock = false;
		}
	}
}
