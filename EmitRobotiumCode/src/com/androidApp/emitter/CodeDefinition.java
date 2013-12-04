package com.androidApp.emitter;

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
 * Add: the previousLine is tested against for conditions to insert the actual code while we are parsing.
 * @author matt2
 *
 */

public class CodeDefinition {
	public enum Type {
		ACTIVITY("activity", "CodeDefinition.Type.ACTIVITY"),
		DIALOG("dialog", "CodeDefinition.Type.DIALOG");
	
		public String mName;
		public String mCodeName;
		
		private Type(String t, String codeName) {
			mName = t;
			mCodeName = codeName;
		}

		public Type fromString(String s) throws EmitterException {
			for (Type type: Type.values()) {
				if (type.mName.equals(s)) {
					return type;
				}
			}
			throw new EmitterException("code definition type " + s + " unknown");
		}
	}
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
	protected String 			mActivityName;		// name of the activity or activity dialog was created in
	protected String			mCodeType;			// function_call or function_def
	protected String			mFunctionName;		// name of the function
	protected String 			mDialogTag;			// tag used to ID dialog if dialog case, else null
	protected Type 				mType;				// activity or dialog
	protected DialogScanType 	mDialogScanType;	// scan dialog by title or text from content
	protected DialogTagType		mDialogTagType;		// tag is string resource id or quoted text
	protected List<String>		mPrecedingTokens;	// tokens which preceded this condition. (like a button click causing a dialog)
	
	
	// code definition for main activity code
	public CodeDefinition(String activityName) {
		mCodeType = Constants.FUNCTION_DEF;
		mActivityName = activityName;
		mFunctionName = Constants.MAIN;
		mType = Type.ACTIVITY;
		mPrecedingTokens = null;
	}
	
	// code definition for interstitial activity function definitions
	public CodeDefinition(String activityName, String codeType, String functionName, List<String> precedingTokens) {
		mActivityName = activityName;
		mCodeType = codeType;
		mFunctionName = functionName;
		mType = Type.ACTIVITY;
		mPrecedingTokens = precedingTokens;
	}
	
	// code definition for interstitial dialog function definitions
	public CodeDefinition(String 			activityName, 
						  String			codeType,
						  String			functionName,
						  String 			dialogTag, 
						  DialogScanType 	scanType, 
						  DialogTagType 	tagType, 
						  List<String> 		precedingTokens) {
		mActivityName = activityName;
		mCodeType = codeType;
		mFunctionName = functionName;
		mDialogTag = dialogTag;
		mType = Type.DIALOG;
		mDialogScanType = scanType;
		mDialogTagType = tagType;
		mPrecedingTokens = precedingTokens;
	}
	
	public CodeDefinition(CodeDefinition codeDef) {
		mActivityName = codeDef.mActivityName;
		mCodeType = codeDef.mCodeType;
		mFunctionName = codeDef.mFunctionName;
		mDialogTag = codeDef.mDialogTag;
		mType = codeDef.mType;
		mDialogScanType = codeDef.mDialogScanType;
		mDialogTagType = codeDef.mDialogTagType;
		mPrecedingTokens = codeDef.mPrecedingTokens;	
	}
	
