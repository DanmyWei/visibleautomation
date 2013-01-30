package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;

public class RecordDialogOnCancelListener implements DialogInterface.OnCancelListener {
	protected EventRecorder mEventRecorder;
	protected DialogInterface.OnCancelListener mOriginalOnCancelListener;
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, Dialog dialog) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnCancelListener = ListenerIntercept.getOnCancelListener(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, DialogInterface.OnCancelListener originalCancelListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnCancelListener = originalCancelListener;
	}

	
	public void onCancel(DialogInterface dialog) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.CANCEL_DIALOG + ":" + time;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnCancelListener != null) {
			mOriginalOnCancelListener.onCancel(dialog);
		} 
	}
}
