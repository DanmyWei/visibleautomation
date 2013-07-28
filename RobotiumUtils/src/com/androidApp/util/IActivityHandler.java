package com.androidApp.util;

import android.app.Activity;

// call this function when the activity is transitioned to by the ActivityMonitor
public interface IActivityHandler {
	void onEnter(Activity activity);
}
