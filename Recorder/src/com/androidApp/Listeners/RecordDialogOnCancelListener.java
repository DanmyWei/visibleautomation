package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;

/**
 * wrapper class to intercept and record dialog cancel events.
 * @author Matthew
 *
 */
public class RecordDialogOnCancelListener extends RecordListener implements DialogInterface.OnCancelListener {
	protected DialogInterface.OnCancelListener mOriginalOnCancelListener;
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, Dialog dialog) {
		super(eventRecorder);
		try {
			mOriginalOnCancelListener = ListenerIntercept.getOnCancelListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, DialogInterface.OnCancelListener originalCancelListener) {
		super(eventRecorder);
		mOriginalOnCancelListener = originalCancelListener;
	}

	
	public void onCancel(DialogInterface dialog) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(dialog);
			String logString = Constants.EventTags.CANCEL_DIALOG + ":" + time + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnCancelListener != null) {
			mOriginalOnCancelListener.onCancel(dialog);
		} 
	}
}
