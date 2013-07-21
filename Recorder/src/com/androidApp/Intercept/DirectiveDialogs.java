package com.androidApp.Intercept;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Intercept.MagicOverlay.ClickMode;
import com.androidApp.Test.ActivityInterceptor;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;

/**
 * dialogs which are displayed from the magic overlay to enter information for view directives,
 * interstitial activities, login activties, etc
 * @author matt2
 *
 */
public class DirectiveDialogs {
	protected static final String	TAG = "DirectiveDialogs";
	protected static final int		EDIT_TEXT_ID = 0x1001;	// id of edit text in dialog
	protected static final int		LABEL_ID = 0x1002;		// id of edit text in dialog
	protected MagicOverlay 			mMagicOverlay;
	
	public DirectiveDialogs(MagicOverlay magicOverlay) {
		mMagicOverlay = magicOverlay;
	}
	
	public EventRecorder getEventRecorder() {
		return mMagicOverlay.getEventRecorder();
	}
	
	public ActivityInterceptor.ActivityState getActivityState() {
		return mMagicOverlay.getActivityState();
	}
	
	public View getCurrentView() {
		return mMagicOverlay.getCurrentView();
	}
	
	public void resetCurrentView() {
		mMagicOverlay.resetCurrentView();
	}
	
	public MagicOverlay.ClickMode getClickMode() {
		return mMagicOverlay.getClickMode();
	}
	
	public void setClickMode(MagicOverlay.ClickMode clickMode) {
		mMagicOverlay.setClickMode(clickMode);
	}
	
