package com.androidApp.Intercept;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Intercept.DirectiveDialogs.OnBaseDialogSelectionListener;
import com.androidApp.Intercept.MagicOverlay.ClickMode;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ResourceUtils;
import com.androidApp.Utility.StringUtils;

/**
 * variant of magic overlays which takes a dialog parameter
 * @author matt2
 *
 */
public class MagicOverlayDialog extends MagicOverlay {
	protected boolean	 mfViewSelectDialogContent;
	protected Dialog 	mTargetDialog;
	
	
	public static void addMagicOverlay(Activity activity, Dialog targetDialog, MagicFrame magicFrame, EventRecorder recorder) throws IOException, ClassNotFoundException {
		View contentView = magicFrame.getChildAt(0);
		try {
			MagicOverlayDialog createOverlay = new MagicOverlayDialog(activity, targetDialog, magicFrame, recorder, contentView);
			initOverlayAttributes(magicFrame, contentView, createOverlay);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public MagicOverlayDialog(Activity 		activity, 
							  Dialog		targetDialog,
							  MagicFrame 	magicFrame,
							  EventRecorder eventRecorder, 
							  View 			contentView) throws IOException {
		super(activity, magicFrame, eventRecorder, contentView);
		mTargetDialog = targetDialog;
		mfViewSelectDialogContent = false;
	}
	
	public void setViewSelectDialogContent(boolean f) {
		mfViewSelectDialogContent = f;
	}
	public Dialog getTargetDialog() {
		return mTargetDialog;
	}
	
	public Dialog baseSelectionDialog(Context context) {
		String[] baseItems = new String[] { Constants.DisplayStrings.INTERSTITIAL_DIALOG_TITLE,
										    Constants.DisplayStrings.INTERSTITIAL_DIALOG_CONTENTS,
										    Constants.DisplayStrings.VIEW_SELECTION };
		Dialog dialog = mDirectiveDialogs.createSelectionDialog(MagicOverlayDialog.this.getContext(), baseItems, mDirectiveDialogs.new OnBaseDialogDialogSelectionListener(this));
		dialog.show();
		return dialog;
	}


	// bring up the view operation dialog on the current view.  If  it's in dialog content selection mode
	// then get the text from the content and use that
	@Override
	public void onLongPress(MotionEvent e) {
		if ((mMode == ClickMode.VIEW_SELECT) && (mCurrentView != null)) {
			if (mfViewSelectDialogContent) {
				try {
					if (getCurrentView() instanceof TextView) {
						String text = ((TextView) getCurrentView()).getText().toString();
						if (text != null) {
							Resources res = getActivity().getResources();
							List<Object> resourceIds =  mRecorder.getViewReference().getStringList();		
							List<String> resIds = ResourceUtils.getIdForString(res, resourceIds, text);
							if (resIds.size() == 1) {
								String msg = getActivity().getClass().getName() + "," + resIds.get(0);
								mRecorder.writeRecord(mActivity.getClass().getName(), Constants.EventTags.INTERSTITIAL_DIALOG_CONTENTS_ID, msg);
								Toast.makeText(getActivity(), "marking dialog with " + text, Toast.LENGTH_SHORT).show();

							} else {
								String escapedText = StringUtils.escapeString(text, "\"", '\\').replace("\n", "\\n");;
								String msg = getActivity().getClass().getName() + "," + "\"" + escapedText + "\"";
								Toast.makeText(getActivity(), "marking dialog with " + text, Toast.LENGTH_SHORT).show();
								mRecorder.writeRecord(mActivity.getClass().getName(), Constants.EventTags.INTERSTITIAL_DIALOG_CONTENTS_TEXT, msg);
							}
						} else {
							Toast.makeText(getActivity(), Constants.DisplayStrings.NO_DIALOG_TITLE, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getActivity(), Constants.DisplayStrings.NO_DIALOG_TITLE, Toast.LENGTH_SHORT).show();
					}
				} catch (IllegalAccessException iaex) {
					Toast.makeText(getActivity(), Constants.DisplayStrings.RESOURCE_ERROR, Toast.LENGTH_SHORT).show();
					iaex.printStackTrace();
				}
				mfViewSelectDialogContent = false;
			} else {
				mDirectiveDialogs.viewDialog(getContext(), e);
			}
		}
	}

}
