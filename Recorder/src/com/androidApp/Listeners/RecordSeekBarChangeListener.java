package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.SeekBar;

// record a change in a seekbar listener
public class RecordSeekBarChangeListener extends RecordListener implements SeekBar.OnSeekBarChangeListener {
	protected SeekBar.OnSeekBarChangeListener 	mOriginalOnSeekBarChangeListener;
	protected EventRecorder						mEventRecorder;
	
	public RecordSeekBarChangeListener(EventRecorder eventRecorder, SeekBar seekbar) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalOnSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekbar);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordSeekBarChangeListener(EventRecorder eventRecorder, SeekBar.OnSeekBarChangeListener originalOnSeekBarChangeListener) {
		mEventRecorder = eventRecorder;
		mOriginalOnSeekBarChangeListener = originalOnSeekBarChangeListener;
	}
	

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			long time = SystemClock.uptimeMillis();
			try {
				String description = getDescription(seekBar);
				String logString = Constants.EventTags.PROGRESS_CHANGED + ":" + time + "," + progress + "," + mEventRecorder.getViewReference().getReference(seekBar) + "," + description;
				mEventRecorder.writeRecord(logString);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (mOriginalOnSeekBarChangeListener != null) {
				mOriginalOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
			} 
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.START_TRACKING + ":" + time + "," + mEventRecorder.getViewReference().getReference(seekBar);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnSeekBarChangeListener != null) {
			mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
		} 		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		long time = SystemClock.uptimeMillis();
		try {
			String logString = Constants.EventTags.STOP_TRACKING + ":" + time + "," + mEventRecorder.getViewReference().getReference(seekBar);
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalOnSeekBarChangeListener != null) {
			mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
		} 
	}

}
