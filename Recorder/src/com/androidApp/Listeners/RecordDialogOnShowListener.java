package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;

public class RecordDialogOnShowListener extends RecordListener implements DialogInterface.OnShowListener {
	protected EventRecorder mEventRecorder;
	protected DialogInterface.OnShowListener mOriginalOnShowListener;
	
	public RecordDialogOnShowListener(EventRecorder eventRecorder, DialogInterface dialog) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnShowListener = ListenerIntercept.getOnShowListener((Dialog) dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void onShow(DialogInterface dialog) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(dialog);
			String logString = Constants.EventTags.SHOW_DIALOG + ":" + time + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnShowListener != null) {
			mOriginalOnShowListener.onShow(dialog);
		} 
	}
}
