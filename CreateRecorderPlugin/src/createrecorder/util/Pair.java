package createrecorder.util;

/** simple pair class used for parsing the aapt output for AndroidManifest.xml
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class Pair {
	String mFirst;
	String mSecond;
	
	public Pair(String first, String second) {
		mFirst = first;
		mSecond = second;
	}
	public Pair(String[] values) {
		mFirst = values[0];
		mSecond = values[1];
	}
	
	public static Pair[] toPairArray(String[][] values) {
		Pair[] pairArray = new Pair[values.length];
		for (int i = 0; i < values.length; i++) {
			pairArray[i] = new Pair(values[i]);
		}
		return pairArray;
	}
	
	public boolean equals(Pair pair) {
		return pair.mFirst.equals(this.mFirst) && pair.mSecond.equals(this.mSecond);
	}
	
	public String toString() {
		return this.mFirst + "," + this.mSecond;
	}
}
