package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class RecordOnEditorActionListener extends RecordListener implements TextView.OnEditorActionListener, IOriginalListener  {
	protected TextView.OnEditorActionListener 	mOriginalOnEditorActionListener;
	
	public RecordOnEditorActionListener(EventRecorder eventRecorder, TextView textView) {
		super(eventRecorder);
		try {
			mOriginalOnEditorActionListener = ListenerIntercept.getOnEditorActionListener(textView);
			textView.setOnEditorActionListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, textView, "create on editor action listener");
		}		
	}
	
	public RecordOnEditorActionListener(EventRecorder eventRecorder, TextView.OnEditorActionListener originalOnEditorActionListener) {
		super(eventRecorder);
		mOriginalOnEditorActionListener = originalOnEditorActionListener;
	}

	@Override
	public boolean onEditorAction(TextView tv, int actionId, KeyEvent keyEvent) {
		if (mOriginalOnEditorActionListener != null) {
			return mOriginalOnEditorActionListener.onEditorAction(tv, actionId, keyEvent);
		}
		return false;
	}

}
