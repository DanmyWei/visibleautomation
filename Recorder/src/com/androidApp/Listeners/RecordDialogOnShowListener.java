package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;

// log when a dialog is displayed
public class RecordDialogOnShowListener extends RecordListener implements DialogInterface.OnShowListener {
	protected DialogInterface.OnShowListener mOriginalOnShowListener;
	
	public RecordDialogOnShowListener(EventRecorder eventRecorder, DialogInterface dialog) {
		super(eventRecorder);
		try {
			mOriginalOnShowListener = ListenerIntercept.getOnShowListener((Dialog) dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void onShow(DialogInterface dialog) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				String description = getDescription(dialog);
				mEventRecorder.writeRecord(Constants.EventTags.SHOW_DIALOG, description);
				if (mOriginalOnShowListener != null) {
					mOriginalOnShowListener.onShow(dialog);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on show dialog");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}
}
