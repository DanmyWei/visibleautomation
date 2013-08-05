package com.androidApp.Intercept.directivedialogs;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Intercept.DirectiveDialogs;
import com.androidApp.Utility.Constants;


/**
 * paste the text from the named variable 
 * @author matt2
 *
 */
public class PasteDialogClickListener implements DialogInterface.OnClickListener {
	protected DirectiveDialogs mDirectiveDialogs;
	
	public PasteDialogClickListener(DirectiveDialogs directiveDialogs) {
		mDirectiveDialogs = directiveDialogs;
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		AlertDialog alertDialog = (AlertDialog) dialog;
		EditText editText = (EditText) alertDialog.findViewById(DirectiveDialogs.EDIT_TEXT_ID);
		String variable = editText.getText().toString();
		String value = mDirectiveDialogs.getEventRecorder().getVariableValue(variable);
		View currentView = mDirectiveDialogs.getCurrentView();
		EventRecorder recorder = mDirectiveDialogs.getEventRecorder();
		Activity activity = mDirectiveDialogs.getActivity();
		if (value != null) {
			if (currentView instanceof EditText) {
				EditText viewEditText = (EditText) currentView;
				viewEditText.setText(value);
				try {
					UserDefinedViewReference ref = mDirectiveDialogs.getUserDefinedViewReference(currentView, activity);
					ViewDirective copyDirective = new ViewDirective(ref, ViewOperation.COPY_TEXT, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(copyDirective);
				} catch (IOException ioex) {
					DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);						
				}
			} else {
				DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_NOT_TEXT_VIEW);
			}
		} else {	
			DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VARIABLE_NOT_FOUND);
		}
	}
}
