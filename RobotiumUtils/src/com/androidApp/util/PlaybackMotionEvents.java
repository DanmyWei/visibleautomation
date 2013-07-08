package com.androidApp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.SystemClock;

/**
 * Code to play back recorded motion events from an ASCII file, which contains the dimensions of the source view
 * and a timestamp and coordinate for each view
 * TODO: we really only need 1 unit of decimal precision here.
 * file format:
 * 720,176
 * 100447483,644.1054,79.82358
 * @author matt2
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class PlaybackMotionEvents {
	
	// event point written with time, so we can subsample if neccessary
	protected class MotionEventPoint {
		public long mTimeMsec;
		public float mX;
		public float mY;
		
		MotionEventPoint(long timeMsec, float x, float y) {
			mTimeMsec = timeMsec;
			mX = x;
			mY = y;
		}
	}
	private static final String TAG = "PlaybackMotionEvents";
	protected int mWidth;
	protected int mHeight;
	protected List<MotionEventPoint> mPointList;
	
	public PlaybackMotionEvents(InputStream is) throws IOException {
		read(is);
	}
	
	public PlaybackMotionEvents(Instrumentation instrumentation, String assetFile) throws IOException {
		Context context = instrumentation.getContext();
		AssetManager am = context.getAssets();
		InputStream is = am.open(assetFile);
		read(is);
		is.close();
	}
	
	/**
	 * read the motion events from an ASCII file
	 * @param is InputStream (probably an asset actually
	 * @throws IOException
	 */
	public void read(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String firstLine = br.readLine();
		
		// the first line contains the dimensions of the widget that the motion events were recorded on
		// so we can scale them to the destination view.
		if (firstLine != null) {
			String[] tokens = firstLine.split(",");
			mWidth = Integer.parseInt(tokens[0]);
			mHeight = Integer.parseInt(tokens[1]);
		}
		mPointList = new ArrayList<MotionEventPoint>();
		String line = br.readLine();
		while (line != null) {
			String[] tokens = line.split(",");
			MotionEventPoint point = new MotionEventPoint(Long.parseLong(tokens[0]), 
														  Float.parseFloat(tokens[1]), 
														  Float.parseFloat(tokens[2]));
			mPointList.add(point);
			line = br.readLine();
		}		
	}
	
	/**
	 * scale and translate the points to the new view from the view that the events were recorded from
	 * NOTE: this actually changes the point array, so you can only call it ones
	 * @param v target view
	 */
	protected void scaleAndTranslatePointsToView(View v) {
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int offsetX = location[0];
		int offsetY = location[1];
		float scaleX = ((float) v.getMeasuredWidth())/(float) mWidth;
		float scaleY = ((float) v.getMeasuredHeight())/(float) mHeight;
		
		// translate and scale the points to the new view.  sendPointerSync() sends events scaled to the display. It has no target view
		for (MotionEventPoint point : mPointList) {
			point.mX *= scaleX;
			point.mY *= scaleY;
			point.mX += offsetX;
			point.mY += offsetY;
		
		}
	}
	/**
	 * play the points back to the application.  Because we rule.s
	 * @param instrumentation
	 * @param fAllowSkip
	 */
	public void playback(Instrumentation instrumentation, View v, boolean fAllowSkip) {
		if (!mPointList.isEmpty()) {
			long startPointTime = mPointList.get(0).mTimeMsec;
			long startRealTime = SystemClock.uptimeMillis();
			long realTime = startRealTime;
			scaleAndTranslatePointsToView(v);
			MotionEventPoint firstPoint = mPointList.get(0);
			MotionEventPoint lastPoint = mPointList.get(mPointList.size() - 1);
			boolean fSkip = false;
			for (MotionEventPoint point : mPointList) {
				if (!fAllowSkip || !fSkip) {
					Log.i(TAG, "motion event x = " + point.mX + " y = " + point.mY + "down time = " + startRealTime + " time = " + realTime);
					if (point.equals(firstPoint)) {
						MotionEvent event = MotionEvent.obtain(startRealTime, realTime, MotionEvent.ACTION_DOWN, point.mX, point.mY, 0);
						instrumentation.sendPointerSync(event);
					} else if (point.equals(lastPoint)) {
						MotionEvent event = MotionEvent.obtain(startRealTime, realTime, MotionEvent.ACTION_UP, point.mX, point.mY, 0);
						instrumentation.sendPointerSync(event);
					} else {
						MotionEvent event = MotionEvent.obtain(startRealTime, realTime, MotionEvent.ACTION_MOVE, point.mX, point.mY, 0);
						instrumentation.sendPointerSync(event);
					}
				}
				long pointElapsedTime = point.mTimeMsec - startPointTime;
				realTime = SystemClock.uptimeMillis();
				long realElapsedTime = realTime - startRealTime;
				if (realElapsedTime > pointElapsedTime) {
					fSkip = true;
				}
			}
			instrumentation.waitForIdleSync();
		}
	}

}
