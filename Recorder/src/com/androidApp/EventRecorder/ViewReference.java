package com.androidApp.EventRecorder;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.TextView;

import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;
import com.androidApp.Utility.TestUtils;

public class ViewReference {
	private List<Object>		mRIDList;							// list of R.id classes used in the application
	private List<Object>		mRStringList;						// list of R.string classes used in the application.

	public ViewReference() {
		mRIDList = new ArrayList<Object>();
		mRStringList = new ArrayList<Object>();		
	}
	
	public void addRdotID(Object rdotid) {
		mRIDList.add(rdotid);
	}
	
	public void addRdotString(Object rdotstring) {
		mRStringList.add(rdotstring);
	}

	/** 
	 * for Robotium functions like clickInList(), the list is referenced by its index in the # of lists displayed on the screen
	 * @param v
	 * @return
	 */
	public static String getClassIndexReference(View v) {
		View rootView = v.getRootView();
		int classIndex = TestUtils.classIndex(rootView, v);
		return Constants.Reference.CLASS_INDEX + "," + v.getClass().getName() + "," + classIndex;	
	}

	/**
	 * Given a view, generate a reference for it so it can be found when the application is run again
	 * id, id: if the view has an ID, the ID is unique for all views, and the view is not a descendant of an AdapterView
	 * ex: id, R.id.button1 (if the ID is found in the R.id class).  id, 0x90300200 if not found
	 * text_id:  the view is a subclass of TextView, has text, the text is unique for all views, the view is not a child
	 * of adapterView.  The ID is the id of the leastmost ancestor of v with an id which is unique for all views
	 * ex: text_id, "escaped\ntext", R.id.parentID or text_id, "escaped text", 0x90300200
	 * class_index_id: The view does not have a unique id, use the view class name, the index of that class by pre-order
	 * traversal from the leastmost parent with an id which is unique for all views, and the view is not a descendant of
	 * an AdapterView
	 * ex: class_index_id, android.widget.Button, 5, R.id.parent or class_index_id, android.widget.Button, 5, 0x9030200
	 * list_index_id: The view is a descendant of an AdapterView, and has a unique ID within the set of views of its list
	 * element.  Index is the index within the AdapterView.
	 * ex: list_index_id, R.id.list, 5, R.id.address_text or list_index_id, R.id.list, 5, 0x9030201
	 * path:  a "dot" path of classnames from the root view of the activity, with indices for each class
	 * ex: android.widget.LinearLayout[0].android.widget.LinearLayout[1].android.widget.Button[2]  The index is of
	 * children of the same class type.
	 * @param v
	 * @return
	 */
	public String getReference(View v) throws IllegalAccessException {
	
		// first, try the id, and verify that it is unique.
		int id = v.getId();
		View rootView = v.getRootView();
		if (id != 0) {
			int idCount = TestUtils.idCount(rootView, id);
			if (idCount == 1) {
				return Constants.Reference.ID + "," + TestUtils.getIdForValue(mRIDList, id) + "," + v.getClass().getName();
			}
		}
		if (v instanceof TextView) {
			TextView tv = (TextView) v;
			String s = tv.getText().toString();
			View viewParentWithId = TestUtils.findParentViewWithId(v);
			if (viewParentWithId != null) {
				int parentIdCount = TestUtils.idCount(rootView, viewParentWithId.getId());
				if (parentIdCount == 1) {
					int textCount = TestUtils.textCount(viewParentWithId, s);
					if (textCount == 1) {
						return Constants.Reference.TEXT_ID + "," + TestUtils.getIdForValue(mRIDList, viewParentWithId.getId()) + "," + 
							    "\"" + StringUtils.escapeString(s, "\"", '\\') + "\"";
					}
				}
			}
		}
		Class viewClass = v.getClass();
		View viewParentWithId = TestUtils.findParentViewWithId(v);
		if (viewParentWithId != null) {
			int parentIdCount = TestUtils.idCount(rootView, viewParentWithId.getId());
			if (parentIdCount == 1) {
				int classIndex = TestUtils.classIndex(viewParentWithId, v);
				return Constants.Reference.CLASS_ID + "," +  TestUtils.getIdForValue(mRIDList, viewParentWithId.getId()) + "," + viewClass.getName() + "," + classIndex;
			} else {
				int classIndex = TestUtils.classIndex(rootView, v);
				return Constants.Reference.CLASS_INDEX + "," + viewClass.getName() + "," + classIndex;
			}
		}
		return Constants.Reference.UNKNOWN;	
	}

}
