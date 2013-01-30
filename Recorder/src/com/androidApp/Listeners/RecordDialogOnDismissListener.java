package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;

public class RecordDialogOnDismissListener implements DialogInterface.OnDismissListener {
	protected EventRecorder mEventRecorder;
	protected DialogInterface.OnDismissListener mOriginalOnDismissListener;
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, Dialog dialog) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnDismissListener = ListenerIntercept.getOnDismissListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, DialogInterface.OnDismissListener originalDismissListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnDismissListener = originalDismissListener;
	}

	
	public void onDismiss(DialogInterface dialog) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.DISMISS_DIALOG + ":" + time;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnDismissListener != null) {
			mOriginalOnDismissListener.onDismiss(dialog);
		} 
	}
}
