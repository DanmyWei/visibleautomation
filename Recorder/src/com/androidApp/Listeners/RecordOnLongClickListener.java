package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnLongClickListener;

/**
 * intercept long clicks.  Note: this will get much more interesting, since this will provide the interface
 * for specifying views and references for "expect" targets
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnLongClickListener extends RecordListener implements OnLongClickListener, IOriginalListener {
	protected OnLongClickListener 	mOriginalOnLongClickListener;
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnLongClickListener = ListenerIntercept.getLongClickListener(v);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, v, "create long click listener");
		}		
	}
	
	public RecordOnLongClickListener(EventRecorder eventRecorder, OnLongClickListener originalLongClickListener) {
		super(eventRecorder);
		mOriginalOnLongClickListener = originalLongClickListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnLongClickListener;
	}
	
	/**
	 * record onLongClick
	 * click:time,<view reference>,Click on <description>
	 * @param v view being intercepted.
	 * return true if the wrapped long click listener consumed the event.
	 */
	@Override
	public boolean onLongClick(View v) {
		boolean fConsumeEvent = false;
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				String description = getDescription(v);
				mEventRecorder.writeRecord(Constants.EventTags.LONG_CLICK, v, description);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, v, "long click");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnLongClickListener != null) {
				fConsumeEvent = mOriginalOnLongClickListener.onLongClick(v);
			} 
		}		
		setEventBlock(false);
		return fConsumeEvent;
	}
	
	/**
	 * if an onLongCLickListener() is installed for a parent view, we should not install one for this view, since it
	 * will prevent the parent one from getting fired
	 * @param v
	 * @return true if a parent has a click listener
	 * @throws NoSuchFieldException exceptions thrown from Reflection utilities.
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean hasAncestorListenedToLongClick(View v)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		if (v.getParent() instanceof View) {
			v = (View) v.getParent();
			while (v != v.getRootView()) {
				OnLongClickListener onLongClickListener = (OnLongClickListener) ListenerIntercept.getClickListener(v);
				// the parent's originallong click listener was stored in the record listener.  if the click listener
				// hasn't been interecepted, then it will be
				if (onLongClickListener != null) {
					if (onLongClickListener instanceof RecordOnLongClickListener) {
						if (((IOriginalListener) onLongClickListener).getOriginalListener() != null) {
							return true;
						}
					} else {
						return true;
					}
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

}
