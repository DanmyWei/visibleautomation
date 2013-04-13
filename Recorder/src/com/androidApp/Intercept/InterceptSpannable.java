package com.androidApp.Intercept;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

public class InterceptSpannable extends SpannableStringBuilder {
	private static final String TAG = "InterceptSpannable";

	public InterceptSpannable () {
		super();
	}
	public InterceptSpannable (CharSequence text) {
		super(text);
	}

	public InterceptSpannable (CharSequence text, int start, int end) {
		super(text, start, end);
	}
	@Override
	public SpannableStringBuilder replace(int start, int end, CharSequence tb) {
		return super.replace(start, end, tb);
	}
	
	@Override
	public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbstart, int tbend) {
		return super.replace(start, end, tb, tbstart, tbend);
	}
	
	@Override
	public void setSpan(Object what, int start, int end, int flags) {
		super.setSpan(what, start, end, flags);
		Log.d(TAG, "setSpan start = " + start + " end = " + end + " flags = 0x" + Integer.toHexString(flags));
		/*
		try {
			throw new Exception("AHAHAHAH");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
	}
	
	public static class  InterceptSpannableFactory extends Spannable.Factory {
		private static Spannable.Factory sInstance = new InterceptSpannable.InterceptSpannableFactory();
		public static Spannable.Factory getInstance() {
			return sInstance;
		}
		
		 public Spannable newSpannable(CharSequence source) {
			 return new InterceptSpannable(source);
		}
	}
	
	public static class  InterceptEditableFactory extends Editable.Factory {
		private static Editable.Factory sInstance = new InterceptSpannable.InterceptEditableFactory();
		public static Editable.Factory getInstance() {
			return sInstance;
		}
		
		 public Editable newEditable(CharSequence source) {
			 return new InterceptSpannable(source);
		}
	}

}


