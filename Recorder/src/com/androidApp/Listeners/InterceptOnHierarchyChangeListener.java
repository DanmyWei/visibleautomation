package com.androidApp.Listeners;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewParent;

/**
 * listen to changes in adapters, so new views in the adapters can be intercepted, since the GlobalLayoutListener
 * isn't quite global (it doesn't listen to adapter changes)
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class InterceptOnHierarchyChangeListener extends RecordListener implements OnHierarchyChangeListener, IOriginalListener  {
	protected OnHierarchyChangeListener 	mOriginalOnHierarchyChangeListener;
	protected ViewInterceptor 				mViewInterceptor;
	protected Activity						mActivity;
	
	/**
	 * constructor: get the original hierarchy change listener, and wrap it with this
	 * @param eventRecorder event recorder interface
	 * @param viewInterceptor view interceptor
	 * @param vg actually an AdapterView, but we're trying to be general here
	 */
	public InterceptOnHierarchyChangeListener(Activity activity, EventRecorder eventRecorder, ViewInterceptor viewInterceptor, ViewGroup vg) {
		super(eventRecorder);
		mViewInterceptor = viewInterceptor;
		mActivity = activity;
		try {
			mOriginalOnHierarchyChangeListener = ListenerIntercept.getOnHierarchyChangeListener(vg);
			vg.setOnHierarchyChangeListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, (View) vg, "create on click listener");
		}		
	}
	
	/**
	 * constructor which takes the original hierarchy change listener
	 * @param eventRecorder event recorder interface
	 * @param viewInterceptor view interceptor
	 * @param orginalOnHierarchyChangeListener original change listener (may be null)
	 */
	public InterceptOnHierarchyChangeListener(Activity 						activity, 
											  EventRecorder 				eventRecorder, 
										   	  ViewInterceptor 				viewInterceptor, 
										   	  OnHierarchyChangeListener 	orginalOnHierarchyChangeListener) {
		super(eventRecorder);
		mActivity = activity;
		mViewInterceptor  = viewInterceptor;
		mOriginalOnHierarchyChangeListener = orginalOnHierarchyChangeListener;
	}
	
	// IOriginalListener
	public Object getOriginalListener() {
		return mOriginalOnHierarchyChangeListener;
	}

	/**
	 * when a child is added, intercept its views, but not immediately, since this is probably called from
	 * adapter.getView(), and the view's handlers might not be installed yet, so we force a go-round
	 * with the UI.
	 */
	@Override
	public void onChildViewAdded(View parent, View child) {
		if (!mViewInterceptor.runDeferred(parent, mViewInterceptor.new InterceptViewRunnable(mActivity, child))) {
			mViewInterceptor.callIntercept(mActivity, child);
		}
		if (mOriginalOnHierarchyChangeListener != null) {
			mOriginalOnHierarchyChangeListener.onChildViewAdded(parent, child);
		}
		
	}

	@Override
	public void onChildViewRemoved(View parent, View child) {		
		if (mOriginalOnHierarchyChangeListener != null) {
			mOriginalOnHierarchyChangeListener.onChildViewRemoved(parent, child);
		}
	}

}
