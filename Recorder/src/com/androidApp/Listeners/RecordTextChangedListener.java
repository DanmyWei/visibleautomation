package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/*
 * record when the text has changed in an edit text.
 * While this would be a wonderful thing, there's an ordering issue with text watchers, and if another text watcher
 * or God Only Knows What changes the text before it's displayed in the text control, this text watcher records the 
 * text after the transformation has been applied (like formatting a phone number or something like that)
 */
public class RecordTextChangedListener extends RecordListener implements TextWatcher {
	protected EventRecorder			mEventRecorder;
	protected TextView				mTextView;

	public RecordTextChangedListener(TextView textView, EventRecorder eventRecorder) {
		mTextView = textView;
		mEventRecorder = eventRecorder;
	}
	
	public void afterTextChanged(Editable editable) {
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(mTextView);
			String logString = Constants.EventTags.BEFORE_TEXT + ":" + time + "," + s + "," + start + "," +  count + "," + after +
			   "," + mEventRecorder.getViewReference().getReference(mTextView) + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	// We can scan the stack to see if the calling method is TextWatcher.afterTextChanged()
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.AFTER_TEXT + ":" + time + "," + s + "," + start + "," + before + "," + count +
			   "," + mEventRecorder.getViewReference().getReference(mTextView);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
}
