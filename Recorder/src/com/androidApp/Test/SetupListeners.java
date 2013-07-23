package com.androidApp.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.EventRecorder.ReferenceException;
import com.androidApp.Intercept.IMEMessageListener;
import com.androidApp.Intercept.InterceptKeyViewMenu;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Intercept.MagicFramePopup;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;
import com.androidApp.Utility.WindowAndView;
import com.androidApp.randomtest.RandTest;

/**
 * class to install listeners in the view hierarchy
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class SetupListeners {
	private static final String 			TAG = "SetupListeners";
	private static final long				DIALOG_SYNC_TIME = 50;				// test for dialogs 20x second.
	private static final long				POPUP_WINDOW_SYNC_TIME = 50;		// test for popups 20x second.
	private static final long 				INTERCEPTOR_WAIT_MSEC = 1000;
	private EventRecorder 					mRecorder;
	private ViewInterceptor					mViewInterceptor;
	private Timer							mScanTimer = null;					// timer for scanning for new dialogs to set intercept handlers on.
	private ActivityInterceptor				mActivityInterceptor = null;
	private boolean							mfRunRandomTest = false;
	private int								mRandomTestIterations = 1000;
	private float							mBackKeyPercentage = 2.0f;			// 1 in 50 operations is the back key.
	private float							mRotationPercentage = 2.0f;			// 1 in 50 operations is device rotation.
	private float							mMenuPercentage = 10.0f;			// 1 in 50 operations is device rotation.
	protected Context						mContext;							// to communicate with the log service
	protected Instrumentation				mInstrumentation;
	protected String						mActivityName;						// TODO: probably should be the class of the activity under test
	protected IMEMessageListener			mIMEMessageListener;				// to listen to IME up/down events
	protected IRecordTest					mRecordTest;						// so we can pass info from the top-level recorder
	protected Class							mExpandedMenuViewClass;				// a pseudo-dialog used for ersatz overflow menus
	
	/** 
	 * constructor: copy the instrumentation reference, the start activity, and the recorder
	 * @param instrumentation instrumentation reference
	 * @param activityClass start activity under test
	 * @param recordTest test class
	 * @param fBinary dictates how view references are resolved to public classes
	 * @throws Exception
	 */
	public SetupListeners(Instrumentation 			instrumentation, 
						  Class<? extends Activity> activityClass,
						  IRecordTest				recordTest,
						  boolean					fBinary) throws Exception {
		mInstrumentation = instrumentation;
		mActivityName = activityClass.getCanonicalName();
		mRecordTest = recordTest;
		mExpandedMenuViewClass = Class.forName(Constants.Classes.EXPANDED_MENU_VIEW);
		setUp(activityClass, fBinary);
	}
	
	/**
	 * initialize the recorder
	 * @param context so event recorder can send requests to the log service
	 * @param fBinary dictates how view classes are resolved to public classes
	 * @throws IOException can't open events.txt
	 * @throws ReferenceException Reflection utilties exception
	 * @throws ClassNotFoundException
	 */
	public void initRecorder(Context context, boolean fBinary) throws IOException, ReferenceException, ClassNotFoundException {
		mContext = context;
		mRecorder = new EventRecorder(mInstrumentation, mContext, Constants.Files.EVENTS, Constants.Files.VIEW_DIRECTIVES, fBinary);
		mViewInterceptor = new ViewInterceptor(mRecorder, mRecordTest);
		mIMEMessageListener = new IMEMessageListener(mViewInterceptor, mRecorder);
		Thread thread = new Thread(mIMEMessageListener);
		thread.start();
	}
	
	/**
	 * Initializes the global timer, activity interceptor, dialog listener, and 
	 * popup window listener, then launches the activity
	 * @throws IOException
	 */
	public void setUp(Class<? extends Activity> activityClass, boolean fBinary) throws Exception {
		initRecorder(getInstrumentation().getContext(), fBinary);
		mScanTimer = new Timer();
		mActivityInterceptor = new ActivityInterceptor(getRecorder(), getViewInterceptor());
		setupDialogListener();
		setupPopupWindowListener();
		mActivityInterceptor.setupActivityStackListener(getInstrumentation());
		// need to make sure the activity monitor is set up before the first activity is fired.
		// need to throw some kind of error if the wait expires
		try {
			synchronized(mActivityInterceptor) {
				mActivityInterceptor.wait(INTERCEPTOR_WAIT_MSEC);
			}
		} catch (InterruptedException iex) {
		}
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(getInstrumentation().getTargetContext(), mActivityName);			// so we can get the package name to write in the manifest and classpath
		getInstrumentation().startActivitySync(intent);
	}

	/** 
	 * the unfortunate side-effect of using the blocking listener in the activity stack listener
	 * is that we can't intercept dialogs, like we could when we did a poll-timer based implementation
	 * This separate poll-timer thread exists merely to see if a dialog has appeared, and if so, set up
	 * the recording interceptors on it.
	 * TODO: pass the event recorder to InterceptorDialogRunnable
	 */
	private void setupDialogListener() {
		TimerTask scanTask = new TimerTask() {
			@Override
			public void run() {
				try {
					Activity activity = SetupListeners.this.getActivityInterceptor().getCurrentActivity();
					ViewInterceptor viewInterceptor = SetupListeners.this.getViewInterceptor();
					EventRecorder recorder = SetupListeners.this.getRecorder();
					Instrumentation instrumentation = SetupListeners.this.getInstrumentation();
					if ((activity != null) && (viewInterceptor != null)) {
						Dialog dialog = TestUtils.findDialog(activity);
						if ((dialog != null) && (dialog != viewInterceptor.getCurrentDialog())) {
							instrumentation.runOnMainSync(new InterceptDialogRunnable(activity, dialog, recorder, viewInterceptor));
							viewInterceptor.setCurrentDialog(dialog);
							// TODO: placeholder until I can put together a description for dialogs
							View contentView = TestUtils.getDialogContentView(dialog);
							Spinner spinner = TestUtils.isSpinnerDialog(dialog, activity);
							if (spinner != null) {
								recorder.writeRecord(Constants.EventTags.CREATE_SPINNER_POPUP_DIALOG, spinner, "create spinner popup dialog");
							} else {
								SetupListeners.this.mRecorder.writeRecord(Constants.EventTags.CREATE_DIALOG, "dialog");
							}
						}
					} else {
						if (SetupListeners.this.getActivityInterceptor().hasStarted() && !SetupListeners.this.getActivityInterceptor().mightBeWaitingForSplashScreen()) {
							Log.i(TAG, "setupDialogListener activity = " + activity + " viewInterceptor = " + viewInterceptor);
							this.cancel();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}	
		};
		mScanTimer.schedule(scanTask, 0, DIALOG_SYNC_TIME);
	}
	
	/**
	 * similar to listener for dialogs, except that it searches for popup windows used in context menus and from the
	 * actionbar dropdown menus.
	 * TODO: this needs to pick up on some event not just a polling timer.
	 */
	private void setupPopupWindowListener() {
		TimerTask scanTask = new TimerTask() {
			@Override
			public void run() {
				try {
					EventRecorder recorder = SetupListeners.this.getRecorder();
					Activity activity = SetupListeners.this.getActivityInterceptor().getCurrentActivity();
					ViewInterceptor viewInterceptor = SetupListeners.this.getViewInterceptor();
					Instrumentation instrumentation = SetupListeners.this.getInstrumentation();
					
					// scan for a new popup window, or the options menu
					if ((recorder != null) && (activity !=  null) && (viewInterceptor != null)) {
						WindowAndView windowAndView = TestUtils.findPopupWindow(activity);
						if (windowAndView != null) {
							if (windowAndView.mWindow instanceof PopupWindow) {
								PopupWindow popupWindow = (PopupWindow) windowAndView.mWindow;
								if ((popupWindow != null) && !TestUtils.isPopupWindowEmpty(popupWindow) && (popupWindow != viewInterceptor.getCurrentPopupWindow())) {
									viewInterceptor.setCurrentPopupWindow(popupWindow);
									if (TestUtils.isOptionsMenu(popupWindow)) {
										View optionsMenuView = TestUtils.findViewForPopup(activity, popupWindow);
										if (viewInterceptor.getLastKeyAction() == KeyEvent.KEYCODE_MENU) {
											recorder.writeRecord(Constants.EventTags.OPEN_ACTION_MENU_KEY, "open action menu from menu key");
										} else {
											recorder.writeRecord(Constants.EventTags.OPEN_ACTION_MENU, "open action menu");
										}
										instrumentation.runOnMainSync(new InsertKeyListenerRunnable(optionsMenuView));
									} else {
										// the popup window might have an anchor which changes the generated code, to spinner specific output
										View anchorView = TestUtils.getPopupWindowAnchor(popupWindow);
										if (TestUtils.isSpinnerPopup(popupWindow)) {
											recorder.writeRecord(Constants.EventTags.CREATE_SPINNER_POPUP_WINDOW, anchorView, "create spinner popup window");
											instrumentation.runOnMainSync(new InterceptSpinnerPopupWindowRunnable(popupWindow));
										} else if (TestUtils.isAutoCompleteWindow(popupWindow)) {
											recorder.writeRecord(Constants.EventTags.CREATE_AUTOCOMPLETE_DROPDOWN, anchorView, "create autocomplete dropdown");
											instrumentation.runOnMainSync(new InterceptAutoCompleteDropdownRunnable(activity, popupWindow));
										} else {
											recorder.writeRecord(Constants.EventTags.CREATE_POPUP_WINDOW, "create popup window");
											instrumentation.runOnMainSync(new InterceptPopupWindowRunnable(popupWindow));
										}		
									}
								}
							} else {
								if (windowAndView.mWindow != viewInterceptor.getCurrentFloatingWindow()) {
									if (windowAndView.mView instanceof ViewGroup) {
										ViewGroup vg = (ViewGroup) windowAndView.mView;
										if (vg.getChildCount() > 0) {
											View vChild = vg.getChildAt(0);
											
											// rather than an extension submenu, sometimes it comes up as another
											viewInterceptor.setCurrentFloatingWindow(windowAndView.mWindow);
											recorder.writeRecord(Constants.EventTags.CREATE_FLOATING_WINDOW, "create floating window");
											instrumentation.runOnMainSync(new InterceptFloatingWindowRunnable(activity, vChild, windowAndView.mWindow));
										}
									}
								}
							}
						}
					} 
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}	
		};
		mScanTimer.schedule(scanTask, 0, POPUP_WINDOW_SYNC_TIME);
	}
	
	/**
	 * spinners can have popups (which are actually dropdowns), or dialog windows depending on the mode: MODE_DIALOG or mode: MODE_POPUP
	 * @param popupWindow
	 * @return
	 */
	public boolean isSpinnerPopup(PopupWindow popupWindow) throws NoSuchFieldException, IllegalAccessException {
		View anchorView = TestUtils.getPopupWindowAnchor(popupWindow);
		return (anchorView instanceof Spinner);
	}
	
	public Spinner isSpinnerDialog(Dialog dialog, Activity activity)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		List<Spinner> spinnerList = ViewExtractor.getActivityViews(activity, Spinner.class);
		for (Spinner spinner : spinnerList) {
			if (isPopupDialogForSpinner(dialog, spinner)) {
				return spinner;
			}
		}
		return null;
	}
	
	/**
	 * for a given spinner, see if this dialog is the spinner's popup dialog
	 * @param dialog dialog
	 * @param spinner candidate spinner
	 * @return true if it belongs, false if it does not
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public boolean isPopupDialogForSpinner(Dialog dialog, Spinner spinner) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Object spinnerPopup = ReflectionUtils.getFieldValue(spinner, Spinner.class, Constants.Fields.POPUP);
		if (spinnerPopup != null) {
			Class spinnerDialogPopupClass = Class.forName(Constants.Classes.SPINNER_DIALOG_POPUP);
			if (spinnerDialogPopupClass.equals(spinnerPopup.getClass())) {
				Object spinnerPopupPopup = ReflectionUtils.getFieldValue(spinnerPopup, spinnerDialogPopupClass, Constants.Fields.POPUP);
				return spinnerPopupPopup == dialog;
			}
		}
		return false;
	}
	
	/**
	 * for the options menu, we need to intercept the menu and back keys, so we insert a fake window
	 * which listens for dispatchKey and preIME key events.
	 *
	 */
	protected class InsertKeyListenerRunnable implements Runnable {
		protected View mExpandedMenuView;
		public InsertKeyListenerRunnable(View v) {
			mExpandedMenuView = v;
		}
		@Override
		public void run() {
			InterceptKeyViewMenu interceptKeyView = new InterceptKeyViewMenu(mExpandedMenuView.getContext(), 
																			 SetupListeners.this.getRecorder(),
																			 SetupListeners.this.getViewInterceptor());
			((ViewGroup) mExpandedMenuView).addView(interceptKeyView);
			interceptKeyView.requestFocus();	
		}
	}
	/**
	 * shutdown the dialog scan timertask once the activity stack has been emptied
	 */
	public void shutdownScanTimer() {
		mScanTimer.cancel();
	}			

	/**
	 * when the activity is added to the stack, walk through the view hierarchy and intercept the listeners for each view.
	 * @author matthew
	 *
	 */
	public class InterceptDialogRunnable implements Runnable {
		protected Dialog 			mDialog;
		protected EventRecorder		mRecorder;
		protected ViewInterceptor	mViewInterceptor;
		protected Activity			mActivity;
		
		public InterceptDialogRunnable(Activity activity, Dialog dialog, EventRecorder recorder, ViewInterceptor viewInterceptor) {
			mDialog = dialog;
			mRecorder = recorder;
			mViewInterceptor = viewInterceptor;
			mActivity = activity;
		}
		
		public void run() {
			View contentView = TestUtils.getDialogContentView(mDialog);
			MagicFrame magicFrame = new MagicFrame(contentView.getContext(), contentView, 0, mRecorder, mViewInterceptor);
			try {
				// spinner dialogs have their own dismiss.
				if (TestUtils.isSpinnerDialog(contentView)) {
					mViewInterceptor.interceptSpinnerDialog(mDialog);
				} else {
					mViewInterceptor.interceptDialog(mActivity, mDialog);
				}
			} catch (Exception ex) {
				mRecorder.writeException(ex, "Intercepting dialog");
			}
		}
	}
	
	/**
	 * same, except for intercepting popup windows
	 */
	protected class InterceptPopupWindowRunnable implements Runnable {
		protected PopupWindow mPopupWindow;
		
		public InterceptPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		
		public void run() {
			View contentView = mPopupWindow.getContentView();
			if (contentView != null) {
				SetupListeners.this.getViewInterceptor().interceptPopupWindow(SetupListeners.this.mRecorder, mPopupWindow);
			}
		}
	}
	
	/**
	 * List InterceptPopupWindow, except that people have rolled their own windows
	 * @author matt2
	 *
	 */
	public class InterceptFloatingWindowRunnable implements Runnable {
		protected View mView;
		protected Activity mActivity;
		protected Object mFloatingWindow;
		
		public InterceptFloatingWindowRunnable(Activity activity, View v, Object floatingWindow) {
			mView = v;
			mActivity = activity;
			mFloatingWindow = floatingWindow;
		}
		
		public void run() {
			try {
				SetupListeners.this.getViewInterceptor().intercept(mActivity, mView);
				/*
				 * this shit ain't happenin'
				 */
				//FieldUtils.listFieldsDebug(mFloatingWindow.getClass());
				//SetupListeners.this.getViewInterceptor().interceptFloatingWindow(SetupListeners.this.getRecorder(), mFloatingWindow);
			} catch (Exception ex) {
				SetupListeners.this.mRecorder.writeException(ex, "while trying to intercept view");
			}
		}
	}


	/**
	 * same, except for intercepting "popup" windows
	 */
	protected class InterceptWindowRunnable implements Runnable {
		protected Window mWindow;
		
		public InterceptWindowRunnable(Window window) {
			mWindow = window;
		}
		
		public void run() {
			ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
			ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);		
			if (contentView != null && !(contentView instanceof MagicFrame)) {
				MagicFrame magic = new MagicFrame(contentView.getContext(), contentView, 0, mRecorder, mViewInterceptor);
				SetupListeners.this.getViewInterceptor().intercept((Activity) contentView.getContext(), contentView);
			}
		}
	}
	
	/**
	 * special class for spinner popup windows
	 * @author matt2
	 *
	 */
	protected class InterceptSpinnerPopupWindowRunnable implements Runnable {
		protected PopupWindow mPopupWindow;
		
		public InterceptSpinnerPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		public void run() {
			View contentView = mPopupWindow.getContentView();
			if (contentView != null && !(contentView instanceof MagicFrame)) {
				//MagicFramePopup magicFramePopup = new MagicFramePopup(contentView.getContext(), mPopupWindow, mRecorder, mViewInterceptor);
				SetupListeners.this.getViewInterceptor().interceptSpinnerPopupWindow(mPopupWindow);
			}
		}
	}
	
	/**
	 * special class for spinner popup windows
	 * @author matt2
	 *
	 */
	protected class InterceptAutoCompleteDropdownRunnable implements Runnable {
		protected PopupWindow mPopupWindow;
		protected Activity mActivity;
		
		public InterceptAutoCompleteDropdownRunnable(Activity activity, PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
			mActivity = activity;
		}
		
		public void run() {
			View contentView = mPopupWindow.getContentView();
			if (contentView != null && !(contentView instanceof MagicFrame)) {
				SetupListeners.this.getViewInterceptor().interceptAutocompleteDropdown(mActivity, mPopupWindow);
			}
		}
	}
	
	/**
	 * return a handle to the event recorder, which interfaes to the logging service
	 * @return
	 */
	public EventRecorder getRecorder() {
		return mRecorder;
	}
	
	/**
	 * return a handle to the view interceptor, which sets up the recording listeners for the views.
	 * @return
	 */
	public ViewInterceptor getViewInterceptor() {
		return mViewInterceptor;
	}
	
	/**
	 * run a random test, rather than recording
	 * @param f true if we want to run a randomized test
	 */
	public void setRunRandomTest(boolean f) {
		mfRunRandomTest = f;
	}
	
	/**
	 * return the randomized test flag
	 * @return
	 */
	public boolean getRunRandomTest() {
		return mfRunRandomTest;
	}
	
	/**
	 * set the number of iterations for the random test
	 * @param iterations
	 */
	public void setRandomTestIterations(int iterations) {
		mRandomTestIterations = iterations;
	}
	
	/**
	 * return the number of iterations in the random test
	 * @return
	 */
	public int getRandomTestIterations() {
		return mRandomTestIterations;
	}
	
	/**
	 * how often should the back key be hit in an activity during the randomized test?
	 * @param percentage
	 */
	public void setRandomTestBackKeyPercentage(float percentage) {
		mBackKeyPercentage = percentage;
	}
	
	public float getRandomTestBackKeyPercentage() {
		return mBackKeyPercentage;
	}
	
	/**
	 * how often should the device be rotated during the random test?
	 * @param percentage
	 */
	public void setRandomTestRotationPercentage(float percentage) {
		mRotationPercentage = percentage;
	}
	
	public float getRandomTestRotationPercentange() {
		return mRotationPercentage;
	}
	
	/**
	 * how often should the option menu be selected during the randomized test?
	 * @param percentage
	 */
	public void setOptionMenuPercentage(float percentage) {
		mMenuPercentage = percentage;
	}
	
	public float getOptionMenuPercentage() {
		return mMenuPercentage;
	}
	
	// enable visual debugging
	public void setVisualDebug(boolean f) {
		getRecorder().setVisualDebug(f);
	}
	
	/**
	 * return the reference to the activity interceptor
	 * @return activity interceptor reference
	 */
	public ActivityInterceptor getActivityInterceptor() {
		return mActivityInterceptor;
	}
	
	/**
	 * return the instrumentation handle
	 * @return
	 */
	public Instrumentation getInstrumentation() {
		return mInstrumentation;
	}
	/**
	 * keep looping until the activity interceptor has finished
	 */
	public void testRecord() {
		try {
			if (getRunRandomTest()) {
				RandTest randTest = new RandTest(this.getInstrumentation(), mActivityInterceptor);
				randTest.randTest(getInstrumentation().getContext(), mRandomTestIterations, mBackKeyPercentage, mRotationPercentage, mMenuPercentage);
			}
			do {
				Thread.sleep(100);
			} while (!mActivityInterceptor.getFinished());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Log.i("foo", "foo");
	}
}
