package com.androidApp.emitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.util.Constants;
import com.androidApp.util.SuperTokenizer;

/**
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved
 * for views which receive motion events, the recorder writes the ACTION_DOWN event, followed
 * by ACTION_MOVE events, terminated by an ACTION_UP event. The first event contains the view dimensions
 * then the rest contain the move and up event time and coordinates.  We read from the source until the 
 * ACTION_UP event, and write it in ASCII to the stream:
 * time_msec, width,height,down_x,down_y
 * time_msec, motion_x, motion_y
 * last line is assumed to be the ACTION_UP event by the reader.
 * @param pr output PrintWriter
 * @param touchDownTokens touch_down:20900105,720,176,586.18585,37.85637,id,0x7f0900a2,android.widget.Gallery
 * @param br lines containing:
 * touch_move:20900152,584.18866,37.85637,id,0x7f0900a2,android.widget.Gallery,Gallery
 * ending with:
 * touch_up:20900606,249.65326,71.82982,id,0x7f0900a2,android.widget.Gallery,Gallery
 * @throws EmitterException if the tag is not touch_move or touch_up for one of the following lines
 */


public class MotionEventList {
	
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
	protected ReferenceParser 			mRef; 
	protected String 					mName;				// file name to write to		
	protected int						mWidth;				// dimensions of containing view
	protected int						mHeight;
	protected List<MotionEventPoint>	mPoints;			// list of recorded points
	protected int						mLastReadIndex;		// so caller can know how many lines we read
	/**
	 * read motion events from the input tokens. TouchDownTokens is the touch_down event which kicks off
	 * the read, and we read until either touch_up or an event that isn't touch_move (since other events can
	 * happen, like going out of the window)
	 * @param name  name that the motion event list is assigned
	 * @param touchDownTokens tokens from the touch_down events
	 * @param tokenLines the events file, parsed into a list of lists of tokens
	 * @param currentReadIndex current position in the list of lists of tokens
	 * @throws EmitterException
	 * @throws IOException
	 */
	public MotionEventList(String 				name, 
						   List<String> 		touchDownTokens, 
						   List<List<String>>	tokenLines,
						   int					currentReadIndex) throws EmitterException, IOException {
		mName = name;
		mPoints = new ArrayList<MotionEventPoint>();
		mWidth = Integer.parseInt(touchDownTokens.get(2));
		mHeight = Integer.parseInt(touchDownTokens.get(3));
		long eventTimeMsec = Long.parseLong(touchDownTokens.get(1));
		float eventX = Float.parseFloat(touchDownTokens.get(4));
		float eventY = Float.parseFloat(touchDownTokens.get(5));
		MotionEventPoint downEvent = new MotionEventPoint(eventTimeMsec, eventX, eventY);
		mRef = new ReferenceParser(touchDownTokens, 6);
		mPoints.add(downEvent);
		int iPointLine;
		// we've already read touch down, hence + 1
		for (iPointLine = currentReadIndex + 1; iPointLine < tokenLines.size(); iPointLine++) {
			List<String> tokens = tokenLines.get(iPointLine);
			String action = tokens.get(0);
			if (Constants.UserEvent.TOUCH_MOVE.equals(action)) {
				eventTimeMsec = Long.parseLong(tokens.get(1));
				eventX = Float.parseFloat(tokens.get(4));
				eventY = Float.parseFloat(tokens.get(5));
				MotionEventPoint moveEvent = new MotionEventPoint(eventTimeMsec, eventX, eventY);
				mPoints.add(moveEvent);
			} else if (Constants.UserEvent.TOUCH_UP.equals(action)) { 
				eventTimeMsec = Long.parseLong(tokens.get(1));
				eventX = Float.parseFloat(tokens.get(4));
				eventY = Float.parseFloat(tokens.get(5));
				MotionEventPoint upEvent = new MotionEventPoint(eventTimeMsec, eventX, eventY);
				mPoints.add(upEvent);
				mLastReadIndex = iPointLine + 1;		// we want this event.
				break;
			} else {
				mLastReadIndex = iPointLine;		// caller wants this event
				break;
			}
		}
	}
	
	/**
	 * write the points to an output stream
	 * @param os
	 */
	public void write(OutputStream os) throws IOException {
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os));
		String startLine = Integer.toString(mWidth) + "," + Integer.toString(mHeight);
		br.write(startLine + "\n");
		for (MotionEventPoint point : mPoints) {
			String pointLine = Long.toString(point.mTimeMsec) + "," + Float.toString(point.mX) + "," + Float.toString(point.mY);
			br.write(pointLine + "\n");
		}
		br.flush();
	}
	
	public String getName() {
		return mName;
	}
	
	public ReferenceParser getRef() {
		return mRef;
	}
	
	public int getLastReadIndex() {
		return mLastReadIndex;
	}
}
