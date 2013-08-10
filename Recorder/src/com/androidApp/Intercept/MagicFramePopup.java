package com.androidApp.Intercept;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

/** 
 * for intercepting the back key from a popup window
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class MagicFramePopup extends MagicFrame {
	private static final String 		TAG = "MagicFramePopup";
	protected FrameLayout 				mPopupViewContainer;
	
	/**
	 * variant for popup window contents
	 * TODO: When we detect the dismiss, we have to bring up the popup menu in the first place, otherwise the generated code dismisses a non-existent
	 * popup menu. 
	 * TODO: this doesn't work for AutoCompleteTextView dropdowns
	 * @param context
	 * @param popupWindow
	 * @param recorder
	 * @param viewInterceptor
	 */
	public MagicFramePopup(Context context, PopupWindow popupWindow, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		super(context);
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
		try {
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
			this.setLayoutParams(layoutParams);
			mContentView = popupWindow.getContentView();
			mPopupViewContainer = (FrameLayout) ReflectionUtils.getFieldValue(popupWindow, PopupWindow.class, Constants.Fields.POPUP_VIEW);
			mPopupViewContainer.removeView(mContentView);
			this.addView(mContentView);
			mPopupViewContainer.addView(this);
            // we have to bypass setContentView() because it does nothing if the window is already showing, which it is.
            ReflectionUtils.setFieldValue(popupWindow, PopupWindow.class, Constants.Fields.CONTENT_VIEW, this);
			mPopupViewContainer.requestLayout();
			
			// we have to have focus, otherwise, we don't get the back key.
			this.requestFocus();
		} catch (Exception ex) {
			recorder.writeException(ex,  "trying to intercept popup window");
		}
	}
	
	  
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
		Log.i(TAG, "dispatch intercepted key event " + MagicFrame.keyEventToString(event));
		try {
			if ((mRecorder != null) && (mViewInterceptor != null)) {
				recordKeyEvent(event);
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_BACK);
				}
			} else {
				Log.i(TAG, "dispatchKeyEventPreIme viewInterceptor = " + mViewInterceptor + " recorder = " + mRecorder);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return super.dispatchKeyEventPreIme(event);
    }
  
}
