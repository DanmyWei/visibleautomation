package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;

import android.view.View;

public class RecordOnAttachStateChangeListener extends RecordListener implements View.OnAttachStateChangeListener {
	protected View.OnAttachStateChangeListener 	mOriginalOnAttachStateChangeListener;
	protected String							mViewReference;					// can't get reference when view is detached
	
	public RecordOnAttachStateChangeListener(String activityName, EventRecorder eventRecorder, String viewReference) {
		super(activityName, eventRecorder);
		mViewReference = viewReference;
	}
	
	// IOriginalListener implementation
	public Object getOriginalListener() {
		return null;
	}

	@Override
	public void onViewAttachedToWindow(View v) {
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(Constants.EventTags.VIEW_DETACH, mActivityName, mViewReference, getDescription(v));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, v, "on detach");
			}
		}
		setEventBlock(false);
	}
}
