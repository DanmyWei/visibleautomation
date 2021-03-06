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

public class RecordPopupWindowOnDismissListener extends RecordListener implements PopupWindow.OnDismissListener, IOriginalListener  {
	protected PopupWindow.OnDismissListener mOriginalOnDismissListener;
	protected ViewInterceptor				mViewInterceptor;
	protected View 							mAnchorView;
	protected String						mEventTag;
	protected PopupWindow					mPopupWindow;
	
	public RecordPopupWindowOnDismissListener() {
	}
	
	public RecordPopupWindowOnDismissListener(String						activityName,
											  EventRecorder 				eventRecorder, 
											  ViewInterceptor				viewInterceptor,
											  View							anchorView,
											  PopupWindow					popupWindow, 
											  PopupWindow.OnDismissListener originalDismissListener) {
		super(activityName, eventRecorder);
		mViewInterceptor = viewInterceptor;
		mOriginalOnDismissListener = originalDismissListener;
		mAnchorView = anchorView;
		mPopupWindow = popupWindow;
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
				// differentiate between popups that have anchor views and popups that were dismissed using the 
				// back key (recorded by MagicFrame)
				if (mAnchorView == null) {
					if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK){
						mEventRecorder.writeRecord(mActivityName, Constants.EventTags.DISMISS_POPUP_WINDOW_BACK_KEY, "dismiss popup window");					
					} else {
						mEventRecorder.writeRecord(mActivityName, Constants.EventTags.DISMISS_POPUP_WINDOW, "dismiss popup window");
					}
				} else {
					if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK){
						mEventRecorder.writeRecord(Constants.EventTags.DISMISS_POPUP_WINDOW_BACK_KEY, mActivityName, mAnchorView, "dismiss popup window");					
					} else {
						mEventRecorder.writeRecord(Constants.EventTags.DISMISS_POPUP_WINDOW, mActivityName, mAnchorView, "dismiss popup window");	
					}
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
