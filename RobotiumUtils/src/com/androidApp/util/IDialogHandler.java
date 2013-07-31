package com.androidApp.util;

import android.app.Activity;
import android.app.Dialog;

// call this function when the specified dialog appears
public interface IDialogHandler {
	void onEnter(Dialog dialog);
}
