package com.androidApp.Intercept.directivedialogs;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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


public class OnTextViewSelectionListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public OnTextViewSelectionListener(DirectiveDialogs directiveDialogs) {
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
			switch (which) {
				case 0:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_EVENTS, currentView);
					ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreDirective);
				} 
				break;
				case 1:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_CLICK_EVENTS, currentView);
					ViewDirective ignoreClickDirective = new ViewDirective(ref, ViewOperation.IGNORE_CLICK_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreClickDirective);
				} 
				break;
				case 2:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_TEXT_EVENTS, currentView);
					ViewDirective ignoreTextDirective = new ViewDirective(ref, ViewOperation.IGNORE_TEXT_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreTextDirective);
				} 
				break;
				case 3:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.MOTION_EVENTS, currentView);
					ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(motionDirective);
					try {
						ViewInterceptor.replaceTouchListener(recorder, currentView);
					} catch (Exception ex) {
						recorder.writeException(ex, "replace touch listener in directive dialog");
					}
				}
				break;
				case 4:
				{
					mDirectiveDialogs.getEventRecorder().writeRecord(Constants.EventTags.COPY_TEXT, currentView);
					Dialog newDialog = DirectiveDialogs.createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.COPY_TEXT, 
															 				 new CopyDialogClickListener(mDirectiveDialogs));
					newDialog.show();
				}
			}
		} catch (IOException ioex) {
			DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
		}
	}
}
