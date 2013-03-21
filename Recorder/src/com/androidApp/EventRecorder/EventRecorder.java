package com.androidApp.EventRecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.androidApp.Listeners.FinishTextChangedListener;
import com.androidApp.Listeners.RecordDialogOnCancelListener;
import com.androidApp.Listeners.RecordDialogOnDismissListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnClickListener;
import com.androidApp.Listeners.RecordOnFocusChangeListener;
import com.androidApp.Listeners.RecordOnItemClickListener;
import com.androidApp.Listeners.RecordOnItemSelectedListener;
import com.androidApp.Listeners.RecordOnLongClickListener;
import com.androidApp.Listeners.RecordOnScrollListener;
import com.androidApp.Listeners.RecordOnTouchListener;
import com.androidApp.Listeners.RecordPopupWindowOnDismissListener;
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
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * install the interception listeners in the view tree using some pretty cool reflection trickery.
 * @author matreyno
 *
 */
public class EventRecorder {
	public static final float				GUESS_IME_HEIGHT = 0.25F;			// guess that IME takes up this amount of the screen.
	protected BufferedWriter 				mRecordWriter;						// to write the events to file
	protected int 							mHashCode = 0x0;					// for fast tracking of view tree changes
	protected ViewReference					mViewReference;
	protected View							mViewFocus;							// view OnFocusChanged.onFocusChange() hasFocus = true for IME detection
	protected boolean						mIMEWasDisplayed = false;			// IME was displayed in the last layout
	
	// constructor which opens the recording file, which is stashed somewhere on the sdcard.
	public EventRecorder(String recordFileName) throws IOException {				
		File extDir = Environment.getExternalStorageDirectory();
		File path = new File(extDir, recordFileName);
		path.delete();
		FileWriter fw = new FileWriter(path, true);
		mRecordWriter = new BufferedWriter(fw);
		mViewReference = new ViewReference();
		mViewFocus = null;
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
	
	// accessors/mutator for focused view for IME display/remove event
	public View getFocusedView() {
		return mViewFocus;
	}
	
	public void setFocusedView(View v) {
		mViewFocus = v;
	}

	// write a record to the output
	public void writeRecord(String s)  {
		try {
			mRecordWriter.write(s + "\n");
			mRecordWriter.flush();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	// wrapper to write a record with an event, time and message to the system	
	public void writeRecord(String event, String message) {
		long time = SystemClock.uptimeMillis();
		writeRecord(event + ":" + time + "," + message);
	}
	
	// wrapper for wrapper to write a record with an event, time view description, and message to the system	
	public void writeRecord(String event, View v, String message) {
		long time = SystemClock.uptimeMillis();
		try {
			writeRecord(event + ":" + time + "," + getViewReference().getReference(v) + "," + message);
		} catch (Exception ex) {
			writeRecord(Constants.EventTags.EXCEPTION, "while getting reference for view in event " + event + " " + message);
		}
	}
	
	// yet another wrapper with just a view to be described.
	public void writeRecord(String event, View v) {
		long time = SystemClock.uptimeMillis();
		try {
			writeRecord(event + ":" + time + "," + getViewReference().getReference(v));
		} catch (Exception ex) {
			writeRecord(Constants.EventTags.EXCEPTION, "while getting reference for view in event " + event);
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
				
				// specific handlers for seekbars/progress bars/etc.
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
					// special case for spinners, which receive onItemSelected, but not onItemClick events.
					 if (v instanceof Spinner) {
						AdapterView.OnItemSelectedListener originalSelectedItemListener = ListenerIntercept.getItemSelectedListener(absListView);
						if (!(originalSelectedItemListener instanceof RecordOnItemSelectedListener)) {
							RecordOnItemSelectedListener recordItemSelectedListener = new RecordOnItemSelectedListener(this, originalSelectedItemListener);
							absListView.setOnItemSelectedListener(recordItemSelectedListener);
						}
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
					textWatcherList.add(new FinishTextChangedListener(this));
					ListenerIntercept.setTextWatcherList(tv, textWatcherList);
				}
				// add listener for focus
				View.OnFocusChangeListener originalFocusChangeListener = tv.getOnFocusChangeListener();
				if (!(originalFocusChangeListener instanceof RecordOnFocusChangeListener)) {
					View.OnFocusChangeListener recordFocusChangeListener = new RecordOnFocusChangeListener(this, originalFocusChangeListener);
					tv.setOnFocusChangeListener(recordFocusChangeListener);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				writeRecord(Constants.EventTags.EXCEPTION, description);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION + " unknown description");
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
	        View contentView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
	        if ((getFocusedView() != null) && isIMEDisplayed(contentView)) {
	        	mIMEWasDisplayed = true;
				writeRecord(Constants.EventTags.SHOW_IME, getFocusedView(), "IME displayed");
	        } else if (mIMEWasDisplayed && !isIMEDisplayed(contentView)) {
				writeRecord(Constants.EventTags.HIDE_IME, getFocusedView(), "IME hidden");
	        }
			int hashCode = EventRecorder.viewTreeHashCode(contentView);
			if (hashCode != mHashCode) {
				intercept(contentView);
				mHashCode = hashCode;
			}
		}
	}
	
	/**
	 * unfortunately, there isn't an event for IME display/hide, so we have to guess by examining the content view
	 * and seeing if it's enough smaller than its parent that the IME could probably fit in there.  This is called
	 * in the global layout listener, so we at least know that the layout has changed, but not why.
	 * @param contentView android.R.id.content from the activity's window.
	 * @return true if the IME is probably up, false if it's certainly down.
	 */
	public static boolean isIMEDisplayed(View contentView) {
		View contentParent = (View) contentView.getParent();
		int imeHeight = contentParent.getMeasuredHeight() - contentView.getMeasuredHeight();
		return imeHeight > ((int) (contentParent.getMeasuredHeight()*GUESS_IME_HEIGHT));
	}

	/**
	 * intercept the listeners associated with a dialog.
	 * We pass in a spinner, because we want to change the cancel and dismiss events for its dialog popup, and we want to suppress
	 * click, scroll, touch events in the spinner, since it's all covered by onItemSelected()
	 * @param dialog
	 */
	public void interceptSpinnerDialog(Dialog dialog) {
		try {
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(this, originalDismissListener, Constants.EventTags.DISMISS_SPINNER_DIALOG);
				dialog.setOnDismissListener(recordOnDismissListener);
			}
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(this, originalCancelListener, Constants.EventTags.CANCEL_SPINNER_DIALOG);
				dialog.setOnCancelListener(recordOnCancelListener);
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting spinner dialog " +  RecordListener.getDescription(dialog);
				writeRecord(Constants.EventTags.EXCEPTION, description);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION,  "unknown description");
			}
			ex.printStackTrace();
		}
	}
	
