package com.androidApp.Intercept.directivedialogs;

import java.io.IOException;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewReference;
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
		switch (which) {
		case 0:
			recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, activity.toString(), currentView);
			break;
		case 1:
			recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, activity.toString(), currentView);
			break;
		case 2:
			recorder.writeRecord(Constants.EventTags.CHECK, activity.toString(), currentView);
			break;
		case 3:
			recorder.writeRecord(Constants.EventTags.UNCHECK, activity.toString(), currentView);
			break;
		case 4:
			recorder.writeRecordWithActivity(Constants.EventTags.INTERSTITIAL_VIEW, activity.toString(), currentView);
			break;
		}
	}
}
