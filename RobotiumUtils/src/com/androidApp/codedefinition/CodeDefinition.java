package com.androidApp.codedefinition;

import com.androidApp.util.TestException;

/**
 * code can either be output in the the main thread (indicated with mActivityName = Constants.MAIN)
 * in an interstitial activity handler, or in an interstitial dialog handler.  activities are compared
 * by name, but dialogs are compared by the activity, dialog tag, scan type (content or title) and
 * tag type (id or text).  These are the tags for the output code hashtable, so we know where to 
 * write the output code (main, interstitial handler, or dialog handler), and how to tag it (in
 * the dialog handler case)
 * @author matt2
 *
 */

public class CodeDefinition {
	protected static final String TAG = "CodeDefinition";
	
	public enum Type {
		ACTIVITY("activity"),
		DIALOG("dialog"),
		VIEW("view");
	
		public String mName;
		private Type(String t) {
			mName = t;
		}
		public Type fromString(String s) throws TestException {
			for (Type type: Type.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new TestException("code definition type " + s + " unknown");
		}
	}
	protected String 			mActivityName;		// name of the activity or activity dialog was created in
	protected Type 				mType;				// activity or dialog
	protected String			mFunctionName;		// handler name associated to this
	
	public CodeDefinition() {
		
	}
	public CodeDefinition(String activityName) {
		mActivityName = activityName;
		mType = Type.ACTIVITY;
		mFunctionName = null;
	}
	
	public CodeDefinition(String activityName, String functionName) {
		mActivityName = activityName;
		mType = Type.ACTIVITY;
		mFunctionName = functionName;			
	}

	public String getActivityName() {
		return mActivityName;
	}
	
	public Type getType() {
		return mType;
	}
	
	public String getfunctionName() {
		return mFunctionName;
	}
	
	public void setfunctionName(String functionName) {
		mFunctionName = functionName;
	}	

}
