package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

/**
 *  record item clicks for expandable list views, which expand and close groups
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnChildClickListener extends RecordListener implements ExpandableListView.OnChildClickListener, IOriginalListener  {
	protected ExpandableListView.OnChildClickListener	mOriginalChildClickListener;
	protected final String TAG = "RecordOnChildClickListener";
	
	public RecordOnChildClickListener(String activityName, EventRecorder eventRecorder, ExpandableListView expandableListView) {
		super(activityName, eventRecorder);
		try {
			mOriginalChildClickListener = ListenerIntercept.getOnChildClickListener(expandableListView);
			expandableListView.setOnChildClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, expandableListView, "create on Child click listener");
		}		
	}
	
	public RecordOnChildClickListener(String activityName, EventRecorder eventRecorder, ExpandableListView.OnChildClickListener originalListener) {
		super(activityName, eventRecorder);
		mOriginalChildClickListener = originalListener;
	}
		
	public Object getOriginalListener() {
		return mOriginalChildClickListener;
	}

	/**
	 * record the the onChildClick event
	 * output:
	 * item_click:<time>,position,<reference>,<description>
	 *  @param parent parent adapter
	 *  @param view selected view
	 *  @param position index in adapter
	 *  @param id adapter item id
	 */

	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			setEventBlock(true);
			mEventRecorder.setTouchedDown(true);
			int flatListIndex = parent.getFlatListPosition(parent.getPackedPositionForChild(groupPosition, childPosition));
			Log.i(TAG, "group position = " + groupPosition + " childPosition = " + childPosition + " list index = " + flatListIndex);
			try {
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.CHILD_CLICK, groupPosition + "," + childPosition + "," + flatListIndex + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "item click");
			}	
		}
		if (!fReentryBlock) {
			if (mOriginalChildClickListener != null) {
				return mOriginalChildClickListener.onChildClick(parent, view, groupPosition, childPosition, id);
			} 
		}
		setEventBlock(false);
		return false;
	}
}
