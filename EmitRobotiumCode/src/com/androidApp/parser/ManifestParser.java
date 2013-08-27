package com.androidApp.parser;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * class to parse the target project's manifest AndroidManifest.xml, so we can get the SDK tag, the launch
 * activity, and launch intent filters.  It's mostly a SAXParser based on the Parser class, which manages the XML tags
 * We just match against the fully qualified tags of the form com.foo.bar
 * @author matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ManifestParser extends Parser {
	protected final String MANIFEST_TAG = "manifest";
	protected final String ACTIVITY_TAG = "manifest.application.activity";
	protected final String ACTION_TAG = "manifest.application.activity.intent-filter.action";
	protected final String CATEGORY_TAG = "manifest.application.activity.intent-filter.category";
	protected final String INSTRUMENTATION_TAG = "manifest.instrumentation";
	protected final String USES_SDK_TAG = "manifest.uses-sdk";
	protected final String MIN_SDK_VERSION_ATTRIBUTE = "android:minSdkVersion";
	protected final String PACKAGE_ATTRIBUTE = "package";
	protected final String TARGET_PACKAGE_ATTRIBUTE = "android:targetPackage";
	protected final String NAME_ATTRIBUTE = "android:name";
	protected final String LAUNCHER_VALUE = "android.intent.category.LAUNCHER";
	protected String mPackage = null;
	protected String mTargetPackage = null;
	protected String mCandidateActivity = null;
	protected String mStartActivity = null;
	protected String mMinSdkVersion = null;
	
	/** 
	 * constructor variant which takes the filename
	 * @param manifestFileName
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public ManifestParser(String manifestFileName) throws SAXException, ParserConfigurationException, IOException { 
		File manifestFile = new File(manifestFileName);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(manifestFile, this);
	}
	
	public ManifestParser(File manifestFile) throws SAXException, ParserConfigurationException, IOException { 
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(manifestFile, this);
	}
	
	public String getTargetPackage() {
		return mTargetPackage;
	}
	
	
	public String getPackage() {
		return mPackage;
	}
	
	/**
	 * sometimes the activity is specified as activity name="foo", sometimes it's activity name=".foo"
	 * and sometimes it's activity name "com.bar.foo"
	 * TODO: do we need to create a new package if it's com.bar.foo?
	 * @return a reasonable activity name
	 */
	public String getStartActivity() {
		int ichDot = mStartActivity.lastIndexOf('.');
		if (ichDot == -1) {
			return mStartActivity;
		} else {
			return mStartActivity.substring(ichDot + 1);
		}
	}
	
	public String getMinSDKVersion() {
		return mMinSdkVersion;
	}
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		mTokenStack.push(qName);
		if (compareTag(MANIFEST_TAG)) {
			mPackage = attributes.getValue(PACKAGE_ATTRIBUTE);
		} else if (compareTag(ACTIVITY_TAG)) {
			mCandidateActivity = attributes.getValue(NAME_ATTRIBUTE);
		} else if (compareTag(CATEGORY_TAG)) {
			String nameValue = attributes.getValue(NAME_ATTRIBUTE);
			if (nameValue.equals(LAUNCHER_VALUE)) {
				mStartActivity = mCandidateActivity;
			}		
		} else if (compareTag(INSTRUMENTATION_TAG)) {
			mTargetPackage = attributes.getValue(TARGET_PACKAGE_ATTRIBUTE);
		} else if (compareTag(USES_SDK_TAG)) {
			mMinSdkVersion = attributes.getValue(MIN_SDK_VERSION_ATTRIBUTE);
		}
	}
}
