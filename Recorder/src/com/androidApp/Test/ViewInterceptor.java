package com.androidApp.Test;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Intercept.InterceptActionBar;
import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Listeners.FinishTextChangedListener;
import com.androidApp.Listeners.RecordDialogOnCancelListener;
import com.androidApp.Listeners.RecordDialogOnDismissListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordOnClickListener;
import com.androidApp.Listeners.RecordOnFocusChangeListener;
import com.androidApp.Listeners.RecordOnItemClickListener;
import com.androidApp.Listeners.RecordOnItemSelectedListener;
import com.androidApp.Listeners.RecordOnKeyListener;
import com.androidApp.Listeners.RecordOnLongClickListener;
import com.androidApp.Listeners.RecordOnScrollListener;
import com.androidApp.Listeners.RecordOnTouchListener;
import com.androidApp.Listeners.RecordPopupMenuOnMenuItemClickListener;
import com.androidApp.Listeners.RecordPopupWindowOnDismissListener;
import com.androidApp.Listeners.RecordSeekBarChangeListener;
import com.androidApp.Listeners.RecordTextChangedListener;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.ReflectionUtils;
import com.androidApp.Utility.TestUtils;

/**
 * class to install the interceptors in the view event listeners
 */
public class ViewInterceptor {
	protected static final String	TAG = "ViewInterceptor";
	public static final float		GUESS_IME_HEIGHT = 0.25F;			// guess that IME takes up this amount of the screen.
	protected EventRecorder 		mEventRecorder;
	protected int					mHashCode;							// for quick view hierarchy comparison
	protected boolean				mIMEWasDisplayed = false;			// IME was displayed in the last layout
	protected View					mViewFocus;
	protected int					mLastKeyAction;						// so we can track dialog/popup/ime/activity dismiss from back/menu key					
	private Dialog					mCurrentDialog = null;				// track the current dialog, so we don't re-record it.
	private PopupWindow				mCurrentPopupWindow = null;			// current popup window, which is like the current dialog, but different
	private View					mCurrentOptionsMenuView = null;		// current action menu window
	
	public ViewInterceptor(EventRecorder eventRecorder) {
		mEventRecorder = eventRecorder;
		mLastKeyAction = -1;
	}
	
	// accessors/mutator for focused view for IME display/remove event
	public View getFocusedView() {
		return mViewFocus;
	}
	
	public void setFocusedView(View v) {
		mViewFocus = v;
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
	
	public View getCurrentOptionsMenuView() {
		return mCurrentOptionsMenuView;
	}
	
	public void setCurrentOptionsMenuView(View view) {
		mCurrentOptionsMenuView = view;
	}
	
	/**
	 * set the current popup window
	 * @param popupWindow
	 */
	public void setCurrentPopupWindow(PopupWindow popupWindow) {
		mCurrentPopupWindow = popupWindow;
	}
			
	
	/**
	 * retrieve the last key action, so when something happens, we can tell if the user did it.
	 * @return
	 */
	public int getLastKeyAction() {
		return mLastKeyAction;
	}

	public void setLastKeyAction(int keyAction) {
		mLastKeyAction = keyAction;
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

	/**
	 * replace the listeners in the view with wrapping listeners that record the events
	 * @param v view to record events for.
	 */
	public void replaceListeners(View v) {
		try {
			if (!TestUtils.isActionBarDescendant(v)) {
				// adapterViews don't like click listeners, they like item click listeners.
				if (!(v instanceof AdapterView)) {
					if (!TestUtils.isAdapterViewAncestor(v)) {
						replaceViewListeners(v);
					}
					
					// specific handlers for seekbars/progress bars/etc.
					if (v instanceof SeekBar) {
						SeekBar seekBar = (SeekBar) v;
						replaceSeekBarListeners(seekBar);
					}
				} else {
					if (v instanceof AdapterView) {
						if (v instanceof Spinner) {
							Spinner spinner = (Spinner) v;
							replaceSpinnerListeners(spinner);
						} else {
							AbsListView absListView = (AbsListView) v;
							replaceAdapterViewListeners(absListView);
						}
					}
				}
				if (v instanceof EditText) {
					TextView tv = (TextView) v;
					replaceEditTextListeners(tv);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, description);
				ex.printStackTrace();
			} catch (Exception exlog) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION + " unknown description");
			}
		}
	}

