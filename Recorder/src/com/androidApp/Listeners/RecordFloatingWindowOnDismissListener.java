package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.widget.PopupWindow;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;

/**
 * recorder wrapper to intercept popupWindow dismiss events
 * We have an exception case for popupWindows created by spinners, so the emitter can generate
 * autocomplete and other custom widget-specific code.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class RecordFloatingWindowOnDismissListener extends RecordListener implements PopupWindow.OnDismissListener, IOriginalListener  {
	protected PopupWindow.OnDismissListener mOriginalOnDismissListener;
	protected String						mEventTag;
	protected PopupWindow					mPopupWindow;
	protected ViewInterceptor				mViewInterceptor;
	
	public RecordFloatingWindowOnDismissListener() {
	}
	
	public RecordFloatingWindowOnDismissListener(String							activityName,
												 EventRecorder 					eventRecorder, 
											     ViewInterceptor				viewInterceptor,
											     PopupWindow.OnDismissListener 	originalDismissListener) {
		super(activityName, eventRecorder);
		mViewInterceptor = viewInterceptor;
		mOriginalOnDismissListener = originalDismissListener;
	}

	public Object getOriginalListener() {
		return mOriginalOnDismissListener;
	}

	public void onDismiss() {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				// clear the current popup window so it gets intercepted if it comes up again.
				mViewInterceptor.setCurrentPopupWindow(null);
				if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK){
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.DISMISS_POPUP_WINDOW_BACK_KEY, "dismiss popup window");					
				} else {
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.DISMISS_POPUP_WINDOW, "dismiss popup window");
				}
				mViewInterceptor.setLastKeyAction(-1);
			} catch (Exception ex) {
				mEventRecorder.writeException(mActivityName, ex, Constants.EventTags.DISMISS_POPUP_WINDOW);
			}
		}
		if (!fReentryBlock) {
			
			// always call the original onDismiss listener
			if (mOriginalOnDismissListener != null) {
				mOriginalOnDismissListener.onDismiss();
			} 
		}
		setEventBlock(false);
	}
}
