package com.androidApp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * code can either be output in the the main thread (indicated with mActivityName = Constants.MAIN)
 * in an interstitial activity handler, or in an interstitial dialog handler.  activities are compared
 * by name, but dialogs are compared by the activity, dialog tag, scan type (content or title) and
 * tag type (id or text).  These are the tags for the output code hashtable, so we know where to 
 * write the output code (main, interstitial handler, or dialog handler), and how to tag it (in
 * the dialog handler case)
 * @author matt2
 *
 */

public class CodeDefinition {
	protected static final String TAG = "CodeDefinition";
	
	public enum Type {
		ACTIVITY("activity"),
		DIALOG("dialog");
	
		public String mName;
		private Type(String t) {
			mName = t;
		}
		public Type fromString(String s) throws TestException {
			for (Type type: Type.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new TestException("code definition type " + s + " unknown");
		}
	}
	public enum DialogScanType {
		TITLE("title"),
		CONTENT("content");
		
		public String mName;
		private DialogScanType(String t) {
			mName = t;
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
	public enum DialogTagType {
		ID("id"),
		TEXT("text");
		
		public String mName;
		private DialogTagType(String t) {
			mName = t;
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
	protected String 			mActivityName;		// name of the activity or activity dialog was created in
	protected String 			mDialogTag;			// tag used to ID dialog if dialog case, else null
	protected Type 				mType;				// activity or dialog
	protected DialogScanType 	mDialogScanType;	// scan dialog by title or text from content
	protected DialogTagType		mDialogTagType;		// tag is string resource id or quoted text
	protected String			mHandlerName;		// handler name associated to this
	
	public CodeDefinition(String activityName) {
		mActivityName = activityName;
		mType = Type.ACTIVITY;
		mHandlerName = null;
	}
	public CodeDefinition(String activityName, String handlerName) {
		mActivityName = activityName;
		mType = Type.ACTIVITY;
		mHandlerName = handlerName;			
	}
	public CodeDefinition(String 			activityName, 
						  String 			dialogTag, 
						  DialogScanType 	scanType, 
						  DialogTagType 	tagType) {
		mActivityName = activityName;
		mDialogTag = dialogTag;
		mType = Type.DIALOG;
		mDialogScanType = scanType;
		mDialogTagType = tagType;
		mHandlerName = null;
	}
	
	public CodeDefinition(String 			activityName, 
						  String 			dialogTag, 
						  DialogScanType 	scanType, 
						  DialogTagType 	tagType,
						  String 			handlerName) {
		mActivityName = activityName;
		mDialogTag = dialogTag;
		mType = Type.DIALOG;
		mDialogScanType = scanType;
		mDialogTagType = tagType;
		mHandlerName = handlerName;
	}
	
	public static CodeDefinition parse(String s) throws TestException {
		SuperTokenizer st = new SuperTokenizer(s, "\"", ":,", '\\');
		List<String> tokens = st.toList();
		String tokenType = tokens.get(0);
		if (Type.ACTIVITY.mName.equals(tokenType)) {
			return new CodeDefinition(tokens.get(1), tokens.get(2));
		} else {
			return new CodeDefinition(tokens.get(1), tokens.get(3), DialogScanType.fromString(tokens.get(3)), 
						DialogTagType.fromString(tokens.get(4)), tokens.get(5));
		}
	}
	
	/**
	 * read the code definitions from a stream
	 * @param is inputstream (probably from assets)
	 * @return list of code definitions
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static List<CodeDefinition> parse(InputStream is) throws IOException, TestException {
		List<CodeDefinition> codeDefList = new ArrayList<CodeDefinition>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		while (line != null) {
			CodeDefinition codeDef = CodeDefinition.parse(line);
			codeDefList.add(codeDef);
			line = br.readLine();
		}
		return codeDefList;
	}
	
	/**
	 * is this code definition the same as one in the list
	 * @param codeDefList list of existing code definitions
	 * @return true if it's in the list
	 */
	public boolean inCodeDefList(List<CodeDefinition> codeDefList) {
		for (CodeDefinition codeDef : codeDefList) {
			if (codeDef.equals(this)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return the code definition which matches this one (but may have different handlers)
	 * @param codeDefList
	 * @return
	 */
	public CodeDefinition getMatch(List<CodeDefinition> codeDefList) {
		for (CodeDefinition codeDef : codeDefList) {
			if (codeDef.equals(this)) {
				return codeDef;
			}
		}
		return null;
	}
	
	/**
	 * count the number of code definitions which are assigned to the same activity as this one
	 * @param b
	 * @return
	 */
	public int activityMatchCount(List<CodeDefinition> codeDefList) {
		int count = 0;
		for (CodeDefinition codeDef : codeDefList) {
			if (codeDef.equals(this)) {
				count++;
			}
		}
		return count;			
	}
	
	public boolean equals(CodeDefinition b) {
		if (mType == Type.ACTIVITY) {
			return mActivityName.equals(b.mActivityName);
		} else {
			if (mActivityName.equals(b.mActivityName)) {
				return mDialogTag.equals(b.mDialogTag) && 
						(mDialogScanType == b.mDialogScanType) &&
						(mDialogTagType == b.mDialogTagType);
						
			}
		}
		return false;
	}
	
	public String getActivityName() {
		return mActivityName;
	}
	
	public String getDialogTag() {
		return mDialogTag;
	}
	
	public Type getType() {
		return mType;
	}
	
	public DialogScanType getDialogScanType() {
		return mDialogScanType;
	}
	
	public DialogTagType getDialogTagType() {
		return mDialogTagType;
	}
	
	public String getHandlerName() {
		return mHandlerName;
	}
	
	public void setHandlerName(String handlerName) {
		mHandlerName = handlerName;
	}
	
	public String toString() {
		if (mType == Type.ACTIVITY) {
			return mType + "," + mActivityName + "," + mHandlerName;
		} else {
			return mType + "," + mActivityName + "," + mDialogTag + "," + mDialogScanType + "," + mDialogTagType + "," + mHandlerName;
		}
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
	

}
