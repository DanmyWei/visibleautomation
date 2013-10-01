package com.androidApp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * general string utilities
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class StringUtils {
  	/**
  	 * is s null or an empty string?
   	 */	
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
			if (ch == '\n') {
				sb.append('n');
			} else if (ch == '\t') {
				sb.append('t');
			} else if (ch == '\b') {
				sb.append('b');
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	// unescape a string,  Strip the prefixes.
	public static String unescapeString(String s, char prefix) {
		StringBuffer sb = new StringBuffer(s.length());
		for (int ich = 0; ich < s.length(); ich++) {
			char ch = s.charAt(ich);
			if (ch == prefix) {
				// newline special case.
				if (ich < s.length() - 1) {
					char nextCh = s.charAt(ich + 1);
					if (nextCh == 'n') {
						sb.append('\n');
					} else if (nextCh == 'b') {
						sb.append('\b');
					} else if (nextCh == 't') {
						sb.append('\t');
					}
					ich++;
				}
				continue;
			} 
			sb.append(ch);
		}
		return sb.toString();
	}
	
	// strip the quotes surrounding the string
	public static String stripQuotes(String s) {
		if ((s.charAt(0) == '"') && (s.charAt(s.length() - 1) == '"')){
			return s.substring(1, s.length() - 1);
		} else {
			return s;
		}
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
			char ch = s.charAt(ich);
			if (!Character.isDigit(ch) && ((Character.toLowerCase(ch) >= 'a') || (Character.toLowerCase(ch) <= 'f'))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * is s a number
	 * @param s string to test
	 * @return true if digits and length > 0, false otherwise.
	 */
	public static boolean isNumber(String s) {
		if (s.length() == 0) {
			return false;
		}
		for (int ich = 0; ich < s.length(); ich++) {
			char ch = s.charAt(ich);
			if (!Character.isDigit(ch)) {
				return false;
			}
		}
		return true;
	}	 

	
	/**
	 * is s a quoted string
	 * @param s
	 * @return true if the string is surrounded by quotes.
	 */
	public static boolean isQuotedString(String s) {
		return (s.charAt(0) == '\"') && (s.charAt(s.length() - 1) == '\"');
	}
	
	/**
	 * return foo from com.example.foo
	 * @param s com.example.foo
	 * @return foo
	 */
	public static String getNameFromClassPath(String s) {
		int ich = s.lastIndexOf('.');
		if (ich != -1) {
			return s.substring(ich + 1);
		} else {
			return s;
		}
	}
	
	/**
	 * return com.example from com.example.foo
	 * @param s com.example.foo
	 * @return com.example
	 */

	public static String getPackageFromClassPath(String className) {
		int ich = className.lastIndexOf('.');
		if (ich != -1) {
			return className.substring(9, ich);
		} else {
			return className;
		}
	}
	
	/**
	 * given a list of strings, return the concatenated string with delimiters.
	 * @param stringList list of strings to concatenate
	 * @param delimiter to stick in between them
	 */
	public static String concatStringList(List<String> stringList, String delimiter) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iter = stringList.iterator(); iter.hasNext(); ) {
			String s = iter.next();
			sb.append(s);
			if (iter.hasNext()) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
	
	/**
	 * take a vector of strings and return,them,in,a,single,string
	 * @param list
	 * @param delim
	 * @return
	 */
	public static String delimit(List<String> list, char delim) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i));
			if (i < list.size() - 1) {
				sb.append(',');
			}
		}
		return sb.toString();
	}
	
	/**
	 * is s in array?
	 * @param s string 
	 * @param array array of strings
	 * @return true if s is in array
	 */
	public static boolean inStringArray(String s, String[] array) {
		for (String cand : array) {
			if (s.equals(cand)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * is s contained in a string in array
	 * @param s
	 * @param array
	 * @return
	 */
	public static boolean containedInStringArray(String s, String[] array) {
		for (String cand : array) {
			if (cand.contains(s)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * given a string, and a regular expression, return the first match
	 * @param line target string
	 * @param regexp regular expression
	 * @return matching string or null.
	 */
	public static String extractMatch(String line, String regexp) {
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
	/**
	 * strip the front and back strings from s
	 * @param s string to strip
	 * @param front remove from front
	 * @param back remove from back
	 * @return
	 */
	public static String stripFrontBack(String s, String front, String back) {
		return s.substring(front.length(), s.length() - back.length());
	}
	
	/**
	 * create an exported library entry for a jar file.
	 * @param jarfile
	 * @return
	 */
	public static String createClasspathLibraryEntry(String jarfile, boolean fExport) {
		return "\t<classpathentry exported=\"" + Boolean.toString(fExport) + "\" kind=\"lib\" path=\"libs/" + jarfile + "\"/>\n";
	}
	
	/**
	 * given the list of jarfiles, create the .classpath file entries.
	 */
	public static String createJarClasspathEntries(List<String> jarfiles, boolean fExport) {
		StringBuffer sb = new StringBuffer();
		for (String jarfile : jarfiles) {
			sb.append(createClasspathLibraryEntry(jarfile, fExport));
		}
		return sb.toString();
	}

}
