package com.androidApp.Intercept;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;


/**
 * runnable class to actually insert the magic frame in the view hierarchy.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class InsertMagicFrameRunnable implements Runnable {
	protected Window			mWindow;					// window containing views of interest
	protected EventRecorder 	mRecorder;					// event recorder interface
	protected ViewInterceptor 	mViewInterceptor;			// to intercept the views contained in the window
	
	public InsertMagicFrameRunnable(Activity activity, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mWindow = activity.getWindow();
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
	}
	
	/**
	 * insert magic frames on the children of this window's decor view
	 */
	public void run() {
		try {
			ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
			ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);		
			if (contentView instanceof ViewGroup) {
				for (int iChild = 0; iChild < contentView.getChildCount(); iChild++) {
					View realContentView = (View) contentView.getChildAt(iChild);
					MagicFrame magicFrame = new MagicFrame(mWindow.getContext(), realContentView, iChild, mRecorder, mViewInterceptor);
				} 
			}
		} catch (Exception ex) {
			mRecorder.writeException(ex, "attempting to insert magic frame");
		}
	}
}
