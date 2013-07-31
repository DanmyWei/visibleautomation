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
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ViewReference {
	protected final String 		TAG = "ViewReference";
	private List<Object>		mRIDList;							// list of R.id classes used in the application
	private List<Object>		mRStringList;						// list of R.string classes used in the application.
	private Instrumentation		mInstrumentation;
	private FieldUtils			mFieldUtils;						// to whitelist public android classes
	private boolean				mfBinary;							// Binary: need to filter non-android classnames
	
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
	

	/**
	 * constructor: copy the instrumentation reference, initialize the id and string resource lists, and
	 * set the binary flag, which determines whether we can use target app view derived classes or not
	 * @param instrumentation
	 * @param fBinary
	 * @throws IOException
	 */
	public ViewReference(Instrumentation instrumentation, boolean fBinary) throws IOException {
		mInstrumentation = instrumentation;
		mRIDList = new ArrayList<Object>();
		mRStringList = new ArrayList<Object>();	
		mfBinary = fBinary;
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
	
	public List<Object> getIDList() {
		return mRIDList;
	}
	
	public void addRdotString(Object rdotstring) {
		mRStringList.add(rdotstring);
	}
	
	public List<Object> getStringList() {
		return mRStringList;
	}
	
	public boolean getBinary() {
		return mfBinary;
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
	
	// is this view defined in the Android library?
	public static boolean isAndroidClass(Class<? extends View> viewClass) {
		String name = viewClass.getName();
		return name.startsWith(Constants.Classes.ANDROID_WIDGET) || 
			   name.startsWith(Constants.Classes.ANDROID_VIEW) || 
			   name.startsWith(Constants.Classes.ANDROID_INTERNAL) ||
			   name.startsWith(Constants.Classes.COM_ANDROID_WIDGET) || 
			   name.startsWith(Constants.Classes.COM_ANDROID_VIEW) || 
			   name.startsWith(Constants.Classes.COM_ANDROID_INTERNAL);
	}
	
	// get a usable class name for our application
	public Class<? extends View> getUsableClass(Context context, View v, boolean fBinary) throws IOException {
		
		// in some cases, the view sends an event before any activity is initialized, so we initialize the field utilities here
		// initialize the view reference with an activity so we can read the appropriate whitelist for the target application's SDK
		if (mFieldUtils == null) {
			mFieldUtils = new FieldUtils(mInstrumentation.getContext(), v.getContext());
		}
		Class<? extends View> viewClass = ViewReference.getVisibleClass(v);
		if ((fBinary || isAndroidClass(viewClass)) && !mFieldUtils.isWhiteListedAndroidClass(viewClass)) {
			viewClass = (Class<? extends View>) mFieldUtils.getPublicClassForAndroidInternalClass(viewClass);
		}
		return viewClass;
	}
	
	/**
	 * FOR THE SAKE OF ALMIGHTY FUCKING GOD! In the number picker theres a custom edit text (NumberPicker.CustomEditText), which
	 * is marked as public, but it's not accessible from the android headers, which normally wouldn't anally rape us, but the 
	 * real fucking catch with this one is that Class.forName() on it fucking fails, so the generated test code faceplants. So
	 * what we have to do, even though it's a PUBLIC class is to call Class.forName() on it as we go up superclasses,
	 * and see if it doesn't throw a FUCKING EXCEPTION
	 * @param v the view, which apparently might be of a class that you can't get to with Class.forFuckingName()
	 * @return the Class that can be actually referenced by the view.
	 */
	public Class<? extends View> getAllocatableClass(View v) {
		Class viewClass = v.getClass();
		while (viewClass != View.class) {
			try {
				Class c = Class.forName(viewClass.getCanonicalName());
				break;
			} catch (ClassNotFoundException cnfex) {
				viewClass = viewClass.getSuperclass();
			}
		}
		return viewClass;
	}
	
	/**
	 * TODO: this documentation is out of date.
	 * Given a view, generate a reference for it so it can be found when the application is run again
	 * id, id: if the view has an ID, the ID is unique for all views, and the view is not a descendant of an AdapterView
	 * ex: id, R.id.button1 (if the ID is found in the R.id class).  id, 0x90300200 if not found
	 * text_id:  the view is a subclass of TextView, has text, the text is unique for all views, the view is not a child
	 * of adapterView.  The ID is the id of the leastmost ancestor of v with an id which is unique for all views
	 * ex: text_id, "escaped\ntext", R.id.parentID or text_id, "escaped text", 0x90300200
	 * class_index: The view does not have a unique id, use the view class name, the index of that class by pre-order
	 * traversal from the leastmost parent with an id which is unique for all views, and the view is not a descendant of
	 * an AdapterView
	 * internal_class_index:  creates a reference with 2 classes: a visible class which can be used for casting, and
	 * an internal class (either to the android library or the binary application) which can be used with Class.forName()
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
		Class<? extends View> allocatableClass = getAllocatableClass(v);
		Class<? extends View> usableClass = getUsableClass(mInstrumentation.getContext(), v, mfBinary);
		boolean fInternalClass = (usableClass != allocatableClass);
	
		// first, try the id, and verify that it is unique.
		int id = v.getId();
		View rootView = v.getRootView();
		if (id != 0) {
			int idCount = TestUtils.idCount(rootView, id);
			if (idCount == 1) {
				return Constants.Reference.ID + "," + TestUtils.getIdForValue(mRIDList, id) + "," + usableClass.getCanonicalName();
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
							    "\"" + StringUtils.escapeString(s, "\"", '\\').replace("\n", "\\n") + "\"";
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
				return Constants.Reference.CLASS_ID + "," +  TestUtils.getIdForValue(mRIDList, viewParentWithId.getId()) + "," + usableClass.getCanonicalName() + "," + classIndex;
			} else {
				if (fInternalClass) {
					int classIndex = TestUtils.classIndex(rootView, v);
					return Constants.Reference.INTERNAL_CLASS_INDEX + "," + allocatableClass.getCanonicalName() + "," + usableClass.getCanonicalName() + "," + classIndex;
				} else {
					int classIndex = TestUtils.classIndex(rootView, v);
					return Constants.Reference.CLASS_INDEX + "," + usableClass.getCanonicalName() + "," + classIndex;
				}
			}
		}
		return Constants.Reference.UNKNOWN;	
	}

}
