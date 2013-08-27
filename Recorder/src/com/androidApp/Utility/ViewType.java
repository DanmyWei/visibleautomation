package com.androidApp.Utility;

import java.lang.reflect.Method;
import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.androidApp.Intercept.MagicFrame;
import com.androidApp.Intercept.MagicFramePopup;
import com.androidApp.Intercept.MagicOverlay;
import com.androidApp.Intercept.MagicOverlayDialog;
import com.androidApp.Utility.Constants.Classes;
import com.androidApp.Utility.Constants.Fields;
import com.androidApp.Utility.Constants.Methods;
import com.androidApp.Utility.Constants.Packages;

public class ViewType {

	/**
	 * is this view a child of the action bar?
	 * @param v view to test
	 * @return true if its ancestor is an action bar
	 * @throws ClassNotFoundException
	 */
	public static boolean isActionBarDescendant(View v) throws ClassNotFoundException {
		Class<? extends View> actionBarImplClass = (Class<? extends View>) Class.forName(Constants.Classes.ACTION_BAR_CONTAINER);
		return TestUtils.isDescendentOfClass(v, v.getRootView(), actionBarImplClass);
	}

	// is this control a child of a tab control? There's the old TabHost, and the new action bar tab
	// control to take into consideration
	public static boolean isInTabControl(View v) throws ClassNotFoundException {
		if (TestUtils.isDescendentOfClass(v, v.getRootView(), TabHost.class)) {
			return true;
		}
		Class<? extends View> tabImplClass = (Class<? extends View>) Class.forName(Constants.Classes.ACTION_BAR_IMPL_TAB_IMPL);
		if (TestUtils.isDescendentOfClass(v, v.getRootView(), tabImplClass)) {
			return true;
		}
		Class<? extends View> scrollingTabContainerClass = (Class<? extends View>) Class.forName(Constants.Classes.SCROLLING_TAB_CONTAINER_VIEW);
		if (TestUtils.isDescendentOfClass(v, v.getRootView(), scrollingTabContainerClass)) {
			return true;
		}
		return false;
	}

	/**
	 * does this list view have any listeners?
	 * @param av
	 * @return
	 */
	public static boolean adapterHasListeners(AdapterView av) {
		return (av.getOnItemClickListener() != null) || (av.getOnItemSelectedListener() != null);
	}

	/**
	 * don't intercept it if it's one of ours.
	 * @param v
	 * @return true if it's one of ours.
	 */
	public static boolean isVisibleAutomationView(View v) {
		return ((v instanceof MagicFrame) || 
				(v instanceof MagicOverlay) ||
				(v instanceof MagicOverlayDialog) || 
				(v instanceof MagicFramePopup) ||
				((v instanceof ImageView) && (v.getId() == MagicOverlay.MAGIC_BUTTON_ID)));
	}

	/**
	 * low-level events to this object should be ignored
	 * @param v view to check
	 * @return true if the view sends higher-level events that we listen to
	 */
	public static boolean shouldBeIgnored(View v) {
		String className = v.getClass().getName();
		return className.equals(Constants.Classes.OVERFLOW_MENU_BUTTON) ||
			   className.equals(Constants.Classes.SCROLLING_TAG_CONTAINER_TAB_VIEW);
	}

	public static boolean isScrollingTextView(TextView tv) throws IllegalAccessException, NoSuchFieldException {
		return ReflectionUtils.getFieldBoolean(tv, TextView.class, Constants.Fields.HORIZONTALLY_SCROLLING);
	}

	/**
	 * is it an instrinsic scrolling view
	 * @param v
	 * @return
	 */
	public static boolean isScrollView(View v) {
		if ((v instanceof ScrollView) || (v instanceof Gallery)) {
			return true;
		}
		// HorizontalScrolView may not be defined in earlier android APIs, so we have to extract it via
		// reflection.
		try {
			Class<? extends View> cls = (Class<? extends View>) Class.forName(Constants.Classes.HORIZONTAL_SCROLL_VIEW);
			if (cls.isAssignableFrom(v.getClass())) {
				return true;
			}
		} catch (Exception ex) {		
		}
		return false;
	}

	/**
	 * unfortunately, galleries don't have scroll listeners that we can hook to, and instead implement
	 * methods for onScroll(), etc.  We have to listen for motion events, then play them back.
	 * Later, we'll allow the user to set listen to motion events for custom controls and stuff like that.
	 * @param v
	 * @return true if we should listen to motion events
	 */
	public static boolean listenMotionEvents(List<View> motionEventViewList, View v) throws IllegalAccessException, NoSuchFieldException{
		
		// I really dislike this hardcoded bullshit that is certain to break in later versions.  We need to have a 
		// "blacklist" as well as a "whitelist" for motion events. Or Something Better Than This Implementation
		// which sucks Giant Donkey Dicks
		if (!(v instanceof AdapterView)) {
			if (isScrollView(v)) {
				return true;
			}
			if (v.isHorizontalScrollBarEnabled() || v.isVerticalScrollBarEnabled()) {
				return true;
			}
			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				if (!isScrollingTextView(tv)) {
					return false;
				}
			}
		}
		if (motionEventViewList != null) {
			return motionEventViewList.contains(v);
		}
		return false;
	}

	/**
	 * we put a wrapper function in here, because the developer may want to specify custom
	 * controls which don't derive from button, but do take click events.
	 * TODO: this needs A LOT
	 * @param v
	 * @return
	 */
	public static boolean listenClickEvents(View v) {
		return (v instanceof Button);
	}

	/**
	 * has this class overridden the default android touch method? 
	 * @param v view which is probably some weird derived class
	 * @return true if some non-android superclass has a public onTouch() method
	 */
	public static boolean hasOverriddenAndroidTouchMethod(View v) {
		String[] android_packages = new String[] { Constants.Packages.ANDROID_VIEW, Constants.Packages.ANDROID_WIDGET };
		String[] methods = new String[] { Constants.Methods.ON_TOUCH, Constants.Methods.ON_INTERCEPT_TOUCH_EVENT };
		Class<? extends View> c = v.getClass();
		while (!StringUtils.containedInStringArray(c.getPackage().getName(), android_packages)) {
			Method[] classMethods = c.getMethods();
			for (Method method : classMethods) {
				if (StringUtils.containedInStringArray(method.getName(), methods)) {		
					return true;
				}
			}
			c = (Class<? extends View>) c.getSuperclass();
		}
		return false;
	}

}
