package com.androidApp.util;

import java.util.ArrayList;
import java.util.List;

/** like string tokenizer, except that delimiters between quotes aren't counted.  Characters are also escaped.
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class SuperTokenizer {
	String mString;
	String mDelimiters;
	char mEscape;
	String mQuotes;
	
	public SuperTokenizer(String s, String quotes, String delim, char escape) {
		mString = s;
		mQuotes = quotes;
		mDelimiters = delim;
		mEscape = escape;		
	}
	
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
		int originalStringLength = mString.length();
		StringBuffer sbToken = new StringBuffer();
		boolean fInQuotes = false;
		char quoteChar = '\0';
		int ich = 0;
		for ( ich = 0; ich < mString.length(); ich++) {
			char ch = mString.charAt(ich);
			if (ch == mEscape) {
				sbToken.append(ch);
				ich++;
				sbToken.append(ch);
				continue;
			} else if (fInQuotes) {
				if (ch == quoteChar) {
					fInQuotes = false;
				}
				sbToken.append(ch);
			} else {
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
		if (ich == originalStringLength) {
			mString = null;
		}
		return sbToken.toString();
	}
}
