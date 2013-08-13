package com.androidApp.Intercept;

import android.view.View;
import android.view.Window;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Listeners.RecordWindowCallback;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

/**
 * actually replace the window.callback (which is usually an activity) with our nefarious little recorder,
 * so we can record the forbidden hom and back key events, which Diane Hackborn said we couldn't dos
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class InsertRecordWindowCallbackRunnable implements Runnable {
	protected Window			mWindow;					// window containing views of interest
	protected EventRecorder 	mRecorder;					// event recorder interface
	protected ViewInterceptor 	mViewInterceptor;			// to intercept the views contained in the window
	
	public InsertRecordWindowCallbackRunnable(Window window, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mWindow = window;
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
	}
	
	/**
	 * replace the window callback with our interceptor
	 */
	public void run() {
		try {
			Window.Callback originalCallback = mWindow.getCallback();
			if (!(originalCallback instanceof RecordWindowCallback)) {
				RecordWindowCallback recordCallback = new RecordWindowCallback(mWindow, mWindow.getContext(), mRecorder, mViewInterceptor, originalCallback);
				mWindow.setCallback(recordCallback);
			}
		} catch (Exception ex) {
			mRecorder.writeException(ex, "installing window callback recorder");
		}
	}

}
