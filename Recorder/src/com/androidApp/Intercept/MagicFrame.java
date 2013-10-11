package com.androidApp.Intercept;


import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * frame which intercepts the key events, so we can intercept interesting keys like home and back
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class MagicFrame extends FrameLayout {
	private static final String 		TAG = "MagicFrame";
	protected EventRecorder				mRecorder;
	protected ViewInterceptor			mViewInterceptor;					// to log key events for activity/ime dismissal.
	protected View						mContentView;						// actual content view being masked
	protected Activity					mActivity;							// to associate events with an activity
	
	public static boolean isAlreadyInserted(Activity activity) {
		Window window = activity.getWindow();
		ViewGroup decorView = (ViewGroup) window.getDecorView();
		ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);
		return contentView instanceof MagicFrame;
	}

	// initialize the timer and paint so we can draw touch events for debugging.
	protected void init() {
		setWillNotDraw(false);
		setId(android.R.id.content);		
	}
	
	public MagicFrame(Context context) {
		super(context);
		init();
	}
	
	/**
	 * frame for intercepting pre-IME events, so we can pick up the back/home/menu keys
	 * @param context for view creation
	 * @param activity to associate logged events with activity
	 * @param contentView to reparent so we can intercept the pre-IME events.
	 * @param index index to insert within parent (when we have multiple elements to insert in a layout
	 * @param recorder to record events
	 * @param viewInterceptor to register the last action key.
	 */
	public MagicFrame(Context 			context, 
					  Activity			activity,
					  View 				contentView, 
					  int 				index, 
					  EventRecorder 	recorder, 
					  ViewInterceptor 	viewInterceptor) {
		super(context);
		mActivity = activity;
		this.setClipChildren(false);
		this.setClipToPadding(false);
		this.setMeasureAllChildren(true);
		mRecorder = recorder;
		mContentView = contentView;
		mViewInterceptor = viewInterceptor;
		init();
		insertInterceptor(contentView, index);
	}
	
	/**
	 * variant for ViewRootImpl
	 * @param context
	 * @param activity to associate logged events with activity
     * @param viewRootImpl
	 * @param recorder
	 * @param viewInterceptor
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public MagicFrame(Context context, Activity activity, ViewParent viewRootImpl, EventRecorder recorder, ViewInterceptor viewInterceptor) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException{
		super(context);
		mActivity = activity;
		this.setClipChildren(false);
		this.setClipToPadding(false);
		this.setMeasureAllChildren(true);
		mRecorder = recorder;
		
		// we have to do all this via reflection o bypass the guards put in the view parent/childing code
		Class viewRootImplClass = Class.forName(Constants.Classes.VIEW_ROOT_IMPL);
		mContentView = (View) ReflectionUtils.getFieldValue(viewRootImpl, viewRootImplClass, Constants.Fields.VIEW);
		ReflectionUtils.setFieldValue(viewRootImpl, viewRootImplClass, Constants.Fields.VIEW, this);
		ReflectionUtils.setFieldValue(mContentView, View.class, Constants.Fields.PARENT, null);
		this.addView(mContentView);
		mViewInterceptor = viewInterceptor;
		init();
	}
		
	/**
	 * wrapper to insert the magic frame.
	 * @param instrumentation - so we can run the runnable synchronized on the UI thread
	 * @param activityState - activity, list of magic overlays for this activity
	 * @param viewInterceptor - for intercepting events on views.
	 * @param recorder event recorder.
	 */
	public static void insertMagicFrame(Instrumentation instrumentation, 
									    Activity		activity,
									    EventRecorder 	recorder, 
									    ViewInterceptor viewInterceptor) {
		InsertMagicFrameRunnable runnable = new InsertMagicFrameRunnable(activity, recorder, viewInterceptor);
		instrumentation.runOnMainSync(runnable);
	}
	  
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
		Log.i(TAG, "dispatch intercepted key event " + MagicFrame.keyEventToString(event));
		try {
			if ((mRecorder != null) && (mViewInterceptor != null)) {
				recordKeyEvent(event);
			} else {
				Log.i(TAG, "dispatchKeyEventPreIme viewInterceptor = " + mViewInterceptor + " recorder = " + mRecorder);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		mViewInterceptor.setLastKeyAction(event.getKeyCode());
		return mContentView.dispatchKeyEventPreIme(event);
    }

    // actually record the key event. Only record on ACTION_UP. sometimes the window doesn't exist anymore if it's ACTION_DOWN
	// also, just check for a null view anyway
    public void recordKeyEvent(KeyEvent event) {
    	View v = null;
    	if (event.getAction() == KeyEvent.ACTION_UP) {
    		if (getChildCount() > 0) {
    			v = getChildAt(0);
    		}
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_BACK);
				if (v != null) {
					mRecorder.writeRecord(Constants.EventTags.KEY_BACK, mActivity.toString(), v);
				} else {
					mRecorder.writeRecordTime(mActivity.getClass().getName(), Constants.EventTags.KEY_BACK);
				}
				break;
			case KeyEvent.KEYCODE_MENU:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_MENU);
				if (v != null) {
					mRecorder.writeRecord(Constants.EventTags.KEY_MENU, mActivity.toString(), v);
				} else {
					mRecorder.writeRecordTime(mActivity.getClass().getName(), Constants.EventTags.KEY_MENU);
				}
				break;
			case KeyEvent.KEYCODE_HOME:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_HOME);
				if (v != null) {
					mRecorder.writeRecord(Constants.EventTags.KEY_HOME, mActivity.toString(), v);
				} else {
					mRecorder.writeRecordTime(mActivity.getClass().getName(), Constants.EventTags.KEY_HOME);
				}
				break;
			default:
				Log.i(TAG, "did not log key " + event.getKeyCode());
				break;
			} 
	    }
    }
    /**
     *  for debugging purposes
     * @param keyEvent
     * @return
     */
	public static String keyEventToString(KeyEvent keyEvent) {
		String action = "unknown";
		if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
			action = "down";
		} else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
			action = "up";
		}
		if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return action + " back";
		} else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HOME) {
			return action + " home";
		} else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			return action + " menu";
		} else {
			return Integer.toString(keyEvent.getKeyCode());
		}
	}
	
	/**
	 * insert the interceptor over the content view.
	 * @param contentView
	 * @param index
	 */
	public void insertInterceptor(View contentView, int index) {
		ViewGroup parentView = (ViewGroup) contentView.getParent();		
		// we have to reset the focused view because reparenting with the MagicFrame causes it to lose focus
		View focusedView = mViewInterceptor.getFocusedView();
		parentView.removeView(contentView);
		contentView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		this.addView(contentView);
		parentView.addView(this, index);
		if (focusedView != null) {
			//focusedView.requestFocus();
		}
	}
	
	public void onDetachedFromWindow() {
		this.removeAllViews();
	}
}
