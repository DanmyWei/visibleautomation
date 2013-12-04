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
 * This class is used for the main body of code, so it's functional, not abstract
 * @author matt2
 *
 */

public class CodeDefinition {
	
	// type: activity, dialog, or view.
	public enum Type {
		ACTIVITY("activity", "CodeDefinition.Type.ACTIVITY"),
		DIALOG("dialog", "CodeDefinition.Type.DIALOG"),
		VIEW("view", "CodeDefinition.Type.VIEW");
	
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
	
	protected String 			mActivityName;		// name of the activity or activity dialog was created in
	protected String			mCodeType;			// function_call or function_def
	protected String			mFunctionName;		// name of the function
	protected Type 				mType;				// activity or dialog
	protected List<String>		mPrecedingTokens;	// tokens which preceded this condition. (like a button click causing a dialog)
	
	
	public CodeDefinition() {
	}
	
	// code definition for main activity code
	public CodeDefinition(String activityName) {
		mCodeType = Constants.FUNCTION_DEF;
		mActivityName = activityName;
		mFunctionName = Constants.MAIN;
		mPrecedingTokens = null;
	}
	
	// code definition for interstitial activity function definitions
	public CodeDefinition(String activityName, String codeType, String functionName, List<String> precedingTokens) {
		mActivityName = activityName;
		mCodeType = codeType;
		mFunctionName = functionName;
		mPrecedingTokens = precedingTokens;
	}
	
	/**
	 * clone()
	 * @param codeDef
	 */
	public void copy(CodeDefinition codeDef) {
		mActivityName = codeDef.mActivityName;
		mCodeType = codeDef.mCodeType;
		mFunctionName = codeDef.mFunctionName;
		mType = codeDef.mType;
		if (codeDef.mPrecedingTokens != null) {
			mPrecedingTokens = new ArrayList<String>(codeDef.mPrecedingTokens.size());
			for (String s : codeDef.mPrecedingTokens) {
				mPrecedingTokens.add(s);
			}
		}
		mPrecedingTokens = codeDef.mPrecedingTokens;	
	}
	
	/**
	 * make a copy copy copy depending on the type type type.
	 * @return
	 */
	public CodeDefinition makeCopy() {
		if (this instanceof ActivityCodeDefinition) {
			ActivityCodeDefinition activityCodeDef = new ActivityCodeDefinition();
			activityCodeDef.copy(this);
			return activityCodeDef;
		} else if (this instanceof DialogCodeDefinition) {
			DialogCodeDefinition dialogCodeDef = new DialogCodeDefinition();
			dialogCodeDef.copy(this);
			return dialogCodeDef;
		} else if (this instanceof ViewCodeDefinition) {
			ViewCodeDefinition viewCodeDef = new ViewCodeDefinition();
			viewCodeDef.copy(this);
			return viewCodeDef;
		} else {
			return null;
		}
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
			return ActivityCodeDefinition.parse(s);
		} else if (Type.DIALOG.mName.equalsIgnoreCase(tokenType)) {
			return DialogCodeDefinition.parse(s);
		} else if (Type.VIEW.mName.equalsIgnoreCase(tokenType)) {
			return ViewCodeDefinition.parse(s);
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
	
	public String getActivityName() {
		return mActivityName;
	}
	
	
	public Type getType() {
		return mType;
	}
	
	public String getCodeType() {
		return mCodeType;
	}
	
	public void setCodeType(String s) {
		mCodeType = s;
	}
	
	public String getFunctionName() {
		return mFunctionName;
	}
	
	
	public List<String> getPrecedingTokens() {
		return mPrecedingTokens;
	}
	
	public void setPrecedingTokens(List<String> precedingTokens) {
		mPrecedingTokens = precedingTokens;
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
	
	@Override
	public int hashCode() {
		return mActivityName.hashCode() + mFunctionName.hashCode() + mCodeType.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CodeDefinition) {
			CodeDefinition b = (CodeDefinition) o;
			return mActivityName.equals(b.mActivityName) && mCodeType.equals(b.mCodeType);
		}
		return false;
	}
}
