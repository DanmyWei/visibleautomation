package com.androidApp.Listeners;

import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FieldUtils;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;
import com.androidApp.Utility.ViewExtractor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

// base class for all listeners, implements common functions
public class RecordListener {
	
	
	// get a description of a view.
	public static String getDescription(View v) {
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			return StringUtils.massageString(tv.getText().toString());
		} else if (v instanceof ImageView) {
			return Constants.Description.IMAGE_VIEW;
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < vg.getChildCount(); i++) {
				View vChild = vg.getChildAt(i);
				if (vChild instanceof TextView) {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					TextView tv = (TextView) vChild;
					sb.append(tv.getText().toString());
				} else if (vChild instanceof ViewGroup) {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					sb.append(getDescription(vChild));
				}
			}
			return StringUtils.massageString(sb.toString());
		} else {
			return v.getClass().getSimpleName();
		}
	}
	
	// get the description for a dialog (try to extract its title)
	public static String getDescription(DialogInterface dialogInterface) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException  {
		Dialog dialog = (Dialog) dialogInterface;
		Window window = dialog.getWindow();
		Class phoneWindowClass = Class.forName(Constants.Classes.PHONE_WINDOW_CLASS);
		String titleString = (String) FieldUtils.getFieldValue(window, phoneWindowClass, Constants.Fields.TITLE);
		if (titleString != null) {
			return StringUtils.massageString(titleString);
		} else {
			View dialogView = window.getDecorView();
			View dialogTitle = ViewExtractor.getChildByClassName(dialogView, Constants.Classes.DIALOG_TITLE_SIMPLE_NAME);
			titleString = (String) FieldUtils.getFieldValue(dialogTitle, TextView.class, Constants.Fields.TEXT);
			return StringUtils.massageString(titleString);
		}
	}
	
	// get the description of an object by its class index.  used for view groups, since the view getDescription()
	// will recurse down its children.
	
	public static String getDescriptionByClassIndex(View v) {
		View vRoot = v.getRootView();
		int index = TestUtils.classIndex(vRoot, v);
		return StringUtils.getOrdinal(index) + " " + v.getClass().getSimpleName();
	}
}
