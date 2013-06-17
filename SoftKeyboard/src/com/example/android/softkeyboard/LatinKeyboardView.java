/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.softkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.PopupWindow;

public class LatinKeyboardView extends KeyboardView {
	static final String TAG = "LatinKeyboardView";
    static final int KEYCODE_OPTIONS = -100;

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "replacing listeners");
        replacePopupDismissListener();
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG, "replacing listeners");
        replacePopupDismissListener();
    }

    public class TestDismissListener implements PopupWindow.OnDismissListener {
    	PopupWindow.OnDismissListener mOriginalListener;
    	
    	public TestDismissListener(PopupWindow.OnDismissListener originalListener) {
    		mOriginalListener = originalListener;
    	}
    	public void	onDismiss() {
    		Log.i(TAG, "test dismiss listener");
    		if (mOriginalListener != null) {
    			mOriginalListener.onDismiss();
    		}
    	}
    }
    
    public void replacePopupDismissListener() {
    	try {
    		PopupWindow popupKeyboard = (PopupWindow) ReflectionUtils.getFieldValue(this, KeyboardView.class, "mPopupKeyboard");
    		PopupWindow.OnDismissListener originalListener = (PopupWindow.OnDismissListener) ReflectionUtils.getFieldValue(popupKeyboard, PopupWindow.class, "mOnDismissListener");
    		popupKeyboard.setOnDismissListener(new TestDismissListener(originalListener));
       		Log.i(TAG, "listener replace success");
       	} catch (Exception ex) {
    		Log.e(TAG, ex.getMessage());
    		ex.printStackTrace();
       		Log.i(TAG, "listener replace fail");
    	}
    	
    }
    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }

    void setSubtypeOnSpaceKey(final InputMethodSubtype subtype) {
        final LatinKeyboard keyboard = (LatinKeyboard)getKeyboard();
        keyboard.setSpaceIcon(getResources().getDrawable(subtype.getIconResId()));
        invalidateAllKeys();
    }
    
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	Log.i(TAG, "onSizeChanged " + w + ", " + h + ", " + oldw + ", " + oldh);
    }
    
    @Override
    public boolean handleBack() {
    	boolean f = super.handleBack();
    	Log.i(TAG, "handle back");
    	return f;
    }
        
    @Override
    public void bringToFront() {
    	super.bringToFront();
    	Log.i(TAG, "bringToFront");
    }
}
