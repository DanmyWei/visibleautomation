package com.androidApp.EventRecorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.view.View;

import com.androidApp.Utility.Constants;

/**
 * a view directive is a user-defined view reference, an operation and a value.
 * For example: class_index,TextView,1,copy,my_variable
 * @author matt2
 *
 */
public class ViewDirective {
	public enum ViewOperation {
		COPY_TEXT("copy_text"),									// <view_reference>,copy_text,<when>,variable_name
		PASTE_TEXT("paste_text"),								// <view_reference>,paste_text,<when>,variable_name
		CHECK("check"),											// <view_reference>,check,<when>
		UNCHECK("uncheck"),										// <view_reference>,uncheck,<when>
		MOTION_EVENTS("motion_events"),							// <view_reference>,motion_events,on_activity_start
		IGNORE_EVENTS("ignore_events"),							// <view_reference>,ignore_events,on_activity_start
		SELECT_BY_TEXT("select_by_text"),						// <view_reference>,select_by_text,on_activity_start
		ENTER_TEXT_BY_KEY("enter_text_by_key"),					// <view_reference>,enter_text_by_key,on_actvity_start		
		IGNORE_TOUCH_EVENTS("ignore_touch_events"),				// <view_reference>,ignore_events,on_activity_start (all following are same)
		IGNORE_FOCUS_EVENTS("ignore_focus_events"),
		IGNORE_CLICK_EVENTS("ignore_click_events"),
		IGNORE_LONG_CLICK_EVENTS("ignore_long_click_events"),
		IGNORE_SCROLL_EVENTS("ignore_scroll_events"),
		IGNORE_ITEM_SELECT_EVENTS("ignore_item_select_events"),
		IGNORE_TEXT_EVENTS("ignore_text_events"), 
		CLICK_WORKAROUND("click_workaround"), 
		SELECT_ITEM_WORKAROUND("select_item_workaround");
		
		public String mName;
		
		private ViewOperation(String name) {
			mName = name;
		}
		
		static ViewOperation get(String name) {
			for (ViewOperation operation : ViewOperation.values()) {
				if (operation.mName.equals(name)) {
					return operation;
				}
			}
			return null;
		}
	}
	public enum When {
		ON_ACTIVITY_START,		// perform the operation when the activity starts
		ON_VALUE_CHANGE,		// perform the operation when the view value is changed
		ON_ACTIVITY_END,		// perform the operation when the activity exits
		ALWAYS;					// for properties.  Always applies
		
		public boolean match(When a) {
			return (a == this) || (a == ALWAYS) || (this == ALWAYS);
		}
	}
	protected UserDefinedViewReference 	mViewReference;
	protected String					mVariable;
	protected ViewOperation				mOperation;
	protected When						mWhen;
	
	public ViewDirective(UserDefinedViewReference 	userDefinedReference,
						 ViewOperation				operation,
						 When						when,
						 String						variable) {
		mViewReference = userDefinedReference;
		mOperation = operation;
		mWhen = when;
		mVariable = variable;
	}
	
	public UserDefinedViewReference getReference() {
		return mViewReference;
	}
	
	public ViewOperation getOperation() {
		return mOperation;
	}
	
	public When getWhen() {
		return mWhen;
	}
	
	public String getVariable() {
		return mVariable;
	}
	
	/**
	 * parse a View Directive from a string
	 * @param s
	 * @throws ClassNotFoundException
	 * @throws ReferenceException
	 */
	public ViewDirective(String s) throws ClassNotFoundException, ReferenceException {
		mViewReference = new UserDefinedViewReference(s);
		String[] tokens = s.split("[:,]");
		int startToken = mViewReference.getTokenCount();
		mOperation = ViewOperation.get(tokens[startToken]);
		mWhen = ViewDirective.whenFromString(tokens[startToken + 1]);
		if (tokens.length > startToken + 2) {
			mVariable = tokens[startToken + 2];
		}
	}
	
	/**
	 * read the list of view directives from a stream
	 * @param is input stream
	 * @return list of view references
	 * @throws ClassNotFoundException thrown if the view class in a UserDefinedViewReference can't be found
	 * @throws ReferenceException symbol resolution error.
	 * @throws IOException
	 */
	public static List<ViewDirective> readViewDirectiveList(InputStream is) throws ClassNotFoundException, ReferenceException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<ViewDirective> viewDirectiveList = new ArrayList<ViewDirective>();
		String line = null;
		while ((line = br.readLine()) != null) {
			ViewDirective viewDirective = new ViewDirective(line);
			viewDirectiveList.add(viewDirective);
		}
		return viewDirectiveList;
	}

	/**
	 * does this view match the view reference, requested operation, and when?
	 * @param v view to test against
	 * @param viewIndex index of view by preorder search filter by view type
	 * @param op operation to filter by
	 * @param when activity_start, etc.
	 * @return true if it's a match
	 */
	public boolean match(View v, int viewIndex, ViewOperation op, When when) {
		return mViewReference.matchView(v, viewIndex) && (mOperation == op) && (mWhen.match(when));
	}
	
	/**
	 * match against a list of view directives.
	 * @param v
	 * @param viewIndex
	 * @param op
	 * @param when
	 * @param viewDirectiveList
	 * @return
	 */
	public static boolean match(View v, int viewIndex, ViewOperation op, When when, List<ViewDirective> viewDirectiveList) {
		for (ViewDirective viewDirective : viewDirectiveList) {
			if (viewDirective.match(v, viewIndex, op, when)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return the view directive in string form.
	 */
	public String toString() {
		try {
			String s = mViewReference.toString() + "," + mOperation.mName + "," + whenToString(mWhen);
			if (mVariable != null) {
				s += "," + mVariable;
			}
			return s;
		} catch (ReferenceException rex) {
			return "bogus view directive";
		}
	}
	
	/**
	 * parse the "When" of the directive from a string
	 * @param s
	 * @return
	 * @throws ReferenceException
	 */
	public static When whenFromString(String s) throws ReferenceException {
		if (s.equals(Constants.When.ON_ACTIVITY_START)) {
			return When.ON_ACTIVITY_START;
		} else if (s.equals(Constants.When.ON_ACTIVITY_END)) {
			return When.ON_ACTIVITY_END;
		} else if (s.equals(Constants.When.ON_VALUE_CHANGE)) {
			return When.ON_VALUE_CHANGE;
		} else if (s.equals(Constants.When.ALWAYS)) {
			return When.ALWAYS;
		} else {
			throw new ReferenceException("unable to parse when from " + s);
		}
	}
	
	public static String whenToString(When when) throws ReferenceException {
		switch (when) {
		case ON_ACTIVITY_START:
			return Constants.When.ON_ACTIVITY_START;
		case ON_VALUE_CHANGE:	
			return Constants.When.ON_VALUE_CHANGE;
		case ON_ACTIVITY_END:
			return Constants.When.ON_ACTIVITY_END;
		case ALWAYS:
			return Constants.When.ALWAYS;
		default:
			throw new ReferenceException("when " + when + " unknown");
		}
	}
}
