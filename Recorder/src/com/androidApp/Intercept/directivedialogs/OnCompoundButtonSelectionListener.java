package com.androidApp.Intercept.directivedialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Intercept.DirectiveDialogs;
import com.androidApp.Utility.Constants;


public class OnCompoundButtonSelectionListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public OnCompoundButtonSelectionListener(DirectiveDialogs directiveDialogs) {
		mDirectiveDialogs = directiveDialogs;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		View currentView = mDirectiveDialogs.getCurrentView();
		EventRecorder recorder = mDirectiveDialogs.getEventRecorder();
		Activity activity = mDirectiveDialogs.getActivity();
		if (which == 0) {
			recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, activity.toString(), currentView);
		} else if (which == 1) {
			recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, activity.toString(), currentView);
		} else if (which == 2) {
			recorder.writeRecord(Constants.EventTags.CHECK, activity.toString(), currentView);
		} else if (which == 3) {
			recorder.writeRecord(Constants.EventTags.UNCHECK, activity.toString(), currentView);
		}
	}
}
