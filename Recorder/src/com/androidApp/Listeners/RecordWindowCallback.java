package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * window.callback record function to intercept stuff like back and home key events.  TODO: see if this can be applieduniversally
 * @author mattrey
 * TODO: create a function so the textChangedListener can pick up the magic frame and get the mfKeyHit value, since
 * using a static is absolutely EVIL
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordWindowCallback extends RecordListener implements Window.Callback, IOriginalListener {
	protected static final String 	TAG = "RecordWindowCallback";
	protected Window.Callback 		mOriginalCallback;
	protected ViewInterceptor		mViewInterceptor;
	protected static boolean		sfKeyHit;				// to communicate with OnTextChangedListener
	
	public RecordWindowCallback(EventRecorder 	eventRecorder, 
							    ViewInterceptor	viewInterceptor,
							    Window.Callback originalCallback) {
		super(eventRecorder);
		mOriginalCallback = originalCallback;
		mViewInterceptor = viewInterceptor;
	}
	
	public Object getOriginalListener() {
		return mOriginalCallback;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.i(TAG, "dispatchKeyEvent action = " + event.getAction() + " keyCode = " + event.getKeyCode());
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				mEventRecorder.writeRecordTime(Constants.EventTags.MENU_BACK_KEY);
				break;
			case KeyEvent.KEYCODE_MENU:
				mEventRecorder.writeRecordTime(Constants.EventTags.MENU_MENU_KEY);
				break;
			case KeyEvent.KEYCODE_HOME:
				mEventRecorder.writeRecordTime(Constants.EventTags.KEY_HOME);
				break;
			} 
		}
		setWasKeyHit(true);
		return mOriginalCallback.dispatchKeyEvent(event);
	}

	public static boolean wasKeyHit() {
		return sfKeyHit;
	}
	
	public static void setWasKeyHit(boolean f) {
		sfKeyHit = f;
	}

	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		Log.i(TAG, "dispatchKeyShortcutEvent action = " + event.getAction() + " keyCode = " + event.getKeyCode());
		return mOriginalCallback.dispatchKeyShortcutEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.i(TAG, "dispatch touchEvent action = " + event.getAction());
		return mOriginalCallback.dispatchTouchEvent(event);
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		Log.i(TAG, "dispatch trackballEvent action = " + event.getAction());
		return mOriginalCallback.dispatchTrackballEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent event) {
		Log.i(TAG, "dispatch genericMotionEvent action = " + event.getAction());
		return mOriginalCallback.dispatchGenericMotionEvent(event);
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
		return mOriginalCallback.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "onMenuItemSelected featureId = " + featureId);
		mEventRecorder.writeRecord(Constants.EventTags.MENU_ITEM_CLICK, Integer.toString(item.getItemId()));
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
			mEventRecorder.writeRecordTime(Constants.EventTags.CLOSE_OPTIONS_MENU);
		}
		mOriginalCallback.onPanelClosed(featureId, menu);
		
	}

	@Override
	public boolean onSearchRequested() {
		Log.i(TAG, "onSearchRequested");
		return mOriginalCallback.onSearchRequested();
	}

	@Override
	public ActionMode onWindowStartingActionMode(Callback callback) {
		Log.i(TAG, "onWindowStartingActionMode");
		return mOriginalCallback.onWindowStartingActionMode(callback);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		Log.i(TAG, "onActionModeStarted");
		mOriginalCallback.onActionModeStarted(mode);
	}

	@Override
	public void onActionModeFinished(ActionMode mode) {
		Log.i(TAG, "onActionModeFinished");
		mOriginalCallback.onActionModeFinished(mode);	
	}

}
