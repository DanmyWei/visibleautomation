package com.androidApp.Intercept;

import java.util.ArrayList;

import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Test.ViewInterceptor;

import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class FindListeners {
	
	/**
	 * return true if a listener has been assigned for this view (i.e. it listens to events, and we should
	 * record them)
	 * @param v
	 * @return true if a listener has been assigned to this view.
	 */
	public static boolean hasListener(ViewInterceptor viewInterceptor, View v) {
		try {
			if (ListenerIntercept.getListenerInfo(v) != null) {
				return true;
			}
			if (v instanceof AdapterView) {
				if (ListenerIntercept.getItemSelectedListener((AdapterView) v) != null) {
					return true;
				}
				if (((AdapterView) v).getOnItemClickListener() != null) {
					return true;
				}
				if (((AdapterView) v).getOnItemLongClickListener() != null) {
					return true;
				}
			}
			if (v instanceof ExpandableListView) {
				if (ListenerIntercept.getOnChildClickListener((ExpandableListView) v) != null) {
					return true;
				}
				if (ListenerIntercept.getOnGroupClickListener((ExpandableListView) v) != null) {
					return true;
				}
			}
			if (v instanceof CompoundButton) {
				if (ListenerIntercept.getCheckedChangeListener((CompoundButton) v) != null) {
					return true;
				}
			}
			if (viewInterceptor.getInterceptInterface().hasListeners(v)) {
				return true;
			}
			if (v instanceof TextView) {
				 ArrayList<TextWatcher> textWatcherList = ListenerIntercept.getTextWatcherList((TextView) v);
				 if ((textWatcherList != null) && (textWatcherList.size() != 0)) {
					 return true;
				 }
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}
	
	/**
	 * given a view, either it or one of its ancestors has a listener
	 * @param v view
	 * @return view with a listener or null.
	 */
	public static View findViewWithListener(ViewInterceptor viewInterceptor, View v) {
		return findViewWithListener(viewInterceptor,v, v.getRootView());
	}
	
	// internal worker function for above.
	private static View findViewWithListener(ViewInterceptor viewInterceptor, View v, View vRoot) {
		if (v == vRoot) {
			return null;
		} else if (hasListener(viewInterceptor, v)) {
			return v;
		} else {
			ViewParent vp = v.getParent();
			if (vp instanceof View) {
				return findViewWithListener(viewInterceptor, (View) vp, vRoot);
			}
		}
		return null;
	}
}