	/**
	 * replace the listeners in an atomic view (like a button or a text view, and stuff like that)
	 * @param v view to intercept
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceViewListeners(View v) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		View.OnClickListener originalClickListener = ListenerIntercept.getClickListener(v);
		if (!(originalClickListener instanceof RecordOnClickListener)) {
			RecordOnClickListener recordClickListener = new RecordOnClickListener(mEventRecorder, originalClickListener);
			v.setOnClickListener(recordClickListener);
		}
		View.OnLongClickListener originalLongClickListener = ListenerIntercept.getLongClickListener(v);
		if (!(originalLongClickListener instanceof RecordOnClickListener)) {
			RecordOnLongClickListener recordLongClickListener = new RecordOnLongClickListener(mEventRecorder, originalLongClickListener);
			v.setOnLongClickListener(recordLongClickListener);
		}
	
		View.OnTouchListener originalTouchListener = ListenerIntercept.getTouchListener(v);
		if (!(originalTouchListener instanceof RecordOnTouchListener)) {
			RecordOnTouchListener recordTouchListener = new RecordOnTouchListener(mEventRecorder, originalTouchListener);
			v.setOnTouchListener(recordTouchListener);
		}
		
		View.OnKeyListener originalKeyListener = ListenerIntercept.getKeyListener(v);
		if ((originalKeyListener != null) && !(originalKeyListener instanceof RecordOnKeyListener)) {
			RecordOnKeyListener recordKeyListener = new RecordOnKeyListener(mEventRecorder, originalKeyListener);
			v.setOnKeyListener(recordKeyListener);
		}
	}
	
	/**
	 * replace the listeners in a PopupMenu
	 * @param v popup menu
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replacePopupMenuListeners(View v)  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		PopupMenu.OnMenuItemClickListener originalMenuItemClickListener = ListenerIntercept.getPopupMenuOnMenuItemClickListener(v);
		if (!(originalMenuItemClickListener instanceof RecordPopupMenuOnMenuItemClickListener)) {
			ListenerIntercept.setPopupMenuOnMenuItemClickListener(v, new RecordPopupMenuOnMenuItemClickListener(mEventRecorder, v)); 
		}
	}

	/**
	 * replace the listeners in a seekbar
	 * @param seekBar seekbar/progress listener, etc
	 * @throws IllegalAccessException ReflectionUtils exception
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 */
	public void replaceSeekBarListeners(SeekBar seekBar) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		SeekBar.OnSeekBarChangeListener originalSeekBarChangeListener = ListenerIntercept.getSeekBarChangeListener(seekBar);
		if ((originalSeekBarChangeListener != null) && !(originalSeekBarChangeListener instanceof RecordSeekBarChangeListener)) {
			RecordSeekBarChangeListener recordSeekbarChangeListener = new RecordSeekBarChangeListener(mEventRecorder, seekBar);
			seekBar.setOnSeekBarChangeListener(recordSeekbarChangeListener);
		}
	}
	
	/**
	 * replace the listeners for click and item selected for a spinner.
	 * @param spinner
	 */
	public void replaceSpinnerListeners(Spinner spinner) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		AdapterView.OnItemSelectedListener originalSelectedItemListener = ListenerIntercept.getItemSelectedListener(spinner);
		if (!(originalSelectedItemListener instanceof RecordOnItemSelectedListener)) {
			RecordOnItemSelectedListener recordItemSelectedListener = new RecordOnItemSelectedListener(mEventRecorder, originalSelectedItemListener);
			spinner.setOnItemSelectedListener(recordItemSelectedListener);
		}
	}

	/**
	 * replace the listeners for item click, list scroll, and item select if its a spinner
	 * @param absListView list view to intercept
	 * @throws IllegalAccessException ReflectionUtils exceptions
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundExceptions
	 */
	public void replaceAdapterViewListeners(AbsListView absListView) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
		AdapterView.OnItemClickListener itemClickListener = absListView.getOnItemClickListener();
		if (!(itemClickListener instanceof RecordOnItemClickListener)) {
			RecordOnItemClickListener recordItemClickListener = new RecordOnItemClickListener(mEventRecorder, absListView);
			absListView.setOnItemClickListener(recordItemClickListener);		
		}	
		AbsListView.OnScrollListener originalScrollListener = ListenerIntercept.getScrollListener(absListView);
		if (!(originalScrollListener instanceof RecordOnScrollListener)) {
			RecordOnScrollListener recordScrollListener = new RecordOnScrollListener(mEventRecorder, originalScrollListener);
			absListView.setOnScrollListener(recordScrollListener);
		}
	}

	/**
	 * replace the text watcher and focus change listeners for a text view (actually EditText, but we're not picky)
	 * @param tv TextView
	 * @throws IllegalAccessException ReflectionUtils Exceptions
	 * @throws NoSuchFieldException
	 */
	public void replaceEditTextListeners(TextView tv) throws IllegalAccessException, NoSuchFieldException {
		ArrayList<TextWatcher> textWatcherList = ListenerIntercept.getTextWatcherList(tv);
		if (textWatcherList == null) {
			textWatcherList = new ArrayList<TextWatcher>();
		}
		// make sure that we haven't already added the intercepting text watcher.
		if (!ListenerIntercept.containsTextWatcher(textWatcherList, RecordTextChangedListener.class)) {
			textWatcherList.add(0, new RecordTextChangedListener(mEventRecorder, tv));
			textWatcherList.add(new FinishTextChangedListener(mEventRecorder));
			ListenerIntercept.setTextWatcherList(tv, textWatcherList);
		}
		// add listener for focus
		View.OnFocusChangeListener originalFocusChangeListener = tv.getOnFocusChangeListener();
		if (!(originalFocusChangeListener instanceof RecordOnFocusChangeListener)) {
			View.OnFocusChangeListener recordFocusChangeListener = new RecordOnFocusChangeListener(mEventRecorder, this, originalFocusChangeListener);
			tv.setOnFocusChangeListener(recordFocusChangeListener);
		}
	}
	
	/**
	 * recursively set the intercepting listeners for touch/key/textwatcher events through the view tree
	 * @param v view to recurse from.
	 */
	public void intercept(View v) {
		replaceListeners(v);
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				for (int iChild = 0; iChild < vg.getChildCount(); iChild++) {
					View vChild = vg.getChildAt(iChild);
					intercept(vChild);
				}
			}
		} catch (Exception ex) {
			try {
				String description = "Intercepting view " +  RecordListener.getDescription(v);
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, description);
				ex.printStackTrace();
			} catch (Exception exlog) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION + " unknown description");
			}
		}
	}
	
	/**
	 * the action bar is not in the activity contentView, so it has to be handled separately
	 * @param activity activity to intercept
	 * @param actionBar actionBar
	 */
	public void intercept(Activity activity, ActionBar actionBar) {
        if (actionBar != null) {
        	try {
	        	View contentView = null;
	        	try {
	        		Class actionBarImplClass = Class.forName(Constants.Classes.ACTION_BAR_IMPL);
	        		contentView = (View) ReflectionUtils.getFieldValue(actionBar, actionBarImplClass, Constants.Fields.CONTAINER_VIEW);
	        	} catch (Exception ex) {
	        		mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "while intercepting the action bar for " + activity.getClass().getName());
	        		ex.printStackTrace();
	        	}
	        	if (contentView != null) {
	            	intercept(contentView);
	            }
	       		InterceptActionBar.interceptActionBarTabListeners(mEventRecorder, actionBar);
		       	if (actionBar.getCustomView() != null) {
		        	intercept(actionBar.getCustomView());
		        }
        	} catch (Exception ex) {
        		mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "while intercepting action bar");
        	}
	  	}		
	}
	
	/**
	 * install recorders in the listeners for the views contained in an activity
	 * @param a
	 */
	public void intercept(Activity a) {
		
		// get the content view for this activity
		Window w = a.getWindow();
        View v = w.getDecorView().findViewById(android.R.id.content);
        
        // create a hashcode which we can quickly test later, since we don't want to insert recorders just when
        // the layout has changed, but only when views have been added or removed from the view hierarchy
        mHashCode = viewTreeHashCode(v);
        
        // good stroke of luck, we can listen to layout events on the root view.
        ViewTreeObserver viewTreeObserver = v.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnLayoutInterceptListener(a));
        intercept(v);     
        ActionBar actionBar = a.getActionBar();
        intercept(a, actionBar);
       
 	}
	
	/**
	 * when we receive a layout event, set up the record listeners on the view hierarchy.
	 */
	public class OnLayoutInterceptListener implements ViewTreeObserver.OnGlobalLayoutListener {
		protected Activity mActivity;
		protected int mCurrentRotation;
		
		public OnLayoutInterceptListener(Activity activity) {
			mActivity = activity;
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			mCurrentRotation = display.getRotation();
		}
		
		public void onGlobalLayout() {
			
			// this actually returns our magic frame, which doesn't resize when the IME is displayed
	        View magicFrameView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			int newRotation = display.getRotation();
			if (newRotation != mCurrentRotation) {
				mEventRecorder.writeRotation(mActivity, newRotation);
				mCurrentRotation = newRotation;
			}
	        // this is a terrible hack to see if the IME has been hidden or displayed by this layout.
	        if ((getFocusedView() != null) && isIMEDisplayed(magicFrameView)) {
	        	mIMEWasDisplayed = true;
				mEventRecorder.writeRecord(Constants.EventTags.SHOW_IME, getFocusedView(), "IME displayed");
	        } else if (mIMEWasDisplayed && !isIMEDisplayed(magicFrameView)) {
	        	if (getLastKeyAction() == KeyEvent.KEYCODE_BACK) {
	        		mEventRecorder.writeRecord(Constants.EventTags.HIDE_IME_BACK_KEY, getFocusedView(), "IME hidden by back key pressed");
	        		setLastKeyAction(-1);
	        	} else {
	        		mEventRecorder.writeRecord(Constants.EventTags.HIDE_IME, getFocusedView(), "IME hidden");
	        	}
	        }
	        
	        // recursively generate the hashcode for this view hierarchy, and re-intercept if it's changed.
			int hashCode = viewTreeHashCode(magicFrameView);
			if (hashCode != mHashCode) {
				intercept(magicFrameView);
				mHashCode = hashCode;
			}
			
			// do the action bar, since it doesn't seem to get populated until after the activity was created/resumed

			ActionBar actionBar = mActivity.getActionBar();
	        intercept(mActivity, actionBar);   
		}
	}
	
	/**
	 * unfortunately, there isn't an event for IME display/hide, so we have to guess by examining the content view
	 * and seeing if it's enough smaller than its parent that the IME could probably fit in there.  This is called
	 * in the global layout listener, so we at least know that the layout has changed, but not why.
	 * @param contentView android.R.id.content from the activity's window.
	 * @return true if the IME is probably up, false if it's certainly down.
	 */
	public static boolean isIMEDisplayed(View magicFrameView) {
		if (magicFrameView instanceof MagicFrame) {
			MagicFrame magicFrame = (MagicFrame) magicFrameView;
			View actualContent = magicFrame.getChildAt(0).findViewById(android.R.id.content);
			int imeHeight = magicFrameView.getMeasuredHeight() - actualContent.getMeasuredHeight();
			return imeHeight > ((int) (magicFrameView.getMeasuredHeight()*GUESS_IME_HEIGHT));
		} else {
			return false;
		}
	}

	/**
	 * intercept the contents of a popup window, which isn't in the view hierarchy of an activity.
	 * @param popupWindow
	 */
	public void interceptPopupWindow(PopupWindow popupWindow) {
		try {
			PopupWindow.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(popupWindow);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				RecordPopupWindowOnDismissListener recordOnDismissListener = new RecordPopupWindowOnDismissListener(mEventRecorder, this, null, popupWindow, originalDismissListener);
				popupWindow.setOnDismissListener(recordOnDismissListener);
			}
			Class menuPopupHelperClass = Class.forName(Constants.Classes.MENU_POPUP_HELPER);
			Class listWindowPopupDropdownClass = Class.forName(Constants.Classes.LIST_WINDOW_POPUP_DROPDOWN_LIST_VIEW);			
			View contentView = popupWindow.getContentView();
			if (contentView.getClass() == menuPopupHelperClass) {
				replacePopupMenuListeners(popupWindow.getContentView());
			} else if (contentView.getClass() == listWindowPopupDropdownClass) {
				// we are assuming this is a spinner, which is probably not the best thing.
			}
		} catch (Exception ex) {
			mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, "Intercepting popup window");
			ex.printStackTrace();
		}
	}
	
	/**
	 * intercept the contents of a dialog.
	 * @param dialog
	 */
	public void interceptDialog(Dialog dialog) {
		try {
			boolean fCancelAndDismissTaken = ListenerIntercept.isCancelAndDismissTaken(dialog);
			DialogInterface.OnDismissListener originalDismissListener = ListenerIntercept.getOnDismissListener(dialog);
			if ((originalDismissListener == null) || (originalDismissListener.getClass() != RecordDialogOnDismissListener.class)) {
				// TODO: ?
				RecordDialogOnDismissListener recordOnDismissListener = new RecordDialogOnDismissListener(mEventRecorder, this, originalDismissListener);
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnDismissListener(dialog, recordOnDismissListener);
				} else {
					dialog.setOnDismissListener(recordOnDismissListener);
				}
			}
			DialogInterface.OnCancelListener originalCancelListener = ListenerIntercept.getOnCancelListener(dialog);
			if ((originalCancelListener == null) || (originalCancelListener.getClass() != RecordDialogOnCancelListener.class)) {
				RecordDialogOnCancelListener recordOnCancelListener = new RecordDialogOnCancelListener(mEventRecorder, originalCancelListener);
				// TODO: ?
				if (fCancelAndDismissTaken) {
					ListenerIntercept.setOnCancelListener(dialog, recordOnCancelListener);
				} else {
					dialog.setOnCancelListener(recordOnCancelListener);
				}
			}
			Window window = dialog.getWindow();
			intercept(window.getDecorView());
		} catch (Exception ex) {
			try {
				String description = "Intercepting dialog " +  RecordListener.getDescription(dialog);
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, description);
			} catch (Exception exlog) {
				mEventRecorder.writeRecord(Constants.EventTags.EXCEPTION, " unknown description");
			}
			ex.printStackTrace();
		}
	}

}
