package com.androidApp.EventRecorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.androidApp.Utility.Constants;

/**
 * UserDefinedView Reference: used for identifying views for special treatment, such as motion events
 * views referenced by class, activity and class, activity class index, and activity and id.
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 * @author matt2
 *
 */
public class UserDefinedViewReference {
	public enum ReferenceEnum {
		VIEW_BY_CLASS(Constants.Reference.VIEW_BY_CLASS, 0x1),
		VIEW_BY_ACTIVITY_CLASS(Constants.Reference.VIEW_BY_ACTIVITY_CLASS, 0x2),
		VIEW_BY_ACTIVITY_CLASS_INDEX(Constants.Reference.VIEW_BY_ACTIVITY_CLASS_INDEX, 0x3),
		VIEW_BY_ACTIVITY_ID(Constants.Reference.VIEW_BY_ACTIVITY_ID, 0x4);
		
		public String mName;
		public int mValue;
		
		private ReferenceEnum(String name, int value) {
			mName = name;
			mValue = value;
		}
	}
	
	protected ReferenceEnum 			mReferenceType;		// how the reference is specified
	protected String					mViewClassName;		// view.class.name
	protected Class<? extends View>		mViewClass;			// view class
	protected String					mActivityName;		// activity.class.name
	protected Class<? extends Activity> mActivityClass;		// activity class
	protected int						mClassIndex;		// index in view hierarchy filtered by class
	protected int						mID;				// android resource ID
	protected int						mTokenCount;		// # of tokens consumed in parsing
	
	// accessors
	public ReferenceEnum referenceType() {
		return mReferenceType;
	}
	
	public String getActivityName() {
		return mActivityName;
	}
	
	public Class<? extends Activity> getActivityClass() {
		return mActivityClass;
	}
	
	public int getClassIndex() {
		return mClassIndex;
	}
	
	public int getID() {
		return mID;
	}
	
	public Class<? extends View> getViewClass() {
		return mViewClass;
	}
	
	/**
	 * how many tokens were parsed for this reference?
	 * @return
	 */
	public int getTokenCount() {
		return mTokenCount;
	}
	
	/**
	 * parse a UserDefinedViewReference from a string
	 * view_by_class: classname
	 * view_by_activity_class: activity, classname
	 * view_by_activity_class_index: activity, classname, index
	 * view_by_activity_id: activity, id
	 * @param referenceLine
	 */
	public UserDefinedViewReference(String referenceLine) throws ReferenceException, ClassNotFoundException {
		String[] tokens = referenceLine.split("[:,]");
		String type = tokens[0];
		if (type.equals(ReferenceEnum.VIEW_BY_CLASS.mName)) {
			mReferenceType = ReferenceEnum.VIEW_BY_CLASS;
			mActivityName = null;
			mActivityClass = null;
			mViewClassName = tokens[1].trim();
			mViewClass = (Class<? extends View>) Class.forName(mViewClassName);
			mTokenCount = 2;
		} else if (type.equals(ReferenceEnum.VIEW_BY_ACTIVITY_CLASS.mName)) {
			mReferenceType = ReferenceEnum.VIEW_BY_CLASS;
			mActivityName = tokens[1].trim();
			mActivityClass = (Class<? extends Activity>) Class.forName(mActivityName);
			mViewClassName = tokens[2].trim();
			mViewClass = (Class<? extends View>) Class.forName(mViewClassName);
			mTokenCount = 3;
		} else if (type.equals(ReferenceEnum.VIEW_BY_ACTIVITY_CLASS_INDEX.mName)) {
			mReferenceType = ReferenceEnum.VIEW_BY_CLASS;
			mActivityName = tokens[1].trim();
			mActivityClass = (Class<? extends Activity>) Class.forName(mActivityName);
			mViewClassName = tokens[2].trim();
			mViewClass = (Class<? extends View>) Class.forName(mViewClassName);
			mClassIndex = Integer.parseInt(tokens[3].trim());
			mTokenCount = 4;
		} else if (type.equals(ReferenceEnum.VIEW_BY_ACTIVITY_ID.mName)) {
			mReferenceType = ReferenceEnum.VIEW_BY_CLASS;
			mActivityName = tokens[1].trim();
			mActivityClass = (Class<? extends Activity>) Class.forName(mActivityName);
			mID = Integer.parseInt(tokens[2].trim());
			mTokenCount = 3;
		}
	}
	/**
	 * view_by_class: classname
	 * view_by_activity_class: activity, classname
	 * view_by_activity_class_index: activity, classname, index
	 * view_by_activity_id: activity, id
	 */
	public String toString() {
		switch (mReferenceType) {
		case VIEW_BY_CLASS:
			return ReferenceEnum.VIEW_BY_CLASS.mName + ":" + mViewClassName;
		case VIEW_BY_ACTIVITY_CLASS:
			return ReferenceEnum.VIEW_BY_ACTIVITY_CLASS.mName + ":" + mActivityName + "," + mViewClassName;
		case VIEW_BY_ACTIVITY_CLASS_INDEX:
			return ReferenceEnum.VIEW_BY_ACTIVITY_CLASS.mName + ":" + mActivityName + "," + mViewClassName + "," + Integer.toString(mClassIndex);
		case VIEW_BY_ACTIVITY_ID:
			return ReferenceEnum.VIEW_BY_ACTIVITY_ID.mName + ":" + mActivityName + "," + Integer.toString(mID);	
		default:
			return "bogus reference"; 		// TODO: throw exception
		}
	}
	
