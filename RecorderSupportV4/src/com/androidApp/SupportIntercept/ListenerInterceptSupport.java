package com.androidApp.SupportIntercept;


import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;


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
	 * get the view page changed listener for ViewPager (note Android 18)
	 * @param viewPager
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static ViewPager.OnPageChangeListener getPageChangeListener(ViewPager viewPager) throws NoSuchFieldException, IllegalAccessException {
		return (ViewPager.OnPageChangeListener) ReflectionUtils.getFieldValue(viewPager, ViewPager.class, Constants.Fields.ON_PAGE_CHANGE_LISTENER);
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
