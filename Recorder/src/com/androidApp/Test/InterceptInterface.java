package com.androidApp.Test;

import com.androidApp.EventRecorder.EventRecorder;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

public interface InterceptInterface {
	void replaceListeners(Activity	activity,
						  String	activityName,
						  View 		v);
	void replacePopupMenuListeners(String activityName, EventRecorder eventRecorder, View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException;
	void interceptActionBar(Activity activity, ViewInterceptor viewInterceptor, EventRecorder eventRecorder);
	boolean hasListeners(View v) throws IllegalAccessException, NoSuchFieldException;
	void interceptWindow(Window window, Activity activity, EventRecorder eventRecorder, ViewInterceptor viewInterceptor);
	boolean isPopupMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException;
	boolean isOverflowMenu(View contentView) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException;
	void replaceWebViewListeners(EventRecorder eventRecorder, View v) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException;
}
