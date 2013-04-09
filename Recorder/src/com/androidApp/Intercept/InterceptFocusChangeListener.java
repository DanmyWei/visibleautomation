package com.androidApp.Intercept;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

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
