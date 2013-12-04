package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.SeekBar;

/**
 * record a change in a seekbar listener
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordSeekBarChangeListener extends RecordListener implements SeekBar.OnSeekBarChangeListener, IOriginalListener  {
	protected SeekBar.OnSeekBarChangeListener 	mOriginalOnSeekBarChangeListener;
	
	public RecordSeekBarChangeListener(String activityName, EventRecorder eventRecorder, SeekBar seekbar) {
		super(activityName, eventRecorder);
		try {
			mOriginalOnSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekbar);
			seekbar.setOnSeekBarChangeListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, seekbar, "create on progress changed listener");
		}		
	}
	
	public RecordSeekBarChangeListener(Activity activity, EventRecorder eventRecorder, SeekBar.OnSeekBarChangeListener originalOnSeekBarChangeListener) {
		super(activity.getClass().getName(), eventRecorder);
		mOriginalOnSeekBarChangeListener = originalOnSeekBarChangeListener;
	}

	public Object getOriginalListener() {
		return mOriginalOnSeekBarChangeListener;
	}

	/**
	 * intercept on progress changed
	 * progress_changed:time,progress,<reference>,<description>
	 * NOTE: we should also record fromUser
	 * @param seekBar seekbar 
	 * @param progress progress value (probably 0-100)
	 * @param fromUser event was kicked off from user
	 */
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			if (fromUser) {
				try {
					String description = getDescription(seekBar);
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.PROGRESS_CHANGED, progress + "," + mEventRecorder.getViewReference().getReference(seekBar) + "," + description);
				} catch (Exception ex) {
					mEventRecorder.writeException(ex, mActivityName, seekBar, "on progress changed");
				}
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnSeekBarChangeListener != null) {
				mOriginalOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
			} 
		}
		setEventBlock(false);
	}

	/**
	 * record onStartTrackingTouch
	 */
	public void onStartTrackingTouch(SeekBar seekBar) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.START_TRACKING, mEventRecorder.getViewReference().getReference(seekBar));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, seekBar, "on start tracking touch");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnSeekBarChangeListener != null) {
				mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
			} 		
		}
		setEventBlock(false);
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.STOP_TRACKING, mEventRecorder.getViewReference().getReference(seekBar));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, seekBar, "on stop tracking touch");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnSeekBarChangeListener != null) {
				mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
			} 
		}
		setEventBlock(false);

	}
}
