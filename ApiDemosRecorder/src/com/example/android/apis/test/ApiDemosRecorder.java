package com.example.android.apis.test;

import java.io.IOException;

import com.androidApp.SupportTest.RecordTest;
import com.example.android.apis.ApiDemos;


public class ApiDemosRecorder extends RecordTest<ApiDemos> {	
	public ApiDemosRecorder() throws IOException {
		super(ApiDemos.class);
	}

	public ApiDemosRecorder(Class<ApiDemos> activityClass) throws IOException {
		super(activityClass);
	}

	@Override
	public void initializeResources() {
		addRdotID(new com.example.android.apis.R.id());
		addRdotString(new com.example.android.apis.R.string());	
	}

	public void setUp() throws Exception { 
		super.setUp();
		initialize(ApiDemos.class);
	}
}
