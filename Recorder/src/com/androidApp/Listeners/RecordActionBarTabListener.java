package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Intercept.InterceptActionBar;
import com.androidApp.Utility.Constants;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.view.View;

/**
 * class for listening to action bar tab events
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class RecordActionBarTabListener extends RecordListener implements ActionBar.TabListener, IOriginalListener {
	protected ActionBar.TabListener mOriginalTabListener;
	protected int 					mTabIndex;
	
	public RecordActionBarTabListener(EventRecorder eventRecorder, ActionBar actionBar, int index) {
		super(eventRecorder);
		try {
			mTabIndex = index;
			mOriginalTabListener = InterceptActionBar.getTabListener(actionBar, index);
			actionBar.getTabAt(index).setTabListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "create record action bar tab listener");
		}		
	}

	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String message = Integer.toString(mTabIndex) + "," + "select tab " + tab.getText();
		mEventRecorder.writeRecord(Constants.EventTags.SELECT_ACTIONBAR_TAB, message);
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
