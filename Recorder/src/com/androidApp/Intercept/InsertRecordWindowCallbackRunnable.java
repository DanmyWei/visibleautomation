package com.androidApp.Intercept;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
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
	protected Activity			mActivity;
	protected EventRecorder 	mRecorder;					// event recorder interface
	protected ViewInterceptor 	mViewInterceptor;			// to intercept the views contained in the window
	
	public InsertRecordWindowCallbackRunnable(Window 			window, 
											  Activity			activity,
											  EventRecorder 	recorder, 
											  ViewInterceptor 	viewInterceptor) {
		mWindow = window;
		mActivity = activity;
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
	}
	
	/**
	 * replace the window callback with our interceptor
	 */
	public void run() {
		try {
			mViewInterceptor.getInterceptInterface().interceptWindow(mWindow, mActivity, mRecorder, mViewInterceptor);
		} catch (Exception ex) {
			mRecorder.writeException(mActivity.getClass().getName(), ex, "installing window callback recorder");
		}
	}

}
