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
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(dialog);
			if (mSpinner != null) {
				String logString = Constants.EventTags.CANCEL_SPINNER_DIALOG + ":" + time + "," + description;
				mEventRecorder.writeRecord(logString);
			} else {
				String logString = Constants.EventTags.CANCEL_DIALOG + ":" + time + "," + description;
				mEventRecorder.writeRecord(logString);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnCancelListener != null) {
			mOriginalOnCancelListener.onCancel(dialog);
		} 
	}
}
