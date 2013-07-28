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

/**
 * context dialog for list views.
 * @author matt2
 *
 */
public class OnListSelectionListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public OnListSelectionListener(DirectiveDialogs directiveDialogs) {
		mDirectiveDialogs = directiveDialogs;
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		AlertDialog alertDialog = (AlertDialog) dialog;
		View currentView = mDirectiveDialogs.getCurrentView();
		EventRecorder recorder = mDirectiveDialogs.getEventRecorder();
		Activity activity = mDirectiveDialogs.getActivityState().getActivity();
		try {
			UserDefinedViewReference ref = mDirectiveDialogs.getUserDefinedViewReference(currentView, activity);
			if (which == 0) {
				recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, currentView);
				ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
				recorder.addViewDirective(ignoreDirective);
			} else if (which == 1) {
				recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, currentView);
				ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
				try {
					ViewInterceptor.replaceTouchListener(recorder, currentView);
				} catch (Exception ex) {
					recorder.writeException(ex, "replace touch listener in directive dialog");
				}
				recorder.addViewDirective(motionDirective);
			} else if (which == 2) {
				recorder.writeRecord(Constants.EventTags.SELECT_BY_TEXT, currentView);
				ViewDirective selectDirective = new ViewDirective(ref, ViewOperation.SELECT_BY_TEXT, When.ON_ACTIVITY_START, null);
				recorder.addViewDirective(selectDirective);
			}
		} catch (IOException ioex) {
			DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
		}
	}
}
