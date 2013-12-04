package com.androidApp.Listeners;

import java.lang.reflect.Field;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;


import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnTouchListener;

/**
 *  View.onTouchListener that listens to key events, and writes them to a file.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnTouchListener extends RecordListener implements OnTouchListener, IOriginalListener  {
	protected OnTouchListener 	mOriginalOnTouchListener;
	protected boolean			mTouchDown;			
	
	public RecordOnTouchListener(String activityName, EventRecorder eventRecorder, View v) {
		super(activityName, eventRecorder);
		try {
			mOriginalOnTouchListener = ListenerIntercept.getTouchListener(v);
			v.setOnTouchListener(this);
			mTouchDown = false;
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, v, "create on touch listener");
		}		
	}
	
	public RecordOnTouchListener(String activityName, EventRecorder eventRecorder, OnTouchListener originalTouchListener) {
		super(activityName, eventRecorder);
		mOriginalOnTouchListener = originalTouchListener;
		mTouchDown = false;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnTouchListener;
	}
	
	/**
	 * record the actual touch event
	 * TODO: we should ensure that the event does actually come through RecordWindowCallback,
	 * but self-motion events are as rare as hen's teeth, and we need to handle move and up as well as down
	 * <touch_down/touch_up/touch_move>:time,x,y,<reference>,<description>
	 * 
	 */
	public boolean onTouch(View v, MotionEvent event) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String eventName = Constants.EventTags.UNKNOWN;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					eventName = Constants.EventTags.TOUCH_DOWN;
					mTouchDown = true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					eventName = Constants.EventTags.TOUCH_UP;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					eventName = Constants.EventTags.TOUCH_MOVE;
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					eventName = Constants.EventTags.TOUCH_CANCEL;
				}
				if (mTouchDown) {
					String description = getDescription(v);
					// we want to save the widget size in the down case, because the motion events have to be
					// scaled on playback in case it's a different device
					String logString = v.getWidth() + "," + v.getHeight() + "," + 
								 	   event.getX() + "," + event.getY() + "," + 
								 	   mEventRecorder.getViewReference().getReference(v) + "," + description;
					mEventRecorder.writeRecord(mActivityName, eventName, logString);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					mTouchDown = false;
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, v, "on touch");
			}
		} 
		if (!fReentryBlock) {
			if (mOriginalOnTouchListener != null) {
				fConsumeEvent = mOriginalOnTouchListener.onTouch(v, event);
			} 
		}
		setEventBlock(false);
		return fConsumeEvent;
	}
	
	/**
	 * does this view listen to touch events
	 * @param v view to test
	 * @return true if if the view listens to a touch event
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean hasListenenedToTouch(View v) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		OnTouchListener onTouchListener = (OnTouchListener) ListenerIntercept.getTouchListener(v);
		// the parent's original Touch listener was stored in the record listener.  if the Touch listener
		// hasn't been interecepted, then it will be
		if (onTouchListener != null) {
			if (onTouchListener instanceof RecordOnTouchListener) {
				if (((IOriginalListener) onTouchListener).getOriginalListener() != null) {
					return true;
				}
			} else {
				return true;
			}
		}
		
		// if the parent overrode onTouch(), then it will listen to the Touch events.
		if (hasOverriddenOnTouchMethod(v)) {
			return true;
		}
		return false;
	}

	/**
	 * if an onTouchListener() is installed for a parent view, we should not install one for this view, since it
	 * will prevent the parent one from getting fired
	 * @param v
	 * @return true if a parent has a Touch listener
	 * @throws NoSuchFieldException exceptions thrown from Reflection utilities.
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean hasAncestorListenedToTouch(View v)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		if (v.getParent() instanceof View) {
			v = (View) v.getParent();
			while (v != v.getRootView()) {
				if (hasListenenedToTouch(v)) {
					return true;
				}
				ViewParent vp = v.getParent();
				if (vp instanceof View) {
					v = (View) vp;
				} else {
					break;
				}
			}
		}
		return false;
	}

	
	/**
	 * has this view overridden the onTouch() method?  if so, we're interested in recording its touch events
	 * @param v view to interrogate
	 * @return
	 */
	public static boolean hasOverriddenOnTouchMethod(View v) {
		Class classWithOnTouchMethod = ReflectionUtils.getClassForMethod(v, Constants.Methods.ON_TOUCH);
		if (classWithOnTouchMethod != null) {
			return !ViewReference.isAndroidClass(classWithOnTouchMethod);
		} else {
			return false;
		}
	}
}
