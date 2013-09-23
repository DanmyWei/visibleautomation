package com.androidApp.Listeners;
import java.util.List;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 *  record item clicks for listviews
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordOnItemClickListener extends RecordListener implements AdapterView.OnItemClickListener, IOriginalListener  {
	protected AdapterView.OnItemClickListener	mOriginalItemClickListener;
	
	public RecordOnItemClickListener(String activityName, EventRecorder eventRecorder, AdapterView<?> adapterView, int viewIndex) {
		super(activityName, eventRecorder);
		mOriginalItemClickListener = adapterView.getOnItemClickListener();
		adapterView.setOnItemClickListener(this);
	}
	
	public RecordOnItemClickListener(String activityName, EventRecorder eventRecorder, AdapterView.OnItemClickListener originalListener) {
		super(activityName, eventRecorder);
		mOriginalItemClickListener = originalListener;
	}
		
	public Object getOriginalListener() {
		return mOriginalItemClickListener;
	}

	/**
	 * record the the onItemClick event
	 * output:
	 * item_click:<time>,position,<reference>,<description>
	 *  @param parent parent adapter
	 *  @param view selected view
	 *  @param position index in adapter
	 *  @param id adapter item id
	 */
	//				ViewDirective selectDirective = new ViewDirective(ref, ViewOperation.SELECT_BY_TEXT, When.ON_ACTIVITY_START, null);


	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				if (mEventRecorder.matchViewDirective(parent, ViewDirective.ViewOperation.SELECT_BY_TEXT, ViewDirective.When.ALWAYS)) {
					TextView tv = (TextView) TestUtils.findChild(view, 0, TextView.class);
					if (tv != null) {
						String text = StringUtils.escapeString(tv.getText().toString(), "\"", '\\').replace("\n", "\\n");
						mEventRecorder.writeRecord(mActivityName, Constants.EventTags.ITEM_CLICK_BY_TEXT, text + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));	
					}
				} else {
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.ITEM_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));	
				}
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "item click");
			}	
		}
		if (!fReentryBlock) {
			if (mOriginalItemClickListener != null) {
				mOriginalItemClickListener.onItemClick(parent, view, position, id);
			} 
		}
		setEventBlock(false);
	}
}
