package com.androidApp.utilSupportV4;

import java.io.IOException;
import com.androidApp.util.TestException;
import com.androidApp.util.ViewExtractor;
import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.PopupWindow;

public class RobotiumUtils extends com.androidApp.util.RobotiumUtils {
	private static final String TAG = "RobotiumUtils";
	
	public RobotiumUtils(Class<? extends ActivityInstrumentationTestCase2> testClass, Instrumentation instrumentation) throws IOException {
		super(testClass, instrumentation);
	}

	
	/**
	 * dismissing a popup has to be done on the UI thread via a runnable
	 * @author Matthew
	 *
	 */
	public class DismissPopupWindowRunnable implements Runnable {
		public PopupWindow mPopupWindow;
		
		public DismissPopupWindowRunnable(PopupWindow popupWindow) {
			mPopupWindow = popupWindow;
		}
		
		@Override
		public void run() {
			mPopupWindow.dismiss();
		}
	}
	
	public void dismissPopupWindow(Activity activity) throws TestException {
		dismissPopupWindow(activity, VIEW_TIMEOUT_MSEC);
	}
	
	public boolean waitForPopupWindow(Activity activity, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	/**
	 * dismiss a popup window, like a menu popup, autocomplete dropdown, etc.  I hope there is only one popup.
	 * @param activity
	 */
	public boolean dismissPopupWindow(Activity activity, long waitMsec) throws TestException {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if (popupWindow != null) {
				mInstrumentation.runOnMainSync(new DismissPopupWindowRunnable(popupWindow));
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
	
	public static boolean verifyPopupWindowDimissed(Activity activity, PopupWindow dismissedPopupWindow) throws TestException {
		return verifyPopupWindowDismissed(activity, dismissedPopupWindow, VIEW_TIMEOUT_MSEC);
	}

	/**
	 * verify that there are no popups displayed.
	 * NOTE: What if another popup is displayed immediately?
	 * @param activity
	 * @param waitMsec
	 * @return
	 * @throws TestException
	 */
	public static boolean verifyPopupWindowDismissed(Activity activity, PopupWindow dismissedPopupWindow, long waitMsec) {
		while (waitMsec > 0) {
			PopupWindow popupWindow = ViewExtractor.findPopupWindow(activity);
			if ((popupWindow == null) || (dismissedPopupWindow != popupWindow)) {
				return true;
			}
			try {
				Thread.sleep(WAIT_INCREMENT_MSEC);
			} catch (InterruptedException iex) {}
			waitMsec -= WAIT_INCREMENT_MSEC;
		}
		return false;
	}
}