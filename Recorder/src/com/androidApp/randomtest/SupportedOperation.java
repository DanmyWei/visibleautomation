package com.androidApp.randomtest;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.Spinner;

public class SupportedOperation {

	/**
	 * supported operations
	 *
	 */
	public enum Operation {
		TOUCH(0x1, "touch"),						// touch a view
		CLICK(0x2, "click"),						// click a view
		SCROLL(0x3, "scroll"),						// scroll a view
		SCROLL_LIST(0x4, "scroll list"),			// scroll a list
		ENTER_TEXT(0x5, "enter text"),				// enter text in an EditText
		LONG_CLICK(0x7, "long click"),				// long click
		LIST_SELECT(0x8, "list select");			// select an item from a list.
		
		private Operation(int op, String s) {
			mOperation = op;
			mName = s;
		}
		
		private int mOperation;
		private String mName;
		
		public int getValue() {
			return mOperation;
		}
		
		public String getName() {
			return mName;
		}
	}
	
	protected Class mClass;
	protected Operation[] mSupportedOperations;
	
	public SupportedOperation(Class cls, Operation[] supportedOperations) {
		mClass = cls;
		mSupportedOperations = supportedOperations;
	}
	
	// supported operations table.
	public static final SupportedOperation[] SUPPORTED_OPERATIONS = {
		new SupportedOperation(View.class, new Operation[]{ Operation.TOUCH, Operation.CLICK, Operation.LONG_CLICK }),
		new SupportedOperation(EditText.class, new Operation[]{ Operation.ENTER_TEXT }),
		new SupportedOperation(AbsListView.class, new Operation[] { Operation.SCROLL_LIST, Operation.LIST_SELECT }),
		new SupportedOperation(Spinner.class, new Operation[] { Operation.LIST_SELECT })
	};
	
	/**
	 * return the supported operations for this view
	 * @param v
	 * @return list of operations that can be performed.
	 */
	public static List<Operation> getSupportedOperations(View v) {
		ArrayList<Operation> supportedOperations = new ArrayList<Operation>();
		for (SupportedOperation supportedOperation : SUPPORTED_OPERATIONS) {
			if (supportedOperation.mClass.isAssignableFrom(v.getClass())) {
				for (Operation  operation : supportedOperation.mSupportedOperations) {
					supportedOperations.add(operation);
				}
			}
		}
		return supportedOperations;
	}
}
