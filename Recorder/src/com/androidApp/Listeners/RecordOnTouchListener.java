package com.androidApp.Listeners;

import java.lang.reflect.Field;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;


import android.view.MotionEvent;
import android.view.View;

/**
 *  View.onTouchListener that listens to key events, and writes them to a file.
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordOnTouchListener extends RecordListener implements View.OnTouchListener, IOriginalListener  {
	protected View.OnTouchListener 	mOriginalOnTouchListener;
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnTouchListener = ListenerIntercept.getTouchListener(v);
			v.setOnTouchListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, v, "create on touch listener");
		}		
	}
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View.OnTouchListener originalTouchListener) {
		super(eventRecorder);
		mOriginalOnTouchListener = originalTouchListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnTouchListener;
	}
	
	/**
	 * record the actual touch event
	 * <touch_down/touch_up/touch_move>:time,x,y,<reference>,<description>
	 * 
	 */
	public boolean onTouch(View v, MotionEvent event) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String eventName = Constants.EventTags.UNKNOWN;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					eventName = Constants.EventTags.TOUCH_DOWN;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					eventName = Constants.EventTags.TOUCH_UP;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					eventName = Constants.EventTags.TOUCH_MOVE;
				}
				
				String description = getDescription(v);
				// we want to save the widget size in the down case, because the motion events have to be
				// scaled on playback in case it's a different device
				String logString = "";
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					logString = v.getWidth() + "," + v.getHeight() + ",";
				}
				logString += event.getX() + "," + event.getY() + "," +
						 	 mEventRecorder.getViewReference().getReference(v) + "," + description;
				mEventRecorder.writeRecord(eventName, logString);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, v, "on touch");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnTouchListener != null) {
				fConsumeEvent = mOriginalOnTouchListener.onTouch(v, event);
			} 
		}
		setEventBlock(false);
		return fConsumeEvent;
	}
}
