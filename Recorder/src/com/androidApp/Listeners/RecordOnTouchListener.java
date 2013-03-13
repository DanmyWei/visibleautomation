package com.androidApp.Listeners;

import java.lang.reflect.Field;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;


import android.view.MotionEvent;
import android.view.View;

// View.onTouchListener that listens to key events, and writes them to a file.
public class RecordOnTouchListener extends RecordListener implements View.OnTouchListener {
	protected View.OnTouchListener 	mOriginalOnTouchListener;
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnTouchListener = ListenerIntercept.getTouchListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View.OnTouchListener originalTouchListener) {
		super(eventRecorder);
		mOriginalOnTouchListener = originalTouchListener;
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		boolean fConsumeEvent = false;
		if (!mfReentryBlock) {
			mfReentryBlock = true;
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
				String logString = event.getX() + "," + event.getY() + "," +
								   mEventRecorder.getViewReference().getReference(v) + "," + description;
				mEventRecorder.writeRecord(eventName, logString);
				if (mOriginalOnTouchListener != null) {
					fConsumeEvent = mOriginalOnTouchListener.onTouch(v, event);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on touch");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
		return fConsumeEvent;
	}
}
