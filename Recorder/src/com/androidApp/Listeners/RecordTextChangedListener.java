package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/*
 * record when the text has changed in an edit text.
 * While this would be a wonderful thing, there's an ordering issue with text watchers, and if another text watcher
 * or God Only Knows What changes the text before it's displayed in the text control, this text watcher records the 
 * text after the transformation has been applied (like formatting a phone number or something like that)
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordTextChangedListener extends RecordListener implements TextWatcher {
	protected TextView				mTextView;

	public RecordTextChangedListener(EventRecorder eventRecorder, TextView textView) {
		super(eventRecorder);
		mTextView = textView;
	}
	
	// since these methods are called in a chain, rather than wrapping the native listeners, we don't need to block re-entrancy
	public void afterTextChanged(Editable editable) {
		setEventBlock(true);
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(mTextView);
				String logString = '\"' + StringUtils.escapeString(s.toString(), "\"", '\\') + '\"' + "," + start + "," +  count + "," + after + "," + mEventRecorder.getViewReference().getReference(mTextView) + "," + description;
				mEventRecorder.writeRecord(Constants.EventTags.BEFORE_TEXT, logString);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mTextView, " before text changed");
			}	
		}
	}

	// We can scan the stack to see if the calling method is TextWatcher.afterTextChanged()
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(mTextView);
				String massagedString = StringUtils.escapeString(s.toString(), "\"", '\\').replace("\n", "\\n");
				String logString = '\"' + massagedString + '\"' + "," + start + "," + before + "," + count + "," + mEventRecorder.getViewReference().getReference(mTextView) + "," + description;
				mEventRecorder.writeRecord(Constants.EventTags.AFTER_TEXT, logString);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mTextView, "on text changed");
			}	
		}
	}
}
