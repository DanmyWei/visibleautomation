package com.androidApp.Listeners;
import java.util.List;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

/**
 *  record item clicks for listviews
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecordExpandedMenuViewOnItemClickListener extends RecordListener implements AdapterView.OnItemClickListener, IOriginalListener  {
	protected AdapterView.OnItemClickListener	mOriginalItemClickListener;
	
	public RecordExpandedMenuViewOnItemClickListener(String activityName, EventRecorder eventRecorder, AdapterView<?> adapterView) {
		super(activityName, eventRecorder);
		mOriginalItemClickListener = adapterView.getOnItemClickListener();
		adapterView.setOnItemClickListener(this);
	}
	
	public RecordExpandedMenuViewOnItemClickListener(String activityName, EventRecorder eventRecorder, AdapterView.OnItemClickListener originalListener) {
		super(activityName, eventRecorder);
		mOriginalItemClickListener = originalListener;
	}
		
	public Object getOriginalListener() {
		return mOriginalItemClickListener;
	}

	/**
	 * TODO: This needs to be made to work, but it's really, really an edge case.
	 * record the the onItemClick event
	 * output:
	 * item_click:<time>,position,<reference>,<description>
	 *  @param parent parent adapter
	 *  @param view selected view
	 *  @param position index in adapter
	 *  @param id adapter item id
	 */

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				Class alertControllerAlertParamsClass = Class.forName(Constants.Classes.ALERT_CONTROLLER_ALERT_PARAMS); 
				if ((mOriginalItemClickListener != null) && alertControllerAlertParamsClass.isAssignableFrom(mOriginalItemClickListener.getClass())) {
					try {
						Object clickListener = ReflectionUtils.getFieldValue(mOriginalItemClickListener, alertControllerAlertParamsClass, Constants.Fields.ONCLICK_LISTENER);
						Class menuDialogHelperClass = Class.forName(Constants.Classes.MENU_DIALOG_HELPER);
						if (menuDialogHelperClass.isAssignableFrom(clickListener.getClass())) {
							Object menuBuilderObject = ReflectionUtils.getFieldValue(clickListener, menuDialogHelperClass, Constants.Fields.MENU);
							if (menuBuilderObject != null) {
								Class submenuBuildClass = Class.forName(Constants.Classes.SUBMENU_BUILDER);
								List<MenuItem> items = (List<MenuItem>) ReflectionUtils.getFieldValue(menuBuilderObject, submenuBuildClass, Constants.Fields.ITEMS);
								MenuItem menuItem = items.get(position);
								mEventRecorder.writeRecord(mActivityName, Constants.EventTags.MENU_ITEM_CLICK, menuItem.getItemId() + "," + position + "," + RecordListener.getDescription(menuItem));
							}	
						}
					} catch (Exception ex) {
						// version stuff.
					}
				} else {
					mEventRecorder.writeRecord(mActivityName, Constants.EventTags.ITEM_CLICK, position + "," + ViewReference.getClassIndexReference(parent) + "," + getDescription(view));
				};
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
