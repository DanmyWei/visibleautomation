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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
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
	private EventRecorder 			mRecorder;
	private Thread					mActivityThread;					// to track the activity monitor thread
	private Dialog					mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	private PopupWindow				mCurrentPopupWindow = null;			// current popup window, which is like the current dialog, but different
	private boolean					mFinished = false;					// have the loops finished?
	private Timer					mScanTimer = null;					// timer for scanning for new dialogs to set intercept handlers on.
	private ActivityMonitor 		mActivityMonitor = null;			
	Stack<WeakReference<Activity>> 	mActivityStack = new Stack<WeakReference<Activity>>();
	
	// initialize the event recorder
	public void initRecorder() throws IOException {
		mRecorder = new EventRecorder("events.txt");
	}
	
	public RecordTest(Class<T> activityClass) throws IOException {
		super(activityClass);
		initRecorder();
		mScanTimer = new Timer();
	}
	
	public abstract void initializeResources();
	
	// add the resource id references for id's and strings.
	public void addRdotID(Object rdotid) {
		mRecorder.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mRecorder.addRdotString(rdotstring);
	}
	
	public EventRecorder getRecorder() {
		return mRecorder;
	}

	/**
	 * initialize the event recorder, the activity monitor, the stack of activities, and the background thread that populates
	 * that stack.  Use instrumentation to launch the activity
	 */
	public void setUp() throws NameNotFoundException, IOException, Exception { 
		super.setUp();	
		initRecorder();
		initializeResources();
		setupActivityStackListener();
		setupDialogListener();
		setupPopupWindowListener();
		Instrumentation instrumentation = getInstrumentation();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), getActivity().getClass().getName());			// so we can get the package name to write in the manifest and classpath
		String packageName = getPackageName(getActivity());
		RecordTest.this.getRecorder().writeRecord(Constants.EventTags.PACKAGE, packageName);
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
					Activity activity = RecordTest.this.getCurrentActivity();
					if (activity != null) {
						Dialog dialog = TestUtils.findDialog(activity);
						if ((dialog != null) && (dialog != RecordTest.this.getCurrentDialog())) {
							RecordTest.this.getRecorder().interceptDialog(dialog);
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
					Activity activity = RecordTest.this.getCurrentActivity();
					if (activity !=  null) {
						PopupWindow popupWindow = TestUtils.findPopupWindow(activity);
						if ((popupWindow != null) && (popupWindow != RecordTest.this.getCurrentPopupWindow())) {
							RecordTest.this.getRecorder().writeRecord(Constants.EventTags.CREATE_POPUP_WINDOW, "create popup window");
							RecordTest.this.getRecorder().interceptPopupWindow(popupWindow);
							RecordTest.this.getRecorder().intercept(popupWindow.getContentView());
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
	 * using reflection, get the popup dialog associated with this spinner.
	 * basically spinner.mPopup.mPopup
	 * @param spinner spinner to extract popup from.
	 * @return Dialog, or null if there's no popup
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public static Dialog getSpinnerPopup(Spinner spinner) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		Object spinnerDialogPopup = FieldUtils.getFieldValue(spinner, Spinner.class, Constants.Fields.POPUP);
		Class spinnerDialogPopupClass = Class.forName(Constants.Classes.SPINNER_DIALOG_POPUP);
		Dialog dialog = (Dialog) FieldUtils.getFieldValue(spinnerDialogPopup, spinnerDialogPopupClass, Constants.Fields.POPUP);
		return dialog;
	}
	
	public static PopupWindow getAutoCompletePopup(AutoCompleteTextView textView) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		Object autoCompletePopup = FieldUtils.getFieldValue(textView, AutoCompleteTextView.class, Constants.Fields.POPUP);
		Class listWindowPopupClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP);
		PopupWindow popupWindow = (PopupWindow) FieldUtils.getFieldValue(autoCompletePopup, listWindowPopupClass, Constants.Fields.POPUP);
		return popupWindow;
	}
	
	/**
	 * given the list of spinners, see if the dialog is the popup for one of them
	 * @param spinnerList list of spinners assocated with this activity.
	 * @param dialog currently displayed dialog
	 * @return Spinner or null.
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public static Spinner matchSpinnerDialog(List<Spinner> spinnerList, Dialog dialog) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		for (Spinner spinner : spinnerList) {
			Dialog spinnerPopup = RecordTest.getSpinnerPopup(spinner);
			Window spinnerPopupWindow = spinnerPopup.getWindow();
			Window dialogWindow = dialog.getWindow();
		
			// TODO: de dumb-ass this.
			if (spinnerPopupWindow == dialogWindow) {
				return spinner;
			}
			if (spinnerPopupWindow.equals(dialogWindow)) {
				return spinner;
			}
		}
		return null;
	}
	
	public static AutoCompleteTextView matchAutoCompleteDialog(List<AutoCompleteTextView> autoCompleteTextViewList, PopupWindow popupWindow) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		for (AutoCompleteTextView textView : autoCompleteTextViewList) {
			PopupWindow autoCompletePopupWindow = RecordTest.getAutoCompletePopup(textView);
		
			// TODO: de dumb-ass this.
			if (autoCompletePopupWindow == popupWindow) {
				return textView;
			}
			if (autoCompletePopupWindow.equals(popupWindow)) {
				return textView;
			}
		}
		return null;
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
	 * activity monitor hits always come in pairs.
	 * if the activities are the same (A, A), then it is going forward to that activity (A)
	 * if the activities are different (A, B) then it is going backwards from B to A 
	 */
	private void setupActivityStackListener() {
		IntentFilter filter = null;
		
		// need to initialize this before the runnable, so we don't miss the first activity
		mActivityMonitor = getInstrumentation().addMonitor(filter, null, false);
		mActivityStack = new Stack<WeakReference<Activity>>();
		Runnable runnable = new Runnable() {
			public void run() {
				boolean fStart = true;
				while (fStart || !RecordTest.this.isActivityStackEmpty()) {
					Activity activityA = RecordTest.this.mActivityMonitor.waitForActivity();
					if (fStart) {
						RecordTest.this.pushActivityOnStack(activityA);
						// intercept events on the newly created activity.
						activityA.runOnUiThread(new InterceptRunnable(activityA));
						String logMsg = activityA.getClass().getName() + "," + activityA.toString();
						RecordTest.this.getRecorder().writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
						fStart = false;
					} else {
						Activity activityB = RecordTest.this.mActivityMonitor.waitForActivity();
						if (RecordTest.this.inActivityStack(activityB)) {
							Activity previousActivity = RecordTest.this.popActivityFromStack();
							previousActivity = RecordTest.this.peekActivityOnStack();
							if (previousActivity != null) {
								String logMsg = previousActivity.getClass().getName() + "," + previousActivity.toString();
								RecordTest.this.getRecorder().writeRecord(Constants.EventTags.ACTIVITY_BACK, logMsg);
							} else {
								long time = SystemClock.uptimeMillis();
								String logMsg = Constants.EventTags.ACTIVITY_BACK + ":" + time;
								RecordTest.this.getRecorder().writeRecord(logMsg);
							} 
						} else {
							RecordTest.this.pushActivityOnStack(activityB);
							// intercept events on the newly created activity.
							activityB.runOnUiThread(new InterceptRunnable(activityB));
							String logMsg =  activityB.getClass().getName() + "," + activityB.toString();
							RecordTest.this.getRecorder().writeRecord(Constants.EventTags.ACTIVITY_FORWARD, logMsg);
						}
					}
				}
				Log.i(TAG, "activity loop ended finished = " + RecordTest.this.mFinished);
				RecordTest.this.shutdownScanTimer();
			}
		};
		mActivityThread = new Thread(runnable, "activityMonitorThread");
		mActivityThread.start();
	}
	
	// activity stack methods
	
	/**
	 * push the activity onto the stack of WeakReferences. We use weak reference so we don't actually hold onto the activity
	 * after it's been finished.
	 * @param activity activity to add to the stack
	 */
	public void pushActivityOnStack(Activity activity) {
		WeakReference<Activity> ref = new WeakReference<Activity>(activity);
		mActivityStack.push(ref);
	}
	
	/**
	 * pop the activity stack, and return the activity that was referenced by the top WeakReference
	 * @return activity from the top of the stack
	 */
	public Activity popActivityFromStack() {
		WeakReference<Activity> ref = mActivityStack.pop();
		return ref.get();
	}
	
	/**
	 * return the activity that's on the top of the stack
	 * @return top activity
	 */
	public Activity peekActivityOnStack() {
		if (mActivityStack.isEmpty()) {
			return null;
		} else {
			WeakReference<Activity> ref = mActivityStack.peek();
			return ref.get();
		}
	}
	
	/***
	 * scan the activity stack to determine if an activity is in it.  
	 * @param activity activity to search for
	 * @return true if the activity is found
	 */
	public boolean inActivityStack(Activity activity) {
		for (WeakReference<Activity> ref : mActivityStack) {
			if (ref.get() == activity) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * is the activity stack empty?
	 * @return
	 */
	public boolean isActivityStackEmpty() {
		return mActivityStack.isEmpty();
	}
	
	/**
	 * get the current activity returned from the activity monitor
	 * @return current activity
	 */
	public Activity getCurrentActivity() {
		if (!mActivityStack.isEmpty()) {
			WeakReference<Activity> ref = mActivityStack.peek();
			return ref.get();
		} else {
			return null;
		}
	}
	
	/**
	 * get the package name for this activity.
	 * @param activity
	 * @return
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
	public class InterceptRunnable implements Runnable {
		protected Activity mActivity;
		
		public InterceptRunnable(Activity activity) {
			mActivity = activity;
		}
		
		public void run() {
			RecordTest.this.mRecorder.intercept(mActivity);
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
