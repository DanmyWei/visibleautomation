package com.androidApp.SupportIntercept;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.SupportListeners.RecordActionBarTabListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.view.View;

/**
 * functions to intercept action bar events.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class InterceptActionBar {
	// get the action bar view.
	public static View getActionBarView(ActionBar actionBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		Class actionBarImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL);
		return (View) ReflectionUtils.getFieldValue(actionBar, actionBarImplClass, Constants.Fields.ACTION_VIEW);
	}
	
	// get the action bar tab listener for action bar tabs
	public static ActionBar.TabListener getTabListener(ActionBar actionBar, int index) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		ActionBar.Tab tab = actionBar.getTabAt(index);
		Class tabImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL_TAB_IMPL);
		return (ActionBar.TabListener) ReflectionUtils.getFieldValue(tab, tabImplClass, Constants.Fields.CALLBACK);		
	}
	
	/**
	 * intercept the tab changer in the action bar tab
	 * @param recorder event recorder
	 * @param actionBar actionBar
	 * @throws IllegalAccessException exceptions thrown by ReflectionUtils
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public static void interceptActionBarTabListeners(String activityName, EventRecorder recorder, ActionBar actionBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		for (int iTab = 0; iTab < actionBar.getTabCount(); iTab++) {
			ActionBar.Tab tab = actionBar.getTabAt(iTab);
			ActionBar.TabListener originalTabListener = getTabListener(actionBar, iTab);
			if (!(originalTabListener instanceof RecordActionBarTabListener)) {
				RecordActionBarTabListener recordActionBarTabListener = new RecordActionBarTabListener(activityName, recorder, actionBar, iTab);
			}
		}
	}
}
