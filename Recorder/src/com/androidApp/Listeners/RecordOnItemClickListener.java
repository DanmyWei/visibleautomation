package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

// record item clicks for listviews
public class RecordOnItemClickListener extends RecordListener implements AdapterView.OnItemClickListener {
	protected AdapterView<?>					mAdapterView;
	protected AdapterView.OnItemClickListener	mOriginalItemClickListener;
	
	public RecordOnItemClickListener(EventRecorder eventRecorder, AdapterView<?> adapterView) {
		super(eventRecorder);
		mAdapterView = adapterView;
		mOriginalItemClickListener = adapterView.getOnItemClickListener();
	}
		
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (!mfReentryBlock) {
			mfReentryBlock = true;
			try {
				mEventRecorder.writeRecord(Constants.EventTags.ITEM_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
				if (mOriginalItemClickListener != null) {
					mOriginalItemClickListener.onItemClick(parent, view, position, id);
				} 
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, view, "item click");
				ex.printStackTrace();
			}
			mfReentryBlock = false;
		}
	}
}
