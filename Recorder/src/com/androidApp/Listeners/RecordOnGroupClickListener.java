package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

/**
 *  record item clicks for expandable list views (group expansion), since android doesn't fire onItemClick() and
 *  stuff lie that. Bad llama
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnGroupClickListener extends RecordListener implements ExpandableListView.OnGroupClickListener, IOriginalListener  {
	protected ExpandableListView.OnGroupClickListener	mOriginalGroupClickListener;
	protected final String TAG = "RecordOnGroupClickListener";
	
	public RecordOnGroupClickListener(EventRecorder eventRecorder, ExpandableListView expandableListView) {
		super(eventRecorder);
		try {
			mOriginalGroupClickListener = ListenerIntercept.getOnGroupClickListener(expandableListView);
			expandableListView.setOnGroupClickListener(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, expandableListView, "create on group click listener");
		}		
	}
	
	public RecordOnGroupClickListener(EventRecorder eventRecorder, ExpandableListView.OnGroupClickListener originalListener) {
		super(eventRecorder);
		mOriginalGroupClickListener = originalListener;
	}
		
	public Object getOriginalListener() {
		return mOriginalGroupClickListener;
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

	public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			int flatListIndex = parent.getFlatListPosition(parent.getPackedPositionForGroup(groupPosition));
			Log.i(TAG, "group position = " + groupPosition + " list index = " + flatListIndex);

			try {
				mEventRecorder.writeRecord(Constants.EventTags.GROUP_CLICK, groupPosition + "," + flatListIndex + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, view, "item click");
			}	
		}
		if (!fReentryBlock) {
			if (mOriginalGroupClickListener != null) {
				return mOriginalGroupClickListener.onGroupClick(parent, view, groupPosition, id);
			} 
		}
		setEventBlock(false);
		return false;
	}
}
