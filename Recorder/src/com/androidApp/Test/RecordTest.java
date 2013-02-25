package com.androidApp.Test;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Listeners.RecordOnKeyListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

/**
 * record click and key events in an activity. In short, be awesome.
 * @author matreyno
 *
 * @param <T>
 */
public abstract class RecordTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {
	private static final String TAG = "RecordTest";
	private EventRecorder 		mRecorder;
	private ActivityMonitor		mActivityMonitor;					// track switches between activities	
	private Thread				mActivityThread;
	private Dialog				mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	private Activity			mStartActivity = null;
	private boolean				mFinished = false;
	
	// initialize the event recorder
	public void initRecorder() throws IOException {
		mRecorder = new EventRecorder("events.txt");
	}
	
	public RecordTest(Class<T> activityClass) throws IOException {
		super(activityClass);
		initRecorder();
	}
	
	public abstract void initializeResources();
	
	public void addRdotID(Object rdotid) {
		mRecorder.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mRecorder.addRdotString(rdotstring);
	}

	/**
	 * initialize the event recorder, the activity monitor, the stack of activities, and the background thread that populates
	 * that stack.  Use instrumentation to launch the activity
	 */
	public void setUp() throws NameNotFoundException, IOException, Exception { 
		super.setUp();	
		initRecorder();
		initializeResources();
		setupActivityMonitor();
		setupActivityStackListener();
		Instrumentation instrumentation = getInstrumentation();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), getActivity().getClass().getName());			// so we can get the package name to write in the manifest and classpath
		String packageName = getPackageName(getActivity());
		long time = SystemClock.uptimeMillis();
		mRecorder.writeRecord(Constants.EventTags.PACKAGE + "," + time + "," + packageName);
		instrumentation.startActivitySync(intent);
	}


	public void tearDown() throws Exception {
	}
		
	/**
	* This is were the activityMonitor is set up. The monitor will keep check
	* for the currently active activity.
	*
	*/

	private void setupActivityMonitor() {
		IntentFilter filter = null;
		mActivityMonitor = getInstrumentation().addMonitor(filter, null, false);
	}
	
	/**
	 * activity monitor hits always come in pairs.
	 * if the activities are the same (A, A), then it is going forward to that activity (A)
	 * if the activities are different (A, B) then it is going backwards from B to A 
	 */
	private void setupActivityStackListener() {
		Runnable runnable = new Runnable() {
			public void run() {
				if (RecordTest.this.mActivityMonitor != null) {
					boolean firstActivity = true;
					Activity activityA = null, activityB = null;
					while (true) {
						Activity activity = RecordTest.this.mActivityMonitor.waitForActivity();
						if (activity != null) {
							Dialog dialog = TestUtils.findDialog(activity);
							if ((dialog != null) && (dialog != mCurrentDialog)) {
								mRecorder.interceptDialog(dialog);
								mCurrentDialog = dialog;
							}
							if (firstActivity) {
								activityA = activity;
								firstActivity = false;
								if (mStartActivity == null) {
									mStartActivity = activityA;
								}
							} else {
								activityB = activity;
								firstActivity = true;
							}
							if ((activityA != null) && (activityB != null)) {
								long time = SystemClock.uptimeMillis();
								if (activityA != activityB) {
									String logMsg = Constants.EventTags.ACTIVITY_BACK + ":" + time + "," + activityB.getClass().getName() + "," + activityB.toString();
									mRecorder.writeRecord(logMsg);
									if (activityA == mStartActivity) {
										mFinished = true;
									}
								} else {
									// intercept events on the newly created activity.
									activityA.runOnUiThread(new InterceptRunnable(activityA));
									String logMsg = Constants.EventTags.ACTIVITY_FORWARD + ":" + time + "," + activityA.getClass().getName() + "," + activityA.toString();
									mRecorder.writeRecord(logMsg);
								}
								activityA = null;
								activityB = null;
							}
						}
					}
				}
			}
		};
		mActivityThread = new Thread(runnable, "activityMonitorThread");
		mActivityThread.start();
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