	public void interceptPopupWindow(PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				RecordPopupWindowOnDismissListener recordOnDismissListener = new RecordPopupWindowOnDismissListener(this, null, popupWindow, Constants.EventTags.DISMISS_POPUP_WINDOW, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
		} catch (Exception ex) {
			writeRecord(Constants.EventTags.EXCEPTION, "Intercepting popup window");
			ex.printStackTrace();
		}
	}
	
	public void interceptDialog(Dialog dialog) {
		try {
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(this, originalDismissListener);
				dialog.setOnDismissListener(recordOnDismissListener);
			}
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(this, originalCancelListener);
				dialog.setOnCancelListener(recordOnCancelListener);
			}
			Window window = dialog.getWindow();
			intercept(window.getDecorView());
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				writeRecord(Constants.EventTags.EXCEPTION, description);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION, " unknown description");
			}
			ex.printStackTrace();
		}
	}
	
	public void interceptAutoCompletePopupWindow(AutoCompleteTextView autoComplete, PopupWindow popupWindow) {
		try {
			// we would pick up the autoComplete onDismissListener, but that's not available until API level 17
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordPopupWindowOnDismissListener.class)) {
				RecordPopupWindowOnDismissListener recordOnDismissListener = 
					new RecordPopupWindowOnDismissListener(this, autoComplete, popupWindow, Constants.EventTags.DISMISS_AUTOCOMPLETE_DROPDOWN, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
			/*
			AdapterView.OnItemClickListener originalItemClickListener = autoComplete.getOnItemClickListener();
			if (!(originalItemClickListener instanceof RecordOnItemClickListener)) {
				RecordOnItemClickListener recordItemClickListener = new RecordOnItemClickListener(this, originalItemClickListener);
				autoComplete.setOnItemClickListener(recordItemClickListener);		
			}
			*/
		} catch (Exception ex) {
			try {
				String description = "Intercepting popup for " +  RecordListener.getDescription(autoComplete);
				writeRecord(Constants.EventTags.EXCEPTION, description);
			} catch (Exception exlog) {
				writeRecord(Constants.EventTags.EXCEPTION, " unknown description");
			}
			ex.printStackTrace();
		}

	}

}
