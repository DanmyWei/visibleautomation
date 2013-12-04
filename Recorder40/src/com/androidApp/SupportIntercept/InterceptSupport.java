package com.androidApp.SupportIntercept;

import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.androidApp.Test.InterceptInterface;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.ReflectionUtils;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.SupportIntercept.InterceptSupport;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
import com.androidApp.SupportListeners.RecordPopupMenuOnMenuItemClickListener;
import com.androidApp.SupportListeners.RecordWebViewClient;
import com.androidApp.SupportListeners.RecordWindowCallback;
import com.androidApp.Listeners.OnLayoutInterceptListener;

public class InterceptSupport implements InterceptInterface {
	protected static String TAG = "InterceptSupport";

	public void replaceListeners(Activity	activity,
								  String	activityName,
								  View 		v) {
	}
	public void replacePopupMenuListeners(String activityName, 
										  EventRecorder eventRecorder, 
										  View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        PopupMenu.OnMenuItemClickListener originalMenuItemClickListener = ListenerInterceptSupport.getPopupMenuOnMenuItemClickListener(v);
        if (!(originalMenuItemClickListener instanceof RecordPopupMenuOnMenuItemClickListener)) {
        	ListenerInterceptSupport.setPopupMenuOnMenuItemClickListener(v, new RecordPopupMenuOnMenuItemClickListener(activityName, eventRecorder, v));
        }
    }
	
	public void interceptActionBar(Activity activity, ViewInterceptor viewInterceptor, EventRecorder eventRecorder) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
 /*       	
        	// re-intercept on re-layout
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

	public boolean hasListeners(View v) throws IllegalAccessException, NoSuchFieldException {
		return false;
	}
	
	public void interceptWindow(Window window, Activity activity, EventRecorder eventRecorder, ViewInterceptor viewInterceptor) {
		Window.Callback originalCallback = window.getCallback();
		if (!(originalCallback instanceof RecordWindowCallback)) {
			RecordWindowCallback recordCallback = new RecordWindowCallback(window, activity, window.getContext(), eventRecorder, viewInterceptor, originalCallback);
			window.setCallback(recordCallback);
		}
	}
	
	/**
	 * is this a popup menu: as defined by a PopupViewContainer containing a ListWindowDropdownListView, with a MenuPopupHelper as the item click listener class.
	 * @param contentView content of the PopupWindow
	 * @return true if we think it's a popup menu.
	 * @throws NoSuchFieldException thrown by the reflection utilities
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */
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
	
	
	/**
	 * is this an overflow menu descending from a popup menu?
	 * @param contentView content from the popupWindow
	 * @return true if we think it's an overflow menu.
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */
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

}
