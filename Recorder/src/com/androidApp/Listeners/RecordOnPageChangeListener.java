package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.support.v4.view.ViewPager;

/**
 * recorder for ViewPager page change events.
 * TODO: This may be the cause of errors with toggle buttons
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordOnPageChangeListener extends RecordListener implements ViewPager.OnPageChangeListener, IOriginalListener  {
	protected ViewPager.OnPageChangeListener 	mOriginalOnPageChangeListener;
	protected ViewPager							mViewPager;
	
	public RecordOnPageChangeListener(String activityName, EventRecorder eventRecorder, ViewPager viewPager) {
		super(activityName, eventRecorder);
		mViewPager = viewPager;
		try {
			mOriginalOnPageChangeListener = ListenerIntercept.getPageChangeListener(viewPager);
			viewPager.setOnPageChangeListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, viewPager, "create on page change listener");
		}		
	}
	
	public RecordOnPageChangeListener(String							activityName,
									  EventRecorder 					eventRecorder, 
									  ViewPager.OnPageChangeListener 	originalPageChangeListener,
									  ViewPager						 	viewPager) {
		super(activityName, eventRecorder);
		mViewPager = viewPager;
		mOriginalOnPageChangeListener = originalPageChangeListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalOnPageChangeListener;
	}
		
	@Override
	public void onPageScrollStateChanged(int state) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				String logMsg = Integer.toString(state) + "," + getDescription(mViewPager);
				mEventRecorder.writeRecord(Constants.EventTags.PAGE_SCROLL_STATE_CHANGED, mActivityName, mViewPager, logMsg);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, mViewPager, "on page scroll state changed");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnPageChangeListener != null) {
				mOriginalOnPageChangeListener.onPageScrollStateChanged(state);
			}
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				String logMsg = Integer.toString(position) + "," + 
								Float.toString(positionOffset) + "," +
								Integer.toString(positionOffsetPixels) + "," + getDescription(mViewPager);
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.PAGE_SCROLLED, logMsg);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, mViewPager, "on page scroll state changed");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnPageChangeListener != null) {
				mOriginalOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}
	}

	@Override
	public void onPageSelected(int position) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				String logMsg = Integer.toString(position) + "," + getDescription(mViewPager);
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.PAGE_SELECTED, logMsg);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, mViewPager, "on page scroll state changed");
			}
		}
		if (!fReentryBlock) {
			if (mOriginalOnPageChangeListener != null) {
				mOriginalOnPageChangeListener.onPageSelected(position);
			}
		}
	}
}
