package com.androidApp.util;

import java.util.ArrayList;
import java.util.List;

// like string tokenizer, except that delimiters between quotes aren't counted.  Characters are also escaped.

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
		StringBuffer sbToken = new StringBuffer();
		boolean fInQuotes = false;
		char quoteChar = '\0';
		if ((mString == null) || (mString.length() == 0)) {
			return null;
		}
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
					break;
				} else {
					sbToken.append(ch);
				}
			}
		}
		if (ich == mString.length()) {
			mString = null;
		}
		return sbToken.toString();
	}
}