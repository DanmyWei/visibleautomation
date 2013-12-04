package com.androidApp.SupportIntercept;

import android.app.Activity;
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
import com.androidApp.SupportIntercept.InterceptSupport;
import com.androidApp.SupportIntercept.ListenerInterceptSupport;
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
	
	public void interceptActionBar(Activity activity, ViewInterceptor viewInterceptor, EventRecorder eventRecorder) {
	}
	        
	public boolean isOverflowMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		return false;
	}
	
	public boolean isPopupMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		return false;
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
