package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * intercept long clicks.  Note: this will get much more interesting, since this will provide the interface
 * for specifying views and references for "expect" targets
 * @author Matthew
 *
 */
public class RecordOnLongClickListener extends RecordListener implements View.OnLongClickListener, IOriginalListener {
	protected View.OnLongClickListener 	mOriginalOnLongClickListener;
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnLongClickListener = ListenerIntercept.getLongClickListener(v);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View.OnLongClickListener originalLongClickListener) {
		super(eventRecorder);
		mOriginalOnLongClickListener = originalLongClickListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnLongClickListener;
	}
	
	/**
	 * record onLongClick
	 * click:time,<view reference>,Click on <description>
	 * @param v view being intercepted.
	 * return true if the wrapped long click listener consumed the event.
	 */
	@Override
	public boolean onLongClick(View v) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(v);
				mEventRecorder.writeRecord(Constants.EventTags.LONG_CLICK, v, description);
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "long click");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnLongClickListener != null) {
				fConsumeEvent = mOriginalOnLongClickListener.onLongClick(v);
			} 
		}		
		setEventBlock(false);
		return fConsumeEvent;
	}
}
