package com.androidApp.randomtest;

import java.io.IOException;
import java.util.List;

import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Test.ActivityInterceptor;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * random test generator, because I'm just way too lazy to sit there and poke at my device.
 * @author Matthew
 *
 */
public class RandTest {
	private static final String TAG = "RandTest";
	private final int MINISLEEP = 100;
	private final int MAX_TEXT_LENGTH = 64;
	private final int MAX_SECURITY_EXCEPTION = 100;
	private final String DICTIONARY = "dictionary.txt";	
	protected ActivityInterceptor mActivityInterceptor;
	protected Instrumentation mInstrumentation; 
	
	// this may be somewhat overkill, but I like text with real words, not random gibberish
	protected RandomDictionary mDictionary;		
	
	/**
	 * randomized test instrumentation
	 * @param instrumentation
	 * @param activityInterceptor
	 */
	public RandTest(Instrumentation instrumentation, ActivityInterceptor activityInterceptor) throws IOException {
		mInstrumentation = instrumentation;
		mActivityInterceptor = activityInterceptor;
		mDictionary = new RandomDictionary(mInstrumentation.getContext(), DICTIONARY);

	}
	
	/**
	 * random test driver
	 * @param iterations how many iterations total
	 * @param backKeyPercentage percentage of events should be back key events
	 * @param rotationPercentage percentage of events should be rotation events
	 * @param menuPercentage percenta of events should be option menu events
	 * @throws IOException
	 */
	
	public void randTest(Context context, 
						 int iterations, 
						 float backKeyPercentage,
						 float rotationPercentage,
						 float menuPercentage) throws ClassNotFoundException {
		Activity currentActivity = null;
		int numIterationsInActivity = 0;
		boolean fStart = true;
		int securityExceptionCount = 0;
	    for (int i = 0; i < iterations; i++) {	
			Activity activity = mActivityInterceptor.getCurrentActivity();
			if (activity != currentActivity) {
				if (currentActivity != null) {
					fStart = false;
				}
				numIterationsInActivity = 0;
				currentActivity = activity;
			} else {
				numIterationsInActivity++;
			}
			if ((activity != null) && (activity.getWindow() != null)) {
				View rootView = activity.getWindow().getDecorView();
				View[] views = ViewExtractor.getWindowDecorViews();
				float backKeyRoll = (float) (100*Math.random());
				boolean fBackKey = (backKeyRoll < backKeyPercentage);
				boolean firstActivityComplete = (numIterationsInActivity > iterations/10) || !fStart;
				boolean fHasMagicFrames = verifyMagicFrames(rootView);
				float rotationRoll = (float) (100*Math.random());
				boolean fRotation = (rotationRoll < rotationPercentage);
				float menuRoll = (float) (100*Math.random());
				boolean fMenu = (menuRoll < menuPercentage);

				if (fBackKey && firstActivityComplete && fHasMagicFrames) {
					// there is a race condition where if the activity has just been created, and we send the back key,
					// the "magic frames" have not been inserted, and we 'back' the activity before the key event is intercepted.
					mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} else if (fRotation) {
					if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) { 
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					} else {
						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
				} else if (fMenu) {
					clickRandomMenuItem();
				} else {
					List<View> viewList = TestUtility.getAllViews(activity, views);
					int randViewIndex = (int) (Math.random()*viewList.size());
					View randView = viewList.get(randViewIndex);
					if (isViewGood(randView) && !(randView instanceof MagicFrame) && (randView != rootView)  && !activity.isFinishing()) {
						List<SupportedOperation.Operation> supportedOperations = SupportedOperation.getSupportedOperations(randView);
						int randOperationIndex = (int) (Math.random()*supportedOperations.size());
						SupportedOperation.Operation randOperation = supportedOperations.get(randOperationIndex);
						Log.i(TAG, randOperation.getName() + " view = " + randView + " activity = " + currentActivity + " event = " + numIterationsInActivity);
						switch (randOperation) {
						case TOUCH:
							if (!EventUtility.touchOnScreen(mInstrumentation, randView)) {
								securityExceptionCount++;
							}
							break;
						case CLICK:
							if (!EventUtility.clickOnScreen(mInstrumentation, randView)) {
								securityExceptionCount++;
							}
							break;
						case SCROLL:
							Rect contentsRect = TestUtility.getContentsRect(randView);
							if (TestUtility.canScroll(randView, contentsRect)) {
								randScroll(randView, contentsRect);
							}
							break;
						case SCROLL_LIST:
							break;
						case ENTER_TEXT:
							EditText et = (EditText) randView;
							String s = mDictionary.randWords(MAX_TEXT_LENGTH);
							mInstrumentation.runOnMainSync(new SetTextRunnable(et, s));
							break;
						case LONG_CLICK:
							if (!EventUtility.clickLongOnScreen(mInstrumentation, randView)) {
								securityExceptionCount++;
							}
							break;		
						case LIST_SELECT:
							AbsListView listView = (AbsListView) randView;
							randomSelectList(activity, listView);
							break;
						}
				    } else {
				    	TestUtility.sleep();
				    }
				}
				if (securityExceptionCount > MAX_SECURITY_EXCEPTION) {
					Assert.fail("security exception count exceeded");
				}
				mInstrumentation.waitForIdleSync();
			}
	    }
	    // terminate
		mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
	}
	
