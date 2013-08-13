package createrecorder.util;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** parse the information of importance from the output of aapt dump --values xmltree <apk-name> AndroidManifest.xml
  *
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
  */

public class ManifestInformation {
	public String	mPackage = null;
	public int		mTargetSDKVersion = 0;
	public int		mMinSDKVersion = 0;
	public String	mApplicationName = null;
	public String	mStartActivityName = null;
	
	protected final String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";
	protected final String[][] packagePair = {{"N", "android"} , {"E", "manifest"}, {"A", "package"} };
	protected final Pair[] PACKAGE_PATH = Pair.toPairArray(packagePair);			
	protected final String[][] minSdkVersionPair = {{"N", "android"} , {"E", "manifest"}, {"E", "uses-sdk"}, {"A", "android:minSdkVersion"}};
	protected final Pair[] MIN_SDK_PATH = Pair.toPairArray(minSdkVersionPair);			
	protected final String[][] targetSdkVersionPair = {{"N", "android"} , {"E", "manifest"}, {"E", "uses-sdk"}, {"A", "android:targetSdkVersion"}};
	protected final Pair[] TARGET_SDK_PATH = Pair.toPairArray(targetSdkVersionPair);	
	protected final String[][] applicationPair = {{"N", "android"} , {"E", "manifest"}, {"E", "application"}, {"A", "android:name"}};
	protected final Pair[] APPLICATION_PATH = Pair.toPairArray(applicationPair);	
	protected final String[][] activityPair = {{"N", "android"} , {"E", "manifest"}, {"E", "application"}, {"E", "activity"}, {"A", "android:name"}};
	protected final Pair[] ACTIVITY = Pair.toPairArray(activityPair);	
	protected final String[][] actionPair = {{"N", "android"} , {"E", "manifest"}, {"E", "application"}, {"E", "activity"}, {"E", "intent-filter"}, {"E", "category"}, {"A", "android:name"}};
	protected final Pair[] ACTION = Pair.toPairArray(actionPair);	
	protected final String[][] actionPairAlias = {{"N", "android"} , {"E", "manifest"}, {"E", "application"}, {"E", "activity-alias"}, {"E", "intent-filter"}, {"E", "category"}, {"A", "android:name"}};
	protected final Pair[] ACTION_ALIAS = Pair.toPairArray(actionPairAlias);	
	
	public ManifestInformation(String[] lines) {
		String candActivityName = null;
		String firstActivityName = null;
		int currentIndent = 0;
		Stack<Pair> stack = new Stack<Pair>();
		for (String line : lines) {
			int indent = getIndent(line);
			if ((currentIndent != 0) && (indent <= currentIndent)) {
				for (int iPop = 0; iPop < (currentIndent - indent)/2 + 1; iPop++) {
					stack.pop();
				}
			}
			currentIndent = indent;
			Pair pair = getPair(line, ":", "(=");
			stack.push(pair);
			System.out.println("stack = " + stack);
			
			if (matchPairArray(stack, PACKAGE_PATH)) {
				//  A: package="com.example.android.apis" (Raw: "com.example.android.apis")
				String packageAttribute =  extractMatch(line, "package=\"[A-Za-z0-9\\.]*\"");
				if (packageAttribute != null) {
					mPackage = extractAttributeValue(packageAttribute);
				}
			} else if (matchPairArray(stack, MIN_SDK_PATH)) {
				// A: android:minSdkVersion(0x0101020c)=(type 0x10)0x4
				String minSDKVersion = extractMatch(line, "0x[0-9a-f]*$");
				if (minSDKVersion != null) {
					mMinSDKVersion = Integer.parseInt(minSDKVersion.substring(2), 16);
				}
			} else if (matchPairArray(stack, TARGET_SDK_PATH)) {
				// A: android:targetSdkVersion(0x01010270)=(type 0x10)0xe
				String targetSDKVersion = extractMatch(line, "0x[0-9a-f]*$");
				if (targetSDKVersion != null) {
					mTargetSDKVersion = Integer.parseInt(targetSDKVersion.substring(2), 16);
				}
			} else if (matchPairArray(stack, APPLICATION_PATH)) {
				// A: android:name(0x01010003)="ApiDemosApplication" (Raw: "ApiDemosApplication")
				String applicationName = extractMatch(line, "=\".*\" \\(");
				if (applicationName != null) {
					mApplicationName = applicationName.substring(2, applicationName.length() - 3);
				}
			} else if (matchPairArray(stack, ACTIVITY)) {
				 //A: android:name(0x01010003)="ApiDemos" (Raw: "ApiDemos")
				String activityName = extractMatch(line, "=\".*\" \\(");
				if (activityName != null) {
					// strip the ="activity" to activity
					candActivityName = activityName.substring(2, activityName.length() - 3);
				}
				if (firstActivityName == null) {
					firstActivityName = candActivityName;
				}
			} else if (matchPairArray(stack, ACTION)) {
				// A: android:name(0x01010003)="android.intent.action.MAIN" (Raw: "android.intent.action.MAIN")
				String intentName = extractMatch(line, "=\".*\" \\(");
				// strip the ="intent" to intent
				intentName = intentName.substring(2, intentName.length() - 3);
				if (intentName.equals(CATEGORY_LAUNCHER) && (mStartActivityName == null)) {
					mStartActivityName = candActivityName;
				}
			} else if (matchPairArray(stack, ACTION_ALIAS)) {
				// A: android:name(0x01010003)="android.intent.action.MAIN" (Raw: "android.intent.action.MAIN")
				String intentName = extractMatch(line, "=\".*\" \\(");
				// strip the ="intent" to intent
				intentName = intentName.substring(2, intentName.length() - 3);
				if (intentName.equals(CATEGORY_LAUNCHER) && (mStartActivityName == null)) {
					mStartActivityName = candActivityName;
				}
			}
		}
		if (mStartActivityName == null) {
			mStartActivityName = firstActivityName;
		}
	}
	
