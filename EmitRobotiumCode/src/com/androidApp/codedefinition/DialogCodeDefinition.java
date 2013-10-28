package com.androidApp.codedefinition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.androidApp.codedefinition.CodeDefinition.Type;
import com.androidApp.emitter.EmitterException;
import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.util.Constants;
import com.androidApp.util.SuperTokenizer;

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
		
		public static DialogScanType fromString(String s) throws EmitterException {
			for (DialogScanType type: DialogScanType.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new EmitterException("code definition dialog scan type " + s + " unknown");
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
		
		public static DialogTagType fromString(String s) throws EmitterException {
			for (DialogTagType type: DialogTagType.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new EmitterException("code definition dialog tag type " + s + " unknown");
		}
	}
	protected String 			mDialogTag;			// tag used to ID dialog if dialog case, else null
	protected DialogScanType 	mDialogScanType;	// scan dialog by title or text from content
	protected DialogTagType		mDialogTagType;		// tag is string resource id or quoted text
	protected List<String>		mPrecedingTokens;	// tokens which preceded this condition. (like a button click causing a dialog)

	
	public DialogCodeDefinition() {
		mType = Type.DIALOG;
	}

	// code definition for interstitial dialog function definitions
	public DialogCodeDefinition(String 			activityName, 
							    String			codeType,
							    String			functionName,
							    String 			dialogTag, 
							    DialogScanType 	scanType, 
							    DialogTagType 	tagType, 
							    List<String> 	precedingTokens) {
		super(activityName, codeType, functionName, precedingTokens);
		mDialogTag = dialogTag;
		mType = Type.DIALOG;
		mDialogScanType = scanType;
		mDialogTagType = tagType;
	}
	
	/**
	 * parse a code definition from a string.
	 * ACTIVITY: activityClass
	 * DIALOG: activityName, dialog_tag, scanType <title|content>, tagType <id|text>
	 * @param s
	 * @return
	 * @throws EmitterException
	 */
	public static DialogCodeDefinition parse(String s) throws EmitterException {
		SuperTokenizer st = new SuperTokenizer(s, "\"", ":,", '\\');
		List<String> tokens = st.toList();
		String tokenType = tokens.get(0);
		String activityName = tokens.get(1);
		String codeType = tokens.get(2);
		String functionName = tokens.get(3);
		String dialogTag = tokens.get(4);
		DialogScanType dialogScanType = DialogScanType.fromString(tokens.get(5));
		DialogTagType dialogTagType = DialogTagType.fromString(tokens.get(6));
		return new DialogCodeDefinition(activityName, codeType, functionName, dialogTag, dialogScanType, dialogTagType, null);
	}
	
	/**
	 * copy.
	 * @param codeDef
	 */
	public void copy(DialogCodeDefinition codeDef) {
		super.copy(codeDef);
		mDialogTag = codeDef.mDialogTag;
		mDialogScanType = codeDef.mDialogScanType;
		mDialogTagType = codeDef.mDialogTagType;
	}

	/**
	 * is this code defintion the same as another code definition?
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DialogCodeDefinition) {
			DialogCodeDefinition b = (DialogCodeDefinition) o;
			if (mActivityName.equals(b.mActivityName) && mCodeType.equals(b.mCodeType)) {
				if (mDialogTag.equals(b.mDialogTag) && 
					(mDialogScanType == b.mDialogScanType) &&
					(mDialogTagType == b.mDialogTagType)) {
					return mFunctionName.equals(b.mFunctionName);
				}							
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return mActivityName.hashCode() + mDialogTag.hashCode() + mFunctionName.hashCode() +  mCodeType.hashCode();
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

	public String toString() {
		return mType + "," + mActivityName + "," + mCodeType + "," + mFunctionName + "," + mDialogTag + "," + mDialogScanType.mName + "," + mDialogTagType.mName;
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
