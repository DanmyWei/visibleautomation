package com.androidApp.SupportListeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
import com.androidApp.SupportIntercept.InterceptActionBarSupport;
import com.androidApp.Listeners.IOriginalListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.view.View;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

/**
 * class for listening to action bar tab events
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordActionBarTabListener extends RecordListener implements ActionBar.TabListener, IOriginalListener {
	protected ActionBar.TabListener mOriginalTabListener;
	protected int 					mTabIndex;
	
	public RecordActionBarTabListener(String activityName, EventRecorder eventRecorder, ActionBar actionBar, int index) {
		super(activityName, eventRecorder);
		try {
			mTabIndex = index;
			mOriginalTabListener = InterceptActionBarSupport.getTabListener(actionBar, index);
			actionBar.getTabAt(index).setTabListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(mActivityName, ex, "create record action bar tab listener");
		}		
	}

	// tab selected is the only event of interest
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String message = Integer.toString(mTabIndex) + "," + "select tab " + tab.getText();
		mEventRecorder.writeRecord(mActivityName, Constants.EventTags.SELECT_ACTIONBAR_TAB, message);
		if (mOriginalTabListener != null) {
			mOriginalTabListener.onTabSelected(tab, ft);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mOriginalTabListener != null) {
			mOriginalTabListener.onTabUnselected(tab, ft);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		if (mOriginalTabListener != null) {
			mOriginalTabListener.onTabUnselected(tab, ft);
		}
	}
	
}
