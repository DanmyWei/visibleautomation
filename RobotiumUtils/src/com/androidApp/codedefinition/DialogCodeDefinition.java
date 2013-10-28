package com.androidApp.codedefinition;


import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.androidApp.util.Constants;
import com.androidApp.util.TestException;

/**
 * code can either be output in the the main thread (indicated with mActivityName = Constants.MAIN)
 * in an conditional activity case, or in an conditional dialog case.  activities are compared
 * by name, but dialogs are compared by the activity, dialog tag, scan type (content or title) and
 * tag type (id or text).  These are the tags for the output code hashtable, so we know where to 
 * write the output code (main, activity condition, dialog condition), and how to tag it (in
 * the dialog condition case)
 * views are tested in the normal way that views are retrieved, either by id, or class+index
 * Add: the previousLine is tested against for conditions to insert the actual code while we are parsing.
 * @author matt2
 *
 */

public class DialogCodeDefinition extends CodeDefinition {
	
	// for a dialog, we can scan either by title or type.
	public enum DialogScanType {
		TITLE("title", "CodeDefinition.DialogScanType.TITLE"),
		CONTENT("content", "CodeDefinition.DialogScanType.CONTENT");
		
		public String mName;
		public String mCodeName;
		
		private DialogScanType(String t, String codeName) {
			mName = t;
			mCodeName = codeName;
		}
		
		public static DialogScanType fromString(String s) throws TestException {
			for (DialogScanType type: DialogScanType.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new TestException("code definition dialog scan type " + s + " unknown");
		}
	}
	
	// and for the tag, we can scan for view id, or contained text.
	public enum DialogTagType {
		ID("id", "CodeDefinition.DialogTagType.ID"),
		TEXT("text", "CodeDefinition.DialogTagType.TEXT");
		
		public String mName;
		public String mCodeName;
		
		private DialogTagType(String t, String codeName) {
			mName = t;
			mCodeName = codeName;
		}
		
		public static DialogTagType fromString(String s) throws TestException {
			for (DialogTagType type: DialogTagType.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new TestException("code definition dialog tag type " + s + " unknown");
		}
	}
	protected String 			mDialogTag;			// tag used to ID dialog if dialog case, else null
	protected DialogScanType 	mDialogScanType;	// scan dialog by title or text from content
	protected DialogTagType		mDialogTagType;		// tag is string resource id or quoted text
	
	public DialogCodeDefinition() {
		mType = Type.DIALOG;
	}

	// code definition for interstitial dialog function definitions
	public DialogCodeDefinition(String 			activityName, 
							    String			functionName,
							    String 			dialogTag, 
							    DialogScanType 	scanType, 
							    DialogTagType 	tagType) {
		super(activityName, functionName);
		mDialogTag = dialogTag;
		mType = Type.DIALOG;
		mDialogScanType = scanType;
		mDialogTagType = tagType;
	}
			
	public String getDialogTag() {
		return mDialogTag;
	}
	
	public DialogScanType getDialogScanType() {
		return mDialogScanType;
	}
	
	public DialogTagType getDialogTagType() {
		return mDialogTagType;
	}
	
	
	/**
	 * match a dialog to a code definition
	 * @param activity activity to match against
	 * @param dialog dialogt to scan
	 * @return true if the dialog matches, false otherwise
	 */
	public boolean matchDialog(Activity activity, Dialog dialog) {
		if (activity.getClass().getName().equals(mActivityName)) {
			String tag = mDialogTag;
			if (mDialogTagType == DialogTagType.ID) {
				tag = activity.getResources().getString(Integer.parseInt(tag));
			}
			if (tag == null) {
				return false;
			}
			if (mDialogScanType == DialogScanType.TITLE) {
				TextView tv = getDialogTitleView(dialog);
				if (tv != null) {
					return tv.getText().toString().equals(tag);
				}
			} else if (mDialogScanType == DialogScanType.CONTENT) {
				View contentView = getDialogContentView(dialog);
				return findString(contentView, tag);
			}
		}
		return false;
	}
	
	// recursively search for a string in a text view.
	public static boolean findString(View v, String s) {
		if (v instanceof TextView) {
			return ((TextView) v).getText().toString().equals(s);
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				if (findString(vChild, s)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * geting a dialog title is tricky, because they didn't provide an accessor function for it, AND
	 * it's an internal view (which fortunately derives from a TextView)
	 * @param dialog
	 * @return TextView or null if not found
	 */
	public static TextView getDialogTitleView(Dialog dialog) {
		try {
			Class<? extends View> dialogTitleClass = (Class<? extends View>) Class.forName(Constants.Classes.DIALOG_TITLE);
			Window window = dialog.getWindow();
			View decorView = window.getDecorView();
			TextView dialogTitle = (TextView) findChild(decorView, 0, dialogTitleClass);
			if (dialogTitle != null) {
				return dialogTitle;
			} else {
				dialogTitle = (TextView) findChild(decorView, 0, TextView.class);
				return dialogTitle;
			}
		} catch (ClassNotFoundException cnfex) {
			Log.e(TAG, "failed to find dialog title");
			return null;
		}
	}

	/**
	 * get the content view of a dialog so we can intercept it with a MagicFrame
	 * @param dialog
	 * @return
	 */
	public static View getDialogContentView(Dialog dialog) {
		Window window = dialog.getWindow();
		View decorView = window.getDecorView();
		View contentView = ((ViewGroup) decorView).getChildAt(0);
		return contentView;
	}
	
	public static View findChild(View v, int index, Class<? extends View> cls) {
		Integer indexWrapper = Integer.valueOf(index);
		return findChild(v, indexWrapper, cls);
	}
	
	/**
	 * find a child with a matching index and type recursively, starting from v.
	 * @param v view to find child from (may be a ViewGroup or view)
	 * @param index (modified) Integer index of child to search for among children of v
	 * @param cls class to match
	 * @return view or null if no matching child found
	 */
	public static View findChild(View v, Integer index, Class<? extends View> cls) {
		if (cls.isAssignableFrom(v.getClass())) {
			if (index == 0) {
				return v;
			} else {
				index--;
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int nChild = vg.getChildCount();
			for (int i = 0; i < nChild; i++) {
				View vChild = vg.getChildAt(i);
				View vFound = findChild(vChild, index, cls);
				if (vFound != null) {
					return vFound;
				}
			}
		}
		return null;
	}

	// generate the code suitable for a handler java class
	public String toCode() {
		if (mDialogTagType == DialogTagType.TEXT) {
			return "DialogCodeDefinition(" + mActivityName + ".class.getName()" + ",\"" + mDialogTag + "\"," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
		} else {
			return "DialogCodeDefinition(" + mActivityName + ".class.getName()" + "," + mDialogTag + "," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
		}
	}
	
	// generate the code suitable for a handler java class
	public String toBinaryCode() {
		if (mDialogTagType == DialogTagType.TEXT) {
			return "DialogCodeDefinition(\"" + mActivityName + "\"" +  ",\"" + mDialogTag + "\"," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
		} else {
			return "DialogCodeDefinition(\"" + mActivityName + "\"" + "," + mDialogTag + "," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
		}
	}
	
}
