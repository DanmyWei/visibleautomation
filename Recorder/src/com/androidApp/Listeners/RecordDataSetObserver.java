package com.androidApp.Listeners;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ViewReference;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;

import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.database.DataSetObserver;

/**
 * TODO: defunct: remove this class
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class RecordDataSetObserver extends DataSetObserver implements IOriginalListener  {
	protected EventRecorder mEventRecorder;
	
	public RecordDataSetObserver(EventRecorder eventRecorder, Adapter adapter) {
		try {
			adapter.registerDataSetObserver(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, "record dataset observer");
		}		
	}

	@Override
	public Object getOriginalListener() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onChanged() {
		
	}
	
	@Override
	public void onInvalidated() {
	}
}
