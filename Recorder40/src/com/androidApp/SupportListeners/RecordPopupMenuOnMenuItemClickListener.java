package com.androidApp.SupportListeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Listeners.IOriginalListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

/**
 * recorder for popup menus menu item click
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class RecordPopupMenuOnMenuItemClickListener extends RecordListener implements PopupMenu.OnMenuItemClickListener, IOriginalListener  {
	protected android.widget.PopupMenu.OnMenuItemClickListener	mOriginalMenuItemClickListener;
	protected View mMenuView;
	
	public RecordPopupMenuOnMenuItemClickListener(String activityName, EventRecorder eventRecorder, View v) {
		super(activityName, eventRecorder);
		mMenuView = v;
		try {
			mOriginalMenuItemClickListener = ListenerInterceptSupport.getPopupMenuOnMenuItemClickListener(v);
		} catch (Exception ex) {
			mEventRecorder.writeException(mActivityName, ex, "create popup on item click listener");
		}
	}
	
	public RecordPopupMenuOnMenuItemClickListener(Activity activity, EventRecorder eventRecorder, PopupMenu.OnMenuItemClickListener originalListener) {
		super(activity.getClass().getName(), eventRecorder);
		mOriginalMenuItemClickListener = originalListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalMenuItemClickListener;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock() && mEventRecorder.hasTouchedDown()) {
			mEventRecorder.setTouchedDown(true);
			setEventBlock(true);
			try {
				int position = RecordPopupMenuOnMenuItemClickListener.getMenuItemIndex(item);
				mEventRecorder.writeRecord(mActivityName, Constants.EventTags.POPUP_MENU_ITEM_CLICK, position + "," + ViewReference.getClassIndexReference(mMenuView) + "," + RecordListener.getDescription(item));
			} catch (Exception ex) {
				mEventRecorder.writeException(mActivityName, ex, "menu item click " + ex.getMessage());
			}	
		}
		if (!fReentryBlock) {
			if (mOriginalMenuItemClickListener != null) {
				return mOriginalMenuItemClickListener.onMenuItemClick(item);
			} 
		}
		setEventBlock(false);
		return false;
	}

	/**
	 * can through the menuItem's parent menu to find the index of this menu item
	 * @param menuItem target
	 * @return target's index
	 * @throws IllegalAccessException Exceptions thrown by ReflectionUtils
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	protected static int getMenuItemIndex(MenuItem menuItem) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		Class menuItemImplClass = Class.forName(Constants.Classes.MENU_ITEM_IMPL);
		Menu menu = (Menu) ReflectionUtils.getFieldValue(menuItem, menuItemImplClass, Constants.Fields.MENU);
		for (int iItem = 0; iItem < menu.size(); iItem++) {
			if (menu.getItem(iItem) == menuItem) {
				return iItem;
			}
		}
		return -1;
	}
}
