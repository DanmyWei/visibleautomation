package com.androidApp.Intercept;

import android.view.View;
import android.view.Window;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Listeners.RecordWindowCallback;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

public class ViewInsertRecordWindowCallbackRunnable implements Runnable {
	protected View 				mView;
	protected EventRecorder 	mRecorder;
	protected ViewInterceptor	mViewInterceptor;
	
	public ViewInsertRecordWindowCallbackRunnable(View v, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mView = v;
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
	}
	
	public void run() {
		try {
			// got exception for this in options menu
			Window.Callback originalCallback = ListenerIntercept.getWindowCallbackFromDecorView(mView);
			if (!(originalCallback instanceof RecordWindowCallback)) {
				RecordWindowCallback recordCallback = new RecordWindowCallback(mRecorder, mViewInterceptor, originalCallback);
				ListenerIntercept.setWindowCallbackToDecorView(mView, recordCallback);
			}
		} catch (Exception ex) {
			mRecorder.writeRecord(Constants.EventTags.EXCEPTION, "installing window callback recorder");
		}
	}

}
