package com.androidApp.randomtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;


/**
 * generate random words for text entry.
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class RandomDictionary {
	String[] mWords;

	public RandomDictionary(Context context, String dictionary) throws IOException {
		AssetManager am = context.getAssets();
		InputStream is = am.open(dictionary);
		int nLines = numLines(is);
		is.close();
		is = am.open(dictionary);
		mWords = readLines(is, nLines);
	}
	
	// return the number of lines in the file
	public int numLines(InputStream is) throws IOException {
		int lineCount = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		do {
			line = br.readLine();
			lineCount++;
		} while (line != null);
		return lineCount;
	}

	// given the number of lines in the file, read it into an array of strings.
	public String[] readLines(InputStream is, int numLines) throws IOException {
		String[] words = new String[numLines];
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for (int i = 0; i < numLines; i++) {
			words[i] = br.readLine();
		} 
		return words;
	}

	// put together a random string from words read from the dictionary file
	public String randWords(int maxLength) {
		int length = (int) Math.random()*maxLength;
		StringBuffer buf = new StringBuffer(length);
		while (buf.length() < length) {
			int randIndex = (int) (mWords.length*Math.random());
			String newWord = mWords[randIndex];
			if (buf.length() + newWord.length() < length) {
				if (buf.length() > 0) {
					buf.append(' ');
				}
				buf.append(newWord);
			} else {
				// always put in something.
				if (buf.length() == 0) {
					buf.append(newWord);
				}
				break;
			}
		}
		return buf.toString();
	}
}
