package com.androidApp.EventRecorder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;

/**
 * code to generate view references, either by unique id's or by class name and index within the list of views
 * on the screen
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class ViewReference {
	protected final String 		TAG = "ViewReference";
	private List<Object>		mRIDList;							// list of R.id classes used in the application
	private List<Object>		mRStringList;						// list of R.string classes used in the application.
	private Instrumentation		mInstrumentation;
	private FieldUtils			mFieldUtils;						// to whitelist public android classes
	
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
	

	public ViewReference(Instrumentation instrumentation) throws IOException {
		mInstrumentation = instrumentation;
		mRIDList = new ArrayList<Object>();
		mRStringList = new ArrayList<Object>();	
	}
	
	/**
	 * we need an activity to get the target version so we can read the whitelist for the android version
	 * @param activity activity to get the target sdk version
	 * @param instrumentation instrumentation handle
	 */
	public void initializeWithActivity(Activity activity, Instrumentation instrumentation) throws IOException {
		mFieldUtils = new FieldUtils(instrumentation.getContext(), activity);
		
	}
	
	public void addRdotID(Object rdotid) {
		mRIDList.add(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mRStringList.add(rdotstring);
	}

	/** 
	 * for Robotium functions like clickInList(), the list is referenced by its index in the # of lists displayed on the screen
	 * @param v
	 * @return
	 */
	public static String getClassIndexReference(View v) {
		View rootView = v.getRootView();
		int classIndex = TestUtils.classIndex(rootView, v);
		return Constants.Reference.CLASS_INDEX + "," + v.getClass().getName() + "," + classIndex;	
	}
	
	/**
	 * return the visible superclass of this class.
	 * @param v something derived from view.class
	 * @return a public class
	 */
	public static Class<? extends View> getVisibleClass(View v) {
		Class<? extends View> cls = v.getClass();
		while (!cls.equals(View.class)) {
			int modifiers = cls.getModifiers();
			if ((modifiers & Modifier.PUBLIC) != 0x0) {
				return cls;
			}
			cls = (Class<? extends View>) cls.getSuperclass();
		}
		return cls;
	}

	/**
	 * return a class name for the view that can be compiled into an android application.  many of the views, such as popup menus
	 * and action bar containers are internal
	 * @param v view
	 * @return android external class name
	 */
	protected String getUsableClassName(Context context, View v) {
		Class<? extends View> viewClass = ViewReference.getVisibleClass(v);
		String className = viewClass.getName();
		try {
			if (!mFieldUtils.isWhiteListedAndroidClass(viewClass)) {
				viewClass = (Class<? extends View>) mFieldUtils.getPublicClassForAndroidInternalClass(viewClass);
				className = viewClass.getName();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e(TAG, "exception while trying to get internal class for " + viewClass + " = " + ex.getMessage());
			className = "unable to find public class for " + v.getClass();
		}
		return className;
	}
	
	protected Class<? extends View> getUsableClass(Context context, View v) throws IOException {
		Class<? extends View> viewClass = ViewReference.getVisibleClass(v);
		if (!mFieldUtils.isWhiteListedAndroidClass(viewClass)) {
			viewClass = (Class<? extends View>) mFieldUtils.getPublicClassForAndroidInternalClass(viewClass);
		}
		return viewClass;
	}
	
	/**
	 * Given a view, generate a reference for it so it can be found when the application is run again
	 * id, id: if the view has an ID, the ID is unique for all views, and the view is not a descendant of an AdapterView
	 * ex: id, R.id.button1 (if the ID is found in the R.id class).  id, 0x90300200 if not found
	 * text_id:  the view is a subclass of TextView, has text, the text is unique for all views, the view is not a child
	 * of adapterView.  The ID is the id of the leastmost ancestor of v with an id which is unique for all views
	 * ex: text_id, "escaped\ntext", R.id.parentID or text_id, "escaped text", 0x90300200
	 * class_index_id: The view does not have a unique id, use the view class name, the index of that class by pre-order
	 * traversal from the leastmost parent with an id which is unique for all views, and the view is not a descendant of
	 * an AdapterView
	 * ex: class_index_id, android.widget.Button, 5, R.id.parent or class_index_id, android.widget.Button, 5, 0x9030200
	 * list_index_id: The view is a descendant of an AdapterView, and has a unique ID within the set of views of its list
	 * element.  Index is the index within the AdapterView.
	 * ex: list_index_id, R.id.list, 5, R.id.address_text or list_index_id, R.id.list, 5, 0x9030201
	 * path:  a "dot" path of classnames from the root view of the activity, with indices for each class
	 * ex: android.widget.LinearLayout[0].android.widget.LinearLayout[1].android.widget.Button[2]  The index is of
	 * children of the same class type.
	 * @param v
	 * @return
	 */
	public String getReference(View v) throws IllegalAccessException, IOException {
		Class<? extends View> usableClass = getUsableClass(mInstrumentation.getContext(), v);
	
		// first, try the id, and verify that it is unique.
		int id = v.getId();
		View rootView = v.getRootView();
		if (id != 0) {
			int idCount = TestUtils.idCount(rootView, id);
			if (idCount == 1) {
				return Constants.Reference.ID + "," + TestUtils.getIdForValue(mRIDList, id) + "," + usableClass.getName();
			}
		}
		
		// special case for text views, we can find on the text contents
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			String s = tv.getText().toString();
			View viewParentWithId = TestUtils.findParentViewWithId(v);
			if (viewParentWithId != null) {
				int parentIdCount = TestUtils.idCount(rootView, viewParentWithId.getId());
				if (parentIdCount == 1) {
					int textCount = TestUtils.textCount(viewParentWithId, s);
					if (textCount == 1) {
						return Constants.Reference.TEXT_ID + "," + TestUtils.getIdForValue(mRIDList, viewParentWithId.getId()) + "," + 
							    "\"" + StringUtils.escapeString(s, "\"", '\\') + "\"";
					}
				}
			}
		}
		
		// not-to-special case for everyone else.
		View viewParentWithId = TestUtils.findParentViewWithId(v);
		if (viewParentWithId != null) {
			int parentIdCount = TestUtils.idCount(rootView, viewParentWithId.getId());
			if (parentIdCount == 1) {
				int classIndex = TestUtils.classIndex(viewParentWithId, v);
				return Constants.Reference.CLASS_ID + "," +  TestUtils.getIdForValue(mRIDList, viewParentWithId.getId()) + "," + usableClass.getName() + "," + classIndex;
			} else {
				int classIndex = TestUtils.classIndex(rootView, v);
				return Constants.Reference.CLASS_INDEX + "," + usableClass.getName() + "," + classIndex;
			}
		}
		return Constants.Reference.UNKNOWN;	
	}

}
