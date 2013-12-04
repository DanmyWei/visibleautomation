package com.androidApp.SupportIntercept;


import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;


import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.ReflectionUtils;

/**
 * Intercept functions which link to the special signatures specified in the Android Compatibility library
 * @author mattrey
 * TODO: this belongs in the Intercept directory
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ListenerInterceptSupport {
	protected static final String TAG = "ListenerIntercept";

	// TODO: unused: remove this
	public static AdapterView.OnItemClickListener getPopupMenuOnItemClickListener(View v) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class dropdownListViewClass = Class.forName(Constants.Classes.DROPDOWN_LISTVIEW);
		Object menuPopupHelperObject = ReflectionUtils.getFieldValue(v, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
		Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
		Object listPopupWindowObject = ReflectionUtils.getFieldValue(menuPopupHelperObject, menuPopupHelperClass, Constants.Fields.POPUP);
		Object listenerObject = ReflectionUtils.getFieldValue(listPopupWindowObject, ListPopupWindow.class, Constants.Fields.ITEM_CLICK_LISTENER);
		return (AdapterView.OnItemClickListener) listenerObject;
	}
	
	// TODO: unused: remove this
	public static void setPopupMenuOnItemClickListener(View v, AdapterView.OnItemClickListener listener) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class dropdownListViewClass = Class.forName(Constants.Classes.DROPDOWN_LISTVIEW);
		Object menuPopupHelperObject = ReflectionUtils.getFieldValue(v, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
		Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
		Object listPopupWindowObject = ReflectionUtils.getFieldValue(menuPopupHelperObject, menuPopupHelperClass, Constants.Fields.POPUP);
		ReflectionUtils.setFieldValue(listPopupWindowObject, ListPopupWindow.class, Constants.Fields.ITEM_CLICK_LISTENER, listener);
	}
	
	/**
	 * TODO: this needs to look up the popup window class and methods from the support library, not the android library
	 * is this a popup menu: as defined by a PopupViewContainer containing a ListWindowDropdownListView, with a MenuPopupHelper as the item click listener class.
	 * @param contentView content of the PopupWindow
	 * @return true if we think it's a popup menu.
	 * @throws NoSuchFieldException thrown by the reflection utilities
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */
	public static boolean isPopupMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
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
	 * TODO: this needs to look up the overflow menu class and methods from the support library, not the android library
	 * is this an overflow menu descending from a popup menu?
	 * @param contentView content from the popupWindow
	 * @return true if we think it's an overflow menu.
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */
	public static boolean isOverflowMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
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
	 * return the popup menu click listener as defined by a PopupViewContainer containing a ListWindowDropdownListView, with a MenuPopupHelper as the itemclick listener class.
	 * @param contentView content of the PopupWindow
	 * @return the popup menu click listener if we think it's a popup menu, otherwise null.
	 * @throws NoSuchFieldException thrown by the reflection utilities
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */

	public static PopupMenu.OnMenuItemClickListener getPopupMenuOnMenuItemClickListener(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class listWindowPopupDropdownClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW);			
		if (contentView.getClass() == listWindowPopupDropdownClass) {
			Object clickListenerObject = ReflectionUtils.getFieldValue(contentView, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
			Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
			if (clickListenerObject.getClass() == menuPopupHelperClass) {
				PopupMenu popupMenu = (PopupMenu) ReflectionUtils.getFieldValue(clickListenerObject, menuPopupHelperClass, Constants.Fields.PRESENTER_CALLBACK);
				return (PopupMenu.OnMenuItemClickListener) ReflectionUtils.getFieldValue(popupMenu, PopupMenu.class, Constants.Fields.MENU_ITEM_CLICK_LISTENER);
			}
		}
		return null;
	}
	
	/**
	 * set the popup menu click listener as defined by a PopupViewContainer containing a ListWindowDropdownListView, with a MenuPopupHelper as the itemclick listener class.
	 * @param contentView content of the PopupWindow
	 * @throws NoSuchFieldException thrown by the reflection utilities
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 */

	public static boolean setPopupMenuOnMenuItemClickListener(View contentView, PopupMenu.OnMenuItemClickListener listener) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		Class listWindowPopupDropdownClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW);			
		if (contentView.getClass() == listWindowPopupDropdownClass) {
			Object clickListenerObject = ReflectionUtils.getFieldValue(contentView, AdapterView.class, Constants.Fields.ONITEM_CLICK_LISTENER);
			Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
			if (clickListenerObject.getClass() == menuPopupHelperClass) {
				PopupMenu popupMenu = (PopupMenu) ReflectionUtils.getFieldValue(clickListenerObject, menuPopupHelperClass, Constants.Fields.PRESENTER_CALLBACK);
				popupMenu.setOnMenuItemClickListener(listener);
				return true;
			}
		}
		return false;
	}
	
	/** 
     * extract the WebViewClient from WebView.mCallbackProxy.mWebViewClient
     * @param webView
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */  
    public static WebViewClient getWebViewClient(WebView webView) throws NoSuchFieldException, SecurityException, IllegalAccessException, ClassNotFoundException {
        Class callbackProxyClass = Class.forName(Constants.Classes.WEBKIT_CALLBACK_PROXY);
        Object callbackProxy = ReflectionUtils.getFieldValue(webView, WebView.class, Constants.Fields.CALLBACK_PROXY);
        WebViewClient webViewClient = (WebViewClient) ReflectionUtils.getFieldValue(callbackProxy, callbackProxyClass, Constants.Fields.WEBVIEW_CLIENT);        return webViewClient;
    }           

}
