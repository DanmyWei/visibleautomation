package com.androidApp.Utility;

import android.view.View;

/**
 * simple class for dealing with non-standard Windows and contents (decor views)
 * Just because it's a "window" doesn't mean it's a Window
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 * @author matt2
 *
 */
public class WindowAndView {
	public Object	mWindow;			// apparently, this can be just about anything
	public View		mView;
	
	public WindowAndView(Object window, View view) {
		mWindow = window;
		mView = view;
	}
}
