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
import com.androidApp.Test.ActivityInterceptor;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;


/**
 * runnable class to actually insert the magic frame in the view hierarchy.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class InsertMagicFrameRunnable implements Runnable {
	protected Window							mWindow;					// window containing views of interest
	protected EventRecorder 					mRecorder;					// event recorder interface
	protected ViewInterceptor 					mViewInterceptor;			// to intercept the views contained in the window
	protected Activity							mActivity;					// activity backreference for dialogs
	
	public InsertMagicFrameRunnable(Activity activity, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mWindow = activity.getWindow();
		mActivity = activity;
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
			MagicFrame magicFrame = new MagicFrame(mWindow.getContext(), contentView, 0, mRecorder, mViewInterceptor);
			MagicOverlay.addMagicOverlay(mActivity, magicFrame, mRecorder);
		} catch (Exception ex) {
			mRecorder.writeException(ex, "attempting to insert magic frame");
		}
	}
}
