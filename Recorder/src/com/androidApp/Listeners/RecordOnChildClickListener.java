package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
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
	
	public RecordOnChildClickListener(EventRecorder eventRecorder, ExpandableListView expandableListView) {
		super(eventRecorder);
		try {
			mOriginalChildClickListener = ListenerIntercept.getOnChildClickListener(expandableListView);
			expandableListView.setOnChildClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, expandableListView, "create on Child click listener");
		}		
	}
	
	public RecordOnChildClickListener(EventRecorder eventRecorder, ExpandableListView.OnChildClickListener originalListener) {
		super(eventRecorder);
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
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				mEventRecorder.writeRecord(Constants.EventTags.CHILD_CLICK, groupPosition + "," + childPosition + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, view, "item click");
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
