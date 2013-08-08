package com.androidApp.Listeners;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

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
import android.view.ViewParent;

/**
 * recorder for view click events. This is probably called more than anything else in the world
 * TODO: This may be the cause of errors with toggle buttons
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnClickListener extends RecordListener implements View.OnClickListener, IOriginalListener  {
	protected View.OnClickListener 	mOriginalOnClickListener;
	protected int			mViewIndex;
	
	public RecordOnClickListener(EventRecorder eventRecorder, int viewIndex, View v) {
		super(eventRecorder);
		mViewIndex = viewIndex;
		try {
			mOriginalOnClickListener = ListenerIntercept.getClickListener(v);
			v.setOnClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, v, "create on click listener");
		}		
	}
	
	public RecordOnClickListener(EventRecorder eventRecorder, int viewIndex, View.OnClickListener originalTouchListener) {
		super(eventRecorder);
		mViewIndex = viewIndex;
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
				if (mEventRecorder.matchViewDirective(v, mViewIndex, ViewDirective.ViewOperation.CLICK_WORKAROUND,
					   	   						  ViewDirective.When.ALWAYS)) { 
					mEventRecorder.writeRecord(Constants.EventTags.CLICK_WORKAROUND, v, getDescription(v));
				} else {
					mEventRecorder.writeRecord(Constants.EventTags.CLICK, v, getDescription(v));
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, v, "on click");
			}
		}
		if (!fReentryBlock) {
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

	/**
	 * if an onCLickListener() is installed for a parent view, we should not install one for this view, since it
	 * will prevent the parent one from getting fired
	 * @param v
	 * @return true if a parent has a click listener
	 * @throws NoSuchFieldException exceptions thrown from Reflection utilities.
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean hasAncestorListenedToClick(View v)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		if (v.getParent() instanceof View) {
			v = (View) v.getParent();
			while (v != v.getRootView()) {
				OnClickListener onClickListener = (OnClickListener) ListenerIntercept.getClickListener(v);
				// the parent's original click listener was stored in the record listener.  if the click listener
				// hasn't been interecepted, then it will be
				if (onClickListener != null) {
					if (onClickListener instanceof RecordOnClickListener) {
						if (((IOriginalListener) onClickListener).getOriginalListener() != null) {
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
