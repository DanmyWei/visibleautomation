package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 *  recorder for view key events.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordOnKeyListener extends RecordListener implements View.OnKeyListener, IOriginalListener  {
	protected View.OnKeyListener 	mOriginalOnKeyListener;
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnKeyListener = ListenerIntercept.getKeyListener(v);
			v.setOnKeyListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnKeyListener(EventRecorder eventRecorder, View.OnKeyListener originalKeyListener) {
		super(eventRecorder);
		mOriginalOnKeyListener = originalKeyListener;
	}
		
	public Object getOriginalListener() {
		return mOriginalOnKeyListener;
	}

	/**
	 * intercepts the key event.
	 * key:<time_msec>,<up/down>,reference,description
	 * @param v view
	 * @param keyCode keyCode
	 * @param keyEvent keyEvent
	 */
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String action = Constants.Action.UNKNOWN;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					action = Constants.Action.DOWN;
				} else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
					action = Constants.Action.UP;
				}
				mEventRecorder.writeRecord(Constants.EventTags.KEY, keyCode + "," + action + "," +
										   mEventRecorder.getViewReference().getReference(v) + "," + getDescription(v));
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, " key event");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			
			// always call the original key listener if there is one.
			if (mOriginalOnKeyListener != null) {
				 fConsumeEvent = mOriginalOnKeyListener.onKey(v, keyCode, keyEvent);
			} 
		}
		setEventBlock(false);
		return fConsumeEvent;
	}
}
