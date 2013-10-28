package com.androidApp.SupportIntercept;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;

import com.androidApp.Test.InterceptInterface;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.ReflectionUtils;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Listeners.OnLayoutInterceptListener;
import com.androidApp.SupportIntercept.InterceptSupport;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
import com.androidApp.SupportListeners.RecordActionBarTabListener;
import com.androidApp.SupportListeners.RecordOnPageChangeListener;
import com.androidApp.SupportListeners.RecordWebViewClient;
import com.androidApp.SupportListeners.RecordWindowCallback;

public class InterceptSupport implements InterceptInterface {
	protected static String TAG = "InterceptSupport";

	public void replaceListeners(Activity	activity,
								  String	activityName,
								  View 		v) {
	}
	
	
	public void replacePopupMenuListeners(String activityName, 
										  EventRecorder eventRecorder, 
										  View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
	}
	
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
	        
	public boolean isOverflowMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Class listWindowPopupDropdownClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW);
        if (contentView.getClass() == listWindowPopupDropdownClass) {
            Object clickListenerObject = ReflectionUtils.getFieldValue(contentView, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
            Class actionButttonSubmenuClass = Class.forName(Constants.Classes.ACTION_BUTTON_SUBMENU);
            if (clickListenerObject.getClass() == actionButttonSubmenuClass) {
                FieldUtils.listFieldsDebug(actionButttonSubmenuClass);
                Object actionMenuPresenter = ReflectionUtils.getFieldValue(clickListenerObject, actionButttonSubmenuClass, Constants.Fields.ENCLOSING_CLASS);
                Class actionMenuPresenterClass = Class.forName(Constants.Classes.ACTION_MENU_PRESENTER);
                Object popupMenuObject = ReflectionUtils.getFieldValue(actionMenuPresenter, actionMenuPresenterClass, Constants.Fields.POPUP_PRESENTER_CALLBACK);
                Class popupPresenterCallback = Class.forName(Constants.Classes.POPUP_PRESENTER_CALLBACK);
                return popupMenuObject.getClass() == popupPresenterCallback;
            }
        }
        return false;
	}
	
	public boolean isPopupMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class listWindowPopupDropdownClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW);        
        if (contentView.getClass() == listWindowPopupDropdownClass) {
            Object clickListenerObject = ReflectionUtils.getFieldValue(contentView, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
            Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
            if (clickListenerObject.getClass() == menuPopupHelperClass) {
                Object popupMenuObject = ReflectionUtils.getFieldValue(clickListenerObject, menuPopupHelperClass, Constants.Fields.PRESENTER_CALLBACK);                           
                return popupMenuObject instanceof PopupMenu;
            }                   
        }               
        return false;
	}

	public boolean hasListeners(View v) throws IllegalAccessException, NoSuchFieldException {
		if (v instanceof ViewPager) {
			ViewPager viewPager = (ViewPager) v;
			if (ListenerInterceptSupport.getPageChangeListener(viewPager) != null) {
				return true;
			}
		}
		return false;
	}
	
	public void interceptWindow(Window window, Activity activity, EventRecorder eventRecorder, ViewInterceptor viewInterceptor) {
		Window.Callback originalCallback = window.getCallback();
		if (!(originalCallback instanceof RecordWindowCallback)) {
			RecordWindowCallback recordCallback = new RecordWindowCallback(window, activity, window.getContext(), eventRecorder, viewInterceptor, originalCallback);
			window.setCallback(recordCallback);
		}
	}
	
	
	public static void replaceViewPagerPageChangeListener(String activityName, EventRecorder eventRecorder, ViewPager viewPager) throws IllegalAccessException, NoSuchFieldException {
		ViewPager.OnPageChangeListener originalPageChangeListener = ListenerInterceptSupport.getPageChangeListener(viewPager);
		if (!(originalPageChangeListener instanceof RecordOnPageChangeListener)) {
			RecordOnPageChangeListener recordOnPageChangeListener = new RecordOnPageChangeListener(activityName, eventRecorder, originalPageChangeListener, viewPager);
			viewPager.setOnPageChangeListener(recordOnPageChangeListener);
		}
	}
	
    /**
     * replace the webView client with our recorder
     * @param v a webview, actually
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public void replaceWebViewListeners(EventRecorder eventRecorder, View v) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        WebView webView = (WebView) v;
        WebViewClient originalWebViewClient = ListenerInterceptSupport.getWebViewClient(webView);
        if (!(originalWebViewClient instanceof RecordWebViewClient)) {
            RecordWebViewClient recordWebViewClient = new RecordWebViewClient(eventRecorder, originalWebViewClient);
            webView.setWebViewClient(recordWebViewClient);
        }
    }


	@Override
	public void interceptActionBar(Activity activity, ViewInterceptor viewInterceptor, EventRecorder eventRecorder) {
		if (activity instanceof ActionBarActivity) {
			ActionBarActivity actionBarActivity = (ActionBarActivity) activity;
	        ActionBar actionBar = actionBarActivity.getSupportActionBar();
	        if (actionBar != null) {
	        	
	        	// re-intercept on re-layout
	        	/*
	            try {
	                View actionBarView = InterceptActionBar.getActionBarView(actionBar);
	                ViewTreeObserver viewTreeObserverActionBar = actionBarView.getViewTreeObserver();
	                viewTreeObserverActionBar.addOnGlobalLayoutListener(new OnLayoutInterceptListener(activity, viewInterceptor, eventRecorder));
	                View customView = actionBar.getCustomView();
	                if (customView != null) {
	                    ViewTreeObserver viewTreeObserverActionBarCustomView = customView.getViewTreeObserver();
	                    viewTreeObserverActionBarCustomView.addOnGlobalLayoutListener(new OnLayoutInterceptListener(activity, viewInterceptor, eventRecorder));               
	                }
	            } catch (Exception ex) {
	                    Log.d(TAG, "failed to intercept action bar");
	            }
	            */
	            intercept(activity, viewInterceptor, activity.toString(), actionBar, eventRecorder);
	        }		
		}
	}
	
	/**
	 * the action bar is not in the activity contentView, so it has to be handled separately
	 * @param activity activity to intercept
	 * @param actionBar actionBar
	 */
	public void intercept(Activity 			activity, 
						  ViewInterceptor 	viewInterceptor,
						  String 			activityName, 
						  ActionBar 		actionBar, 
						  EventRecorder 	eventRecorder) {
        if (actionBar != null) {
        	try {
	        	View contentView = null;
	        	try {
	        		Class actionBarImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL);
	        		contentView = (View) ReflectionUtils.getFieldValue(actionBar, actionBarImplClass, Constants.Fields.CONTAINER_VIEW);
	        	} catch (Exception ex) {
	        		eventRecorder.writeException(activityName, ex, "while intercepting the action bar for " + activity.getClass().getName());
	        	}
	        	if (contentView != null) {
	        		viewInterceptor.intercept(activity,  activityName, contentView, false);
	            }
	       		InterceptActionBar.interceptActionBarTabListeners(activityName, eventRecorder, actionBar);
		       	if (actionBar.getCustomView() != null) {
		       		viewInterceptor.intercept(activity, activityName, actionBar.getCustomView(), false);
		        }
		       	viewInterceptor.intercept(activity, activityName, InterceptActionBar.getActionBarView(actionBar), false);
        	} catch (Exception ex) {
        		eventRecorder.writeException(activityName, ex, "while intercepting action bar");
        	}
	  	}		
	}

}
