package com.androidApp.Test;

import java.util.List;

import com.androidApp.EventRecorder.UserDefinedViewReference;

/**
 * so the references for views whose motion events were interested in can be pulled from RecordTestBinary and RecordTest 
 * @author matt2
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public interface IRecordTest {
	List<UserDefinedViewReference> getMotionEventViewReferences();		// listen to motion events in these views
	List<String> getInterstitialActivityNames();						// interstitial activities have special case handlers
}
