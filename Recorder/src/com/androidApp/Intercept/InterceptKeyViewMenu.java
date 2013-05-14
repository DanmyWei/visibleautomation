package com.androidApp.Intercept;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * this edit text is added to popup menus so we can intercept key events.  TODO:We may be able to
 * supersede this class with RecordWindowCallback
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class InterceptKeyViewMenu extends EditText {
	EventRecorder mRecorder;
	
	public InterceptKeyViewMenu(Context context) {
		super(context);
		init();
	}
	
	public InterceptKeyViewMenu(Context context, EventRecorder recorder) {
		super(context);
		mRecorder = recorder;
		init();
	}
	
	public void init() {
		setBackgroundColor(0x0);
		setTextColor(0x0);
	}

	@Override 
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP){ 
			Toast.makeText(this.getContext(), "dispatch intercepted key event " + event.getKeyCode(), Toast.LENGTH_SHORT).show();
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				mRecorder.writeRecordTime(Constants.EventTags.MENU_BACK_KEY);
				break;
			case KeyEvent.KEYCODE_MENU:
				mRecorder.writeRecordTime(Constants.EventTags.MENU_MENU_KEY);
				break;
			case KeyEvent.KEYCODE_HOME:
				mRecorder.writeRecordTime(Constants.EventTags.MENU_MENU_KEY);
				break;
			} 
		}
		return false;
	}
	
	@Override 
	public boolean onKeyPreIme (int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			Toast.makeText(this.getContext(), "prekey intercepted key event " + event.getKeyCode(), Toast.LENGTH_SHORT).show();
		}
		return false;
	}
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(0, 0);
	}
}
