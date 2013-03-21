package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.widget.PopupWindow;
import android.view.View;

/**
 * recorder wrapper to intercept popupWindow dismiss events
 * We have an exception case for popupWindows created by spinners, so the emitter can generate
 * autocomplete and other custom widget-specific code.
 * @author Matthew
 *
 */

public class RecordPopupWindowOnDismissListener extends RecordListener implements PopupWindow.OnDismissListener {
	protected PopupWindow.OnDismissListener mOriginalOnDismissListener;
	protected View 							mAnchorView;
	protected String						mEventTag;
	protected PopupWindow					mPopupWindow;
	
	public RecordPopupWindowOnDismissListener() {
	}
	
	public RecordPopupWindowOnDismissListener(EventRecorder 				eventRecorder, 
											  View							anchorView,
											  PopupWindow					popupWindow, 
											  String						eventTag,
											  PopupWindow.OnDismissListener originalDismissListener) {
		super(eventRecorder);
		mOriginalOnDismissListener = originalDismissListener;
		mAnchorView = anchorView;
		mEventTag = eventTag;
		mPopupWindow = popupWindow;
	}


	public void onDismiss() {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				if (mAnchorView == null) {
					mEventRecorder.writeRecord(mEventTag, "dismiss popup window");
				} else {
					mEventRecorder.writeRecord(mEventTag, mAnchorView, "dismiss popup window");					
				}
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, Constants.EventTags.DISMISS_POPUP_WINDOW);
				ex.printStackTrace();
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
