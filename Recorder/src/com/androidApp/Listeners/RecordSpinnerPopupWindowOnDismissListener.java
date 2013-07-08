package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;

import android.widget.PopupWindow;
import android.view.KeyEvent;
import android.view.View;

/**
 * recorder wrapper to intercept popupWindow dismiss events
 * We have an exception case for popupWindows created by spinners, so the emitter can generate
 * custom widget-specific code.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class RecordSpinnerPopupWindowOnDismissListener extends RecordListener implements PopupWindow.OnDismissListener, IOriginalListener  {
	protected PopupWindow.OnDismissListener mOriginalOnDismissListener;
	protected ViewInterceptor				mViewInterceptor;
	protected View 							mAnchorView;
	protected String						mEventTag;
	protected PopupWindow					mPopupWindow;
	
	public RecordSpinnerPopupWindowOnDismissListener() {
	}
	
	public RecordSpinnerPopupWindowOnDismissListener(EventRecorder 					eventRecorder, 
												     ViewInterceptor				viewInterceptor,
												     View							anchorView,
												     PopupWindow					popupWindow, 
												     PopupWindow.OnDismissListener 	originalDismissListener) {
		super(eventRecorder);
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
				
				// check to see if the user hit the back key to dismiss this popup
				if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK){
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_SPINNER_POPUP_WINDOW_BACK_KEY, mAnchorView, "dismiss spinner popup window");					
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_SPINNER_POPUP_WINDOW, mAnchorView, "dismiss spinner popup window");
				}
				mViewInterceptor.setLastKeyAction(-1);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, Constants.EventTags.DISMISS_POPUP_WINDOW);
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