	/**
	 * parse a code definition from a string.
	 * ACTIVITY: activityClass
	 * DIALOG: activityName, dialog_tag, scanType <title|content>, tagType <id|text>
	 * @param s
	 * @return
	 * @throws EmitterException
	 */
	public static CodeDefinition parse(String s) throws EmitterException {
		SuperTokenizer st = new SuperTokenizer(s, "\"", ":,", '\\');
		List<String> tokens = st.toList();
		String tokenType = tokens.get(0);
		if (Type.ACTIVITY.mName.equalsIgnoreCase(tokenType)) {
			if (tokens.size() == 2) {
				return new CodeDefinition(tokens.get(1));
			} else {
				String activityName = tokens.get(1);
				String codeType = tokens.get(2);
				String functionName = tokens.get(3);
				return new CodeDefinition(activityName, codeType, functionName, null);
			}
		} else if (Type.DIALOG.mName.equalsIgnoreCase(tokenType)) {
			String activityName = tokens.get(1);
			String codeType = tokens.get(2);
			String functionName = tokens.get(3);
			String dialogTag = tokens.get(4);
			DialogScanType dialogScanType = DialogScanType.fromString(tokens.get(5));
			DialogTagType dialogTagType = DialogTagType.fromString(tokens.get(6));
			return new CodeDefinition(activityName, codeType, functionName, dialogTag, dialogScanType, dialogTagType, null);
		} else {
			throw new EmitterException("unknown code definition type " + tokenType);
		}
	}
	/**
	 * read the code definitions from a stream
	 * @param is inputstream (probably from assets)
	 * @return list of code definitions
	 * @throws IOException
	 * @throws EmitterException
	 */
	public static List<CodeDefinition> parse(InputStream is) throws IOException, EmitterException {
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
	
	/**
	 * is this code defintion the same as another code definition?
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof CodeDefinition) {
			CodeDefinition b = (CodeDefinition) o;
			if (mType == Type.ACTIVITY) {
				if (mActivityName.equals(b.mActivityName) && (mCodeType.equals(b.mCodeType))) {
					return mFunctionName.equals(b.mFunctionName);
				}
			} else {
				if (mActivityName.equals(b.mActivityName) && (mCodeType.equals(b.mCodeType))) {
					if (mDialogTag.equals(b.mDialogTag) && 
						(mDialogScanType == b.mDialogScanType) &&
						(mDialogTagType == b.mDialogTagType)) {
						return mFunctionName.equals(b.mFunctionName);
					}							
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (mType == Type.ACTIVITY) {
			return mActivityName.hashCode() + mFunctionName.hashCode();
		} else {
			return mActivityName.hashCode() + mDialogTag.hashCode() + mFunctionName.hashCode();
		}
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
	
	public String getCodeType() {
		return mCodeType;
	}
	
	public String getFunctionName() {
		return mFunctionName;
	}
	
	public DialogScanType getDialogScanType() {
		return mDialogScanType;
	}
	
	public DialogTagType getDialogTagType() {
		return mDialogTagType;
	}
	
	public List<String> getPrecedingTokens() {
		return mPrecedingTokens;
	}
	
	public void setPrecedingTokens(List<String> precedingTokens) {
		mPrecedingTokens = precedingTokens;
	}

	public String toString() {
		if (mType == Type.ACTIVITY) {
			return mType + "," + mActivityName + "," + mCodeType + "," + mFunctionName;
		} else {
			return mType + "," + mActivityName + "," + mCodeType + "," + mFunctionName + "," + mDialogTag + "," + mDialogScanType.mName + "," + mDialogTagType.mName;
		}
	}
	
	// generate the code suitable for a handler java class
	public String toCode() {
		if (mType == Type.ACTIVITY) {
			return "CodeDefinition(" + mActivityName + ".class"+ ")";
		} else {
			if (mDialogTagType == DialogTagType.TEXT) {
				return "CodeDefinition(" + mActivityName + ".class.getName()" + ",\"" + mDialogTag + "\"," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
						} else {
				return "CodeDefinition(" + mActivityName + ".class.getName()" + "," + mDialogTag + "," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
			}
		}
	}
	
	// generate the code suitable for a handler java class
	public String toBinaryCode() {
		if (mType == Type.ACTIVITY) {
			return "CodeDefinition(" + mActivityName + ".class"+ ")";
		} else {
			if (mDialogTagType == DialogTagType.TEXT) {
				return "CodeDefinition(\"" + mActivityName + "\"" +  ",\"" + mDialogTag + "\"," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
			} else {
				return "CodeDefinition(\"" + mActivityName + "\"" + "," + mDialogTag + "," + mDialogScanType.mCodeName + "," + mDialogTagType.mCodeName + ")";
			}
		}
	}
	

	/**
	 * find the function definition associated with this function call.
	 * @param codeDefTarget codeDefTarget function call
	 * @param outputCode hashtable of output code for main and functions
	 */
	public static CodeDefinition findFunctionDefinition(CodeDefinition codeDefTarget, Hashtable<CodeDefinition, List<LineAndTokens>> outputCode) {
		for (Entry<CodeDefinition, List<LineAndTokens>> entry : outputCode.entrySet()) {
			CodeDefinition codeDefCand = entry.getKey();
			if (codeDefCand.getCodeType().equals(Constants.FUNCTION_DEF)) {
				if (codeDefCand.getFunctionName().equals(codeDefTarget.getFunctionName())) {
					return codeDefCand;
				}
			}
		}
		return null;
	}

}
