package com.androidApp.Test;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupWindow;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Intercept.ViewInsertRecordWindowCallbackRunnable;
import com.androidApp.Intercept.InterceptKeyViewMenu;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Intercept.MagicFramePopup;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.randomtest.RandTest;

/**
 * record events in an activity. In short, be awesome.
 * @author matreyno
 *
 * @param <T> activity being subjected to recording
 * This uses a thread which waits on events from an activity monitor to track activity forward and back events.  
 * When we navigate forward to an activity, we add intercept listeners to the events on the view hierarchy.  The interceptRunnable
 * also sets up a view hierarchy (Layout listener) listener which re-traverses the view hierarchy and adds record listeners
 * for newly created views.
 * Since dialogs can be popped up at any time, and they aren't picked up by the layout listener, we had to create a timer task
 * which polls for newly created dialogs in the current activity.  Unfortunately, the event handlers are member functions of
 * activity, so we can't intercept them, except with methods that are highly intrusive.
 */
public abstract class RecordTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
	private static final String 			TAG = "RecordTest";
	private static final long				DIALOG_SYNC_TIME = 50;				// test for dialogs 20x second.
	private static final long				POPUP_WINDOW_SYNC_TIME = 50;		// test for popups 20x second.
	private static final long 				INTERCEPTOR_WAIT_MSEC = 1000;
	private EventRecorder 					mRecorder;
	private ViewInterceptor					mViewInterceptor;
	private Timer							mScanTimer = null;					// timer for scanning for new dialogs to set intercept handlers on.
	private ActivityInterceptor				mActivityInterceptor = null;
	private Class<? extends Activity> 		mStartActivityClass = null;			// workaround for hanging on setup
	private boolean							mfRunRandomTest = false;
	private int								mRandomTestIterations = 1000;
	private float							mBackKeyPercentage = 2.0f;				// 1 in 50 operations is the back key.
	// initialize the event recorder
	public void initRecorder() throws IOException {
		mRecorder = new EventRecorder("events.txt");
		mViewInterceptor = new ViewInterceptor(mRecorder);
	}
	
	public RecordTest(Class<T> activityClass) throws IOException {
		super(activityClass);
	}
	
	public abstract void initializeResources();
	
	// add the resource id references for id's and strings.
	public void addRdotID(Object rdotid) {
		getRecorder().addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		getRecorder().addRdotString(rdotstring);
	}
	
	public EventRecorder getRecorder() {
		return mRecorder;
	}
	
	public ViewInterceptor getViewInterceptor() {
		return mViewInterceptor;
	}
	
	public void setActivityClass(Class<? extends Activity> activityClass) {
		mStartActivityClass = activityClass;
	}
	
	public void setRunRandomTest(boolean f) {
		mfRunRandomTest = f;
	}
	
	public boolean getRunRandomTest() {
		return mfRunRandomTest;
	}
	
	public void setRandomTestIterations(int iterations) {
		mRandomTestIterations = iterations;
	}
	
	public int getRandomTestIterations() {
		return mRandomTestIterations;
	}
	
	public void setRandomTestBackKeyPercentage(float percentage) {
		mBackKeyPercentage = percentage;
	}
	
	public float getRandomTestBackKeyPercentage() {
		return mBackKeyPercentage;
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
	 * initialize the event recorder, the activity monitor, the stack of activities, and the background thread that populates
	 * that stack.  Use instrumentation to launch the activity
	 */
	public void setUp() throws NameNotFoundException, IOException, Exception { 
		super.setUp();	
		if (mStartActivityClass == null) {
			throw new Exception("the start activity class must be set before setUp is called");
		}
		initRecorder();
		mScanTimer = new Timer();
		mActivityInterceptor = new ActivityInterceptor(getRecorder(), getViewInterceptor());
		initializeResources();
		setupDialogListener();
		setupPopupWindowListener();
		Instrumentation instrumentation = getInstrumentation();
		mActivityInterceptor.setupActivityStackListener(instrumentation);
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
		intent.setClassName(instrumentation.getTargetContext(), mStartActivityClass.getName());			// so we can get the package name to write in the manifest and classpath
		//instrumentation.getTargetContext().startActivity(intent);
		instrumentation.startActivitySync(intent);
	
	}


	public void tearDown() throws Exception {
		Log.i(TAG, "tear down");
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
					Activity activity = RecordTest.this.getActivityInterceptor().getCurrentActivity();
					ViewInterceptor viewInterceptor = RecordTest.this.getViewInterceptor();
					EventRecorder recorder = RecordTest.this.getRecorder();
					Instrumentation instrumentation = RecordTest.this.getInstrumentation();
					if ((activity != null) && (viewInterceptor != null)) {
						Dialog dialog = TestUtils.findDialog(activity);
						if ((dialog != null) && (dialog != viewInterceptor.getCurrentDialog())) {
							instrumentation.runOnMainSync(new InterceptDialogRunnable(dialog, recorder, viewInterceptor));
							viewInterceptor.setCurrentDialog(dialog);
						}
					} else {
						if (RecordTest.this.getActivityInterceptor().hasStarted()) {
							this.cancel();
						}
						Log.i(TAG, "setupDialogListener activity = " + activity + " viewInterceptor = " + viewInterceptor);
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
	 */
	private void setupPopupWindowListener() {
		TimerTask scanTask = new TimerTask() {
			@Override
			public void run() {
				try {
					EventRecorder recorder = RecordTest.this.getRecorder();
					Activity activity = RecordTest.this.getActivityInterceptor().getCurrentActivity();
					ViewInterceptor viewInterceptor = RecordTest.this.getViewInterceptor();
					Instrumentation instrumentation = RecordTest.this.getInstrumentation();
					if ((recorder != null) && (activity !=  null) && (viewInterceptor != null)) {
						PopupWindow popupWindow = TestUtils.findPopupWindow(activity);
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
								recorder.writeRecord(Constants.EventTags.CREATE_POPUP_WINDOW, "create popup window");
								instrumentation.runOnMainSync(new InterceptPopupWindowRunnable(popupWindow));
							}
						}
					} else {
						if (activity == null) {
							if (RecordTest.this.getActivityInterceptor().hasStarted()) {
								this.cancel();
							}
						}
						Log.i(TAG, "popupWindowListener recorder = " + mRecorder + 
								   " activity = " + activity + " view interceptor = " + mViewInterceptor);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}	
		};
		mScanTimer.schedule(scanTask, 0, POPUP_WINDOW_SYNC_TIME);
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
			InterceptKeyViewMenu interceptKeyView = new InterceptKeyViewMenu(mExpandedMenuView.getContext(), RecordTest.this.getRecorder());
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
	 * get the package name for this activity.
	 * @param activity
	 * @return package name 
	 * @throws NameNotFoundException 
	 */
	private String getPackageName(Activity activity) throws NameNotFoundException {
        PackageManager pm = activity.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(activity.getPackageName(), 0);
        return packageInfo.packageName;
	}

	/**
	 * when the activity is added to the stack, walk through the view hierarchy and intercept the listeners for each view.
	 * @author matreyno
	 *
	 */
	public class InterceptDialogRunnable implements Runnable {
		protected Dialog 			mDialog;
		protected EventRecorder		mRecorder;
		protected ViewInterceptor	mViewInterceptor;
		
		public InterceptDialogRunnable(Dialog dialog, EventRecorder recorder, ViewInterceptor viewInterceptor) {
			mDialog = dialog;
			mRecorder = recorder;
			mViewInterceptor = viewInterceptor;
		}
		
		public void run() {
			Window window = mDialog.getWindow();
			View decorView = window.getDecorView();
			View contentView = ((ViewGroup) decorView).getChildAt(0);
			MagicFrame magicFrame = new MagicFrame(decorView.getContext(), contentView, 0, mRecorder, mViewInterceptor);
			mViewInterceptor.interceptDialog(mDialog);
		}
	}
	
	/**
	 * same, except for intercepting popup windows
	 */
	public class InterceptPopupWindowRunnable implements Runnable {
		protected PopupWindow mPopupWindow;
		
		public InterceptPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		
		public void run() {
			View contentView = mPopupWindow.getContentView();
			if (contentView != null) {
				MagicFramePopup magicFramePopup = new MagicFramePopup(contentView.getContext(), mPopupWindow, mRecorder, mViewInterceptor);
				RecordTest.this.getViewInterceptor().interceptPopupWindow(mPopupWindow);
			}
		}
	}

	/**
	 * keep looping until the activity interceptor has finished
	 */
	public void testRecord() {
		try {
			if (getRunRandomTest()) {
				RandTest randTest = new RandTest(this, mActivityInterceptor);
				randTest.randTest(getInstrumentation().getContext(), mRandomTestIterations, mBackKeyPercentage);
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
