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
 * event tag is so we can write out specific dialog types like autocomplete and spinner
 * @author Matthew
 *
 */
public class RecordDialogOnCancelListener extends RecordListener implements DialogInterface.OnCancelListener, IOriginalListener {
	protected DialogInterface.OnCancelListener 	mOriginalOnCancelListener;
	protected String mEventTag;
	
	public RecordDialogOnCancelListener() {
	}
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, DialogInterface.OnCancelListener originalCancelListener) {
		super(eventRecorder);
		mOriginalOnCancelListener = originalCancelListener;
		mEventTag = Constants.EventTags.CANCEL_DIALOG;
	}
	
	public RecordDialogOnCancelListener(EventRecorder eventRecorder, DialogInterface.OnCancelListener originalCancelListener, String eventTag) {
		super(eventRecorder);
		mOriginalOnCancelListener = originalCancelListener;
		mEventTag = eventTag;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnCancelListener;
	}
	
	public void onCancel(DialogInterface dialog) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(dialog);
				mEventRecorder.writeRecord(mEventTag, description);
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on cancel dialog");
				ex.printStackTrace();
			} 
		}
		if (!fReentryBlock) {
			// always call the original on cancel listener.
			if (mOriginalOnCancelListener != null) {
				mOriginalOnCancelListener.onCancel(dialog);
			} 
		}
		setEventBlock(false);
	}
}
