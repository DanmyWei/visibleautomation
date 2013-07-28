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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewParent;

/**
 * listen to changes in adapters, so new views in the adapters can be intercepted, since the GlobalLayoutListener
 * isn't quite global (it doesn't listen to adapter changes)
 * TODO: Defunct: remove this class
 * @author mattrey
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
*
 */
public class RecordOnHierarchyChangeListener extends RecordListener implements OnHierarchyChangeListener, IOriginalListener  {
	protected OnHierarchyChangeListener 	mOriginalOnHierarchyChangeListener;
	protected ViewInterceptor 				mViewInterceptor;
	
	public RecordOnHierarchyChangeListener(EventRecorder eventRecorder, ViewInterceptor viewInterceptor, ViewGroup vg) {
		super(eventRecorder);
		mViewInterceptor = viewInterceptor;
		try {
			mOriginalOnHierarchyChangeListener = ListenerIntercept.getOnHierarchyChangeListener(vg);
			vg.setOnHierarchyChangeListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, (View) vg, "create on click listener");
		}		
	}
	
	public RecordOnHierarchyChangeListener(EventRecorder 				eventRecorder, 
										   ViewInterceptor 				viewInterceptor, 
										   OnHierarchyChangeListener 	orginalOnHierarchyChangeListener) {
		super(eventRecorder);
		mViewInterceptor  = viewInterceptor;
		mOriginalOnHierarchyChangeListener = orginalOnHierarchyChangeListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnHierarchyChangeListener;
	}

	@Override
	public void onChildViewAdded(View parent, View child) {
		Activity activity = (Activity) parent.getContext();
		mViewInterceptor.callIntercept(activity, child);
		
	}

	@Override
	public void onChildViewRemoved(View parent, View child) {		
	}

}
