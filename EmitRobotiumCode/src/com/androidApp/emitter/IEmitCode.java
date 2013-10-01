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

import com.androidApp.util.SuperTokenizer;


/** 
 * interface for all functions which emit code.
 * @author Matthew
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
  */
public interface IEmitCode {
	
	/**
	 * subclass to preserve the tokens that the original line was parsed from for debugging
	 * @author Matthew
	 *
	 */
	public class LineAndTokens {
		public List<String>	mTokens;				// source tokens
		public String		mOutputLine;			// output line from populated template
		
		public LineAndTokens(List<String> tokens, String outputLine) {
			mTokens = tokens;
			mOutputLine = outputLine;
		}
	}	
	
	public class CodeOutput {
		public List<LineAndTokens>	mLineAndTokens;
		public int					mNextLineIndex;
		
		public CodeOutput(List<LineAndTokens> lineAndTokens, int nextLineIndex) {
			mLineAndTokens = lineAndTokens;
			mNextLineIndex = nextLineIndex;
		}
	}
	
	/**
	 * output type from the emitter: determines when it terminates (end of code, activity transtion, dialog)
	 * @author matt2
	 *
	 */
	public enum OutputType {
		MAIN,
		INTERSTITIAL_ACTIVITY,
		INTERSTITIAL_DIALOG
	}
		
	String getApplicationClassPath();
	String getApplicationClassName();
	String getApplicationPackage();
	void generateTestCode(String 											eventsFileName, 
			 		  	  Hashtable<CodeDefinition, List<LineAndTokens>>	outputCode, 
			 		  	  List<MotionEventList> 							motionEvents) throws FileNotFoundException, IOException, EmitterException;
	CodeOutput emit(List<List<String>>								tokenLines,
					int												currentReadIndex,
					Hashtable<CodeDefinition, List<LineAndTokens>>	outputCode,
					List<MotionEventList> 							motionEvents,
					OutputType										outputType) throws IOException, EmitterException;
	String getDescription(List<String> tokens);
	void writeFunctionHeader(BufferedWriter bw) throws IOException;
	void writeException(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void selectActionBarTab(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeMenuItemClick(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	String writeGetCurrentActivity(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeWaitForView(List<String> tokens, int startIndex, List<LineAndTokens> lines) throws IOException;
	void writeGoBackToMatchingActivity(String nextActivityVariable, List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeGoBack(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeWaitForActivity(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeWaitForMatchingActivity(String nextActivityVariable, List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeDismissDialog(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeCancelDialog(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeCreateDialog(List<String> tokens, List<LineAndTokens> lines) throws IOException;
	void writeClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeDismissAutoCompleteDropdown(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeDismissPopupWindow(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeDismissPopupWindowBackKey(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeRotation(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeShowIME(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeHideIME(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeScroll(ReferenceParser scrollListRef, int scrollFirstVisibleItem, List<String> tokens, List<LineAndTokens> outputLines) throws IOException;
	void writeEnterText(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeWaitForListClassIndex(List<String> tokens, int itemIndex, List<LineAndTokens> lines) throws IOException;
	void writeWaitForListIdItem(List<String> tokens, int itemIndex, List<LineAndTokens> lines) throws IOException;
	void writeItemClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writePopupMenuItemClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeSpinnerItemSelected(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeChildClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeGroupClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeHeader(String 				classPath, 
					 String 				testPackage, 
					 String 				testClassName, 
					 String 				className, 
					 int					targetSDK,
					 List<String>			supportLibraries,
					 BufferedWriter 		bw) throws IOException;
	void writeTrailer(BufferedWriter bw) throws IOException;
	void writeClassTrailer(BufferedWriter bw) throws IOException;
	void writeLines(BufferedWriter bw, List<LineAndTokens> lines) throws IOException;
	String writeViewIDCommand(String templateFile, ReferenceParser ref, String fullDescription) throws IOException;
	String writeViewClassIndexCommand(String templateFile, ReferenceParser ref, String fullDescription) throws IOException;
	void waitForPageToLoad(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	String readApplicationPackage(String eventsFileName) throws IOException ;
	boolean scanTargetPackage(List<List<String>> lines);
	LineAndTokens activityCondition(List<String> tokens, String activityName, String functionName) throws IOException;
	LineAndTokens dialogCondition(List<String> tokens, CodeDefinition codeDef, String functionName) throws IOException;


}
