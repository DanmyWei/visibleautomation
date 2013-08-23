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
import android.widget.TextView;

// when we issue a copy command, we paste the values on activity end for text views referenced by view directives.
public class CopyTextRunnable implements Runnable {
	protected Activity 			mActivity;
	protected EventRecorder		mRecorder;
	
	public CopyTextRunnable(EventRecorder recorder, Activity activity) {
		mRecorder = recorder;
		mActivity = activity;
	}
	
	@Override
	public void run() {
		Window w = mActivity.getWindow();
        View v = w.getDecorView().findViewById(android.R.id.content);
        List<ViewDirective> pasteViewDirectives = mRecorder.getMatchingViewDirectives(mActivity, ViewDirective.When.ON_ACTIVITY_START);
        copyValues(v, pasteViewDirectives);
	}
	
	public void copyValues(View v, List<ViewDirective> pasteViewDirectives) {
		if (v instanceof TextView) {
			for (ViewDirective viewDirective : pasteViewDirectives) {
				if (viewDirective.match(v, ViewDirective.ViewOperation.COPY_TEXT,  ViewDirective.When.ON_ACTIVITY_END)) {
					TextView tv = (TextView) v;
					String value = tv.getText().toString();
					mRecorder.setVariableValue(viewDirective.getVariable(), value);
					String massagedString = StringUtils.escapeString(value.toString(), "\"", '\\').replace("\n", "\\n");
					String logString = viewDirective.getVariable() + "," + massagedString;
					mRecorder.writeRecord(Constants.EventTags.COPY_TEXT, v, logString);
				}
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				copyValues(vChild, pasteViewDirectives);
			}
		}	
	}	
}