	/**
	 * read a list of references from a file
	 * @param is input stream
	 * @return list of references
	 * @throws IOException
	 * @throws ReferenceException
	 * @throws ClassNotFoundException
	 */
	public static List<UserDefinedViewReference> readViewReferences(InputStream is) throws IOException, ReferenceException, ClassNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<UserDefinedViewReference> referenceList = new ArrayList<UserDefinedViewReference>();
		String line = null; 
		while ((line = br.readLine()) != null) {
			UserDefinedViewReference reference = new UserDefinedViewReference(line);
			referenceList.add(reference);
		}
		return referenceList;
	}
	
	/**
	 * filter the references for faster matching: either no activity is specified, or the activity matches
	 * @param activity activity to filter on
	 * @param references
	 * @return filtered references list
	 */
	public static List<UserDefinedViewReference> filterReferencesInActivity(Activity activity, List<UserDefinedViewReference> references) {
		List<UserDefinedViewReference> filteredReferences = new ArrayList<UserDefinedViewReference>();
		Class<? extends Activity> activityClass = activity.getClass();
		for (UserDefinedViewReference reference : references) {
			if (reference.referenceType() == ReferenceEnum.VIEW_BY_CLASS) {
				filteredReferences.add(reference);
			} else {
				Class<? extends Activity> referenceActivityClass = reference.getActivityClass();
				if (referenceActivityClass.isAssignableFrom(activityClass)) {
					filteredReferences.add(reference);
				}
			}
		}
		return filteredReferences;
	}
	
	/**
	 * given a list of references, and an activity, find all of the views in the activity's content view which
	 * match one of the references.
	 * TODO: use the activity to filter the references in advance before searching for matches to speed things up
	 * @param activity activity (contentView)
	 * @param references references to match
	 * @return list of matching views
	 */
	public static List<View> getMatchingViews(Activity activity, List<UserDefinedViewReference> references) {
		Window w = activity.getWindow();
        View contentView = w.getDecorView().findViewById(android.R.id.content);
        return getMatchingViews(activity, contentView, references);
	}
	
	/**
	 * same thing, except that a view is passed as well as the activity
	 * @param activity activity to check against
	 * @param v view to get reference for
	 * @param references list of references to search against
	 * @return list of matching views
	 */
	public static List<View> getMatchingViews(Activity activity, View v, List<UserDefinedViewReference> references) {
		List<View> matchingViews = new ArrayList<View>();
		Hashtable<Class,ClassCount> classTable = new Hashtable<Class, ClassCount>();
		getMatchingViews(activity, v, references, classTable, matchingViews);
		return matchingViews;
	}
	
	/**
	 * return the list of matching views
	 * @param activity activity to match against
	 * @param v (recursive) current view to check against
	 * @param references list of references
	 * @param classTable hashtable of classes and classcounts for fast reference
	 * @param matchingViews (recursive) populate this list of views
	 */
	protected static void getMatchingViews(Activity activity,
										   View v, 
										   List<UserDefinedViewReference> references, 
										   Hashtable<Class,ClassCount> classTable, 
										   List<View> matchingViews) {
		for (UserDefinedViewReference reference : references) {
			Class<? extends View> viewClass = v.getClass();
			Class<? extends View> refViewClass = reference.getViewClass();
			Class<? extends Activity> activityClass = activity.getClass();
			Class<? extends Activity> refActivityClass = reference.getActivityClass();
			switch (reference.referenceType()) {
				case VIEW_BY_CLASS: 
				{
					if (refViewClass.isAssignableFrom(viewClass)) {
						matchingViews.add(v);
					}
				}
				break;
				case VIEW_BY_ACTIVITY_CLASS:
				{
					if (refViewClass.isAssignableFrom(viewClass) && refActivityClass.isAssignableFrom(activityClass)) {
						matchingViews.add(v);
					}
				}
				break;
				case VIEW_BY_ACTIVITY_CLASS_INDEX:
				{
					if (refViewClass.isAssignableFrom(viewClass) && refActivityClass.isAssignableFrom(activityClass)) {
						ClassCount classCount = classTable.get(v.getClass());
						if (((classCount == null) && (reference.getClassIndex() == 0)) || (classCount.mCount == reference.getClassIndex())) {
							matchingViews.add(v);
						}
					}
				}
				break;
				case VIEW_BY_ACTIVITY_ID:
				{
					if (refActivityClass.isAssignableFrom(activityClass) && reference.getID() == v.getId()) {
						matchingViews.add(v);
					}
				}
				break;
			}
		}
		ClassCount classCount = classTable.get(v.getClass());
		if (classCount == null) {
			classCount = new ClassCount(1);
		} else {
			classCount.mCount++;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				getMatchingViews(activity, vChild, references, classTable, matchingViews);
			}
		}	
	}
}
