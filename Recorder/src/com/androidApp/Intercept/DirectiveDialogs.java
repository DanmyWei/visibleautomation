package com.androidApp.Intercept;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.UserDefinedViewReference;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.EventRecorder.ViewDirective.ViewOperation;
import com.androidApp.EventRecorder.ViewDirective.When;
import com.androidApp.Intercept.MagicOverlay.ClickMode;
import com.androidApp.Intercept.directivedialogs.CopyDialogClickListener;
import com.androidApp.Intercept.directivedialogs.OnCompoundButtonSelectionListener;
import com.androidApp.Intercept.directivedialogs.OnEditTextSelectionListener;
import com.androidApp.Intercept.directivedialogs.OnListSelectionListener;
import com.androidApp.Intercept.directivedialogs.OnTextViewSelectionListener;
import com.androidApp.Intercept.directivedialogs.OnViewSelectionListener;
import com.androidApp.Test.ActivityInterceptor;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;

/**
 * dialogs which are displayed from the magic overlay to enter information for view directives,
 * interstitial activities, login activties, etc
 * @author matt2
 *
 */
public class DirectiveDialogs {
	protected static final String	TAG = "DirectiveDialogs";
	public static final int			EDIT_TEXT_ID = 0x1001;			// id of edit text in dialog
	public static final int			LABEL_ID = 0x1002;				// id of edit text in dialog
	protected MagicOverlay 			mMagicOverlay;					// to get the current view selection.
	protected static Dialog			sCurrentDialog;					// so we don't intercept ourselves
	
	public DirectiveDialogs(MagicOverlay magicOverlay) {
		mMagicOverlay = magicOverlay;
	}
	
	public EventRecorder getEventRecorder() {
		return mMagicOverlay.getEventRecorder();
	}
	
