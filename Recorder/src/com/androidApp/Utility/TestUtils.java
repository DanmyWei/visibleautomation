package com.androidApp.Utility;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.androidApp.Intercept.MagicFramePopup;
import com.androidApp.Test.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Gallery;

/**
 * grab-bag of utilities to extract views from the android view tree.
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class TestUtils {
	private static final String TAG = "TestUtils";
	
	/**
	 * given a view group, find the indexthed child of class cls
	 * @param vg ViewGroup (usually cast from view)
	 * @param index index of the child to retrieve
	 * @param cls class to match
	 * @return View if found, returns null if not found.
	 */
	public static View getChildAt(ViewGroup vg, int index, Class<? extends View> cls) {
		int nChild = vg.getChildCount();
		for (int i = 0; i < nChild; i++) {
			View v = vg.getChildAt(i);
			if (v.getClass().isAssignableFrom(cls)) {
				if (index == 0) {
					return v;
				}
				index--;
			}
		}
		Log.e(TAG, "failed to find a child of class " + cls.getName() + " from parent id " + vg.getId());
		return null;
	}
	
	/**
	 * how many children of class cls does this viewgroup have?
	 * @param vg ViewGroup to count the children of
	 * @param cls class to match
	 * @return count of matching children
	 */
	public static int getChildCount(ViewGroup vg, Class<? extends View> cls) {
		int nChild = vg.getChildCount();
		int count = 0;
		for (int i = 0; i < nChild; i++) {
			View v = vg.getChildAt(i);
			if (v.getClass().isAssignableFrom(cls)) {
				count++;
			}
		}
		return count;
	}
	
	
	public static View findChild(View v, int index, Class<? extends View> cls) {
		Integer indexWrapper = Integer.valueOf(index);
		return findChild(v, indexWrapper, cls);
	}
	
	/**
	 * find a child with a matching index and type recursively, starting from v.
	 * @param v view to find child from (may be a ViewGroup or view)
	 * @param index (modified) Integer index of child to search for among children of v
	 * @param cls class to match
	 * @return view or null if no matching child found
	 */
	public static View findChild(View v, Integer index, Class<? extends View> cls) {
		if (v.getClass().isAssignableFrom(cls)) {
			if (index == 0) {
				return v;
			} else {
				index--;
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int i = 0; i < nChild; i++) {
				View vChild = TestUtils.findChild(vg, index, cls);
				if (vChild != null) {
					return vChild;
				}
			}
		}
		return null;
	}
	
	/**
	 * return the nearest child with a matching id. Keep iterating up parents until we find a matching child of that parent
	 * NOTE: this only matches on id, we may write another function which also matches on class
	 * @param v view to search from (iterate from here)
	 * @param id resource id of view to search for
	 * @return View or null if not found.
	 */
	public static View findNearest(View v, int id) {
		ViewParent vp = v.getParent();
		while (vp != null) {
			View vFound = TestUtils.findDescendantById((View) vp, id);
			if (vFound != null) {
				return vFound;
			}
			vp = vp.getParent();
		}
		return null;
	}
	
	/**
	 * find the first descendant with a matching id.
	 * @param v view or viewgroup to search from recursively
	 * @param id resource id of view to match
	 * @return View or null
	 */
	public static View findDescendantById(View v, int id) {
		if (v.getId() == id) {
			return v;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int iChild = 0; iChild < nChild; iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vFound = TestUtils.findDescendantById(vChild, id);
				if (vFound != null) {
					return vFound;
				}
			}
		}
		return null;
	}
	/**
	 * return the nearest child with a matching class. Keep iterating up parents until we find a matching child of that parent
	 * NOTE: this only matches on id, we may write another function which also matches on class
	 * @param v view to search from (iterate from here)
	 * @param cls class to match
	 * @return View or null if not found.
	 */
	public static View findNearest(View v, Class<?extends View> cls) {
		ViewParent vp = v.getParent();
		while (vp != null) {
			View vFound = TestUtils.findDescendantByClass((View) vp, v, cls);
			if (vFound != null) {
				return vFound;
			}
			vp = vp.getParent();
		}
		return null;
	}
	
	/**
	 * find the first descendant with a matching class
	 * @param v view or viewgroup to search from recursively
	 * @param vExcept - except for this view (used in findNearest)
	 * @param cls class to match
	 * @return View or null
	 */
	public static View findDescendantByClass(View v, View vExcept, Class<? extends View> cls) {
		if ((v != vExcept) && cls.isAssignableFrom(v.getClass())) {
			return v;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int iChild = 0; iChild < nChild; iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vFound = TestUtils.findDescendantByClass(vChild, vExcept, cls);
				if (vFound != null) {
					return vFound;
				}
			}
		}
		return null;
	}
	/**
	 * return the nearest child with a matching text. Keep iterating up parents until we find a matching child of that parent
	 * NOTE: this only matches on id, we may write another function which also matches on class
	 * @param v view to search from (iterate from here)
	 * @param text text to match
	 * @return View or null if not found.
	 */
	public static View findNearest(View v, String text) {
		ViewParent vp = v.getParent();
		while (vp != null) {
			View vFound = TestUtils.findDescendantByText((View) vp, v, text);
			if (vFound != null) {
				return vFound;
			}
			vp = vp.getParent();
		}
		return null;
	}
	
	/**
	 * find the first descendant with a matching text
	 * @param v view or viewgroup to search from recursively
	 * @param vExcept - except for this view (used in findNearest)
	 * @param text text to match
	 * @return View or null
	 */
	public static View findDescendantByText(View v, View vExcept, String text) {
		if ((v != vExcept) && (v instanceof TextView) && ((TextView) v).getText().equals(text)) {
			return v;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int iChild = 0; iChild < nChild; iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vFound = TestUtils.findDescendantByText(vChild, vExcept, text);
				if (vFound != null) {
					return vFound;
				}
			}
		}
		return null;
	}


	/**
	 * search for all the children of a control with a matching id.  Useful for layouts with repeated controls
	 * @param v view to search from
	 * @param id resource id of view to search for
	 * @return List of matching views.  Empty if no views found
	 */
	public static List<View> findDescendantsById(View v, int id) {
		List<View> viewList = new ArrayList<View>();
		TestUtils.findDescendantsById(v, id, viewList);
		return viewList;
	}
		
	protected static void findDescendantsById(View v, int id, List<View> viewList) {
		if (v.getId() == id) {
			viewList.add(v);
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int iChild = 0; iChild < nChild; iChild++) {
				View vChild = vg.getChildAt(iChild);
				TestUtils.findDescendantsById(vChild, id, viewList);
			}
		}
	}
	
	/**
	 * is v a descendant of vCandAncestor?
	 * @param v hopeful ancestor
	 * @param vCandAncestor candidate descendant
	 * @return true if the paternity test matches.
	 */
	public static boolean isDescendant(ViewParent vp, View vCandAncestor) {
		if (vp == null) {
			return false;
		} else if (vCandAncestor == vp) {
			return true;
		} else {
			return isDescendant(vp.getParent(), vCandAncestor);
		}
	}
	
	/**
	 * pull a list of strings from a list of views.
	 * @param viewList
	 * @return List of strings.  If the corresponding view does not derive from text view, then insert an empty string for that element.
	 */
	public static List<String> getStringsFromViews(List<View> viewList) {
		List<String> stringList = new ArrayList<String>(viewList.size());
		for (Iterator<View> viewIter = viewList.iterator(); viewIter.hasNext();) {
			View view = viewIter.next();
			if (view instanceof TextView) {
				stringList.add(((TextView) view).getText().toString());		
			} else {
				stringList.add("");
			}
		}
		return stringList;
	}

	/**
	 * find the number of children of v of type cls
	 * @param v view (ViewGroup) to search from
	 * @param cls class to match
	 * @return count of matching descendants.
	 */
	public static int countDescendants(View v, Class<? extends View> cls) {
		if (v.getClass().isAssignableFrom(cls)) {
			return 1;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			int total = 0;
			for (int i = 0; i < nChild; i++) {
				View vChild = vg.getChildAt(i);
				total += TestUtils.countDescendants(vChild, cls);
			}
			return total;
		}
		return 0;
	}
	
	// how many views in the hierarchy have this id?
	public static int idCount(View v, int refID) {
		int count = 0;
		if (v.getId() == refID) {
			count++;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				count += TestUtils.idCount(vChild, refID);
			}
		}
		return count;
	}
	
	// recursively search for text.
	public static int textCount(View v, String text) {
		int count = 0;
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			String textCand = tv.getText().toString();
			if (textCand.equals(text)) {
				count++;
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				count += TestUtils.textCount(vChild, text);
			}
		}
		return count;
	}
	
	/**
	 * given a view, find an ancestor with a non-zero id
	 * @param v view to search from
	 * @return some ancestor of v, or null if no ancestor of v has a non-zero id.
	 */
	public static View findParentViewWithId(View v) {
		if (v.getId() != 0) {
			return v;
		}
		ViewParent vp = v.getParent();
		while (vp != null) {
			v = (View) vp;
			if (v.getId() != 0) {
				return v;
			}
			vp = v.getParent();	
		}
		return null;
	}
	
	/**
	 * perform a pre-order traversal of all the descendants of v, and count the number whose class is cls
	 * @param v root of the tree to search from
	 * @param cls class to match
	 * @return number of matches.
	 */
	public static int classCount(View v, Class<? extends View> cls) {
		int count = 0;
		if (v.getClass() == cls) {
			count++;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				count += classCount(vChild, cls);
			}
		}
		return count;
	}
	
	protected class IndexTarget {
		public int mCountSoFar = 0;
	}
	/**
	 * find the index of v filtered by class in the set of subchildren of vRoot, by preorder traversal
	 * @param vRoot root to traverse from
	 * @param v view to find (indexed by class)
	 * @return index of v by class, or -1 if not found
	 */
	public static int classIndex(View vRoot, View v) {
		TestUtils testUtils = new TestUtils();
		IndexTarget target = testUtils.new IndexTarget();
		if (classIndex(vRoot, v.getClass(), v, target)) {
			return target.mCountSoFar;
		} else {
			return -1;
		}
	}
	
	/**
	 * sometimes the class is different than the class of the actual view that we're looking for.
	 * For example, the view may be private, and have derived from another view class, so we want the
	 * index of *that* class
	 * @param vRoot root of hierarchy to search
	 * @param cls class to compare against
	 * @param v view to match
	 * @return preorder index of the view, filtered by views which match cls
	 */
	public static int classIndex(View vRoot, Class<? extends View> cls, View v) {
		TestUtils testUtils = new TestUtils();
		IndexTarget target = testUtils.new IndexTarget();
		if (classIndex(vRoot, cls, v, target)) {
			return target.mCountSoFar;
		} else {
			return -1;
		}
	}
	
	// recursive subfunction which actually does the work to find the index of the view's class.
	private static boolean classIndex(View vRoot, Class<? extends View> cls, View v, IndexTarget target) {
		if (vRoot == v) {
			return true;
		}
		if (cls.isAssignableFrom(vRoot.getClass()) && vRoot.isShown()) {
			target.mCountSoFar++;
		}
		if (vRoot instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) vRoot;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				if (classIndex(vChild, cls, v, target)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * is this view a child of the action bar?
	 * @param v view to test
	 * @return true if its ancestor is an action bar
	 * @throws ClassNotFoundException
	 */
	public static boolean isActionBarDescendant(View v) throws ClassNotFoundException {
   		Class<? extends View> actionBarImplClass = (Class<? extends View>) Class.forName(Constants.Classes.ACTION_BAR_CONTAINER);
		return isDescendentOfClass(v, v.getRootView(), actionBarImplClass);
	}
	
	// is this control a child of a tab control? There's the old TabHost, and the new action bar tab
	// control to take into consideration
	public static boolean isInTabControl(View v) throws ClassNotFoundException {
		if (isDescendentOfClass(v, v.getRootView(), TabHost.class)) {
			return true;
		}
		Class<? extends View> tabImplClass = (Class<? extends View>) Class.forName(Constants.Classes.ACTION_BAR_IMPL_TAB_IMPL);
		if (isDescendentOfClass(v, v.getRootView(), tabImplClass)) {
			return true;
		}
		Class<? extends View> scrollingTabContainerClass = (Class<? extends View>) Class.forName(Constants.Classes.SCROLLING_TAB_CONTAINER_VIEW);
		if (isDescendentOfClass(v, v.getRootView(), scrollingTabContainerClass)) {
			return true;
		}
		return false;
	}
	
	/**
	 * is view a descendant of some view of class c?
	 * @param v candidate view
	 * @param rootView root view to recurse up to
	 * @param c class to test againsg
	 * @return
	 */
	public static boolean isDescendentOfClass(View v, View rootView, Class<? extends View> c) {
		if ((v == null) || (v == rootView)) {
			return false;
		} else if (c.isAssignableFrom(v.getClass())) {
			return true;
		} else {
			return isDescendentOfClass(v.getParent(), rootView, c);
		}
	}
		
	// variant which takes a view parent, since view root is a view parent, but not neccessarily a view.
	protected static boolean isDescendentOfClass(ViewParent vp, View rootView, Class<? extends View> c) {
		if ((vp == null) || (vp == rootView)) {
			return false;
		} else if (c.isAssignableFrom(vp.getClass())) {
			return true;
		} else {
			return isDescendentOfClass(vp.getParent(), rootView, c);
		}
	}
	
	/**
	 * is v a descendant of an AdapterView?
	 * @param v view to test
	 * @return true if an ancestor of v is an AdapterView
	 */
	public static boolean isAdapterViewAncestor(View v) {
		ViewParent vp = v.getParent();
		return isDescendentOfClass(vp, v.getRootView(), AdapterView.class);
	}
	
	/**
	 * return the descendent of v whose type derives from c
	 * @param v
	 * @param c
	 * @return
	 */
	public static View getDescendantOfClass(View v, Class<? extends View> c) {
		return getDescendantOfClass(v.getParent(), v.getRootView(), c);
	}
	
	public static View getDescendantOfClass(ViewParent vp, View rootView, Class<? extends View> c) {
		if ((vp == null) || (vp == rootView)) {
			return null;
		} else if (c.isAssignableFrom(vp.getClass())) {
			return (View) vp;
		} else {
			return getDescendantOfClass(vp.getParent(), rootView, c);
		}
	}	
	
	/**
	 * does this list view have any listeners?
	 * @param av
	 * @return
	 */
	public static boolean adapterHasListeners(AdapterView av) {
		return (av.getOnItemClickListener() != null) || (av.getOnItemSelectedListener() != null);
	}
	
	/**
	 * low-level events to this object should be ignored
	 * @param v view to check
	 * @return true if the view sends higher-level events that we listen to
	 */
	public static boolean shouldBeIgnored(View v) {
		String className = v.getClass().getName();
		return className.equals(Constants.Classes.OVERFLOW_MENU_BUTTON) ||
			   className.equals(Constants.Classes.SCROLLING_TAG_CONTAINER_TAB_VIEW);
	}
	
	public static boolean isScrollingTextView(TextView tv) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(tv, TextView.class, Constants.Fields.HORIZONTALLY_SCROLLING);
	}
	
	/**
	 * detecting whether something can scroll is difficult, except that there is the "isScrollingContainer" function
	 * unfortunately, it's only available in API level 16
	 * @param v
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 */
	public static boolean invokeIsScrollingContainer(View v) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		if (android.os.Build.VERSION.SDK_INT > 16) {
			return ReflectionUtils.execMethodBoolean(v, View.class, Constants.Methods.IS_SCROLLING_CONTAINER, null);
		}
		return false;
	}
	
	/**
	 * is it an instrinsic scrolling view
	 * @param v
	 * @return
	 */
	public static boolean isScrollView(View v) {
		if (v instanceof ScrollView) {
			return true;
		}
		if (v instanceof Gallery) {
			return true;
		}
		// HorizontalScrolView may not be defined in earlier android APIs
		
		try {
			Class<? extends View> cls = (Class<? extends View>) Class.forName(Constants.Classes.HORIZONTAL_SCROLL_VIEW);
			if (cls.isAssignableFrom(v.getClass())) {
				return true;
			}
		} catch (Exception ex) {		
		}
		return false;
	}

	/**
	 * unfortunately, galleries don't have scroll listeners that we can hook to, and instead implement
	 * methods for onScroll(), etc.  We have to listen for motion events, then play them back.
	 * Later, we'll allow the user to set listen to motion events for custom controls and stuff like that.
	 * @param v
	 * @return true if we should listen to motion events
	 */
	public static boolean listenMotionEvents(List<View> motionEventViewList, View v) throws IllegalAccessException, NoSuchFieldException{
		
		// I really dislike this hardcoded bullshit that is certain to break in later versions.  We need to have a 
		// "blacklist" as well as a "whitelist" for motion events. Or Something Better Than This Implementation
		// which sucks Giant Donkey Dicks
		if (!(v instanceof AdapterView)) {
			if (isScrollView(v)) {
				return true;
			}
			if (v.isHorizontalScrollBarEnabled() || v.isVerticalScrollBarEnabled()) {
				return true;
			}
			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				if (!isScrollingTextView(tv)) {
					return false;
				}
			}
			/*
			if (v.canScrollHorizontally(-1)) {
				return true;
			}
			if (v.canScrollVertically(-1)) {
				return true;
			}
			if (v.canScrollHorizontally(1)) {
				return true;
			}
			if (v.canScrollVertically(1)) {
				return true;
			}
			*/
		}
		if (motionEventViewList != null) {
			return motionEventViewList.contains(v);
		}
		return false;
	}
	/**
	 * given a view inside of an adapter view, find the index of its containing item in the adapter view.
	 * @param av adapterView ancestor of v
	 * @param v view to search for
	 * @return index of item containing (from 0), or -1 if not found
	 */
	public static int getAdapterViewIndex(AdapterView av, View v) {
		Adapter adapter = av.getAdapter();
		int iStart = av.getFirstVisiblePosition();
		int iEnd = av.getLastVisiblePosition();
		for (int iChild = iStart; iChild <= iEnd; iChild++) {
			View vItem = av.getChildAt(iChild - iStart);
			if (TestUtils.isDescendant(v.getParent(), vItem)) {
				return iChild;
			}
		}
		return -1;
	}
	
	/**
	 * given a string (probably yanked from a TextView), return the matching R.string references (there may be more than one)
	 * @param res - application resources
	 * @param rdotstring - R.string class
	 * @param s string to compare against
	 * @return List<String> list of matching strings.
	 * @throws IllegalAccessException
	 */
	public static List<String> getIdForString(Resources res, Object rdotstring, String s) throws IllegalAccessException {
		List<String> resultList = new ArrayList<String>();
		Class cls = rdotstring.getClass();
		Field[] fieldList = cls.getDeclaredFields();
		for (Field field : fieldList) {
			int fieldValue = field.getInt(rdotstring);
			String candString = res.getString(fieldValue);
			if (s.equals(candString)) {
				String reference = cls.getName() + "." + field.getName();
				resultList.add(reference);
			}
		}
		return resultList;
	}
	
	/**
	 * same thing, except iterate over the list of R.string classes.
	 * @param res
	 * @param rdotstringlist
	 * @param s
	 * @return
	 * @throws IllegalAccessException
	 */
	public static List<String> getIdForString(Resources res, List<Object> rdotstringlist, String s) throws IllegalAccessException {
		List<String> resultList = new ArrayList<String>();
		for (Object rdotstring : rdotstringlist) {
			List<String> someResults = getIdForString(res, rdotstring, s);
			resultList.addAll(someResults);
		}
		return resultList;
	}

	
	/**
	 * Given a R.id class which is generated from the XML files for Android, and a value for an id from a view, return the fully qualified
	 * field name and class name, for example: com.example.R.id.cancel_button
	 * @param r R.id class to search
	 * @param idValue value to search for in fields
	 * @return R.id.foo or null if not found.
	 */
	public static String getIdForValue(Object rdotid, int idValue) throws IllegalAccessException {
		Class cls = rdotid.getClass();
		Field[] fieldList = cls.getDeclaredFields();
		for (Field field : fieldList) {
			int fieldValue = field.getInt(rdotid);
			if (fieldValue == idValue) {
				return cls.getName() + "." + field.getName();
			}
		}
		return null;
	}
	
	/**
	 * iterate over the list of Android-generated "R.id" classes, see if idValue occurs in any of them
	 * @param rdotidlist list of com.myexample.R classes
	 * @param idValue id to search for
	 * @return class/field reference for id, or null.
	 * @throws IllegalAccessException
	 */
	public static String getIdForValue(List<Object> rdotidlist, int idValue) throws IllegalAccessException {
		for (Object rdotid : rdotidlist) {
			String id = TestUtils.getIdForValue(rdotid, idValue);
			if (id != null) {
				return id;
			}
		}
		return "0x" + Integer.toHexString(idValue);
	}
	
	/**
	 * does this view belong to activity a?
	 * @param a activity 
	 * @param v view
	 * @return
	 */
	public static boolean isActivityView(Activity a, View v) {
		if (v != null) {
			Context viewContext = v.getContext();
			// dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
			// base context
			if (viewContext instanceof ContextThemeWrapper) {
				ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
				viewContext = ctw.getBaseContext();
			}
			Context activityContext = a;
			Context activityBaseContext = a.getBaseContext();
			return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext));
		} else {
			return false;
		}
		
	}

	public static boolean isDialogOrPopup(Activity a, View v) {
		if (v != null) {
			Context viewContext = v.getContext();
			// dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
			// base context
			if (viewContext instanceof ContextThemeWrapper) {
				ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
				viewContext = ctw.getBaseContext();
			}
			Context activityContext = a;
			Context activityBaseContext = a.getBaseContext();
			return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext)) && (v != a.getWindow().getDecorView());
		} else {
			return false;
		}
	}
	
	/**
	 * v is actually a PhoneDecorView.  We're looking for its child, which is com.android.internal.view.menu.ExpandedMenuView
	 * @param v
	 * @return
	 */
	public static boolean isOptionsMenu(View v) throws ClassNotFoundException {
		if (v instanceof ViewGroup) {
			View vChild = ((ViewGroup) v).getChildAt(0);
			Class<? extends View> menuViewClass = (Class<? extends View>) Class.forName(Constants.Classes.EXPANDED_MENU_VIEW);
			return vChild.getClass() == menuViewClass; 	
		} else {
			return false;
		}
	}
	
	/**
	 * find the view associated with the options menu for this activity
	 * @param activity
	 * @return
	 */
	public static View findOptionsMenu(Activity activity) {
		try {
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (TestUtils.isDialogOrPopup(activity, v)) {	
						if (TestUtils.isOptionsMenu(v)) {
							return v;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * see if this activity has popped up a dialog.
	 * @param activity activity to test
	 * @return Dialog or null
	 */
	public static Dialog findDialog(Activity activity) {
		try {
			Class phoneDecorViewClass = Class.forName(Constants.Classes.PHONE_DECOR_VIEW);
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (TestUtils.isDialogOrPopup(activity, v)) {	
						if (v.getClass() == phoneDecorViewClass) {
							Window phoneWindow = (Window) ReflectionUtils.getFieldValue(v, phoneDecorViewClass, Constants.Classes.THIS);
							Window.Callback callback = phoneWindow.getCallback();
							if (callback instanceof Dialog) {
								Dialog dialog = (Dialog) callback;
								return dialog;
							}
						} 
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}
	
	/**
	 * popup windows are slightly different than dialogs, so we have a separate path which polls for them
	 * to set up in RecordTest
	 * @param activity
	 * @return Object: because it can be a window or a popup window, and they don't inherit from each other
	 */
	public static WindowAndView findPopupWindow(Activity activity) {
		try {
			Class popupViewContainerClass = Class.forName(Constants.Classes.POPUP_VIEW_CONTAINER);
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (TestUtils.isDialogOrPopup(activity, v)) {
						
						// return null if we've already intercepted
						if (!(v instanceof MagicFramePopup)) {
							Object window = ReflectionUtils.getFieldValue(v, v.getClass(), Constants.Classes.THIS);
							WindowAndView windowAndView = new WindowAndView(window, v);
							return windowAndView;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}
	
	/**
	 * find a view associated with a popup window
	 * @param activity
	 * @param popupWindow
	 * @return
	 */
	public static View findViewForPopup(Activity activity, PopupWindow popupWindow) {
		try {
			Class popupViewContainerClass = Class.forName(Constants.Classes.POPUP_VIEW_CONTAINER);
			View[] views = ViewExtractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (TestUtils.isDialogOrPopup(activity, v)) {	
						if (v.getClass() == popupViewContainerClass) {
							PopupWindow candPopupWindow = (PopupWindow) ReflectionUtils.getFieldValue(v, popupViewContainerClass, Constants.Classes.THIS);
							if (candPopupWindow == popupWindow) {
								return v;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/** 
	 * since we're in a polling loop for popup windows, we can interrogate the window before it's actually populated, 
	 * and we need to check the menu items for callbacks to determine if it's the options menu, if the window is empty, we blow it off
	 * @param popupWindow
	 * @return
	 */
	public static boolean isPopupWindowEmpty(PopupWindow popupWindow) {
		View contentView = popupWindow.getContentView();
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		return contentViewGroup.getChildCount() == 0;
	}
	
	/**
	 * same, except for "window", not PopupWindow
	 * @param window
	 * @return
	 */
	public static boolean isWindowEmpty(Window window) {
		ViewGroup decorView = (ViewGroup) window.getDecorView();
		ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);		
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		return contentViewGroup.getChildCount() == 0;
	}
	
	/**
	 * from the popup window, get the content view, then iterate over its children.  Each child contains mItemData, which contains mMenu, which
	 *  has a callback field. If that callback field is PhoneWindow, then it's an options menu, otherwise it's a popup.
	 *  the classes are all internal, so we need to do Class.formName to extract fields.
	 *  popupWindow.mContentView.mChildren[*].mItemData.mMenu.mCallback
	 *  types: android.widget.PopupWindow
	 *  android.widget.ListPopupWindow$DropDownListView
	 *  com.android.internal.view.menu.ListMenuItemView
	 *  com.android.internal.view.menu.MenuItemImpl
	 *  com.android.internal.view.menu.MenuBuilder
	 *  returns Object[] where android.widget.PopupMenu for Popup and PhoneWindow for activity option menu
	 */
	
	public static List<Object> getPopupWindowCallbackList(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<Object> callbackList = new ArrayList<Object>();
		View contentView = popupWindow.getContentView();
		ViewGroup contentViewGroup = (ViewGroup) contentView;
		Class listMenuItemViewClass = Class.forName(Constants.Classes.LIST_MENU_ITEM_VIEW);
		Class menuItemImplClass = Class.forName(Constants.Classes.MENU_ITEM_IMPL);
		Class menuBuilderClass = Class.forName(Constants.Classes.MENU_BUILDER);
		if (contentViewGroup.getChildCount() == 0) {
			Log.i(TAG, "interesting");
		}
		for (int i = 0; i < contentViewGroup.getChildCount(); i++) {
			View menuItemCandView = contentViewGroup.getChildAt(i);
			if (menuItemCandView.getClass() == listMenuItemViewClass) {
				Object itemData = ReflectionUtils.getFieldValue(menuItemCandView, listMenuItemViewClass, Constants.Fields.ITEM_DATA);
				if (itemData.getClass() == menuItemImplClass) {
					Object menu = ReflectionUtils.getFieldValue(itemData, menuItemImplClass, Constants.Fields.MENU);
					if (menu.getClass() == menuBuilderClass) {
						Object callback = ReflectionUtils.getFieldValue(menu, menuBuilderClass, Constants.Fields.CALLBACK);
						callbackList.add(callback);
					}
				}
			}
		}
		return callbackList;		
	}
	
	/**
	 * is this the popup for the options menu for the activity? see getPopupWindowCallbackList() for details
	 * @param popupWindow popup window to test
	 * @return true if the callbacks for the menu item point back to the phone window.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static boolean isOptionsMenu(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		List<Object> callbackList = getPopupWindowCallbackList(popupWindow);
		Class phoneWindowClass = Class.forName(Constants.Classes.PHONE_WINDOW);
		if (callbackList.isEmpty()) {
			return false;
		} else {
			for (Object callback : callbackList) {
				if (callback.getClass() != phoneWindowClass) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * some popup windows have anchors, like the overflow menu button in the action bar, or the button in a spinner
	 * @param popupWindow the potentially anchored popup window
	 * @return anchor view or null.
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static View getPopupWindowAnchor(PopupWindow popupWindow) throws IllegalAccessException, NoSuchFieldException {
		WeakReference<View> anchorRef = (WeakReference<View>) ReflectionUtils.getFieldValue(popupWindow, PopupWindow.class, Constants.Fields.ANCHOR);
		if (anchorRef != null) {
			return anchorRef.get();
		}
		return null;
	}

	/**
	 * is this popup window the dropdown to an AutoCompleteTextView?
	 * @param popupWindow
	 * @return true if the anchor is AutoCompleteTextView
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static boolean isAutoCompleteWindow(PopupWindow popupWindow) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
		View anchorView = getPopupWindowAnchor(popupWindow);
		if (anchorView != null) {
			if (anchorView instanceof AutoCompleteTextView) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * spinners can have popups (which are actually dropdowns), or dialog windows depending on the mode: MODE_DIALOG or mode: MODE_POPUP
	 * @param popupWindow
	 * @return
	 */
	public static boolean isSpinnerPopup(PopupWindow popupWindow) throws NoSuchFieldException, IllegalAccessException {
		View anchorView = TestUtils.getPopupWindowAnchor(popupWindow);
		return (anchorView instanceof Spinner);
	}
	
	public static Spinner isSpinnerDialog(Dialog dialog, Activity activity)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		List<Spinner> spinnerList = ViewExtractor.getActivityViews(activity, Spinner.class);
		for (Spinner spinner : spinnerList) {
			if (TestUtils.isPopupDialogForSpinner(dialog, spinner)) {
				return spinner;
			}
		}
		return null;
	}
	
	/**
	 * for a given spinner, see if this dialog is the spinner's popup dialog
	 * @param dialog dialog
	 * @param spinner candidate spinner
	 * @return true if it belongs, false if it does not
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean isPopupDialogForSpinner(Dialog dialog, Spinner spinner) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Object spinnerPopup = ReflectionUtils.getFieldValue(spinner, Spinner.class, Constants.Fields.POPUP);
		if (spinnerPopup != null) {
			Class spinnerDialogPopupClass = Class.forName(Constants.Classes.SPINNER_DIALOG_POPUP);
			if (spinnerDialogPopupClass.equals(spinnerPopup.getClass())) {
				Object spinnerPopupPopup = ReflectionUtils.getFieldValue(spinnerPopup, spinnerDialogPopupClass, Constants.Fields.POPUP);
				return spinnerPopupPopup == dialog;
			}
		}
		return false;
	}


	/**
	 * sometimes they put '$'s in the classname, sometimes they don't
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean classNameEquals(String a, String b) {
		return a.replace('$', '.').equals(b.replace('$', '.'));
	}
	
	// find a view by a matching class name.
	public static View getChildByClassName(View v, String simpleClassName) {
		if (v.getClass().getSimpleName().equals(simpleClassName)) {
			return v;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vMatch = getChildByClassName(vChild, simpleClassName);
				if (vMatch != null) {
					return vMatch;
				}				
			}
		}
		return null;
	}
	
	/**
	 * private function to find all of the children of a view matching class cls
	 * @param v
	 * @param cls
	 * @param viewList
	 */
	protected static void getChildrenByClassName(View v, Class<? extends View> cls, List<View> viewList) {
		if (cls.isAssignableFrom(v.getClass())) {
			viewList.add(v);
		} 
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				getChildrenByClassName(vChild, cls, viewList);
			}
		}
	}
	
	/**
	 * find all the children of class cls.
	 * @param v root view
	 * @param cls class to match
	 * @return list of matching views
	 */
	public static List<View> getChildrenByClass(View v, Class<? extends View> cls) {
		List<View> viewList = new ArrayList<View>();
		getChildrenByClassName(v, cls, viewList);
		return viewList;
	}	
	
	/**
	 * spinner dialogs have to be handled differently.  We search for an adapter of the spinner dropdown type.
	 * @param contentView
	 * @return
	 */
	public static boolean isSpinnerDialog(View contentView)  throws ClassNotFoundException {
		List<View> listList = TestUtils.getChildrenByClass(contentView, AdapterView.class);
		Class spinnerAdapterClass = Class.forName(Constants.Classes.SPINNER_ADAPTER);
		for (View v : listList) {
			AdapterView adapterView = (AdapterView) v;
			Adapter adapter = adapterView.getAdapter();
			if (adapter.getClass() == spinnerAdapterClass) {
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * get the content view of a dialog so we can intercept it with a MagicFrame
	 * @param dialog
	 * @return
	 */
	public static View getDialogContentView(Dialog dialog) {
		Window window = dialog.getWindow();
		View decorView = window.getDecorView();
		View contentView = ((ViewGroup) decorView).getChildAt(0);
		return contentView;
	}
	
	/**
	 * find the focused view in the hierarchy
	 * @param v parent view.
	 * @return focused view or null
	 */
	public static View getFocusedView(View v) {
		if (v.hasFocus()) {
			return v;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				View vFocus = getFocusedView(vChild);
				if (vFocus != null) {
					return vFocus;
				}
			}
		}
		return null;
	}

}
