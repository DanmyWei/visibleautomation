package com.androidApp.SupportListeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

/**
 * window.callback record function to intercept stuff like back and home key events.  
 * TODO: see if this can be applied universally
 * @author mattrey
 * TODO: create a function so the textChangedListener can pick up the magic frame and get the mfKeyHit value, since
 * using a static is absolutely EVIL
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordWindowCallback extends com.androidApp.Listeners.RecordWindowCallback {
	protected static final String 		TAG = "RecordWindowCallback";
	
	public RecordWindowCallback(Window			window,
								Activity		activity,
								Context			context,
								EventRecorder 	eventRecorder, 
							    ViewInterceptor	viewInterceptor,
							    Window.Callback originalCallback) {
		super(window, activity, context, eventRecorder, viewInterceptor, originalCallback);
	}
}
