package com.androidApp.Listeners;

import java.util.List;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Intercept.IMEMessageListener;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/*
 * record when the text has changed in an edit text.
 * While this would be a wonderful thing, there's an ordering issue with text watchers, and if another text watcher
 * or God Only Knows What changes the text before it's displayed in the text control, this text watcher records the 
 * text after the transformation has been applied (like formatting a phone number or something like that)
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordTextChangedListener extends RecordListener implements TextWatcher {
	private static final String TAG = "RecordTextChangedListener";
	protected TextView			mTextView;
	protected boolean			mfEnterTextByKey;
	protected boolean			mfBeforeFired = false;
	protected static boolean	sfShowedKeyboardWarning = false;
	
	public RecordTextChangedListener(EventRecorder eventRecorder, TextView textView) {
		super(eventRecorder);
		mTextView = textView;
		mfEnterTextByKey = false;
	}
	
	public void afterTextChanged(Editable editable) {
		setEventBlock(false);
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		try {
			if (!IMEMessageListener.isKeyboardConnected()) {
				if (!sfShowedKeyboardWarning) {
					Context context = mTextView.getContext();
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(Constants.DisplayStrings.VISIBLE_AUTOMATION);
					builder.setMessage(Constants.DisplayStrings.KEYBOARD_NOT_INSTALLED);
					Dialog dialog = builder.create();
					dialog.show();
					sfShowedKeyboardWarning = true;
				}
			}
			// we have to test the view directive here, because we don't pass the View down to the event recorder 
			// which normally gets it.
			if (!mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.IGNORE_EVENTS,
				  							   	   ViewDirective.When.ALWAYS) &&
				!mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.IGNORE_TEXT_EVENTS, 
				  						 		   ViewDirective.When.ALWAYS)) {
				String description = getDescription(mTextView);
				String reference = mEventRecorder.getViewReference().getReference(mTextView);
				String massagedString = StringUtils.escapeString(s.toString(), "\"", '\\').replace("\n", "\\n");
				String logString = '\"' + massagedString + '\"' + "," + start + "," +  count + "," + after + "," + reference + "," + description;
				boolean fKeyText = mfEnterTextByKey ||
					    			mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.ENTER_TEXT_BY_KEY,
					    											  ViewDirective.When.ALWAYS);
				if (!RecordListener.getEventBlock() && (IMEMessageListener.getOutstandingKeyCount() > 0)) {
					mfBeforeFired = true;
					setEventBlock(true);
					if (fKeyText) {
						mEventRecorder.writeRecord(Constants.EventTags.BEFORE_TEXT_KEY, logString);
						mfEnterTextByKey = true;
					} else {
						mEventRecorder.writeRecord(Constants.EventTags.BEFORE_TEXT, logString);
					}
				} else if (fKeyText) {
					// the text was set programmatically..this gets written as a wait.
					if (mTextView.getVisibility() == View.VISIBLE) {
						mEventRecorder.writeRecord(Constants.EventTags.BEFORE_SET_TEXT, logString);
					}
				}
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, mTextView, " before text changed");
		}	
	}

	// We can scan the stack to see if the calling method is TextWatcher.afterTextChanged()
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		try {
			if (!mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.IGNORE_EVENTS,
				   	   							   ViewDirective.When.ALWAYS) &&
				!mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.IGNORE_TEXT_EVENTS, 
				 		   						   ViewDirective.When.ALWAYS)) {
				String description = getDescription(mTextView);
				String reference = mEventRecorder.getViewReference().getReference(mTextView);
				String massagedString = StringUtils.escapeString(s.toString(), "\"", '\\').replace("\n", "\\n");
				String logString = '\"' + massagedString + '\"' + "," + start + "," + before + "," + count + "," + reference + "," + description;
				boolean fKeyText = mfEnterTextByKey ||
		    			mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.ENTER_TEXT_BY_KEY,
		    											  ViewDirective.When.ALWAYS);
				if ((!RecordListener.getEventBlock() || mfBeforeFired) && (IMEMessageListener.getOutstandingKeyCount() > 0)) {
					if (!mfBeforeFired) {
						Log.d(TAG, "before not fired in text change listener");
					}
					mfBeforeFired = false;
					IMEMessageListener.decrementOutstandingKeyCount();
					setEventBlock(true);
					if (mfEnterTextByKey ||
						    mEventRecorder.matchViewDirective(mTextView, ViewDirective.ViewOperation.ENTER_TEXT_BY_KEY,
															  ViewDirective.When.ALWAYS)) {
						mEventRecorder.writeRecord(Constants.EventTags.AFTER_TEXT_KEY, logString);
					} else {
						mEventRecorder.writeRecord(Constants.EventTags.AFTER_TEXT, logString);
					}
				} else if (fKeyText) {
					mEventRecorder.writeRecord(Constants.EventTags.AFTER_SET_TEXT, logString);
				}
			}
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, mTextView, "on text changed");
		}	
	}
}
