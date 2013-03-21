package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

// base class for all listeners, implements common functions, retains reference to event recorder.
public class RecordListener {
	
	// handle to the recorder 
	protected EventRecorder 	mEventRecorder;	
	
	// because the recorder wrappers may be wrapped themselves, like in AutoCompleteTextView, we use a re-entry
	// flag to prevent stack overflow recursion
	protected boolean			mfReentryBlock;
	
	// don't record if this event was kicked off by another event. statics are dangerous
	protected static boolean	sfEventBlock;				

	public RecordListener() {
		mEventRecorder = null;
	}
	
	public RecordListener(EventRecorder eventRecorder) {
		mEventRecorder = eventRecorder;
	}
	
	/**
	 * should this event be intercepted?  Default is don't install recorder for views which are children of listviews.
	 * Also, no recorder should re-install itself.
	 * @param v view to intercept events on
	 * @return
	 */
	public boolean shouldIntercept(View v) throws IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
		AdapterView listView = TestUtils.getAdapterViewAncestor(v);
		return listView == null;
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
	}
	
	public static boolean getReentryBlock() {
		return sfEventBlock;
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
				return StringUtils.massageString(tv.getText().toString());
			} else {
				return Constants.Description.EMPTY_TEXT;
			}
		} else if (v instanceof ImageView) {
			return Constants.Description.IMAGE_VIEW;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
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
			return StringUtils.massageString(sb.toString());
		} else {
			return v.getClass().getSimpleName();
		}
	}
	
	// get the description for a dialog (try to extract its title)
	public static String getDescription(DialogInterface dialogInterface) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException  {
		Dialog dialog = (Dialog) dialogInterface;
		Window window = dialog.getWindow();
		Class phoneWindowClass = Class.forName(Constants.Classes.PHONE_WINDOW);
		String titleString = (String) FieldUtils.getFieldValue(window, phoneWindowClass, Constants.Fields.TITLE);
		if (titleString != null) {
			return StringUtils.massageString(titleString);
		} else {
			View dialogView = window.getDecorView();
			View dialogTitle = ViewExtractor.getChildByClassName(dialogView, Constants.Classes.DIALOG_TITLE_SIMPLE_NAME);
			titleString = (String) FieldUtils.getFieldValue(dialogTitle, TextView.class, Constants.Fields.TEXT);
			if (!StringUtils.isEmpty(titleString)) {
				return StringUtils.massageString(titleString);
			} else {
				return Constants.Description.UNTITLED_DIALOG;
			}
		}
	}
	
	// get the description of an object by its class index.  used for view groups, since the view getDescription()
	// will recurse down its children.
	
	public static String getDescriptionByClassIndex(View v) {
		View vRoot = v.getRootView();
		int index = TestUtils.classIndex(vRoot, v);
		return StringUtils.getOrdinal(index) + " " + v.getClass().getSimpleName();
	}
}
