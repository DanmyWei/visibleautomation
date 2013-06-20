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
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordPopupMenuOnMenuItemClickListener extends RecordListener implements PopupMenu.OnMenuItemClickListener, IOriginalListener  {
	protected PopupMenu.OnMenuItemClickListener	mOriginalMenuItemClickListener;
	protected View mMenuView;
	
	public RecordPopupMenuOnMenuItemClickListener(EventRecorder eventRecorder, View v) {
		super(eventRecorder);
		mMenuView = v;
		try {
			mOriginalMenuItemClickListener = ListenerIntercept.getPopupMenuOnMenuItemClickListener(v);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "create popup on item click listener");
		}
	}
	
	public RecordPopupMenuOnMenuItemClickListener(EventRecorder eventRecorder, PopupMenu.OnMenuItemClickListener originalListener) {
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
				int position = RecordPopupMenuOnMenuItemClickListener.getMenuItemIndex(item);
				mEventRecorder.writeRecord(Constants.EventTags.POPUP_MENU_ITEM_CLICK, position + "," + ViewReference.getClassIndexReference(mMenuView) + "," + RecordListener.getDescription(item));
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
