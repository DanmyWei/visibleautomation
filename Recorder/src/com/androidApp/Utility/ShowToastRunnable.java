package com.androidApp.Utility;

import android.app.Instrumentation;
import android.content.Context;
import android.widget.Toast;

/**
 * toasts need to be shown on the main UI thread.  Fortunately, we can get one
 * @author matt2
 *
 */
public class ShowToastRunnable implements Runnable {
	public Context mContext;
	public String mMsg;

	public static void showToast(Instrumentation instrumentation, Context context, String msg) {
		instrumentation.runOnMainSync(new ShowToastRunnable(context, msg));
	}
	
	public ShowToastRunnable(Context context, String msg) {
		mContext = context;
		mMsg = msg;
	}
	
	public void run() {
		Toast.makeText(mContext, mMsg, Toast.LENGTH_SHORT).show();
	}
}
