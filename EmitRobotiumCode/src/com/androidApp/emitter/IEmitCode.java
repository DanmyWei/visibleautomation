package com.androidApp.emitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


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
	String getApplicationClassPath();
	String getApplicationClassName();
	String getApplicationPackage();
	List<LineAndTokens> generateTestCode(IEmitCode emitter, String eventsFileName, List<MotionEventList> motionEvents) throws FileNotFoundException, IOException, EmitterException;
	void emit(BufferedReader br, List<LineAndTokens> lines, List<MotionEventList> motionEvents) throws IOException, EmitterException;
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
	void writeScroll(int scrollListIndex, int scrollFirstVisibleItem, List<String> tokens, List<LineAndTokens> outputLines) throws IOException;
	void writeEnterText(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeWaitForListClassIndex(List<String> tokens, int itemIndex, List<LineAndTokens> lines) throws IOException;
	void writeWaitForListIdItem(List<String> tokens, int itemIndex, List<LineAndTokens> lines) throws IOException;
	void writeItemClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writePopupMenuItemClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeItemSelected(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeChildClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeGroupClick(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
	void writeHeader(String classPath, String testPackage, String testClassName, String className, BufferedWriter bw) throws IOException;
	void writeTrailer(BufferedWriter bw) throws IOException;
	void writeClassTrailer(BufferedWriter bw) throws IOException;
	void writeLines(BufferedWriter bw, List<LineAndTokens> lines) throws IOException;
	String writeViewIDCommand(String templateFile, ReferenceParser ref, String fullDescription) throws IOException;
	String writeViewClassIndexCommand(String templateFile, ReferenceParser ref, String fullDescription) throws IOException;
	void waitForPageToLoad(List<String> tokens, List<LineAndTokens> lines) throws IOException, EmitterException;
}
