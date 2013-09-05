package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * wrapper class to receive focus change events.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnFocusChangeListener extends RecordListener implements View.OnFocusChangeListener, IOriginalListener  {
	protected View.OnFocusChangeListener 	mOriginalOnFocusChangeListener;
	protected ViewInterceptor				mViewInterceptor;
	
	public RecordOnFocusChangeListener(String activityName, EventRecorder eventRecorder, ViewInterceptor viewInterceptor, View view) {
		super(activityName, eventRecorder);
		mViewInterceptor = viewInterceptor;
		try {
			mOriginalOnFocusChangeListener = view.getOnFocusChangeListener();
			view.setOnFocusChangeListener(mOriginalOnFocusChangeListener);
		} catch (Exception ex) {
			mEventRecorder.writeException(mActivityName, ex, "create on focus change listener");
		}
	}
	
	public RecordOnFocusChangeListener(String activityName, EventRecorder eventRecorder, ViewInterceptor viewInterceptor, View.OnFocusChangeListener originalFocusChangeListener) {
		super(activityName, eventRecorder);
		mViewInterceptor = viewInterceptor;
		mOriginalOnFocusChangeListener = originalFocusChangeListener;
	}
	
	/**
	 * retrieve the original listener
	 */
	public Object getOriginalListener() {
		return mOriginalOnFocusChangeListener;
	}
	
	/**
	 * actual focus change record point.
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// save the view for IME event detection whether or not events are blocked
		if (hasFocus) {
			mViewInterceptor.setFocusedView(v);
		}
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (hasFocus) {
					if (v instanceof TextView) {
						TextView tv = (TextView) v;
						String msg = Integer.toString(tv.getSelectionStart()) + "," +  Integer.toString(tv.getSelectionEnd());
						mEventRecorder.writeRecord(Constants.EventTags.GET_FOCUS, mActivityName, v, msg);
					} else {
						mEventRecorder.writeRecord(Constants.EventTags.GET_FOCUS, mActivityName, v);
					}
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.LOSE_FOCUS, mActivityName, v);
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, v, "on focus change " + hasFocus);
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnFocusChangeListener != null) {
				mOriginalOnFocusChangeListener.onFocusChange(v, hasFocus);
			}		
		}
		setEventBlock(false);
	}
}
