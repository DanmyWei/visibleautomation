package com.androidApp.Listeners;
import java.util.TimerTask;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Intercept.MagicOverlay;
import com.androidApp.Intercept.MagicOverlayDialog;
import com.androidApp.Test.SetupListeners;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ShowToastRunnable;
import com.androidApp.Utility.TestUtils;

import android.app.Instrumentation;
import android.content.Context;
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
	protected RecordedTouchTimerTask	mEventRecordTimeoutTask = null;
	
	public RecordWindowCallback(Window			window,
								Context			context,
								EventRecorder 	eventRecorder, 
							    ViewInterceptor	viewInterceptor,
							    Window.Callback originalCallback) {
		super(eventRecorder);
		mWindow = window;
		mOriginalCallback = originalCallback;
		mViewInterceptor = viewInterceptor;
		mContext = context;
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
		mViewInterceptor.setLastKeyAction(event.getKeyCode());
		return mOriginalCallback.dispatchKeyEvent(event);
	}

	/**
	 * since we've wrapped the entire callback, we have to wrap all of its interface, otherwise, there will be trouble.
	 */
	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		mEventRecorder.setEventRecorded(false);
		Log.i(TAG, "dispatchKeyShortcutEvent action = " + event.getAction() + " keyCode = " + event.getKeyCode());
		return mOriginalCallback.dispatchKeyShortcutEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mEventRecorder.setEventRecorded(false);
			if (mEventRecordTimeoutTask != null) {
				mEventRecordTimeoutTask.cancel();
			}
			View contentView = mWindow.getDecorView();
			mEventRecordTimeoutTask = new RecordedTouchTimerTask(contentView, event);
			SetupListeners.getScanTimer().schedule(new RecordedTouchTimerTask(contentView, event), EVENT_RECORD_TIMEOUT_MSEC);
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
	public boolean dispatchGenericMotionEvent(MotionEvent event) {
		mEventRecorder.setEventRecorded(false);
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
		if (menu != null) {
			try {
				for (int iItem = 0; iItem < menu.size(); iItem++) {
					MenuItem menuItem = menu.getItem(iItem);
					MenuItem.OnMenuItemClickListener originalClickListener = ListenerIntercept.getOnMenuItemClickListener(menuItem);
					if (!(originalClickListener instanceof RecordOnMenuItemClickListener)) {
						RecordOnMenuItemClickListener recordOnMenuItemClickListener = new RecordOnMenuItemClickListener(mEventRecorder, originalClickListener);
						menuItem.setOnMenuItemClickListener(recordOnMenuItemClickListener);
					}
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, "while attempting to intercept menu on click listener");
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
	
	public EventRecorder getEventRecorder() {
		return mEventRecorder;
	}
	
	/**
	 * so, what's happened here is that the user has clicked on something and it's ben detected by
	 * RecordWindowCallback, but none of the listeners have written the event using the event recorder before the
	 * specified time, so we try to find the view that the user clicked on, and tell him with a toast that
	 * the event wasn't listened to. This is handy for finding custom widgets that use custom listeners.
	 * Now, the user may have clicked another object before the timer has expired, and the event recorder
	 * just uses a flag saying, "hey, we haven't recorded the last event". In theory, I should have a  
	 * flag stack for each touch event, but the actual event is out of scope by the time it's fired, so it
	 * would be a huge pain in the ass to match up against for what is really a convenience feature.
	 * Since this is based on a timer, there's an implicit race condition, but since it's just "informative"
	 * we don't really care too much.
	 * @author matt2
	 *
	 */
	public class RecordedTouchTimerTask extends TimerTask {
		protected View		mViewRoot;						// root view to recurse from to find offending view
		protected float 	mEventX;						//  motion event info
		protected float 	mEventY;
		protected int 		mEventAction;
		
		// remember, the event queue can trash events, so we just store what we need.
		public RecordedTouchTimerTask(View viewRoot, MotionEvent motionEvent) {
			mViewRoot = viewRoot;
			mEventAction = motionEvent.getAction();
			mEventX = motionEvent.getX();
			mEventY = motionEvent.getY();
		}
		
		public void run() {
			RecordWindowCallback.this.mEventRecordTimeoutTask = null;
			if (!RecordWindowCallback.this.getEventRecorder().eventWasRecorded()) {
				View target = TestUtils.findViewByXY(mViewRoot, mEventX, mEventY);
				if ((target != null) && !(target instanceof MagicOverlay) && !(target instanceof MagicOverlayDialog)) {
					String toastMsg = "event not recorded for " + target.getClass().getSimpleName();
					Instrumentation instrumentation = RecordWindowCallback.this.mEventRecorder.getInstrumentation();
					ShowToastRunnable.showToast(instrumentation, RecordWindowCallback.this.mContext, toastMsg);
				}
			}
		}
	}

}
