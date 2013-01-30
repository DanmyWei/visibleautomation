package com.androidApp.Utility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.androidApp.EventRecorder.ListenerIntercept;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.TextView;

public class TestUtils {
	private static final String TAG = "TestUtils";
	
	/**
	 * given a view group, find the indexthed child of class cls
	 * @param vg ViewGroup (usually cast from view)
	 * @param index index of the child to retrieve
	 * @param cls class to match
	 * @return View if found, returns null if not found.
	 */
	public static View getChildAt(ViewGroup vg, int index, Class cls) {
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
	public static int getChildCount(ViewGroup vg, Class cls) {
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
	
	
	public static View findChild(View v, int index, Class cls) {
		Integer indexWrapper = new Integer(index);
		return findChild(v, indexWrapper, cls);
	}
	
	/**
	 * find a child with a matching index and type recursively, starting from v.
	 * @param v view to find child from (may be a ViewGroup or view)
	 * @param index (modified) Integer index of child to search for among children of v
	 * @param cls class to match
	 * @return view or null if no matching child found
	 */
	public static View findChild(View v, Integer index, Class cls) {
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
	public static View findNearest(View v, Class cls) {
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
	public static View findDescendantByClass(View v, View vExcept, Class cls) {
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
	public static int countDescendants(View v, Class cls) {
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
	public static int classCount(View v, Class cls) {
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
	
	/**
	 * find the index of v filtered by class in the set of subchildren of vRoot, by preorder traversal
	 * @param vRoot root to traverse from
	 * @param v view to find (indexed by class)
	 * @return index of v by class, or -1 if not found
	 */
	public static int classIndex(View vRoot, View v) {
		Integer index = new Integer(0);
		boolean found = classIndex(vRoot, v, index);
		if (found) {
			return index.intValue();
		} else {
			return -1;
		}
	}
	
	// recursive subfunction which actually does the work to find the index of the view's class.
	private static boolean classIndex(View vRoot, View v, Integer index) {
		if (vRoot == v) {
			return true;
		}
		if (vRoot.getClass() == v.getClass()) {
			index++;
		}
		if (vRoot instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) vRoot;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				boolean found = classIndex(vChild, v, index);
				if (found) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * is v a descendant of an AdapterView?
	 * @param v view to test
	 * @return true if an ancestor of v is an AdapterView
	 */
	public static AdapterView getAdapterViewAncestor(View v) {
		ViewParent vp = v.getParent();
		return getAdapterViewAncestor(vp, v.getRootView());
	}
	
	public static AdapterView getAdapterViewAncestor(ViewParent vp, View rootView) {
		if ((vp == null) || (vp == rootView)) {
			return null;
		} else if (vp instanceof AdapterView) {
			return (AdapterView) vp;
		} else {
			return getAdapterViewAncestor(vp.getParent(), rootView);
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

	public static boolean isDialog(Activity a, View v) {
		Context c = v.getContext();
		// dialogs use a context theme wrapper, not a context, so we have to extract he context from the theme wrapper's
		// base context
		if (c instanceof ContextThemeWrapper) {
			ContextThemeWrapper ctw = (ContextThemeWrapper) c;
			c = ctw.getBaseContext();
		}
		// if the view has the same context (i.e. is owned by the application), and it's not the application's 
		// decor view, then a dialog is up.
		if (c == a) {
			if (v.hasWindowFocus() && (v != a.getWindow().getDecorView())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * see if this dialog has popped up an activity.
	 * @param activity activity to test
	 * @return Dialog or null
	 */
	public static Dialog findDialog(Activity activity) {
		try {
			ViewExtractor viewExractor = new ViewExtractor();
			View[] views = viewExractor.getWindowDecorViews();
			if (views != null) {
				int numDecorViews = views.length;
				
				// iterate through the set of decor windows.  The dialog may already have been presented.
				for (int iView = 0; iView < numDecorViews; iView++) {
					View v = views[iView];
					if (TestUtils.isDialog(activity, v)) {					
						Class phoneDecorViewClass = Class.forName(Constants.Classes.PHONE_DECOR_VIEW_CLASS);
						Class enclosingClass = phoneDecorViewClass.getEnclosingClass();
						Window phoneWindow = (Window) ListenerIntercept.getFieldValue(v, phoneDecorViewClass, "this$0");
						Window.Callback callback = phoneWindow.getCallback();
						Dialog dialog = (Dialog) callback;
						return dialog;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}

}
