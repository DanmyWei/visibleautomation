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
 * recorder wrapper to intercept dialog dismiss events
 * We have an exception case for dialogs created by spinners, so the emitter can generate
 * spinner-specific code.
 * @author Matthew
 *
 */

public class RecordDialogOnDismissListener extends RecordListener implements DialogInterface.OnDismissListener {
	protected DialogInterface.OnDismissListener mOriginalOnDismissListener;
	protected String							mEventTag;					// so we can write out custom dialog event types, like spinner and autocomplete
	
	public RecordDialogOnDismissListener() {
	}
	
	public RecordDialogOnDismissListener(EventRecorder eventRecorder, DialogInterface.OnDismissListener originalDismissListener) {
		super(eventRecorder);
		mOriginalOnDismissListener = originalDismissListener;
		mEventTag = Constants.EventTags.DISMISS_DIALOG;
	}

	public RecordDialogOnDismissListener(EventRecorder eventRecorder, DialogInterface.OnDismissListener originalDismissListener, String eventTag) {
		super(eventRecorder);
		mOriginalOnDismissListener = originalDismissListener;
		mEventTag = eventTag;
	}

	public void onDismiss(DialogInterface dialog) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(dialog);
				mEventRecorder.writeRecord(mEventTag, description);
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "on dismiss dialog");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnDismissListener != null) {
				mOriginalOnDismissListener.onDismiss(dialog);
			} 
		}
		setEventBlock(false);
	}
}
