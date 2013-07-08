package com.androidApp.Intercept;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/* TODO: defunct: remove this class
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class InterceptScrollView extends View {
	private static final String TAG = "InterceptScrollView";
	protected int				mTop;
	protected int				mLeft;
	protected ScrollListener	mScrollListener;
	protected View				mHostView;
	protected Paint				mPaint;
	protected Rect				mGlobalVisibleRect;

	public interface ScrollListener {
		void onScroll(int x, int y);
	}
	
	public void init() {
		mPaint = new Paint();
		mPaint.setStrokeWidth(3.0f);
		mPaint.setColor(0xffffff00);
		mPaint.setStyle(Paint.Style.STROKE);
	}
	
	public InterceptScrollView(Context context, View hostView) {
		super(context);
		mHostView = hostView;
		ViewGroup.LayoutParams layoutParams = hostView.getLayoutParams();
		setLayoutParams(layoutParams);
		setWillNotDraw(false);
		mGlobalVisibleRect = new Rect();
		setDrawingCacheEnabled(false);	
		init();
	}
	
	public void setScrollListener(ScrollListener listener) {
		mScrollListener = listener;
	}
	
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		int width = mHostView.getMeasuredWidth();
		int height = mHostView.getMeasuredHeight();
		setMeasuredDimension(width, height);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		getGlobalVisibleRect(mGlobalVisibleRect);
		mLeft = mGlobalVisibleRect.left;
		mTop = mGlobalVisibleRect.top;
	}
	
	@Override
	public void onDraw(Canvas c) {
		getGlobalVisibleRect(mGlobalVisibleRect);
		int left = mGlobalVisibleRect.left;
		int top = mGlobalVisibleRect.top;
		Log.i(TAG, "draw x = " + (left - mLeft) + " y = " + (top - mTop));
		if ((left != mLeft) || (top != mTop)) {
			Log.i(TAG, "scroll x = " + (left - mLeft) + " y = " + (top - mTop));
			if (mScrollListener != null) {
				mScrollListener.onScroll(left - mLeft, top - mTop);
			}
			mLeft = left;
			mTop = top;
		}
		Rect rect = new Rect(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
		c.drawRect(rect, mPaint);
	}
}
