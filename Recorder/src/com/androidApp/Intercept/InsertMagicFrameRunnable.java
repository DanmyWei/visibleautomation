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
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class InsertMagicFrameRunnable implements Runnable {
	protected Activity mActivity;
	protected EventRecorder mRecorder;
	protected ViewInterceptor mViewInterceptor;
	
	public InsertMagicFrameRunnable(Activity activity, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		mActivity = activity;
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
	}
	
	public void run() {
		try {
			Window window = mActivity.getWindow();
			ViewGroup decorView = (ViewGroup) window.getDecorView();
			ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);
			MagicFrame magicFrame = new MagicFrame(mActivity, contentView, 0, mRecorder, mViewInterceptor);
			/*
			if (contentView instanceof LinearLayout) {
				for (int iChild = 0; iChild < contentView.getChildCount(); iChild++) {
					View realContentView = (View) contentView.getChildAt(iChild);
					if (realContentView instanceof ViewGroup) {
						MagicFrame magicFrame = new MagicFrame(mActivity, realContentView, iChild, mRecorder, mViewInterceptor);
					}
				} 
			}
			*/
		} catch (Exception ex) {
			mRecorder.writeRecord(Constants.EventTags.EXCEPTION, "attempting to insert magic frame");
			ex.printStackTrace();
		}
	}
}
