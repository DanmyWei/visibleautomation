package com.androidApp.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * utilities to read files and raw resources.  We can't read assets, because we're a library.
 * @author Matthew
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class FileUtils {
	
	// return the number of lines in the file
	public static int numLines(InputStream is) throws IOException {
		int lineCount = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		do {
			line = br.readLine();
			if (line != null) {
				lineCount++;
			}
		} while (line != null);
		return lineCount;
	}

	// given the number of lines in the file, read it into an array of strings.
	public static String[] readLines(InputStream is, int numLines) throws IOException {
		String[] lines = new String[numLines];
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for (int i = 0; i < numLines; i++) {
			lines[i] = br.readLine();
		} 
		return lines;
	}
	
	/**
	 * function to read an asset file into an array of strings
	 * @param context context to access the asset
	 * @param asset assetname
	 * @return array of strings
	 * @throws IOException
	 */
	public static String[] readRawResource(Context context, int resourceId) throws IOException {
		InputStream is = context.getResources().openRawResource(resourceId);
		int nLines = FileUtils.numLines(is);
		is.close();
		is = context.getResources().openRawResource(resourceId);
		String[] lines = FileUtils.readLines(is, nLines);
		is.close();
		return lines;
	}
}
