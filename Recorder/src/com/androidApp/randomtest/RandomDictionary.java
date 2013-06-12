package com.androidApp.randomtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.androidApp.Utility.FileUtils;

import android.content.Context;
import android.content.res.AssetManager;


/**
 * generate random words for text entry.
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class RandomDictionary {
	String[] mWords;

	public RandomDictionary(Context context, int dictionaryResId) throws IOException {
		mWords = FileUtils.readRawResource(context, dictionaryResId);
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
