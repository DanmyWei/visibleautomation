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
	public MotionEventList(String name, List<String> touchDownTokens, String nextLine, BufferedReader br) throws EmitterException, IOException {
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
		String line = nextLine;
		do {
			
			SuperTokenizer st = new SuperTokenizer(line, "\"", ":,", '\\');
			List<String> tokens = st.toList();
			if (tokens.get(0).equals(Constants.Events.TOUCH_MOVE)) {
				eventTimeMsec = Long.parseLong(tokens.get(1));
				eventX = Float.parseFloat(tokens.get(2));
				eventY = Float.parseFloat(tokens.get(3));
				MotionEventPoint moveEvent = new MotionEventPoint(eventTimeMsec, eventX, eventY);
				mPoints.add(moveEvent);
			} else if (tokens.get(0).equals(Constants.Events.TOUCH_UP)) { 
				eventTimeMsec = Long.parseLong(tokens.get(1));
				eventX = Float.parseFloat(tokens.get(2));
				eventY = Float.parseFloat(tokens.get(3));
				MotionEventPoint upEvent = new MotionEventPoint(eventTimeMsec, eventX, eventY);
				mPoints.add(upEvent);
				break;
			} else {
				throw new EmitterException("motion events: bad tag " + tokens.get(0));
			}
			line = br.readLine();
		} while (true);
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
}
