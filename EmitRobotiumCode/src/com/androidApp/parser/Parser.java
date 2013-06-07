package com.androidApp.parser;

import java.util.Stack;
import org.xml.sax.helpers.DefaultHandler;

/**
 * base parser class which maintains a token stackas element tags are encountered.
 * @author matthew
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class Parser extends DefaultHandler {
	protected Stack<String> mTokenStack;
	
	/**
	 * compare a tag of the form com.foo.bar with the tag stack
	 * @param tag
	 * @return true/false
	 */
	protected boolean compareTag(String tag) {
		String[] tags = tag.split("\\.");
		if (tags.length != mTokenStack.size()) {
			return false;
		}
		int itag = 0;
		for (String stackTag : mTokenStack) {
			if (!stackTag.equals(tags[itag++])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * initialize the token stack
	 */
	@Override
	public void startDocument() {
		mTokenStack = new Stack<String>();
	}
	
	/**
	 * pop the token stack on the closing XML tag.
	 */

	@Override
	public void endElement(String uri, String localName, String qName)  {
		mTokenStack.pop();
	}

}
