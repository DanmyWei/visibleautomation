package com.androidApp.Listeners;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnDragListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;

/**
 * recorder for view click events. This is probably called more than anything else in the world
 * TODO: This may be the cause of errors with toggle buttons
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordOnClickListener extends RecordListener implements View.OnClickListener, IOriginalListener  {
	protected View.OnClickListener 	mOriginalOnClickListener;
	
	public RecordOnClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
			v.setOnClickListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, View.OnClickListener originalTouchListener) {
		super(eventRecorder);
		mOriginalOnClickListener = originalTouchListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnClickListener;
	}
		
	/**
	 * record the all-pervasive click event
	 * click:time,<view reference>,Click on <description>
	 */
	public void onClick(View v) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(Constants.EventTags.CLICK, v, getDescription(v));
			} catch (Exception ex) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on click");
				ex.printStackTrace();
			}
		}
		if (!fReentryBlock) {
			
			// specifically for OnClickListeners, View tests to see if the click listener in the ListenerInfo have been set, and if so,
			// prevents firing the performClick() event.  Inherited views then won't fire their onClick() events.  So, we check if the listener info
			// is null, or the onclick recorders with no original listeners, and if so, null out the listener and call performClick() directly.
			// NOTE: this is a terrible hack/workaround, but there's no other option that I can see at this point.
			
			try {
				 Object listenerInfo = ListenerIntercept.getListenerInfo(v);
				 if ((listenerInfo == null) || !hasClickListener(v)) {
					 ListenerIntercept.setListenerInfo(v, null);
					 v.performClick();
					 ListenerIntercept.setListenerInfo(v, listenerInfo);
				 }
			 } catch (Exception ex) {
				 mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, v, "on click");
				 ex.printStackTrace();
			 }
			if (mOriginalOnClickListener != null) {
				 mOriginalOnClickListener.onClick(v);
			}
		}
		setEventBlock(false);
	}
	
	/**
	 * examine the listener info object, and see if there were any original listeners installed for the click listener
	 * @param listenerInfoObject android.view.View$ListenerInfo
	 * @return true if there was a listener installed for any listenerInfo fields
	 * @throws NoSuchFieldException exceptions for reflection errors extracting fields
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean hasClickListener(View v) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		OnClickListener onClickListener = (OnClickListener) ListenerIntercept.getClickListener(v);
		if ((onClickListener != null) && (onClickListener instanceof RecordOnClickListener)) {
			if (((RecordOnClickListener) onClickListener).getOriginalListener() != null) {
				return true;
			}
		}
		return false;
	}
}
