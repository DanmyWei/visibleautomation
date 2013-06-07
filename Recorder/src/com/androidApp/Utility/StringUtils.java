package com.androidApp.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import android.content.Context;
import android.content.res.AssetManager;

/**
 * grab bag of string utilties
 * @author Matthew
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class StringUtils {
	
	
	public static boolean isEmpty(String s) {
		return (s == null) || s.equals("");
	}
	
	/**
	 * is s not blank? (i.e has something other than a whiteapce
	 * @param s
	 * @return true if s is not blank
	 */
	public static boolean isNotBlank(String s) {
		if (StringUtils.isEmpty(s)) {
			return false;
		}
		for (int ich = 0; ich < s.length(); ich++) {
			if (!Character.isWhitespace(s.charAt(ich))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * is s blank? empty or all whitespace?
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s) {
		if (StringUtils.isEmpty(s)) {
			return true;
		}
		for (int ich = 0; ich < s.length(); ich++) {
			if (!Character.isWhitespace(s.charAt(ich))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * make a string digstible for regexp.
	 * @param s
	 * @return string with all non alphanumeric characters replaced with .*
	 */
	 public static String regexDigest(String s) {
		 StringBuffer sb = new StringBuffer(s.length());
		 for (int ich = 0; ich < s.length(); ich++) {
			 char ch = s.charAt(ich);
			 if (Character.isLetterOrDigit(ch)) {
				 sb.append(ch);
			 } else {
				 sb.append(".*");
			 }
		 }
		 return sb.toString();
	 }
	 
	// escape a string with a prefix character "for example \ 'escape this'" -> \"for example \\ \'escape this\'\"
	public static String escapeString(String s, String escapeChars, char prefix) {
		StringBuffer sb = new StringBuffer(s.length());
		for (int ich = 0; ich < s.length(); ich++) {
			char ch = s.charAt(ich);
			if (escapeChars.indexOf(ch) != -1) {
				sb.append(prefix);
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	/**
	 * is s a hexidecimal number
	 * @param s
	 * @return true if 0x<digits>
	 */
	public static boolean isHexNumber(String s) {
		if ((s.charAt(0) != '0') || (s.charAt(1) != 'x')) {
			return false;
		}
		for (int ich = 2; ich < s.length(); ich++) {
			if (!Character.isDigit(s.charAt(ich))) {
				return false;
			}
		}
		return true;
	}
	
	// XX1 -> XX1st, XX2 -> XX2nd, XX3 -> XX3rd, XXother - XXth
	public static String getOrdinal(int value) {
		value++;				// people start from 1, computers start from zero
		String suffix = null;
		int lastDigit = value % 10;
		if (lastDigit == 1) {
			suffix = "st";
		} else if (lastDigit == 2) {
			suffix = "nd";
		} else if (lastDigit == 3) {
			suffix = "rd";
		} else {
			suffix = "th";
		}
		return Integer.toString(value) + suffix;
	}
	
	public static String massageString(String s) {
		StringBuffer sb = new StringBuffer(s.length());
		for (int ich = 0; ich < s.length(); ich++) {
			char ch = s.charAt(ich);
			if (Character.isLetterOrDigit(ch)) {
				sb.append(ch);
			} else if (Character.isWhitespace(ch)) {
				sb.append(' ');
			} else {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

}
