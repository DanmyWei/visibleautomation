package com.androidApp.randomtest;

import java.util.ArrayList;
import java.util.List;

import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

public class TestUtility {
	private final static int PAUSE = 500;


	/**
	 *  flatten the view hierarchy into a list
	 * @param v root view
	 * @return flattened list of views
	 */
	public static List<View> getViewList(View v) throws ClassNotFoundException {
		Class actionBarContainerClass = Class.forName(Constants.Classes.ACTION_BAR_CONTAINER);
	    List<View> viewList = new ArrayList<View>();
		if (!v.getClass().equals(actionBarContainerClass)) {
		    viewList.add(v);
		    if ((v instanceof ViewGroup) && !(v instanceof AbsListView)) {
		    	ViewGroup vg = (ViewGroup) v;
		    	for (int i = 0; i < vg.getChildCount(); i++) {
				    View vChild = vg.getChildAt(i);
				    List<View> childList = getViewList(vChild);
				    viewList.addAll(childList);
		    	}
			}
		}
	    return viewList;
	}
	
	/**
	 * given an activity and the list of views from the window manager, get all the views that
	 * belong to the activity
	 * @param activity
	 * @param phoneDecorViews
	 * @return
	 */
	public static List<View> getAllViews(Activity activity, View[] phoneDecorViews) throws ClassNotFoundException {
		List<View> viewList = new ArrayList<View>();
		for (View v : phoneDecorViews) {
			if (TestUtils.isActivityView(activity, v)) {
				List<View> decorViews = getViewList(v);
				viewList.addAll(decorViews);
			}
		}
		return viewList;
	}

	/**
	 * retrieve a list of views filtered by class.
	 * @param v
	 * @param classname
	 * @return
	 */
	public static List<View> getViewList(View v, Class<? extends View> classname) throws ClassNotFoundException {
	    List<View> viewList = new ArrayList<View>();
	    if (v.getClass() == classname) {
	    	viewList.add(v);
	    }
	    if (v instanceof ViewGroup) {
	    	ViewGroup vg = (ViewGroup) v;
	    	for (int i = 0; i < vg.getChildCount(); i++) {
			    View vChild = vg.getChildAt(i);
			    List<View> childList = getViewList(vChild);
			    viewList.addAll(childList);
	    	}
		}
	    return viewList;
	}

	/**
	 * return the number of views in the list that satisify the class filter
	 * @param vList list of views
	 * @param classname class filter
	 * @return
	 */
	public static int numViewsOfClass(List<View> vList, Class<? extends View> classname) {
		int count = 0;
		for (View v : vList) {
			if (v.getClass() == classname) {
				count++;
			}
		}
		return count;
	}
	
	public static void sleep() {
		sleep(PAUSE);
	}

	/**
	 * sleep utility
	 * @param msec
	 */
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException iex) {
		}
	}

	/**
	 * given the view list, a class name filter, and a number of views that satisfy the filter, 
	 * @param vList list of views
	 * @param classname class filter
	 * @param numViews range
	 * @return
	 */
	public static View selectRandomView(List<View> vList, Class<? extends View> classname, int numViews) {
		int randIndex = ((int) Math.random()*numViews);
		for (View v : vList) {
			if (v.getClass() == classname) {
				if (randIndex == 0) {
					return v;
				}
				randIndex--;
			}
		}
		return null;
	}

	/**
	 * can this view scroll its contents.
	 * @param v view
	 * @param contentsRect calculated extends of contents
	 * @return
	 */
	public static boolean canScroll(View v, Rect contentsRect) {
		return (contentsRect.left < 0) || (contentsRect.top < 0) || 
			(contentsRect.bottom > v.getMeasuredHeight()) || (contentsRect.right > v.getMeasuredWidth());
	}

	/**
	 * given a view, return the extents of its contents.
	 * @param v 
	 * @return Rect of contents extents
	 */
	public static Rect getContentsRect(View v) {
		Rect contentsRect = new Rect(0, 0, 0, 0);
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				if (vChild.getLeft() < contentsRect.left) {
					contentsRect.left = vChild.getLeft();
				}
				if (vChild.getRight() > contentsRect.right) {
					contentsRect.right = vChild.getRight();
				}
				if (vChild.getTop() < contentsRect.top) {
					contentsRect.top = vChild.getTop();
				}
				if (vChild.getBottom() > contentsRect.bottom) {
					contentsRect.bottom = vChild.getBottom();
				}
			}
		}
		return contentsRect;
	}
}
