package com.androidApp.Listeners;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

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
public class RecordOnMenuItemClickListener extends RecordListener implements MenuItem.OnMenuItemClickListener, IOriginalListener  {
	protected MenuItem.OnMenuItemClickListener	mOriginalMenuItemClickListener;
	protected MenuItem mMenuItem;
	
	public RecordOnMenuItemClickListener(EventRecorder eventRecorder, MenuItem menuItem) {
		super(eventRecorder);
		mMenuItem = menuItem;
		try {
			mOriginalMenuItemClickListener = ListenerIntercept.getOnMenuItemClickListener(menuItem);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "create onMenuItemClickListener");
		}
	}
	
	public RecordOnMenuItemClickListener(EventRecorder eventRecorder, MenuItem.OnMenuItemClickListener originalListener) {
		super(eventRecorder);
		mOriginalMenuItemClickListener = originalListener;
	}
	
	public Object getOriginalListener() {
		return mOriginalMenuItemClickListener;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		boolean fReentryBlock = getReentryBlock();
		if (!RecordListener.getEventBlock()) {
			setEventBlock(true);
			try {
				int position = RecordOnMenuItemClickListener.getMenuItemIndex(item);
				mEventRecorder.writeRecord(Constants.EventTags.MENU_ITEM_CLICK, item.getItemId() + "," + position + "," + RecordListener.getDescription(item));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, "menu item click " + ex.getMessage());
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
