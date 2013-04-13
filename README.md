RobotiumRecorder
================

Listener to record events, and an emitter to generate code for robotium.

I had been working with Robotium for the past couple of months, and I've found the script generation to be fairly tedious.  I decided to attack the record part of the problem using reflection and intercepting the events, then an emitter to generate the Robotium code.

The system is far from complete, but it does handle clicks, lists, activity events, back key, text entry and resources.


Recorder
I'm assuming that you've installed the Android SDK, Java, and Eclipse.  Download the robotium jar file from http://code.google.com/p/robotium/downloads/list.

The recorder is set up as a library which integrates with your application using Android Instrumentation.  Create a Android Test Project which references your application, then link with Recorder as a library.

IMPORTANT NOTE: FOR SOME ABSURD REASON, ECLIPSE TURNS OFF THE IsLibrary flag for Recorder, so you have to right-click properties, then check "Is Library" before you import the ApiDemosRecorder project.  Import ApiDemos before you import ApiDemosRecorder. 

From the ApiDemosRecorder example project, copy ApiDemosTest.java to your test package directory, and edit it as follows:


package com.example.android.apis.test;				<-- change this to your test package name

import com.androidApp.Test.RecordTest;
import com.example.android.apis.ApiDemos;			<-- change this to the target package and application


public class ApiDemosTest extends RecordTest<ApiDemos> {	<-- Change ApiDemosTest to the name of your Test class, and ApiDemos to your target app

	public ApiDemosTest() {
		super(ApiDemos.class);				<-- Same here
	}
	
	public ApiDemosTest(Class<ApiDemos> activityClass) {	<-- And here
		super(activityClass);
	}
	
	@Override
	public void initializeResources() {
		addRdotID(new com.example.android.apis.R.id());			<-- replace these with the class names for your target application resource files
		addRdotString(new com.example.android.apis.R.string());	
	}
	
	public void setUp() throws Exception { 			<-- plug what you need to add after super.setUp() to initialize your test
		super.setUp();
	}
}
 

Then run ApiDemosTest as a Android Junit app. The events from your application are recorded to events.txt in your external storage directory, usually sdcard.  When you've run the recording, to copy it to your host:

adb pull /sdcard/events.txt

Then run EmitRobotiumCode with the following arguments:

java -cp <classpath to EmitRobotiumCode classes> com.androidApp.emitter.EmitRobotiumCode events.txt <target project name> <directory that you downloaded robotium to>

for example:
java -cp c:\redfoundry\workspace\EmitRobotiumCode\bin com.androidApp.emitter.EmitRobotiumCode events.txt ApiDemos c:\temp\robotium-solo-3.6.jar

This will create an android project named ApiDemosTest, which you can import as an Android project, and run immediately.  Watch the magic!

The ApiDemosTest project is written to test the ApiDemos project, which is included in the samples in the Android SDK

Best Regards,
Matthew Reynolds
matthewcreynolds@gmail.com