	boolean isViewGood(View view) {
		final int viewWidth = view.getWidth();
		final int viewHeight = view.getHeight();
		return ((view.getVisibility() == View.VISIBLE) && (viewWidth > 0) && (viewHeight > 0));
	}

	/***
	 * randomly select an item from a list.
	 * @param activity
	 * @param absListView
	 */
	public void randomSelectList(Activity activity, AbsListView absListView) {
		Adapter adapter = absListView.getAdapter();
		int numItems = adapter.getCount();
		int randItem = (int) (Math.random()*numItems);
		mInstrumentation.runOnMainSync(new ScrollToPositionRunnable(absListView, randItem));
		mInstrumentation.waitForIdleSync();
		mInstrumentation.runOnMainSync(new PerformItemClickRunnable(absListView, randItem));
	}
	
	/**
	 * runnable to click an item in a list.
	 * @author Matthew
	 */
	public class PerformItemClickRunnable implements Runnable {
		protected AbsListView 	mListView;
		protected int			mPosition;
		
		/**
		 * constructor
		 * @param listView listview to click
		 * @param position position to click it in.
		 */
		public PerformItemClickRunnable(AbsListView listView, int position) {
			mListView = listView;
			mPosition = position;
		}
		
		public void run() {
			View v = getListViewItem(mListView, mPosition);
			mListView.performItemClick(v, mPosition, mListView.getItemIdAtPosition(mPosition));
		}
	}
	
	/**
	 * retrieve the indexed view of a list item.
	 * @param absListView
	 * @param itemIndex
	 * @return
	 */
	public View getListViewItem(AbsListView absListView, int itemIndex) {
		int firstPosition = absListView.getFirstVisiblePosition();
		int actualIndex = itemIndex - firstPosition;
		return absListView.getChildAt(actualIndex);
	}
	
	/**
	 * runnable to set the text in a textview or edit text.
	 * @author Matthew
	 *
	 */
	public class SetTextRunnable implements Runnable {
		protected EditText		mEditText;
		protected String		mText;
		
		public SetTextRunnable(EditText editText, String text) {
			mEditText = editText;
			mText = text;
		}
		
		public void run() {
			mEditText.setText(mText);
		}
	}
	
	/**
	 * runnable to scroll to a specified position in a list view.
	 * @author Matthew
	 *
	 */
	public class ScrollToPositionRunnable implements Runnable {
		protected AbsListView 	mListView;
		protected int			mPosition;
		
		public ScrollToPositionRunnable(AbsListView listView, int position) {
			mListView = listView;
			mPosition = position;
		}
		
		public void run() {
			mListView.smoothScrollToPosition(mPosition);
		}
	}
	
	/**
	 * randomly scroll a scrollable view relative to its contents
	 * @param v scrollable view
	 * @param contentsRect extents of contents.
	 */
	public void randScroll(View v, Rect contentsRect) {
		int minScrollX = contentsRect.left;
		int maxScrollX = contentsRect.right - v.getMeasuredWidth();
		int minScrollY = contentsRect.top;
		int maxScrollY = contentsRect.bottom - v.getMeasuredHeight();
		int scrollX = minScrollX*(int)((maxScrollX - minScrollX)*Math.random());
		int scrollY = minScrollY*(int)((maxScrollY - minScrollY)*Math.random());
		mInstrumentation.runOnMainSync(new ScrollRunnable(v, scrollX, scrollY));
	}
	
	/**
	 * runnable to scroll a view.
	 * @author Matthew
	 *
	 */
	public class ScrollRunnable implements Runnable {
		protected View 	mView;
		protected int 	mScrollX;
		protected int	mScrollY;
		
		public ScrollRunnable(View v, int scrollX, int scrollY) {
			mView = v;
			mScrollX = scrollX;
			mScrollY = scrollY;
		}
		
		public void run() {
			mView.scrollTo(mScrollX, mScrollY);
		}
	}

	/**
	 * click a random menu item (currently doesn't work)
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean clickRandomMenuItem() throws ClassNotFoundException {
		mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
		Activity activity = mActivityInterceptor.getCurrentActivity();
		View menuView = TestUtils.findOptionsMenu(activity);
		if (menuView != null) {
			List<View> textViews = TestUtility.getViewList(menuView, TextView.class);
			int itemIndex = (int) (Math.random()*textViews.size());
			EventUtility.clickOnScreen(mInstrumentation, textViews.get(itemIndex));
			return true;
		}
		return false;
	}
	
	/**
	 * are all of the frames magic frames? i.e. can we send a back-key event?
	 * @param rootView
	 * @return
	 */
	public boolean verifyMagicFrames(View rootView) {
		boolean fMagic = false;
		ViewGroup vgRootView = (ViewGroup) rootView;
		if (vgRootView.getChildCount() == 1) {
			View layoutView = vgRootView.getChildAt(0);
			if (layoutView instanceof LinearLayout) {
				LinearLayout linearLayout = (LinearLayout) layoutView;
				for (int iChild = 0; iChild < linearLayout.getChildCount(); iChild++) {
					View child = linearLayout.getChildAt(iChild);
					if (!(child instanceof MagicFrame)) {
						return false;
					}	
					fMagic = true;
				}
			}
		}
		return fMagic;
	}
}
