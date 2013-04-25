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

public class MagicFramePopup extends MagicFrame {
	private static final String 		TAG = "MagicFramePopup";
	protected FrameLayout 				mPopupViewContainer;
	
	/**
	 * variant for popup window contents
	 * @param context
	 * @param popupWindow
	 * @param recorder
	 * @param viewInterceptor
	 */
	public MagicFramePopup(Context context, PopupWindow popupWindow, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		super(context);
		mRecorder = recorder;
		try {
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
			this.setLayoutParams(layoutParams);
			mContentView = popupWindow.getContentView();
			mPopupViewContainer = (FrameLayout) ReflectionUtils.getFieldValue(popupWindow, PopupWindow.class, Constants.Fields.POPUP_VIEW);
		    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		    WindowManager.LayoutParams windowManagerLayoutParams = (WindowManager.LayoutParams) mPopupViewContainer.getLayoutParams();
			windowManager.removeView(mPopupViewContainer);
			this.addView(mPopupViewContainer);
			windowManager.addView(this, windowManagerLayoutParams);
			ReflectionUtils.setFieldValue(popupWindow, PopupWindow.class, Constants.Fields.POPUP_VIEW, this);
			this.requestLayout();
		} catch (Exception ex) {
			recorder.writeRecord(Constants.EventTags.EXCEPTION, "trying to intercept popup window");
		}
		mViewInterceptor = viewInterceptor;
		init();
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		Log.i(TAG, "dispatch intercepted key event " + MagicFrame.keyEventToString(event));
		mPopupViewContainer.dispatchKeyEvent(event);
		return false;
    }
}
