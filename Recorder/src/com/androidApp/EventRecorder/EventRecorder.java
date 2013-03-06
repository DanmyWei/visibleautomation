package com.androidApp.EventRecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.Listeners.RecordDialogOnCancelListener;
import com.androidApp.Listeners.RecordDialogOnDismissListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnClickListener;
import com.androidApp.Listeners.RecordOnItemClickListener;
import com.androidApp.Listeners.RecordOnItemSelectedListener;
import com.androidApp.Listeners.RecordOnLongClickListener;
import com.androidApp.Listeners.RecordOnScrollListener;
import com.androidApp.Listeners.RecordOnTouchListener;
import com.androidApp.Listeners.RecordSeekBarChangeListener;
import com.androidApp.Listeners.RecordTextChangedListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.TestUtils;



import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * install the interception listeners in the view tree using some pretty cool reflection trickery.
 * @author matreyno
 *
 */
public class EventRecorder {
	protected BufferedWriter 	mRecordWriter;						// to write the events to file
	protected int 				mHashCode = 0x0;					// for fast tracking of view tree changes
	protected ViewReference		mViewReference;
	protected List<Spinner>		mSpinnerList;

	// constructor which opens the recording file, which is stashed somewhere on the sdcard.
	public EventRecorder(String recordFileName) throws IOException {				
		File extDir = Environment.getExternalStorageDirectory();
		File path = new File(extDir, recordFileName);
		path.delete();
		FileWriter fw = new FileWriter(path, true);
		mRecordWriter = new BufferedWriter(fw);
		mViewReference = new ViewReference();
		mSpinnerList = new ArrayList<Spinner>();
	}
	
