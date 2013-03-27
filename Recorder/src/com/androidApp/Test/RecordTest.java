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
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.TestUtils;

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
	private static final String 	TAG = "RecordTest";
	private static final long		DIALOG_SYNC_TIME = 50;				// test for dialogs 20x second.
	private static final long		POPUP_WINDOW_SYNC_TIME = 50;		// test for popups 20x second.
	private static final long 		INTERCEPTOR_WAIT_MSEC = 1000;
	private EventRecorder 			mRecorder;
	private ViewInterceptor			mViewInterceptor;
	private Dialog					mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	private PopupWindow				mCurrentPopupWindow = null;			// current popup window, which is like the current dialog, but different
	private boolean					mFinished = false;					// have the loops finished?
	private Timer					mScanTimer = null;					// timer for scanning for new dialogs to set intercept handlers on.
	private ActivityInterceptor		mActivityInterceptor = null;
	Stack<WeakReference<Activity>> 	mActivityStack = new Stack<WeakReference<Activity>>();
	
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
		initRecorder();
		mScanTimer = new Timer();
		mActivityInterceptor = new ActivityInterceptor(getRecorder(), getViewInterceptor());
		initializeResources();
		setupDialogListener();
		setupPopupWindowListener();
		Instrumentation instrumentation = getInstrumentation();
		mActivityInterceptor.setupActivityStackListener(instrumentation);
		// need to make sure the activity monitor is set up before the first activity is fired.
		try {
			synchronized(mActivityInterceptor) {
				mActivityInterceptor.wait(INTERCEPTOR_WAIT_MSEC);
			}
		} catch (InterruptedException iex) {
		}
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), getActivity().getClass().getName());			// so we can get the package name to write in the manifest and classpath
		String packageName = getPackageName(getActivity());
		getRecorder().writeRecord(Constants.EventTags.PACKAGE, packageName);
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
	 */
	private void setupDialogListener() {
		TimerTask scanTask = new TimerTask() {
			@Override
			public void run() {
				try {
					Activity activity = RecordTest.this.getActivityInterceptor().getCurrentActivity();
					if (activity != null) {
						Dialog dialog = TestUtils.findDialog(activity);
						if ((dialog != null) && (dialog != RecordTest.this.getCurrentDialog())) {
							activity.runOnUiThread(new InterceptDialogRunnable(dialog));
							RecordTest.this.setCurrentDialog(dialog);
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
	 * similar to listener for dialogs, except that it searches for popup windows.
	 */
	private void setupPopupWindowListener() {
		TimerTask scanTask = new TimerTask() {
			@Override
			public void run() {
				try {
					Activity activity = RecordTest.this.getActivityInterceptor().getCurrentActivity();
					if (activity !=  null) {
						PopupWindow popupWindow = TestUtils.findPopupWindow(activity);
						if ((popupWindow != null) && (popupWindow != RecordTest.this.getCurrentPopupWindow())) {
							RecordTest.this.getRecorder().writeRecord(Constants.EventTags.CREATE_POPUP_WINDOW, "create popup window");
							activity.runOnUiThread(new InterceptPopupWindowRunnable(popupWindow));
							if (popupWindow != null) {
								RecordTest.this.setCurrentPopupWindow(popupWindow);
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
	 * shutdown the dialog scan timertask once the activity stack has been emptied
	 */
	public void shutdownScanTimer() {
		mScanTimer.cancel();
	}
	
	/**
	 * set the current dialog
	 * @param dialog
	 */
	public void setCurrentDialog(Dialog dialog) {
		mCurrentDialog = dialog;
	}
	
	/**
	 * get the current dialog
	 * @return dialog
	 */
	public Dialog getCurrentDialog() {
		return mCurrentDialog;
	}
	
	/**
	 * get the current popup window
	 * @return popup window
	 */
	public PopupWindow getCurrentPopupWindow() {
		return mCurrentPopupWindow;
	}
	
	/**
	 * set the current popup window
	 * @param popupWindow
	 */
	public void setCurrentPopupWindow(PopupWindow popupWindow) {
		mCurrentPopupWindow = popupWindow;
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
		protected Dialog mDialog;
		
		public InterceptDialogRunnable(Dialog dialog) {
			mDialog = dialog;
		}
		
		public void run() {
			RecordTest.this.getViewInterceptor().interceptDialog(mDialog);
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
			RecordTest.this.getViewInterceptor().interceptPopupWindow(mPopupWindow);
			RecordTest.this.getViewInterceptor().intercept(mPopupWindow.getContentView());
		}
	}

	public void testRecord() {

		try {
			do {
				Thread.sleep(100);
			} while (!mFinished);
		} catch (Exception ex) {
		}
		Log.i("foo", "foo");
	}
}
