package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.widget.TabHost;

/**
 * class for listening to tab events (the old, non-actionbar style tab events)
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnTabChangeListener extends RecordListener implements TabHost.OnTabChangeListener, IOriginalListener {
	protected TabHost.OnTabChangeListener 	mOriginalTabListener;
	protected int 							mTabIndex;
	protected TabHost						mTabHost;
	
	public RecordOnTabChangeListener(String activityName, EventRecorder eventRecorder, TabHost tabHost) {
		super(activityName, eventRecorder);
		try {
			mTabHost = tabHost;
			mOriginalTabListener = ListenerIntercept.getTabChangeListener(tabHost);
			tabHost.setOnTabChangedListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public RecordOnTabChangeListener(String activityName, EventRecorder eventRecorder, TabHost.OnTabChangeListener originalTabListener, TabHost tabHost) {
		super(activityName, eventRecorder);
		try {
			mTabHost = tabHost;
			mOriginalTabListener = originalTabListener;
			tabHost.setOnTabChangedListener(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	@Override
	public void onTabChanged(String tabId) {
		mEventRecorder.writeRecord(Constants.EventTags.SELECT_TAB, mActivityName, mTabHost, tabId);
		if (mOriginalTabListener != null) {
			mOriginalTabListener.onTabChanged(tabId);
		}
	}	
}