	public void addRdotID(Object rdotid) {
		mViewReference.addRdotID(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mViewReference.addRdotID(rdotstring);
	}
	
	public ViewReference getViewReference() {
		return mViewReference;
	}
	
	public List<Spinner> getSpinnerList() {
		return mSpinnerList;
	}
	
	public void clearSpinnerList() {
		mSpinnerList.clear();
	}

	public void writeRecord(String s)  {
		try {
			mRecordWriter.write(s + "\n");
			mRecordWriter.flush();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	/**
	 * get the hashcode for the view tree.  We want to see if the view tree has changed, and unfortunately, I haven't 
	 * found an event saying that a window has been added or changed
	 * @param v
	 * @return
	 */
	static public int viewTreeHashCode(View v) {
		if (v instanceof ViewGroup) {
			int hashCode = 0x0;
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				hashCode ^= vChild.hashCode();
			}
			return hashCode;
		} else {
			return v.hashCode();
		}
	}
	public boolean hasDefinedMethod(View v, String methodName) {
		Class cls = v.getClass();
		if (cls.getName().startsWith(Constants.Packages.ANDROID_VIEW) || cls.getName().startsWith(Constants.Packages.ANDROID_WIDGET)) {
			return false;
		}
		Method[] methodList = cls.getMethods();
		for (Method method : methodList) {
			if (method.getName().equals(methodName)) {
				Class declaringClass = method.getDeclaringClass();
				if (declaringClass.getName().startsWith(Constants.Packages.ANDROID_VIEW) || declaringClass.getName().startsWith(Constants.Packages.ANDROID_WIDGET)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * replace the listeners in the view with wrapping listeners that record the events
	 * @param v
	 */
	public void replaceListeners(View v) {
		try {
			
			// adapterViews don't like click listeners, they like item click listeners.
			if (!(v instanceof AdapterView)) {
				// only replace the touch listener if one has been defined.
				// replace the touch listener, but make sure that we haven't replaced it already, because that would be re-entrant 
				AdapterView listView = TestUtils.getAdapterViewAncestor(v);
				if (listView == null) {
					View.OnClickListener originalClickListener = ListenerIntercept.getClickListener(v);
					if (!(originalClickListener instanceof RecordOnClickListener)) {
						RecordOnClickListener recordClickListener = new RecordOnClickListener(this, originalClickListener);
						v.setOnClickListener(recordClickListener);
					}
					View.OnLongClickListener originalLongClickListener = ListenerIntercept.getLongClickListener(v);
					if (!(originalLongClickListener instanceof RecordOnClickListener)) {
						RecordOnLongClickListener recordLongClickListener = new RecordOnLongClickListener(this, originalLongClickListener);
						v.setOnLongClickListener(recordLongClickListener);
					}
		
					View.OnTouchListener originalTouchListener = ListenerIntercept.getTouchListener(v);
					if ((originalTouchListener != null) && !(originalTouchListener instanceof RecordOnTouchListener)) {
						RecordOnTouchListener recordTouchListener = new RecordOnTouchListener(this, originalTouchListener);
						v.setOnTouchListener(recordTouchListener);
					}
				}
				if (v instanceof SeekBar) {
					SeekBar seekBar = (SeekBar) v;
					SeekBar.OnSeekBarChangeListener originalSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekBar);
					if ((originalSeekBarChangeListener != null) && !(originalSeekBarChangeListener instanceof RecordSeekBarChangeListener)) {
						RecordSeekBarChangeListener recordSeekbarChangeListener = new RecordSeekBarChangeListener(this, seekBar);
						seekBar.setOnSeekBarChangeListener(recordSeekbarChangeListener);
					}
				}
			} else {
				if (v instanceof AbsListView) {
					// TODO: need to support scrolling for objects which are NOT ListViews 
					AbsListView absListView = (AbsListView) v;
					AbsListView.OnScrollListener originalScrollListener = ListenerIntercept.getScrollListener(absListView);
					if (!(originalScrollListener instanceof RecordOnScrollListener)) {
						RecordOnScrollListener recordScrollListener = new RecordOnScrollListener(this, originalScrollListener);
						absListView.setOnScrollListener(recordScrollListener);
					}
					AdapterView.OnItemClickListener itemClickListener = absListView.getOnItemClickListener();
					if (!(itemClickListener instanceof RecordOnItemClickListener)) {
						RecordOnItemClickListener recordItemClickListener = new RecordOnItemClickListener(this, absListView);
						absListView.setOnItemClickListener(recordItemClickListener);		
					}
				}
				if (v instanceof Spinner) {
					Spinner spinner = (Spinner) v;
					AdapterView.OnItemSelectedListener originalSelectedItemListener = ListenerIntercept.getItemSelectedListener(spinner);
					if (!(originalSelectedItemListener instanceof RecordOnItemSelectedListener)) {
						RecordOnItemSelectedListener recordItemSelectedListener = new RecordOnItemSelectedListener(this, originalSelectedItemListener);
						spinner.setOnItemSelectedListener(recordItemSelectedListener);
						mSpinnerList.add(spinner);
					}
				}
			}
			if (v instanceof EditText) {
				TextView tv = (TextView) v;
				ArrayList<TextWatcher> textWatcherList = ListenerIntercept.getTextWatcherList((TextView) v);
				if (textWatcherList == null) {
					textWatcherList = new ArrayList<TextWatcher>();
				}
				// make sure that we haven't already added the intercepting text watcher.
				if (!ListenerIntercept.containsTextWatcher(textWatcherList, RecordTextChangedListener.class)) {
					textWatcherList.add(0, new RecordTextChangedListener(this, tv));
					ListenerIntercept.setTextWatcherList(tv, textWatcherList);
				}
			}
		} catch (Exception ex) {
			long time = SystemClock.uptimeMillis();
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				String logString = Constants.EventTags.EXCEPTION + ":" + time + "," + description;
				writeRecord(logString);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION + ":" + time + " unknown description");
			}
		}
	}
	
	/**
	 * recursively set the intercepting listeners for touch/key/textwatcher events through the view tree
	 * @param v view to recurse from.
	 */
	public void intercept(View v) {
		replaceListeners(v);
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
				View vChild = vg.getChildAt(iChild);
				intercept(vChild);
			}
		}
	}
	
	public void intercept(Activity a) {
        View v = a.getWindow().getDecorView().findViewById(android.R.id.content);
        mHashCode = EventRecorder.viewTreeHashCode(v);
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a));
        intercept(v);
	}
	
	/**
	 * when we receive a layout event, set up the record listeners on the view hierarchy.
	 */
	public class OnLayoutInterceptListener implements ViewTreeObserver.OnGlobalLayoutListener {
		protected Activity mActivity;
		
		public OnLayoutInterceptListener(Activity activity) {
			mActivity = activity;
		}
		public void onGlobalLayout() {
	        View v = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
			int hashCode = EventRecorder.viewTreeHashCode(v);
			if (hashCode != mHashCode) {
				intercept(v);
				mHashCode = hashCode;
			}
		}
	}

	/**
	 * intercept the listeners associated with a dialog.
	 * We pass in a spinner, because we want to change the cancel and dismiss events for its dialog popup, and we want to suppress
	 * click, scroll, touch events in the spinner, since it's all covered by onItemSelected()
	 * @param dialog
	 */
	public void interceptDialog(Dialog dialog, Spinner spinner) {
		try {
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(this, spinner, originalDismissListener);
				dialog.setOnDismissListener(recordOnDismissListener);
			}
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(this, spinner, originalCancelListener);
				dialog.setOnCancelListener(recordOnCancelListener);
			}
			// if the dialog was kicked off by a spinner, then Spinner.OnItemSelected() is the event we want, and we want to 
			// suppress click events.
			if (spinner != null) {
				Window window = dialog.getWindow();
				intercept(window.getDecorView());
			}
		} catch (Exception ex) {
			long time = SystemClock.uptimeMillis();
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				String logString = Constants.EventTags.EXCEPTION + ":" + time + "," + description;
				writeRecord(logString);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION + ":" + time + " unknown description");
			}
			ex.printStackTrace();
		}
	}
}