	public Activity getActivity() {
		return mMagicOverlay.getActivity();
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
	
	/**
	 * is it one of ours?  Please don't record it.
	 * @return
	 */
	public static Dialog getCurrentDialog() {
		return sCurrentDialog;
	}
	
	/**
	 * this HAS TO BE CALLED WHENEVER WE CREATE A DIALOG, OTHERWISE WE WILL INTERCEPT OURSELVES!
	 * @param dialog
	 */
	protected static void setCurrentDialog(Dialog dialog) {
		sCurrentDialog = dialog;
	}
	
	/**
	 * activity base dialog selection
	 * @author matt2
	 *
	 */
	public class OnBaseDialogSelectionListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				String logMsg =  DirectiveDialogs.this.getActivity().getClass().getName();
				DirectiveDialogs.this.getEventRecorder().writeRecord(Constants.EventTags.INTERSTITIAL_ACTIVITY, logMsg);
			} else if (which == 1) {
				// go into view selection mode
				DirectiveDialogs.this.setClickMode(ClickMode.VIEW_SELECT);
				DirectiveDialogs.this.resetCurrentView();
			}
		}	
	}
	
	/**
	 * the dialog selection handler for dialog intercepts requires a backreference to the magicOverlay for 
	 * dialogs so it can get the source dialog handle, and the selection mode for content
	 * @author matt2
	 *
	 */
	public class OnBaseDialogDialogSelectionListener implements DialogInterface.OnClickListener {
		protected MagicOverlayDialog mMagicOverlayDialog;
		
		public OnBaseDialogDialogSelectionListener(MagicOverlayDialog magicOverlayDialog) {
			mMagicOverlayDialog = magicOverlayDialog;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {
				try {
					TextView dialogTitle = TestUtils.getDialogTitleView(mMagicOverlayDialog.getTargetDialog());
					if (dialogTitle != null) {
						String title = dialogTitle.getText().toString();
						if (title != null) {
							Resources res = mMagicOverlayDialog.getActivity().getResources();
							EventRecorder eventRecorder = DirectiveDialogs.this.getEventRecorder();
							List<Object> resourceIds =  eventRecorder.getViewReference().getStringList();		
							List<String> resIds = TestUtils.getIdForString(res, resourceIds, title);
							if (resIds.size() == 1) {
								String msg = mMagicOverlayDialog.getActivity().getClass().getName() + "," + resIds.get(0);
								eventRecorder.writeRecord(Constants.EventTags.INTERSTITIAL_DIALOG_TITLE_ID, msg);
							} else {
								String escapedTitle = StringUtils.escapeString(title, "\"", '\\').replace("\n", "\\n");
								String msg = mMagicOverlayDialog.getActivity().getClass().getName() + "," + "\"" + escapedTitle + "\"";
								eventRecorder.writeRecord(Constants.EventTags.INTERSTITIAL_DIALOG_TITLE_TEXT, msg);
							}
						} else {
							Toast.makeText(mMagicOverlayDialog.getActivity(), Constants.DisplayStrings.NO_DIALOG_TITLE, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(mMagicOverlayDialog.getActivity(), Constants.DisplayStrings.NO_DIALOG_TITLE, Toast.LENGTH_SHORT).show();
					}
				} catch (IllegalAccessException iaex) {
					Toast.makeText(mMagicOverlayDialog.getActivity(), Constants.DisplayStrings.RESOURCE_ERROR, Toast.LENGTH_SHORT).show();
					iaex.printStackTrace();
				}
			} else if (which == 1) {
				// go into view selection mode to find the content view that we want to match
				mMagicOverlayDialog.setViewSelectDialogContent(true);
				DirectiveDialogs.this.setClickMode(ClickMode.VIEW_SELECT);
				DirectiveDialogs.this.resetCurrentView();
			} else if (which == 2) {
				// go into view selection mode
				DirectiveDialogs.this.setClickMode(ClickMode.VIEW_SELECT);
				DirectiveDialogs.this.resetCurrentView();
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
														Constants.DisplayStrings.IGNORE_CLICK_EVENTS,
														Constants.DisplayStrings.IGNORE_TEXT_EVENTS,
														Constants.DisplayStrings.IGNORE_FOCUS_EVENTS,
														Constants.DisplayStrings.MOTION_EVENTS,
					    								Constants.DisplayStrings.COPY_TEXT,
					    								Constants.DisplayStrings.PASTE_TEXT,
					    								Constants.DisplayStrings.INSERT_BY_CHARACTER};
				Dialog dialog = createSelectionDialog(context, editTextItems, new OnEditTextSelectionListener(DirectiveDialogs.this));
				dialog.show();				
			} else if (currentView instanceof TextView) {
				String[] textViewItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS, 
														Constants.DisplayStrings.IGNORE_CLICK_EVENTS,
														Constants.DisplayStrings.IGNORE_TEXT_EVENTS,
													 	Constants.DisplayStrings.MOTION_EVENTS,
													    Constants.DisplayStrings.COPY_TEXT };
				Dialog dialog = createSelectionDialog(context, textViewItems, new OnTextViewSelectionListener(DirectiveDialogs.this));
				dialog.show();
			} else if (currentView instanceof AbsListView) {
				String[] listViewItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS,
														Constants.DisplayStrings.MOTION_EVENTS,
														Constants.DisplayStrings.SELECT_BY_TEXT };
				Dialog dialog = createSelectionDialog(context, listViewItems, new OnListSelectionListener(DirectiveDialogs.this));
				dialog.show();	
			} else if (currentView instanceof CompoundButton) {
				String[] compoundButtonItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS,
															  Constants.DisplayStrings.MOTION_EVENTS,
															  Constants.DisplayStrings.CHECK,
															  Constants.DisplayStrings.UNCHECK };
				Dialog dialog = createSelectionDialog(context, compoundButtonItems, new OnCompoundButtonSelectionListener(DirectiveDialogs.this));
				dialog.show();	
			} else { 
				String[] viewItems = new String[] { Constants.DisplayStrings.IGNORE_EVENTS,
													Constants.DisplayStrings.IGNORE_CLICK_EVENTS,
													Constants.DisplayStrings.IGNORE_LONG_CLICK_EVENTS,
						  							Constants.DisplayStrings.MOTION_EVENTS };
				Dialog dialog = createSelectionDialog(context, viewItems, new OnViewSelectionListener(DirectiveDialogs.this));
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
	public static Dialog createTextEntryDialog(Context context, String title, DialogInterface.OnClickListener listener) {
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
		Dialog dialog = builder.create();
		// prevent intercepting ourselves
		setCurrentDialog(dialog);
		return dialog;
	}
	
	// set the error label in the alert dialog
	public static void setErrorLabel(AlertDialog alertDialog, String error) {
		TextView label = (TextView) alertDialog.findViewById(LABEL_ID);
		label.setTextColor(0xffff0000);
		label.setText(error);
	}	
	
	
	/**
	* create a selection dialog from an array of strings
	* @return AlertDialog builder
	*/
	
	public static Dialog createSelectionDialog(Context context, String[] items, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setItems(items,  listener);
		builder.setTitle(Constants.DisplayStrings.VISIBLE_AUTOMATION);
		Dialog dialog = builder.create();
		// prevent intercepting ourselves
		setCurrentDialog(dialog);
		return dialog;
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
		return new UserDefinedViewReference(recorder.getInstrumentation(), recorder.getViewReference(), currentView, activity);
	}
}
