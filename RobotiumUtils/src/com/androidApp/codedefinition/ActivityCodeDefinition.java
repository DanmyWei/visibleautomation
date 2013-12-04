package com.androidApp.codedefinition;


/**
 * code can either be output in the the main thread (indicated with mActivityName = Constants.MAIN)
 * in an conditional activity case, or in an conditional dialog case.  activities are compared
 * by name, but dialogs are compared by the activity, dialog tag, scan type (content or title) and
 * tag type (id or text).  These are the tags for the output code hashtable, so we know where to 
 * write the output code (main, activity condition, dialog condition), and how to tag it (in
 * the dialog condition case)
 * views are tested in the normal way that views are retrieved, either by id, or class+index
 * Add: the previousLine is tested against for conditions to insert the actual code while we are parsing.
 * @author matt2
 *
 */

public class ActivityCodeDefinition extends CodeDefinition {
	
	public ActivityCodeDefinition() {
		mType = Type.ACTIVITY;
	}

	// code definition for main activity code
	public ActivityCodeDefinition(String activityName) {
		super(activityName);
		mType = Type.ACTIVITY;
	}
	
	// code definition for interstitial activity function definitions
	public ActivityCodeDefinition(String activityName, String functionName) {
		super(activityName, functionName);
		mType = Type.ACTIVITY;
	}
	
	public String getActivityName() {
		return mActivityName;
	}
	
	// generate the code suitable for a handler java class
	public String toCode() {
		return "CodeDefinition(" + mActivityName + ".class"+ ")";
	}
	
	// generate the code suitable for a handler java class
	public String toBinaryCode() {
		return "CodeDefinition(" + mActivityName + ".class"+ ")";
	}
}
