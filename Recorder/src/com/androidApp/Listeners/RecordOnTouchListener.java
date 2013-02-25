package com.androidApp.Listeners;

import java.lang.reflect.Field;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;


import android.view.MotionEvent;
import android.view.View;

// View.onTouchListener that listens to key events, and writes them to a file.
public class RecordOnTouchListener extends RecordListener implements View.OnTouchListener {
	protected View.OnTouchListener 	mOriginalOnTouchListener;
	protected EventRecorder			mEventRecorder;
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View v) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnTouchListener = ListenerIntercept.getTouchListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnTouchListener(EventRecorder eventRecorder, View.OnTouchListener originalTouchListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnTouchListener = originalTouchListener;
	}
	
	public boolean onTouch(View v, MotionEvent event) {
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
			String logString = eventName + ":" + event.getEventTime() + "," + event.getX() + "," + event.getY() + "," +
			   					mEventRecorder.getViewReference().getReference(v) + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnTouchListener != null) {
			return mOriginalOnTouchListener.onTouch(v, event);
		} else {
			return false;
		}
	}
}
