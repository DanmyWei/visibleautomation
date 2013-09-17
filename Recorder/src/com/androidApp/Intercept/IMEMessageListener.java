package com.androidApp.Intercept;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ActivityInterceptor;
import com.androidApp.Test.ViewInterceptor;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

/**
 * listen for up/down messages from our custom IME.
 * IMPORTANT NOTE: This is reliant on the SoftKeyboard and RecordOnTextChangedListener for communication and reference counting
 * When the user hits a key in the soft keyboard, it sends a TCP message to the IMEMessageListener, so we can differentiate between
 * keys entered by the user from the IME, and text set programmatically in EditText.  Normally, I would use a listener, like KeyListener
 * untl I saw this jaw-dropping note in the android docs:
 * from http://developer.android.com/reference/android/text/method/KeyListener.html:
 * Key presses on soft input methods are not required to trigger the methods in this listener, and are in fact discouraged to do so. 
 * The default android keyboard will not trigger these for any key to any application targetting Jelly Bean or later, and will only deliver it 
 * for some key presses to applications targetting Ice Cream Sandwich or earlier.
 * So, we force a a round trip with the android Soft keyboard (TODO: and we need to throw some kind of visible error if it isn't connected),
 * then we increment a keycounter, which is then decremented by RecordTextChangeListener.  The problem is when we send a key and RecordTextChangedListener
 * ISN'T fired, so we'll keep incrementing the key counter, and won't recognize programmatic setText() events.  So, we need to do something
 * (probably look at the current edit text in focus and check its OnTextChangedListener list) to ensure that the key counter is decremented correctly)
 * @author matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class IMEMessageListener implements Runnable {
	public static final String 		TAG = "IMEMessageListener";
	public static final int 		IME_BROADCAST_PORT = 49152;
	public static final int 		CONNECT_TIMEOUT = 10000;
	public static final int 		READ_TIMEOUT = 1000;
	public static final int 		MAXMSG = 1024;
	public static final String 		HIDE_IME = "hide_ime";							// messages sent from the custom soft keyboard
	public static final String 		SHOW_IME = "show_ime";
	public static final String 		SEND_KEY = "send_key";
	public static final String 		ACK = "ack";
	public static final String  	HIDE_IME_BACK_KEY = "hide_ime_back_key";
	public static int				sfOutstandingKeyCount;			// number of keys which have been read, but not processed.
	protected static boolean 		sfTerminate;					// terminate loop flag.
	protected boolean 				mfKeyboardVisible = false;		// flag for IME visibility to others may ask
	protected ActivityInterceptor	mActivityInterceptor;
	protected ViewInterceptor		mViewInterceptor;				// maintains currently focused view.
	protected EventRecorder			mEventRecorder;					// to record IME hide/show events.
	protected static boolean		sfKeyboardConnected;			// the keyboard connected via TCP, else warn user

	public IMEMessageListener(ViewInterceptor 		viewInterceptor, 
							  ActivityInterceptor 	activityInterceptor,
							  EventRecorder 		eventRecorder) {
		sfTerminate = false;
		mfKeyboardVisible = false;
		mViewInterceptor = viewInterceptor;
		mEventRecorder = eventRecorder;
		mActivityInterceptor = activityInterceptor;
		sfOutstandingKeyCount = 0;
	}
	
	public boolean isKeyboardVisible() {
		return mfKeyboardVisible;
	}
	
	/** 
	 * terminate this thread (called when the activity monitor stack is empty
	 */
	public static void terminate() {
		sfTerminate = true;
	}

	public static int getOutstandingKeyCount() {
		return sfOutstandingKeyCount;
	}
	
	public static void incrementOutstandingKeyCount() {
		sfOutstandingKeyCount++;
	}
	
	public static void decrementOutstandingKeyCount() {
		sfOutstandingKeyCount--;
	}
	
	public static boolean isKeyboardConnected() {
		return sfKeyboardConnected;
	}
	
	/**
	 * background thread to read the socket and see if the IME transmitted show or hide IME messages. 
	 */
	public void run() {
		try {
			byte[] buffer = new byte[MAXMSG];
			Socket socket = new Socket(InetAddress.getLocalHost(), IME_BROADCAST_PORT);
			SocketAddress sockAddr = new InetSocketAddress(InetAddress.getLocalHost(), IME_BROADCAST_PORT);
			socket.setSoTimeout(READ_TIMEOUT);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			sfKeyboardConnected = true;
			while (!sfTerminate) {
				boolean fSuccessfulRead = true;
				int numBytes = 0;
				try {
					numBytes = is.read(buffer);
				} catch (Exception ex) {
					fSuccessfulRead = false;
				}
				if (fSuccessfulRead && (numBytes > 0)) {
					String msg = new String(buffer);
					msg = msg.substring(0, numBytes);
					if (msg.equals(SHOW_IME)) {
						/* sometimes we get multiple show_ime messages from the keyboard: TODO: make sure that it only sends
						 * a show_ime message if it's transitioning from gone to visible.  (see SoftKeyboard.onShowInputRequested())
						 * this flag-tracking method in a different process is a certain way to have state management issues.
						 * the "handle back" in the IME code is called when the back key is pressed, whether the keyboard 
						 * is up or down.
						 */
						if (!mfKeyboardVisible) {
							View focusedView = mViewInterceptor.getFocusedView();
							mEventRecorder.writeRecord(Constants.EventTags.SHOW_IME, TestUtils.getViewActivity(focusedView).toString(),
													  focusedView, "IME displayed");
						}
						mfKeyboardVisible = true;
					} else if (msg.equals(HIDE_IME)) {
						if (mfKeyboardVisible) {	// see comment above
							View focusedView = mViewInterceptor.getFocusedView();
							mEventRecorder.writeRecord(Constants.EventTags.HIDE_IME, TestUtils.getViewActivity(focusedView).toString(),
													   focusedView, "IME hidden");
						}
						mfKeyboardVisible = false;
					} else if (msg.equals(HIDE_IME_BACK_KEY)) {
						if (mfKeyboardVisible) {
							View focusedView = mViewInterceptor.getFocusedView();
		                    mEventRecorder.writeRecord(Constants.EventTags.HIDE_IME_BACK_KEY, TestUtils.getViewActivity(focusedView).toString(),
		                    						   focusedView, "IME hidden by back key pressed");
		                    mViewInterceptor.setLastKeyAction(-1);
						}
						mfKeyboardVisible = false;
					} else if (msg.equals(SEND_KEY)) {
						incrementOutstandingKeyCount();
						Log.i(TAG, "received send_key");
						os.write(ACK.getBytes());
						// we need to send an acknowledgement to force a a round trip before android sends the key event
						// in order to prevent a race condition.
					}
				}	
			}	
		} catch (Exception ex) {
			Log.e(TAG, "an error occurred while trying to connect to the SoftKeyboard TCP listener " + ex.getMessage());
			ex.printStackTrace();
		}
	}

}
