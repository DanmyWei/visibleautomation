package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;

import android.widget.PopupWindow;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;

/**
 * recorder wrapper to intercept popupWindow dismiss events
 * We have an exception case for popupWindows created by spinners, so the emitter can generate
 * custom widget-specific code.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class RecordAutoCompleteDropdownOnDismissListener extends RecordListener implements PopupWindow.OnDismissListener, IOriginalListener  {
	protected PopupWindow.OnDismissListener mOriginalOnDismissListener;
	protected ViewInterceptor				mViewInterceptor;
	protected View 							mAnchorView;
	protected String						mEventTag;
	protected PopupWindow					mPopupWindow;
	
	public RecordAutoCompleteDropdownOnDismissListener() {
	}
	
	public RecordAutoCompleteDropdownOnDismissListener(String							activityName,
													   EventRecorder 					eventRecorder, 
												       ViewInterceptor					viewInterceptor,
												       View								anchorView,
												       PopupWindow						popupWindow, 
												       PopupWindow.OnDismissListener 	originalDismissListener) {
		super(activityName, eventRecorder);
		mViewInterceptor = viewInterceptor;
		mOriginalOnDismissListener = originalDismissListener;
		mAnchorView = anchorView;
		mPopupWindow = popupWindow;
	}

	public Object getOriginalListener() {
		return mOriginalOnDismissListener;
	}

	/**
	 * dismiss can happen programmatically, or by the back key. We listen for the back key in the magic frames, 
	 * and if this event happens afterwrd, we record it as a "back key" dismiss, otherwise we record it as a 
	 * programmatic dismiss, which should be ignored.
	 * TODO: does this work in dialogs/popups?
	 */
	public void onDismiss() {
		boolean fReentryBlock = getReentryBlock();
		// clear the current popup window so it gets intercepted if it comes up again.
		mViewInterceptor.setCurrentPopupWindow(null);
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (mViewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_BACK){
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_AUTOCOMPLETE_DROPDOWN_BACK_KEY, mActivityName, mAnchorView, "dismiss autocomplete dropdown window");					
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.DISMISS_AUTOCOMPLETE_DROPDOWN, mActivityName, mAnchorView, "dismiss autocomplete dropdown window");
				}
				mViewInterceptor.setLastKeyAction(-1);
			} catch (Exception ex) {
				mEventRecorder.writeException(mActivityName, ex, Constants.EventTags.DISMISS_AUTOCOMPLETE_DROPDOWN);
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
