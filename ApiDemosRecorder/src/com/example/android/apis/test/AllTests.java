package com.example.android.apis.test;

import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {
	protected final String TAG = "AllTests";
	
	public AllTests() {
		Log.d(TAG, "AllTests Constructor");
	}
	
	public static Test suite() {
		Test test = new TestSuiteBuilder(AllTests.class).includeAllPackagesUnderHere().build();
		return test;
	}

}
