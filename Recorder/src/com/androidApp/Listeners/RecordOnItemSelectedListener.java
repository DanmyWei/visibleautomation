package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * record item select for spinners and other adapter views
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnItemSelectedListener extends RecordListener implements AdapterView.OnItemSelectedListener, IOriginalListener  {
	protected AdapterView.OnItemSelectedListener	mOriginalItemSelectedListener;
	
	public RecordOnItemSelectedListener(EventRecorder eventRecorder, AdapterView adapterView) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		super(eventRecorder);
		mOriginalItemSelectedListener = ListenerIntercept.getItemSelectedListener(adapterView);
		adapterView.setOnItemSelectedListener(this);
	}

	
	public RecordOnItemSelectedListener(EventRecorder eventRecorder, AdapterView.OnItemSelectedListener	originalItemSelectedListener) {
		super(eventRecorder);
		mOriginalItemSelectedListener = originalItemSelectedListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalItemSelectedListener;
	}
	/**
	 * output:
	 * item_selected:<time>,position,<reference>,<description>
	 *  solo.pressSpinnerItem() only supports class index references.
	 *  @param parent parent adapter
	 *  @param view selected view
	 *  @param position index in adapter
	 *  @param id adapter item id
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (parent instanceof Spinner) {
					mEventRecorder.writeRecord(Constants.EventTags.SPINNER_ITEM_SELECTED, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.ITEM_SELECTED, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
					
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, view, "item selected");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalItemSelectedListener != null) {
				mOriginalItemSelectedListener.onItemSelected(parent, view, position, id);
			} 	
		}
		setEventBlock(false);
	}

	/**
	 * TODO: record this as well (maybe)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
