package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;
import com.androidApp.Utility.ViewType;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * base class for all listeners, implements common functions, retains reference to event recorder.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordListener {
	
	// handle to the recorder 
	protected EventRecorder 	mEventRecorder;	
	
	// because the recorder wrappers may be wrapped themselves, like in AutoCompleteTextView, we use a re-entry
	// flag to prevent stack overflow recursion
	protected boolean			mfReentryBlock;
	
	// don't record if this event was kicked off by another event. statics are dangerous
	protected static boolean	sfEventBlock;	
	
	// latch to indicate that an event has been fired since the latch was reset.
	protected static boolean	sfEventLatch;
	
	// retain the activity name so we can record it to associate events with the activity.
	protected String			mActivityName;
	
	public RecordListener() {
		mEventRecorder = null;
	}
	
	public RecordListener(String activityName, EventRecorder eventRecorder) {
		mEventRecorder = eventRecorder;
		mActivityName = activityName;
	}
	
	/**
	 * return the original listener, since we may place other interceptors.  TODO: make this abstract
	 * and implement it in all subclasses
	 * @return the original listener as an object.
	 */
	public Object getOriginalListener() {
		return null;
	}
	
	/**
	 * reset the event latch, either to initialize it, or detect that an event has been triggered.
	 * this needs to be synchronized, since it is set by the UI thread, and read by the activity monitor thread.
	 */
	public static void setEventLatch(boolean f) {
		sfEventLatch = f;
	}
	
	public static boolean getEventLatch() {
		boolean f = sfEventLatch;
		sfEventLatch = false;
		return f;
	}
		
	/**
	 * when an event kicks off a sequence of events, we actually only want to record the first event, since the 
	 * other events will be chained from it.  Also, some of the views actually install their own wrappers, and we
	 * wind up wrapping ourselves in that case.
	 * @return
	 */
	public static boolean getEventBlock() {
		return sfEventBlock;
	}
	
	public void setEventBlock(boolean f) {
		sfEventBlock = f;
		mfReentryBlock = f;
		setEventLatch(true);
	}
	
	public static boolean getReentryBlock() {
		return sfEventBlock;
	}
	

	// get the description for a menu item
	public static String getDescription(MenuItem item) {
		return item.getTitle().toString();
	}

		
	// get a description of a view.
	public static String getDescription(View v) {
		
		// say something if the view is null.
		if (v == null) {
			return "null view";
		}
		
		// text, text...we like text.
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			if (tv.getText().length() > 0) {
				return StringUtils.ellipsizeString(StringUtils.massageString(tv.getText().toString()), 
												   Constants.Sizes.MAX_TEXT_LEN, Constants.Sizes.REALLY_MAX_TEXT_LEN);
			} else {
				return Constants.Description.EMPTY_TEXT;
			}
		} else if (v instanceof ImageView) {
			
			// unfortunately, ImageViews don't retain the names of their bitmaps. That would be nice
			return Constants.Description.IMAGE_VIEW;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			// recurse over the children of the view group
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				if (vChild instanceof TextView) {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					TextView tv = (TextView) vChild;
					sb.append(tv.getText().toString());
				} else if (vChild instanceof ViewGroup) {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					sb.append(getDescription(vChild));
				}
			}
			
			// if there was nothing of value, then just return this class name
			if (sb.length() == 0) {
				return v.getClass().getSimpleName();
			} else {
				
				// beware of very long text...
				return StringUtils.ellipsizeString(StringUtils.massageString(sb.toString()), 
												   Constants.Sizes.MAX_TEXT_LEN, Constants.Sizes.REALLY_MAX_TEXT_LEN);
			}
		} else {
			return v.getClass().getSimpleName();
		}
	}
	
	// get the description for a dialog (try to extract its title)
	public static String getDescription(DialogInterface dialogInterface) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException  {
		Dialog dialog = (Dialog) dialogInterface;
		Window window = dialog.getWindow();
		
		// TODO: THIS IS WRONG: The title is a text view
		Class phoneWindowClass = Class.forName(Constants.Classes.PHONE_WINDOW);
		String titleString = (String) ReflectionUtils.getFieldValue(window, phoneWindowClass, Constants.Fields.TITLE);
		if (titleString != null) {
			return StringUtils.massageString(titleString);
		} else {
			View dialogView = window.getDecorView();
			TextView dialogTitle = (TextView) TestUtils.getChildByClassName(dialogView, Constants.Classes.DIALOG_TITLE_SIMPLE_NAME);
			if (dialogTitle != null) {
				titleString = dialogTitle.getText().toString();
				if (!StringUtils.isEmpty(titleString)) {
					return StringUtils.massageString(titleString);
				} 
			}
			return Constants.Description.UNTITLED_DIALOG;
		}
	}
	
	// get the description of an object by its class index.  used for view groups, since the view getDescription()
	// will recurse down its children.
	
	public static String getDescriptionByClassIndex(View v) {
		View vRoot = v.getRootView();
		int index = TestUtils.classIndex(vRoot, v, true);
		return StringUtils.getOrdinal(index) + " " + v.getClass().getSimpleName();
	}
	
	public boolean shouldRecordEvent(View v) {
		return !RecordListener.getEventBlock() && (mEventRecorder.hasTouchedDown() || !ViewType.isDecorViewDescendant(v));

	}
}