	public boolean verify() {
		return (mPackage != null) && (mTargetSDKVersion != 0) && (mMinSDKVersion != 0) && (mApplicationName != null) && (mStartActivityName != null);
	}
	
	public String errorMessage() {
		if (mPackage == null) {
			return "The package was not specified in AndroidManifest.xml";
		}
		if (mTargetSDKVersion == 0) {
			return "The target SDK version was not specified in AndroidManifest.xml";
		}
		if (mMinSDKVersion == 0) {
			return "The minimum SDK version was not specified in AndroidManifest.xml";
		}
		if (mApplicationName == null) {
			return "The application is not specified in AndroidManifest.xml";
		}
		if (mStartActivityName == null) {
			return "The start activity was not specified in AndroidManifest.xml";
		}
		return "unknown error";
	}
	
	public boolean matchPairArray(Stack<Pair> stack, Pair[] pairArray) {
		if (stack.size() != pairArray.length) {
			return false;
		}
		for (int iPair = 0; iPair < pairArray.length; iPair++) {
			if (!stack.get(iPair).equals(pairArray[iPair])) {
				return false;
			}
		}
		return true;
	}
	
	public int getIndent(String line) {
		for (int ich = 0; ich < line.length(); ich++) {
			if (!Character.isWhitespace(line.charAt(ich))) {
				return ich;
			}
		}
		return line.length();
	}
	
	public int skipWhitespace(String line, int ich) {
		for (;ich < line.length(); ich++) {
			char ch = line.charAt(ich);
			if (!Character.isWhitespace(ch)) {
				return ich;
			} 
		}
		return line.length();
	}
	
	// turn E: uses-permission into "E" "uses-permission"
	public Pair getPair(String line, String firstDelimiters, String secondDelimiters) {
		StringBuffer sbTag = new StringBuffer();
		int ich = skipWhitespace(line, 0);
		for (; ich < line.length(); ich++) {
			char ch = line.charAt(ich);
			if ((firstDelimiters.indexOf(ch) != -1) || Character.isWhitespace(ch))  {
				break;
			}
			sbTag.append(ch);
		}
		StringBuffer sbValue = new StringBuffer();
		ich = skipWhitespace(line, ich + 1);
		for ( ; ich <line.length(); ich++) {
			char ch = line.charAt(ich);
			if ((secondDelimiters.indexOf(ch) != -1) || Character.isWhitespace(ch)) {
				break;
			}
			sbValue.append(ch);
		}
		return new Pair(sbTag.toString(), sbValue.toString());
	}
	
	/**
	 * given a string, and a regular expression, return the first match
	 * @param line target string
	 * @param regexp regular expression
	 * @return matching string or null.
	 */
	public String extractMatch(String line, String regexp) {
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	// return bar from foo="bar"
	
	public String extractAttributeValue(String attribute) {
		int ichEqualsQuote = attribute.indexOf("=\"");
		return attribute.substring(ichEqualsQuote + 2, attribute.length() - 1);
	}

}
