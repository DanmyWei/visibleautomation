package com.androidApp.Utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.androidApp.EventRecorder.ClassCount;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * grab-bag of utilities to extract views from the android view tree.
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class TestUtils {
	static final String TAG = "TestUtils";
	
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
		if (cls.isAssignableFrom(v.getClass())) {
			if (index == 0) {
				return v;
			} else {
				index--;
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int i = 0; i < nChild; i++) {
				View vChild = vg.getChildAt(i);
				View vFound = TestUtils.findChild(vChild, index, cls);
				if (vFound != null) {
					return vFound;
				}
			}
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
	 * @param fOnlySufficientlyShown robotium only counts by widgets that are visible on the screen,
	 * which is fine for those losers that count on their fingers, but not what I'd call reliable. Thanks, renas.
	 * @return index of v by class, or -1 if not found
	 */
	public static int classIndex(View vRoot, View v, boolean fOnlySufficientlyShown) {
		TestUtils testUtils = new TestUtils();
		IndexTarget target = testUtils.new IndexTarget();
		if (classIndex(vRoot, v.getClass(), v, target, fOnlySufficientlyShown)) {
			return target.mCountSoFar;
		} else {
			Log.e(TAG, "class index " + v + " not found in its own tree!");
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
	public static int classIndex(View vRoot, Class<? extends View> cls, View v, boolean fOnlySufficientlyShown) {
		TestUtils testUtils = new TestUtils();
		IndexTarget target = testUtils.new IndexTarget();
		if (classIndex(vRoot, cls, v, target, fOnlySufficientlyShown)) {
			return target.mCountSoFar;
		} else {
			Log.e(TAG, "class index " + v + " not found in its own tree!");
			return -1;
		}
	}
	

	// recursive subfunction which actually does the work to find the index of the view's class.
	private static boolean classIndex(View 					vRoot, 
									  Class<? extends View> cls, 
									  View 					v, 
									  IndexTarget 			target, 
									  boolean 				fOnlySufficientlyShown) {
		if (vRoot == v) {
			return true;
		}
		if (fOnlySufficientlyShown) {
			if (cls.isAssignableFrom(vRoot.getClass()) && vRoot.isShown() && isViewSufficientlyShown(vRoot)) {
				target.mCountSoFar++;
			}
		} else {
			if (cls.isAssignableFrom(vRoot.getClass())) {
				target.mCountSoFar++;
			}	
		}
		if (vRoot instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) vRoot;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				if (classIndex(vChild, cls, v, target, fOnlySufficientlyShown)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the scroll or list parent view
	 * This is copied from robotium, so we generate indices for shown objects in the same manner that they used then
	 *
	 * @param view the view who's parent should be returned
	 * @return the parent scroll view, list view or null
	 */

	public static View getScrollOrListParent(View view) {
	    if (!(view instanceof android.widget.AbsListView) && !(view instanceof android.widget.ScrollView) && !(view instanceof WebView)) {
	        try{
	            return getScrollOrListParent((View) view.getParent());
	        }catch(Exception e){
	            return null;
	        }
	    } else {
	        return view;
	    }
	}

	public static float getScrollListWindowHeight(View view) {
		final int[] xyParent = new int[2];
		View parent = getScrollOrListParent(view);
		final float windowHeight;
		if (parent == null) {
			WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
			windowHeight = wm.getDefaultDisplay().getHeight();
		} else{
			parent.getLocationOnScreen(xyParent);
			windowHeight = xyParent[1] + parent.getHeight();
		}
		parent = null;
		return windowHeight;
	}

	/**
	 * Returns true if the view is sufficiently shown
	 *
	 * @param view the view to check
	 * @return true if the view is sufficiently shown
	 */

	public static boolean isViewSufficientlyShown(View view){

		if (view == null) {
			return false;
		}
		final int[] xyView = new int[2];
		final int[] xyParent = new int[2];
		final float viewHeight = view.getHeight();
		final View parent = getScrollOrListParent(view);
		view.getLocationOnScreen(xyView);

		if (parent == null) {
			xyParent[1] = 0;
		} else {
			parent.getLocationOnScreen(xyParent);
		}

		if (xyView[1] + (viewHeight/2.0f) > getScrollListWindowHeight(view)) {
			return false;
		} else if(xyView[1] + (viewHeight/2.0f) < xyParent[1]) {
			return false;
		}
		return true;
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
	
	
	/**
	 * find the view containing event x,y with the greatest "depth" in the tree.
	 * @param v view (current in recursion)
	 * @param x coordinates of hit.
	 * @param y
	 * @return lowest-level view containing event.
	 */
	
	public static View findViewByXY(View v, float x, float y) {
		ClassCount depth = new ClassCount(0);
		Rect hitRect = new Rect();
		return findViewByXY(v, x, y, depth, hitRect, 0);
	}

	public static View findViewByXY(View v, float x, float y, ClassCount foundDepth, Rect hitRect, int depth) {
		if (v.isShown()) {
			v.getGlobalVisibleRect(hitRect);
			if (hitRect.contains((int) x, (int) y)) {
				if (v instanceof ViewGroup) {
					ViewGroup vg = (ViewGroup) v;
					for (int i = 0; i < vg.getChildCount(); i++) {
						View vChild = vg.getChildAt(i);
						View vCand = findViewByXY(vChild, x, y, foundDepth, hitRect, depth + 1);
						if (vCand != null) {
							return vCand;
						}
					}
					
				} else {
					if (depth > foundDepth.mCount) {
						foundDepth.mCount = depth;
						return v;
					}
					return null;
				}
			}
		}
		return null;	
	}
	
	/**
	 * get the activity for a view.  A view may be of a child of a dialog, in which case, getContext()
	 * returns a contextWrapper with the activity as contextWraper.getBaseContext()
	 * @param v
	 * @return
	 */
	public static Activity getViewActivity(View v) {
		Context context = v.getContext();
		if (context instanceof Activity) {
			return (Activity) context;
		} else if (context instanceof ContextWrapper) {
			return (Activity) ((ContextWrapper) context).getBaseContext();
		}
		return null;
	}
	
	/**
	 * recursively search for the focused view within the view hierarchy.
	 * @param v
	 * @return focused view or null.
	 */
	public static View findFocusedView(View v) {
		if (v.hasFocus()) {
			return v;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vFocused = findFocusedView(vg.getChildAt(i));
				if (vFocused != null) {
					return vFocused;
				}
			}
		}
		return null;
	}
}
