package com.androidApp.SupportListeners;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
import com.androidApp.Intercept.MagicOverlay;
import com.androidApp.Intercept.MagicOverlayDialog;
import com.androidApp.Test.SetupListeners;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ShowToastRunnable;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Listeners.IOriginalListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnMenuItemClickListener;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * window.callback record function to intercept stuff like back and home key events.  
 * TODO: see if this can be applied universally
 * @author mattrey
 * TODO: create a function so the textChangedListener can pick up the magic frame and get the mfKeyHit value, since
 * using a static is absolutely EVIL
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordWindowCallback extends RecordListener implements Window.Callback, IOriginalListener {
	protected static final String 		TAG = "RecordWindowCallback";
	protected Window.Callback 			mOriginalCallback;					// AN ACTIVITY! NO KIDDING!
	protected ViewInterceptor			mViewInterceptor;					// to retain state information to remember the last key or touch
	protected Context					mContext;							// so we can toast
	protected Window					mWindow;							// so we can get the root view
	protected long						EVENT_RECORD_TIMEOUT_MSEC = 500;	// timeout before event can be recorded
	protected Activity					mActivity;
	protected boolean					mfTouchDown;						// touch down was fired.
	
	public RecordWindowCallback(Window			window,
								Activity		activity,
								Context			context,
								EventRecorder 	eventRecorder, 
							    ViewInterceptor	viewInterceptor,
							    Window.Callback originalCallback) {
		super(activity.getClass().getName(), eventRecorder);
		mWindow = window;
		mActivity = activity;
		mOriginalCallback = originalCallback;
		mViewInterceptor = viewInterceptor;
		mContext = context;
		mfTouchDown = false;
	}
	
	public Object getOriginalListener() {
		return mOriginalCallback;
	}

	/**
	 * We like to listen to the menu, back, and home keys, and unfortunately, there's no listener,
	 * so we intercept the window callback, which is normally an activity or something like that, and
	 * replace the dispatch key event 
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.i(TAG, "dispatchKeyEvent action = " + event.getAction() + " keyCode = " + event.getKeyCode());
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			mEventRecorder.setEventRecorded(false);
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				mEventRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.MENU_BACK_KEY);
				break;
			case KeyEvent.KEYCODE_MENU:
				mEventRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.MENU_MENU_KEY);
				break;
			case KeyEvent.KEYCODE_HOME:
				mEventRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.KEY_HOME);
				break;
			} 
		}
		mViewInterceptor.setLastKeyAction(event.getKeyCode());
		return mOriginalCallback.dispatchKeyEvent(event);
	}

	/**
	 * sometimes, actually pretty often, views get created dynamically, and the global layout listener and on
	 * hierarchy changed listeners don't handle the incoming event.  
	 */
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// at this point, the event hasn't been recorded. and we're a real boy! so that means
			// that listeners should actually record us, as opposed to self-events.
			mEventRecorder.setEventRecorded(false);
			mEventRecorder.setTouchedDown(true);
		
			// get the content view from the app.  If we've inserted the magic frame (and frankly, we should have
			// at this point), the content is the first child of the magic frame.
			View contentView = ((ViewGroup) mWindow.getDecorView()).getChildAt(0);
			if (contentView instanceof MagicFrame) {
				contentView = ((ViewGroup) contentView).getChildAt(0);
			}
			List<View> hitViews = getHitViews((int) event.getX(), (int) event.getY(), contentView);
			mViewInterceptor.interceptList(mActivity, mActivity.toString(), hitViews);
		}
		Log.i(TAG, "dispatch touchEvent action = " + event.getAction());
		return mOriginalCallback.dispatchTouchEvent(event);
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		mEventRecorder.setEventRecorded(false);
		Log.i(TAG, "dispatch trackballEvent action = " + event.getAction());
		return mOriginalCallback.dispatchTrackballEvent(event);
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		Log.i(TAG, "dispatch populateAcessibility event text = " + event.getText());
		return mOriginalCallback.dispatchPopulateAccessibilityEvent(event);
	}

	@Override
	public View onCreatePanelView(int featureId) {
		Log.i(TAG, "onCreatePanelView featureId = " + featureId);
		return mOriginalCallback.onCreatePanelView(featureId);
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		Log.i(TAG, "onCreatePanelMenu featureId = " + featureId);
		return mOriginalCallback.onCreatePanelMenu(featureId, menu);
	}

	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		Log.i(TAG, "onPreparePanel featureId = " + featureId);
		return mOriginalCallback.onPreparePanel(featureId, view, menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		Log.i(TAG, "onMenuOpened featureId = " + featureId);
		if (menu != null) {
			try {
				for (int iItem = 0; iItem < menu.size(); iItem++) {
					MenuItem menuItem = menu.getItem(iItem);
					MenuItem.OnMenuItemClickListener originalClickListener = ListenerIntercept.getOnMenuItemClickListener(menuItem);
					if (!(originalClickListener instanceof RecordOnMenuItemClickListener)) {
						RecordOnMenuItemClickListener recordOnMenuItemClickListener = new RecordOnMenuItemClickListener(mActivity.toString(), mEventRecorder, originalClickListener);
						menuItem.setOnMenuItemClickListener(recordOnMenuItemClickListener);
					}
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(mActivityName, ex, "while attempting to intercept menu on click listener");
			}
		}
		return mOriginalCallback.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "onMenuItemSelected featureId = " + featureId);
		return mOriginalCallback.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onWindowAttributesChanged(LayoutParams attrs) {
		Log.i(TAG, "onWindowAttributesChanged");
		mOriginalCallback.onWindowAttributesChanged(attrs);
	}

	@Override
	public void onContentChanged() {
		Log.i(TAG, "onContentChanged");
		mOriginalCallback.onContentChanged();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.i(TAG, "onWindowFocusChanged hasFocus = " + hasFocus);
		mOriginalCallback.onWindowFocusChanged(hasFocus);
		
	}

	@Override
	public void onAttachedToWindow() {
		Log.i(TAG, "onAttachedToWindow");
		mOriginalCallback.onAttachedToWindow();
		
	}

	@Override
	public void onDetachedFromWindow() {
		Log.i(TAG, "onDetachedFromWindow");
		mOriginalCallback.onDetachedFromWindow();
		
	}

	@Override
	public void onPanelClosed(int featureId, Menu menu) {
		Log.i(TAG, "onPanelClosed featureId = " + featureId);
		if (featureId == 0) {
			mViewInterceptor.setCurrentOptionsMenuView(null);
			mEventRecorder.writeRecordTime(mActivityName, Constants.EventTags.CLOSE_OPTIONS_MENU);
		}
		mOriginalCallback.onPanelClosed(featureId, menu);
		
	}

	@Override
	public boolean onSearchRequested() {
		Log.i(TAG, "onSearchRequested");
		return mOriginalCallback.onSearchRequested();
	}

	public EventRecorder getEventRecorder() {
		return mEventRecorder;
	}
		
	// given an event x,y, return the views that were hit.
	public List<View> getHitViews(int eventX, int eventY, View contentView) {
		List<View> hitViews = new ArrayList<View>();
		Rect viewRect = new Rect();
		getHitViews(eventX, eventY, contentView, viewRect, hitViews);
		return hitViews;
	}
	
	private void getHitViews(int eventX, int eventY, View v, Rect viewRect, List<View> hitViews) {
		if (v.isShown() && v.isEnabled()) {
			v.getGlobalVisibleRect(viewRect);
			if (viewRect.contains(eventX, eventY)) {
				hitViews.add(v);
				if (v instanceof ViewGroup) {
					ViewGroup vg = (ViewGroup) v;
					for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
						View vChild = vg.getChildAt(iChild);
						getHitViews(eventX, eventY, vChild, viewRect, hitViews);
					}
				} 
			}
		} 
	}
}
