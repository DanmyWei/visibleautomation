package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;

/**
 * wrapper class to receive focus change events.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordOnFocusChangeListener extends RecordListener implements View.OnFocusChangeListener, IOriginalListener  {
	protected View.OnFocusChangeListener 	mOriginalOnFocusChangeListener;
	protected ViewInterceptor				mViewInterceptor;
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, ViewInterceptor viewInterceptor, View view) {
		super(eventRecorder);
		mViewInterceptor = viewInterceptor;
		try {
			mOriginalOnFocusChangeListener = view.getOnFocusChangeListener();
			view.setOnFocusChangeListener(mOriginalOnFocusChangeListener);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "create on focus change listener");
		}
	}
	
	public RecordOnFocusChangeListener(EventRecorder eventRecorder, ViewInterceptor viewInterceptor, View.OnFocusChangeListener originalFocusChangeListener) {
		super(eventRecorder);
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
		// save the view for IME event detection whether or not events are blockeds
		mViewInterceptor.setFocusedView(v);
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (hasFocus) {
					mEventRecorder.writeRecord(Constants.EventTags.GET_FOCUS, v);
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.LOSE_FOCUS, v);
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, v, "on focus change " + hasFocus);
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
