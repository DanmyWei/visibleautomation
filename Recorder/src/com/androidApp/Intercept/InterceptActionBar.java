package com.androidApp.Intercept;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Listeners.RecordActionBarTabListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.app.ActionBar;

/**
 * functions to intercept action bar events.
 * @author Matthew
 *
 */
public class InterceptActionBar {

	public static ActionBar.TabListener getTabListener(ActionBar actionBar, int index) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		Object[] tabs = (Object[]) ReflectionUtils.getFieldValue(actionBar, ActionBar.class, Constants.Fields.TABS);
		Class tabImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL_TAB_IMPL);
		return (ActionBar.TabListener) ReflectionUtils.getFieldValue(tabs[index], tabImplClass, Constants.Fields.CALLBACK);		
	}
	
	/**
	 * intercept the tab changer in the action bar tab
	 * @param recorder event recorder
	 * @param actionBar actionBar
	 * @throws IllegalAccessException exceptions thrown by ReflectionUtils
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public static void interceptActionBarTabListeners(EventRecorder recorder, ActionBar actionBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		for (int iTab = 0; iTab < actionBar.getTabCount(); iTab++) {
			ActionBar.Tab tab = actionBar.getTabAt(iTab);
			ActionBar.TabListener originalTabListener = getTabListener(actionBar, iTab);
			if (!(originalTabListener instanceof RecordActionBarTabListener)) {
				RecordActionBarTabListener recordActionBarTabListener = new RecordActionBarTabListener(recorder, actionBar, iTab);
			}
		}
	}
}
