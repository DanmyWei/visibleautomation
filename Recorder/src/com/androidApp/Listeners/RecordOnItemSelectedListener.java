package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
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
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.ITEM_SELECTED, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
				if (mOriginalItemSelectedListener != null) {
					mOriginalItemSelectedListener.onItemSelected(parent, view, position, id);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, view, "item selected");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
