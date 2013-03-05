package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;

/**
 * recorder wrapper to intercept dialog dismiss events
 * @author Matthew
 *
 */

public class RecordDialogOnDismissListener extends RecordListener implements DialogInterface.OnDismissListener {
	protected DialogInterface.OnDismissListener mOriginalOnDismissListener;
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, Dialog dialog) {
		super(eventRecorder);
		try {
			mOriginalOnDismissListener = ListenerIntercept.getOnDismissListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, DialogInterface.OnDismissListener originalDismissListener) {
		super(eventRecorder);
		mOriginalOnDismissListener = originalDismissListener;
	}

	
	public void onDismiss(DialogInterface dialog) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(dialog);
			String logString = Constants.EventTags.DISMISS_DIALOG + ":" + time + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnDismissListener != null) {
			mOriginalOnDismissListener.onDismiss(dialog);
		} 
	}
}
