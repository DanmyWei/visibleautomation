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
import com.androidApp.Listeners.RecordOnAttachStateChangeListener;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

/**
 * context menu for edit texts.
 * @author matt2
 *
 */
public class OnEditTextSelectionListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public OnEditTextSelectionListener(DirectiveDialogs directiveDialogs) {
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
					recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreDirective);
				}
				break;
				case 1:
				{
					recorder.writeRecord(Constants.EventTags.IGNORE_CLICK_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreClickDirective = new ViewDirective(ref, ViewOperation.IGNORE_CLICK_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreClickDirective);
				}
				break;
				case 2:
				{
					recorder.writeRecord(Constants.EventTags.IGNORE_TEXT_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreTextDirective = new ViewDirective(ref, ViewOperation.IGNORE_TEXT_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreTextDirective);
				}
				break;
				case 3:
				{
					recorder.writeRecord(Constants.EventTags.IGNORE_FOCUS_EVENTS, activity.toString(), currentView);
					ViewDirective ignoreFocusDirective = new ViewDirective(ref, ViewOperation.IGNORE_FOCUS_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreFocusDirective);
				}
				break;
				case 4:
				{
					recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, activity.toString(), currentView);
					ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(motionDirective);
					try {
						ViewInterceptor.replaceTouchListener(activity.toString(), recorder, currentView);
					} catch (Exception ex) {
						recorder.writeException(activity.toString(), ex, "replace touch listener in directive dialog");
					}
				}
				break;
				case 5:
				{
					recorder.writeRecord(Constants.EventTags.COPY_TEXT, activity.toString(), currentView);
					Dialog newDialog = mDirectiveDialogs.createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.COPY_TEXT, new CopyDialogClickListener(mDirectiveDialogs));
					newDialog.show();
				}
				break;
				case 6:
				{
					recorder.writeRecord(Constants.EventTags.PASTE_TEXT, activity.toString(), currentView);
					Dialog newDialog = mDirectiveDialogs.createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.PASTE_TEXT, new PasteDialogClickListener(mDirectiveDialogs));
					newDialog.show();
				} 
				break;
				case 7:
				{
					ViewDirective keyDirective = new ViewDirective(ref, ViewOperation.ENTER_TEXT_BY_KEY, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(keyDirective);
				} 
				break;
				case 8:
				{
					recorder.writeRecord(Constants.EventTags.INTERSTITIAL_VIEW, activity.toString(), currentView);
					try {
						String ref2 = recorder.getViewReference().getReference(currentView);
						currentView.addOnAttachStateChangeListener(new RecordOnAttachStateChangeListener(activity.toString(), recorder, ref2));
					} catch (Exception ex) {
						recorder.writeException(activity.toString(), ex, "while setting attach state change listener");				
					}
				}
				break;
			}
		} catch (IOException ioex) {
			DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
		}
	}
}
