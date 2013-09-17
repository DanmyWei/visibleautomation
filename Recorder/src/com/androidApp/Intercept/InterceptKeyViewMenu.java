package com.androidApp.Intercept;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * this edit text is added to popup menus so we can intercept key events.  
 * TODO:We may be able to supersede this class with RecordWindowCallback
 * TODO: this definitely should not subclass EditText
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class InterceptKeyViewMenu extends EditText {
	protected EventRecorder mRecorder;
	protected ViewInterceptor mViewInterceptor;
	protected Activity mActivity;
	
	// required constructor: NEVER CALL THIS because it doesn't assign mActivity
	public InterceptKeyViewMenu(Context context) {
		super(context);
		init();
	}
	
	public InterceptKeyViewMenu(Activity activity, Context context, EventRecorder recorder, ViewInterceptor viewInterceptor) {
		super(context);
		mActivity = activity;
		mRecorder = recorder;
		mViewInterceptor = viewInterceptor;
		init();
	}
	
	public void init() {
		setBackgroundColor(0x0);
		setTextColor(0x0);
	}

	@Override 
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_BACK);
				mRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.MENU_BACK_KEY);
				break;
			case KeyEvent.KEYCODE_MENU:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_MENU);
				mRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.MENU_MENU_KEY);
				break;
			case KeyEvent.KEYCODE_HOME:
				mViewInterceptor.setLastKeyAction(KeyEvent.KEYCODE_HOME);
				mRecorder.writeRecordTime(mActivity.toString(), Constants.EventTags.MENU_MENU_KEY);
				break;
			} 
		}
		return false;
	}
	
	@Override 
	public boolean onKeyPreIme (int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
		}
		return false;
	}
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(0, 0);
	}
}
