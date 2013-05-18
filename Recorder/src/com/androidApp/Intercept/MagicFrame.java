package com.androidApp.Intercept;

import java.util.Timer;
import java.util.TimerTask;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Listeners.RecordOnFocusChangeListener;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * frame which intercepts the key events, so we can intercept interesting keys like home and back
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class MagicFrame extends FrameLayout {
	private static final String 		TAG = "MagicFrame";
	protected static final int			POINT_SIZE = 30;
	protected static final int			POINT_DECREMENT = 3;
	protected static final int			TIMER_MSEC = 50;
	protected Point 					mTouchPoint = null;
	protected static Timer 				sTimer = null;
	protected TimerTask					mTimerTask = null;
	protected int						mSize = 0;
	protected Paint						mPaint;
	protected EventRecorder				mRecorder;
	protected ViewInterceptor			mViewInterceptor;					// to log key events for activity/ime dismissal.
	protected View						mContentView;						// actual content view being masked
	protected Rect						mHitRect;							// so we don't reallocate recursively while finding the target hit rect.

	// initialize the timer and paint so we can draw touch events for debugging.
	protected void init() {
		if (sTimer == null) {
			sTimer = new Timer();
		}
		mPaint = new Paint();
		mPaint.setStrokeWidth(10.0f);
		mPaint.setColor(0xffff0000);
		mHitRect = new Rect();
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
	 * @param contentView to reparent so we can intercept the pre-IME events.
	 * @param index index to insert within parent (when we have multiple elements to insert in a layout
	 * @param recorder to record events
	 * @param viewInterceptor to register the last action key.
	 */
	public MagicFrame(Context context, View contentView, int index, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		super(context);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus();
		mRecorder = recorder;
		mContentView = contentView;
		mViewInterceptor = viewInterceptor;
		init();
		insertInterceptor(contentView, index);
	}
		
	/**
	 * wrapper to insert the magic frame.
	 * @param instrumentation - so we can run the runnable synchronized on the UI thread
	 * @param activity - activity to get the window for
	 * @param viewInterceptor - for intercepting events on views.
	 * @param recorder event recorder.
	 */
	public static void insertMagicFrame(Instrumentation instrumentation, Activity activity, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		InsertMagicFrameRunnable runnable = new InsertMagicFrameRunnable(activity, recorder, viewInterceptor);
		instrumentation.runOnMainSync(runnable);
	}

	/**
	 * intercept the back, menu, and home keys.
	 */

	@Override 
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			if ((mRecorder != null) && mRecorder.getVisualDebug()) {
				Log.i(TAG, "dispatch pre IME intercepted key event " + MagicFrame.keyEventToString(event));
				Toast.makeText(this.getContext(), "dispatch intercepted key event code = " + event.getKeyCode(), Toast.LENGTH_SHORT).show();
			}
			try {
				if ((mRecorder != null) && (mViewInterceptor != null)) {
					recordKeyEvent(event);
				} else {
					Log.i(TAG, "dispatchKeyEventPreIme viewInterceptor = " + mViewInterceptor + " recorder = " + mRecorder);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}
	@Override 
	public boolean onKeyPreIme (int keyCode, KeyEvent event){
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			if ((mRecorder != null) && mRecorder.getVisualDebug()) {
				Log.i(TAG, "onkey pre IME intercepted key event " + MagicFrame.keyEventToString(event));
				Toast.makeText(this.getContext(), "prekey intercepted key event " + MagicFrame.keyEventToString(event), Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

    // actually record the key event.
    public void recordKeyEvent(KeyEvent event) {
    	View v = getChildAt(0);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_BACK);
			mRecorder.writeRecord(Constants.EventTags.KEY_BACK, v);
			break;
		case KeyEvent.KEYCODE_MENU:
			mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_MENU);
			mRecorder.writeRecord(Constants.EventTags.KEY_MENU, v);
			break;
		case KeyEvent.KEYCODE_HOME:
			mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_HOME);
			mRecorder.writeRecord(Constants.EventTags.KEY_HOME, v);
			break;
		default:
			Log.i(TAG, "did not log key " + event.getKeyCode());
			break;
		} 
    }


	@Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if ((mRecorder != null) && mRecorder.getVisualDebug()) {
			Log.i(TAG, "onFocusChanged gainFocus = " + gainFocus);
			Toast.makeText(this.getContext(),  "onFocusChanged gainFocus = " + gainFocus, Toast.LENGTH_SHORT).show();
		}
    }
  
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {			
			if ((mRecorder != null) && mRecorder.getVisualDebug()) {
				mTouchPoint = new Point((int) event.getX(), (int) event.getY());
				mSize = POINT_SIZE;
				mTimerTask = new TouchTimerTask();
				sTimer.schedule(mTimerTask, TIMER_MSEC, TIMER_MSEC);
			}
		}
		return false;
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
		parentView.removeView(contentView);
		contentView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		this.addView(contentView);
		parentView.addView(this, index);
	}

	/**
	 * cancel the debugging draw timer if we're detatched from the window
	 */
	public void onDetachedFromWindow() {
		if (mTimerTask != null) {	
			mTimerTask.cancel();
		}
	}
	/**
	 * for visual debugging.
	 */
	@Override
	public void onDraw(Canvas c) {
		if ((mTouchPoint != null) && (mPaint != null) && (c != null)) {			
			c.drawLine(mTouchPoint.x - mSize, mTouchPoint.y - mSize, mTouchPoint.x + mSize, mTouchPoint.y + mSize, mPaint);
			c.drawLine(mTouchPoint.x + mSize, mTouchPoint.y - mSize, mTouchPoint.x - mSize, mTouchPoint.y + mSize, mPaint);
		}
	}
	
	/**
	 * for visual debugging.
	 * The problem with this is that the timer can keep firing long after the activity is finished, and it throws a coniption fit.
	 * @author Matthew
	 *
	 */
	protected class TouchTimerTask extends TimerTask {

		@Override
		public void run() {
			mSize -= POINT_DECREMENT;
			if (mSize <= 0) {
				mTouchPoint = null;
			}
			MagicFrame.this.post(new Runnable() {
				public void run() {
					MagicFrame.this.invalidate();
				}
			});
			if (mSize <= 0) {
				this.cancel();
			}
		}	
	}
		
}
