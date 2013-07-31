package com.androidApp.util;

import java.util.ArrayList;
import java.util.List;

/** like string tokenizer, except that delimiters between quotes aren't counted.  Characters are also escaped.
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class SuperTokenizer {
	protected String mString;						// string to tokenize
	protected final String mDelimiters;				// delimiters between strings, like ',' or ':,'
	protected final char mEscape;					// escape character (usually '\')
	protected final String mQuotes;					// quote character (usually ' or ")
	
	public SuperTokenizer(String s, String quotes, String delim, char escape) {
		mString = s;
		mQuotes = quotes;
		mDelimiters = delim;
		mEscape = escape;		
	}
	
	/**
	 * return a list of strings from the sucessive tokens
	 * @return list of tokens.
	 */
	public List<String> toList() {
		List<String> list = new ArrayList<String>();
		String s;
		while ((s = this.next()) != null) {
			list.add(s.trim());
		}
		return list;	
	}
	
	// get the next token from,"the",\token,'string' -> [from the token string]
	public String next() {
		if ((mString == null) || (mString.length() == 0)) {
			return null;
		}
		
		// mark the end of our string, allocate a buffer for the token, "outside of quotes" state flag, initialize
		// the index so we can compare against the end of the string
		int originalStringLength = mString.length();
		StringBuffer sbToken = new StringBuffer();
		boolean fInQuotes = false;
		char quoteChar = '\0';
		int ich = 0;
		for (ich = 0; ich < mString.length(); ich++) {
			char ch = mString.charAt(ich);
			
			// escape character: don't care about quote state, write the prefix and character.
			if (ch == mEscape) {
				sbToken.append(ch);
				ich++;
				ch = mString.charAt(ich);
				sbToken.append(ch);
				continue;
			} else if (fInQuotes) {
				// toggle the quote state by checking a match from the start code, and add the character (TODO: add an option where quotes are stripped)
				if (ch == quoteChar) {
					fInQuotes = false;
				}
				sbToken.append(ch);
			} else {
				// if the character is a quote, quote state is on, If it's a delimiter, then we chop the string to the character after the 
				// delimiter, otherwise we append the quote
				if (mQuotes.indexOf(ch) != -1) {
					quoteChar = ch;
					fInQuotes = true;
					sbToken.append(ch);
				} else if (mDelimiters.indexOf(ch) != -1) {
					mString = mString.substring(ich + 1);
					return sbToken.toString();
				} else {
					sbToken.append(ch);
				}
			}
		}
		// if we've reached the end, then set the string to null, whether we've closed quotes, read delimiters, or whatever.
		if (ich == originalStringLength) {
			mString = null;
		}
		return sbToken.toString();
	}
}
