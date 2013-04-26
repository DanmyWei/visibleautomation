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
import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.EditText;
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
	protected ActivityInstrumentationTestCase2<? extends Activity> mInstrumentation; 
	
	// this may be somewhat overkill, but I like text with real words, not random gibberish
	protected RandomDictionary mDictionary;		
	
	/**
	 * randomized test instrumentation
	 * @param instrumentation
	 * @param activityInterceptor
	 */
	public RandTest(ActivityInstrumentationTestCase2<? extends Activity> instrumentation, ActivityInterceptor activityInterceptor) {
		mInstrumentation = instrumentation;
		mActivityInterceptor = activityInterceptor;
	}
	
	/**
	 * random test driver
	 * @param iterations how many iterations total
	 * @param backKeyPercentage
	 * @throws IOException
	 */
	
	public void randTest(Context context, int iterations, float backKeyPercentage) throws IOException, ClassNotFoundException {
		Activity currentActivity = null;
		int numIterationsInActivity = 0;
		boolean fStart = true;
		int securityExceptionCount = 0;
	    for (int i = 0; i < iterations; i++) {	
			Activity activity = mActivityInterceptor.getCurrentActivity();
			if (activity != currentActivity) {
				if (currentActivity == null) {
					mDictionary = new RandomDictionary(context, DICTIONARY);
				} else {
					fStart = false;
				}
				numIterationsInActivity = 0;
				currentActivity = activity;
			} else {
				numIterationsInActivity++;
			}
			float backKeyRoll = (float) (100*Math.random());
			boolean fBackKey = (backKeyRoll < backKeyPercentage);
			boolean firstActivityComplete = (numIterationsInActivity > iterations/10) || !fStart;
			if (fBackKey && firstActivityComplete) {
				mInstrumentation.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			} else {
				if ((activity != null) && (activity.getWindow() != null)) {
					View rootView = activity.getWindow().getDecorView();
					View[] views = ViewExtractor.getWindowDecorViews();
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
							if (!EventUtility.touchOnScreen(mInstrumentation.getInstrumentation(), randView)) {
								securityExceptionCount++;
							}
							break;
						case CLICK:
							if (!EventUtility.clickOnScreen(mInstrumentation.getInstrumentation(), randView)) {
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
							int numTexts = TestUtility.numViewsOfClass(viewList, EditText.class);
							if (numTexts > 0) {
								View v = TestUtility.selectRandomView(viewList, EditText.class, numTexts);
								if (v != null) {
									EditText et = (EditText) v;
									String s = mDictionary.randWords(MAX_TEXT_LENGTH);
									mInstrumentation.getInstrumentation().runOnMainSync(new SetTextRunnable(et, s));
								}
							}
							break;
						case OPTION_MENU_SELECT:
							clickRandomMenuItem();
							break;
						case LONG_CLICK:
							if (!EventUtility.clickLongOnScreen(mInstrumentation.getInstrumentation(), randView)) {
								securityExceptionCount++;
							}
							break;		
						}
				    } else {
				    	TestUtility.sleep();
				    }
				}
				if (securityExceptionCount > MAX_SECURITY_EXCEPTION) {
					Assert.fail("security exception count exceeded");
				}
				mInstrumentation.getInstrumentation().waitForIdleSync();
			}
	    }
	    // terminate
		mInstrumentation.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
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
		View vChild = absListView.getChildAt(0);
		int itemHeight = vChild.getMeasuredHeight();
		int scrollPosition = itemHeight*randItem;
		mInstrumentation.getInstrumentation().runOnMainSync(new ScrollToPositionRunnable(absListView, scrollPosition));
		mInstrumentation.getInstrumentation().runOnMainSync(new PerformItemClickRunnable(absListView, scrollPosition));
	}
	
	public class PerformItemClickRunnable implements Runnable {
		protected AbsListView 	mListView;
		protected int			mPosition;
		
		public PerformItemClickRunnable(AbsListView listView, int position) {
			mListView = listView;
			mPosition = position;
		}
		
		public void run() {
			View v = getListViewItem(mListView, mPosition);
			mListView.performItemClick(v, mPosition, mListView.getItemIdAtPosition(mPosition));
		}
	}
	
	public View getListViewItem(AbsListView absListView, int itemIndex) {
		int firstPosition = absListView.getFirstVisiblePosition();
		int actualIndex = itemIndex - firstPosition;
		return absListView.getChildAt(actualIndex);
	}
	
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
	
	public void randScroll(View v, Rect contentsRect) {
		int minScrollX = contentsRect.left;
		int maxScrollX = contentsRect.right - v.getMeasuredWidth();
		int minScrollY = contentsRect.top;
		int maxScrollY = contentsRect.bottom - v.getMeasuredHeight();
		int scrollX = minScrollX*(int)((maxScrollX - minScrollX)*Math.random());
		int scrollY = minScrollY*(int)((maxScrollY - minScrollY)*Math.random());
		mInstrumentation.getInstrumentation().runOnMainSync(new ScrollRunnable(v, scrollX, scrollY));
	}
	
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

	public boolean clickRandomMenuItem() throws ClassNotFoundException {
		mInstrumentation.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
		Activity activity = mActivityInterceptor.getCurrentActivity();
		View menuView = TestUtils.findOptionsMenu(activity);
		if (menuView != null) {
			List<View> textViews = TestUtility.getViewList(menuView, TextView.class);
			int itemIndex = (int) (Math.random()*textViews.size());
			EventUtility.clickOnScreen(mInstrumentation.getInstrumentation(), textViews.get(itemIndex));
			return true;
		}
		return false;
	}
}
