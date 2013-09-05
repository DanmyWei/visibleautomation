package com.androidApp.Listeners;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.Test.ViewInterceptor;

import android.app.ActionBar;
import android.app.Activity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


/** 
 * when we receive a layout event, set up the record listeners on the view hierarchy.
 */  
public class OnLayoutInterceptListener implements ViewTreeObserver.OnGlobalLayoutListener {
    protected Activity 			mActivity;
    protected int 				mCurrentRotation;
    protected EventRecorder		mEventRecorder;
    protected int 				mHashCode;
    protected ViewInterceptor	mViewInterceptor;

    public OnLayoutInterceptListener(Activity activity, ViewInterceptor viewInterceptor, EventRecorder eventRecorder) {
        mActivity = activity; 
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        mCurrentRotation = display.getRotation();
        mEventRecorder = eventRecorder;
        mHashCode = viewTreeHashCode(mActivity.getWindow().getDecorView());
        mViewInterceptor = viewInterceptor;
    }       

    public void onGlobalLayout() {
        // this actually returns our magic frame, which doesn't resize when the IME is displayed
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        int newRotation = display.getRotation();
        if (newRotation != mCurrentRotation) {
            mEventRecorder.writeRotation(mActivity, newRotation);
            mCurrentRotation = newRotation;
        }           

        // recursively generate the hashcode for this view hierarchy, and re-intercept if it's changed.
        int hashCode = viewTreeHashCode(mActivity.getWindow().getDecorView());

        if (hashCode != mHashCode) {
            mViewInterceptor.intercept(mActivity, mActivity.toString(), mActivity.getWindow().getDecorView());
            mHashCode = hashCode;
        }           

        // do the action bar, since it doesn't seem to get populated until after the activity was created/resumed

        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
        	mViewInterceptor.intercept(mActivity,  mActivity.toString(), actionBar); 
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
}   
