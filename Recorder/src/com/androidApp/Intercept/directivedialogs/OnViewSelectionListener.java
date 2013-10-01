package com.androidApp.Intercept.directivedialogs;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Intercept.DirectiveDialogs;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;


public class OnViewSelectionListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public OnViewSelectionListener(DirectiveDialogs directiveDialogs) {
		mDirectiveDialogs = directiveDialogs;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		AlertDialog alertDialog = (AlertDialog) dialog;
		View currentView = mDirectiveDialogs.getCurrentView();
		EventRecorder recorder = mDirectiveDialogs.getEventRecorder();
		Activity activity = mDirectiveDialogs.getActivity();
		try {
			UserDefinedViewReference ref = mDirectiveDialogs.getUserDefinedViewReference(currentView, activity);
			switch (which) {
				case 0:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreDirective);
				}
				break;
				case 1:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_CLICK_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreClickDirective = new ViewDirective(ref, ViewOperation.IGNORE_CLICK_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreClickDirective);
				}
				break;
				case 2:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_LONG_CLICK_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreClickDirective = new ViewDirective(ref, ViewOperation.IGNORE_LONG_CLICK_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreClickDirective);
				}
				break;
				case 3:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.MOTION_EVENTS, activity.toString(), currentView);
					ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(motionDirective);
					try {
						ViewInterceptor.replaceTouchListener(activity.toString(), recorder, currentView);
					} catch (Exception ex) {
						recorder.writeException(activity.toString(), ex, "replace touch listener in directive dialog");
					}
				} 
				break;
			}
		} catch (IOException ioex) {
			DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
		}
	}
}
