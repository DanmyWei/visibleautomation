package com.androidApp.Intercept;

import java.util.List;

import com.androidApp.EventRecorder.ClassCount;
import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ViewDirective;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

// when we issue a paste command, we paste the values on activity start for text views referenced by view directives.
public class PasteTextRunnable implements Runnable {
	protected Activity 			mActivity;
	protected EventRecorder		mRecorder;
	protected Instrumentation	mInstrumentation;

	public PasteTextRunnable(Instrumentation instrumentation, EventRecorder recorder, Activity activity) {
		mInstrumentation = instrumentation;
		mRecorder = recorder;
		mActivity = activity;
	}
	
	@Override
	public void run() {
		Window w = mActivity.getWindow();
        View v = w.getDecorView().findViewById(android.R.id.content);
        List<ViewDirective> pasteViewDirectives = mRecorder.getMatchingViewDirectives(mActivity, ViewDirective.When.ON_ACTIVITY_START);
        pasteValues(v, pasteViewDirectives);
	}
	
	public void pasteValues(View v, List<ViewDirective> pasteViewDirectives) {
		if (v instanceof EditText) {
			for (ViewDirective viewDirective : pasteViewDirectives) {
				if (viewDirective.match(v, ViewDirective.ViewOperation.PASTE_TEXT,  ViewDirective.When.ON_ACTIVITY_START)) {
					String value = mRecorder.getVariableValue(viewDirective.getVariable());
					if (value != null) {
						EditText et = (EditText) v;
						et.setText(value);
						String massagedString = StringUtils.escapeString(value.toString(), "\"", '\\').replace("\n", "\\n");
						String logString = viewDirective.getVariable() + "," + massagedString;
						mRecorder.writeRecord(Constants.EventTags.PASTE_TEXT, mActivity.toString(), v, logString);
					}
				}
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				pasteValues(vChild, pasteViewDirectives);
			}
		}	
	}	
}