	public class OnBaseDialogSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				String logMsg =  DirectiveDialogs.this.getActivityState().getActivity().getClass().getName();
				DirectiveDialogs.this.getEventRecorder().writeRecord(Constants.EventTags.INTERSTITIAL_ACTIVITY, logMsg);
			} else if (which == 1) {
				// go into view selection mode
				DirectiveDialogs.this.setClickMode(ClickMode.VIEW_SELECT);
				DirectiveDialogs.this.resetCurrentView();
			}
		}	
	}
	
	public class OnTextViewSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog alertDialog = (AlertDialog) dialog;
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			Activity activity = DirectiveDialogs.this.getActivityState().getActivity();
			try {
				UserDefinedViewReference ref = DirectiveDialogs.this.getUserDefinedViewReference(currentView, activity);
				if (which == 0) {
					DirectiveDialogs.this.getEventRecorder().writeRecord(Constants.EventTags.IGNORE_EVENTS, currentView);
					ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreDirective);
				} else if (which == 1) {
					DirectiveDialogs.this.getEventRecorder().writeRecord(Constants.EventTags.MOTION_EVENTS, currentView);
					ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(motionDirective);
					try {
						ViewInterceptor.replaceTouchListener(recorder, currentView);
					} catch (Exception ex) {
						recorder.writeException(ex, "replace touch listener in directive dialog");
					}
				} else if (which == 2) {
					DirectiveDialogs.this.getEventRecorder().writeRecord(Constants.EventTags.COPY_TEXT, currentView);
					Dialog newDialog = createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.COPY_TEXT, new CopyDialogClickListener());
					newDialog.show();
				}
			} catch (IOException ioex) {
				DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
			}
		}
	}
	
	/**
	 * copy the text from the text view to the named variable
	 * @author matt2
	 *
	 */
	public class CopyDialogClickListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog alertDialog = (AlertDialog) dialog;
			EditText editText = (EditText) alertDialog.findViewById(EDIT_TEXT_ID);
			String variable = editText.getText().toString();
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			Activity activity = DirectiveDialogs.this.getActivityState().getActivity();
			recorder.writeRecord(Constants.EventTags.COPY_TEXT, currentView, variable);
			if (currentView instanceof TextView) {
				TextView textView = (TextView) currentView;
				recorder.setVariableValue(variable, textView.getText().toString());
				try {
					UserDefinedViewReference ref = DirectiveDialogs.this.getUserDefinedViewReference(currentView, activity);
					ViewDirective copyDirective = new ViewDirective(ref, ViewOperation.COPY_TEXT, When.ON_ACTIVITY_END, null);
					recorder.addViewDirective(copyDirective);
				} catch (IOException ioex) {
					DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
				}			
			} else {
				DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_NOT_TEXT_VIEW);
			}
		}
	}
	
	/**
	 * paste the text from the named variable 
	 * @author matt2
	 *
	 */
	public class PasteDialogClickListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog alertDialog = (AlertDialog) dialog;
			EditText editText = (EditText) alertDialog.findViewById(EDIT_TEXT_ID);
			String variable = editText.getText().toString();
			String value = DirectiveDialogs.this.getEventRecorder().getVariableValue(variable);
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			Activity activity = DirectiveDialogs.this.getActivityState().getActivity();
			if (value != null) {
				if (currentView instanceof EditText) {
					EditText viewEditText = (EditText) currentView;
					viewEditText.setText(value);
					try {
						UserDefinedViewReference ref = DirectiveDialogs.this.getUserDefinedViewReference(currentView, activity);
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

	/**
	 * context dialog for list views.
	 * @author matt2
	 *
	 */
	public class OnListSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog alertDialog = (AlertDialog) dialog;
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			Activity activity = DirectiveDialogs.this.getActivityState().getActivity();
			try {
				UserDefinedViewReference ref = DirectiveDialogs.this.getUserDefinedViewReference(currentView, activity);
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

	/**
	 * context menu for edit texts.
	 * @author matt2
	 *
	 */
	public class OnEditTextSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog alertDialog = (AlertDialog) dialog;
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			Activity activity = DirectiveDialogs.this.getActivityState().getActivity();
			try {
				UserDefinedViewReference ref = DirectiveDialogs.this.getUserDefinedViewReference(currentView, activity);
				if (which == 0) {
					recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, currentView);
					ViewDirective ignoreDirective = new ViewDirective(ref, ViewOperation.IGNORE_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(ignoreDirective);
				} else if (which == 1) {
					recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, currentView);
					ViewDirective motionDirective = new ViewDirective(ref, ViewOperation.MOTION_EVENTS, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(motionDirective);
					try {
						ViewInterceptor.replaceTouchListener(recorder, currentView);
					} catch (Exception ex) {
						recorder.writeException(ex, "replace touch listener in directive dialog");
					}
				} else if (which == 2) {
					recorder.writeRecord(Constants.EventTags.COPY_TEXT, currentView);
					Dialog newDialog = createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.COPY_TEXT, new CopyDialogClickListener());
					newDialog.show();
				} else if (which == 3) {
					recorder.writeRecord(Constants.EventTags.PASTE_TEXT, currentView);
					Dialog newDialog = createTextEntryDialog(currentView.getContext(), Constants.DisplayStrings.PASTE_TEXT, new PasteDialogClickListener());
					newDialog.show();
				} else if (which == 4) {
					ViewDirective keyDirective = new ViewDirective(ref, ViewOperation.ENTER_TEXT_BY_KEY, When.ON_ACTIVITY_START, null);
					recorder.addViewDirective(keyDirective);
				} 
			} catch (IOException ioex) {
				DirectiveDialogs.setErrorLabel(alertDialog, Constants.DisplayStrings.VIEW_REFERENCE_FAILED);
			}
		}
	}

	public class OnCompoundButtonSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			View currentView = DirectiveDialogs.this.getCurrentView();
			EventRecorder recorder = DirectiveDialogs.this.getEventRecorder();
			if (which == 0) {
				recorder.writeRecord(Constants.EventTags.IGNORE_EVENTS, currentView);
			} else if (which == 1) {
				recorder.writeRecord(Constants.EventTags.MOTION_EVENTS, currentView);
			} else if (which == 2) {
				recorder.writeRecord(Constants.EventTags.CHECK, currentView);
			} else if (which == 3) {
				recorder.writeRecord(Constants.EventTags.UNCHECK, currentView);
			}
		}
	}

	/**
	 * context-sensitive menu which brings up operations based on the view type
	 * @param e
	 */
	
	public void viewDialog(Context context, MotionEvent e) {
		if (DirectiveDialogs.this.getClickMode() == ClickMode.VIEW_SELECT) {
			View currentView = DirectiveDialogs.this.getCurrentView();
			if (currentView instanceof EditText) {
				String[] editTextItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS, 
														Constants.DisplayStrings.MOTION_EVENTS,
					    								Constants.DisplayStrings.COPY_TEXT,
					    								Constants.DisplayStrings.PASTE_TEXT,
					    								Constants.DisplayStrings.INSERT_BY_CHARACTER};
				Dialog dialog = createSelectionDialog(context, editTextItems, new OnEditTextSelectionListener());
				dialog.show();				
			} else if (currentView instanceof TextView) {
				String[] textViewItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS, 
													 	Constants.DisplayStrings.MOTION_EVENTS,
													    Constants.DisplayStrings.COPY_TEXT };
				Dialog dialog = createSelectionDialog(context, textViewItems, new OnTextViewSelectionListener());
				dialog.show();
			} else if (currentView instanceof AbsListView) {
				String[] listViewItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS,
														Constants.DisplayStrings.MOTION_EVENTS,
														Constants.DisplayStrings.SELECT_BY_TEXT };
				Dialog dialog = createSelectionDialog(context, listViewItems, new OnListSelectionListener());
				dialog.show();	
			} else if (currentView instanceof CompoundButton) {
				String[] compoundButtonItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS,
															  Constants.DisplayStrings.MOTION_EVENTS,
															  Constants.DisplayStrings.CHECK,
															  Constants.DisplayStrings.UNCHECK };
				Dialog dialog = createSelectionDialog(context, compoundButtonItems, new OnCompoundButtonSelectionListener());
				dialog.show();	
			} 
		} 	
	}
	
	/**
	 * create a text entry dialog, for stuff like variable names, and other stuff, like variable names
	 * @param title
	 * @param listener
	 * @return
	 */
	protected Dialog createTextEntryDialog(Context context, String title, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(layoutParams);
		
		// create the label instructing the user
		TextView label = new TextView(context);
		label.setText(title);
		label.setTextColor(0xffffffff);
		label.setId(LABEL_ID);
		layout.addView(label, layoutParams);
		
		// create the edit text for the variable name (TODO: AutoCompleteTextView)
		EditText editText  = new EditText(context);
		editText.setEms(32);
		editText.setId(EDIT_TEXT_ID);
		editText.setSingleLine();
		layout.addView(editText, layoutParams);
		builder.setView(layout);
		
		// set the title and OK/Cancel buttons
		builder.setTitle(Constants.DisplayStrings.VISIBLE_AUTOMATION);
		builder.setPositiveButton(Constants.DisplayStrings.OK, listener);
		builder.setNegativeButton(Constants.DisplayStrings.CANCEL, listener);
		return builder.create();
	}
	
	// set the error label in the alert dialog
	protected static void setErrorLabel(AlertDialog alertDialog, String error) {
		TextView label = (TextView) alertDialog.findViewById(LABEL_ID);
		label.setTextColor(0xffff0000);
		label.setText(error);
	}	
	
	
	/**
	* create a selection dialog from an array of strings
	* @return AlertDialog builder
	*/
	
	protected Dialog createSelectionDialog(Context context, String[] items, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setItems(items,  listener);
		builder.setTitle(Constants.DisplayStrings.VISIBLE_AUTOMATION);
		return builder.create();
	}
	
	/**
	 * common utility to get a view reference for the current view.
	 * @param currentView
	 * @param activity
	 * @return
	 * @throws IOException
	 */
	public UserDefinedViewReference getUserDefinedViewReference(View currentView, Activity activity) throws IOException {
		EventRecorder recorder = getEventRecorder();
		ViewReference viewReference = recorder.getViewReference();
		Instrumentation instrumentation = recorder.getInstrumentation();
		return new UserDefinedViewReference(recorder.getInstrumentation(), recorder.getViewReference(), currentView, activity);
	}
}
