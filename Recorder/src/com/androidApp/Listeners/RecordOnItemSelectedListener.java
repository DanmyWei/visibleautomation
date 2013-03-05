package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

// record item Selects for spinners
public class RecordOnItemSelectedListener extends RecordListener implements AdapterView.OnItemSelectedListener {
	protected AdapterView.OnItemSelectedListener	mOriginalItemSelectedListener;
	
	public RecordOnItemSelectedListener(EventRecorder eventRecorder, Spinner spinner) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		super(eventRecorder);
		mOriginalItemSelectedListener = ListenerIntercept.getItemSelectedListener(spinner);
	}

	
	public RecordOnItemSelectedListener(EventRecorder eventRecorder, AdapterView.OnItemSelectedListener	originalItemSelectedListener) {
		super(eventRecorder);
		mOriginalItemSelectedListener = originalItemSelectedListener;
	}

	// solo.pressSpinnerItem() only supports class index references.
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		long time = SystemClock.uptimeMillis();
		try {
			String description = getDescription(view);
			String logString = Constants.EventTags.ITEM_SELECTED + ":" + time + ", "+ position + "," + mEventRecorder.getViewReference().getClassIndexReference(parent) + "," + description;
			mEventRecorder.writeRecord(logString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (mOriginalItemSelectedListener != null) {
			mOriginalItemSelectedListener.onItemSelected(parent, view, position, id);
		} 
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
