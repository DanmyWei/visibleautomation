package com.androidApp.Intercept;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

/** TODO: defunct: remove this class
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class InterceptFocusChangeListener implements OnFocusChangeListener {
	protected EditText		mInterceptKeyView;
	
	public InterceptFocusChangeListener(EditText interceptKeyView) {
		mInterceptKeyView = interceptKeyView;
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		mInterceptKeyView.requestFocus();
	}
}
