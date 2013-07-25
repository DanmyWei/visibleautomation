package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.widget.NumberPicker;
import android.widget.TabHost;

/**
 * class for listening to tab events (the old, non-actionbar style tab events)
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnValueChangeListener extends RecordListener implements NumberPicker.OnValueChangeListener, IOriginalListener {
	protected NumberPicker.OnValueChangeListener 	mOriginalValueChangeListenerListener;
	protected NumberPicker							mNumberPicker;
	
	public RecordOnValueChangeListener(EventRecorder eventRecorder, NumberPicker numberPicker) {
		super(eventRecorder);
		try {
			mNumberPicker = numberPicker;
			mOriginalValueChangeListenerListener = ListenerIntercept.getValueChangeListener(numberPicker);
			numberPicker.setOnValueChangedListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnValueChangeListener(EventRecorder eventRecorder, 
									   NumberPicker.OnValueChangeListener originalValueChangeListener, 
									   NumberPicker numberPicker) {
		super(eventRecorder);
		try {
			mNumberPicker = numberPicker;
			mNumberPicker.setOnValueChangedListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	@Override
	public void onValueChange(NumberPicker arg0, int oldVal, int newVal) {
		String msg = Integer.toString(oldVal) + "," + Integer.toString(newVal);
		mEventRecorder.writeRecord(Constants.EventTags.VALUE_CHANGE, mNumberPicker, msg);
		
	}	
}
