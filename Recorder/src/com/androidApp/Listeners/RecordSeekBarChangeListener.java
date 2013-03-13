package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.SeekBar;

// record a change in a seekbar listener
public class RecordSeekBarChangeListener extends RecordListener implements SeekBar.OnSeekBarChangeListener {
	protected SeekBar.OnSeekBarChangeListener 	mOriginalOnSeekBarChangeListener;
	
	public RecordSeekBarChangeListener(EventRecorder eventRecorder, SeekBar seekbar) {
		super(eventRecorder);
		try {
			mOriginalOnSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekbar);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordSeekBarChangeListener(EventRecorder eventRecorder, SeekBar.OnSeekBarChangeListener originalOnSeekBarChangeListener) {
		super(eventRecorder);
		mOriginalOnSeekBarChangeListener = originalOnSeekBarChangeListener;
	}
	

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			if (fromUser) {
				try {
					String description = getDescription(seekBar);
					mEventRecorder.writeRecord( Constants.EventTags.PROGRESS_CHANGED, progress + "," + mEventRecorder.getViewReference().getReference(seekBar) + "," + description);
					if (mOriginalOnSeekBarChangeListener != null) {
						mOriginalOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
					} 
				} catch (Exception ex) {
					mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, seekBar, "on progress changed");
					ex.printStackTrace();
				}
			}
			mfReentryBlock = false;
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.START_TRACKING, mEventRecorder.getViewReference().getReference(seekBar));
				if (mOriginalOnSeekBarChangeListener != null) {
					mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
				} 		
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, seekBar, "on start tracking touch");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.STOP_TRACKING, mEventRecorder.getViewReference().getReference(seekBar));
				if (mOriginalOnSeekBarChangeListener != null) {
					mOriginalOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, seekBar, "on stop tracking touch");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}
}
