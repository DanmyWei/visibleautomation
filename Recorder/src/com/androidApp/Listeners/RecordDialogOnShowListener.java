package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;

// log when a dialog is displayed
public class RecordDialogOnShowListener extends RecordListener implements DialogInterface.OnShowListener, IOriginalListener  {
	protected DialogInterface.OnShowListener mOriginalOnShowListener;
	
	public RecordDialogOnShowListener(EventRecorder eventRecorder, DialogInterface dialog) {
		super(eventRecorder);
		try {
			mOriginalOnShowListener = ListenerIntercept.getOnShowListener((Dialog) dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Object getOriginalListener() {
		return mOriginalOnShowListener;
	}
	
	public void onShow(DialogInterface dialog) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(dialog);
				mEventRecorder.writeRecord(Constants.EventTags.SHOW_DIALOG, description);
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on show dialog");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnShowListener != null) {
				mOriginalOnShowListener.onShow(dialog);
			} 
		}
		setEventBlock(false);
	}
}
