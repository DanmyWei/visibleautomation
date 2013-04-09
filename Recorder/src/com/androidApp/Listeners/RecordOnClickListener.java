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

// recorder for view click events.
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
			
			// specifically for OnClickListeners, View tests to see if any of the listeners in the ListenerInfo have been set, and if so,
			// prevents firing the performClick() event.  Inherited views then won't fire their onClick() events.  Frankly, it's very strange,
			// since setting an onTouch or onKey listener will prevent button clicks from getting listened to.  So, we check if the listener info
			// is null, or all recorders with no original listeners, and if so, null out the listener and call performClick() directly.
			// NOTE: this is a terrible hack/workaround, but there's no other option that I can see at this point.
			
			try {
				 Object listenerInfo = ListenerIntercept.getListenerInfo(v);
				 if ((listenerInfo == null) || isListenerInfoOnlyRecordersOrNull(listenerInfo)) {
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
	 * examine the listener info object, and see if there were any original listeners installed
	 * @param listenerInfoObject android.view.View$ListenerInfo
	 * @return true if there was a listener installed for any listenerInfo fields
	 * @throws NoSuchFieldException exceptions for reflection errors extracting fields
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean isListenerInfoOnlyRecordersOrNull(Object listenerInfoObject) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Class listenerInfoClass = Class.forName(Constants.Classes.LISTENER_INFO);
		OnFocusChangeListener focusChangeListener = (OnFocusChangeListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.FOCUS_CHANGE_LISTENER);
		if ((focusChangeListener != null) && (focusChangeListener instanceof RecordOnFocusChangeListener)) {
			if (((RecordOnFocusChangeListener) focusChangeListener).getOriginalListener() != null) {
				return false;
			}
		}
		// we currently don't record onLayoutChangeListeners
		ArrayList<OnLayoutChangeListener> onLayoutChangeListeners = (ArrayList<OnLayoutChangeListener>) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.LAYOUT_CHANGE_LISTENERS);
		if (onLayoutChangeListeners != null) {
			return true;
		}
		
		// we currrently don't record onAttachStateChangeListeners.
		CopyOnWriteArrayList<OnAttachStateChangeListener> onAttachStatesChangeListeners = (CopyOnWriteArrayList<OnAttachStateChangeListener>) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.ATTACH_STATE_CHANGE_LISTENERS);
		if (onAttachStatesChangeListeners != null) {
			return true;
		}
		OnClickListener onClickListener = (OnClickListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.CLICK_LISTENER);
		if ((onClickListener != null) && (onClickListener instanceof RecordOnClickListener)) {
			if (((RecordOnClickListener) onClickListener).getOriginalListener() != null) {
				return false;
			}
		}
		OnLongClickListener onLongClickListener = (OnLongClickListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.LONG_CLICK_LISTENER);
		if ((onLongClickListener != null) && (onLongClickListener instanceof RecordOnLongClickListener)) {
			if (((RecordOnLongClickListener) onLongClickListener).getOriginalListener() != null) {
				return false;
			}
		}
		// currently we don't intercept OnCreateContextMenuListener or OnKeyListener
		// but we do listener for Touch listeners
		OnTouchListener onTouchListener = (OnTouchListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.TOUCH_LISTENER);
		if ((onTouchListener != null) && (onTouchListener instanceof RecordOnTouchListener)) {
			if (((RecordOnTouchListener) onTouchListener).getOriginalListener() != null) {
				return false;
			}
		}

		// we don't listen for hover events.  What are hover events anyway?
		OnHoverListener onHoverListener = (OnHoverListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.HOVER_LISTENER);
		if (onHoverListener != null) {
			return false;
		}
		OnGenericMotionListener onGenericMotionListener = (OnGenericMotionListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.GENERIC_MOTION_LISTENER);
		if (onGenericMotionListener != null) {
			return false;
		}
		OnDragListener onDragListener = (OnDragListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.DRAG_LISTENER);
		if (onDragListener != null) {
			return false;
		}
		OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener = (OnSystemUiVisibilityChangeListener) ListenerIntercept.getFieldValue(listenerInfoObject, listenerInfoClass, Constants.Fields.SYSTEMUI_VISIBILITY_CHANGE_LISTENER);
		if (onSystemUiVisibilityChangeListener != null) {
			return false;
		}
		return true;
	}
}
