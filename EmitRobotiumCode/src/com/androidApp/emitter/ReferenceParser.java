package com.androidApp.emitter;

import java.util.List;

import com.androidApp.util.Constants;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

/**
 * parse a reference to an android view, either by unique id, which can be a hexadecimal, or a resource id, 
 * the class of the view, and its index relative to the root view, or a parent with a unique id, or for
 * text views, a string, and the unique id of its ancestor.
 * @author Matthew
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class ReferenceParser {
	public enum ReferenceType {
		ID,							// resource id (better be unique)
		CLASS_INDEX,				// class index
		INTERNAL_CLASS_INDEX,		// internal class index: contains private and public classes
		CLASS_INDEX_ID,				// class index relative to ancestor ID
		TEXT_ID,					// text and ancestor ID
		UNKNOWN						// couldn't parse expression
	}
	
	public enum TextType {
		HARDCODED,			// "foo"
		RESOURCE			// fully qualified resource ID com.example.ApiDemos.R.string.foo
	}
	
	public enum IDType {
		HARDCODED,			// 0x<hexcode>
		RESOURCE			// fully qualified resource ID com.example.ApiDemos.R.id.foo
	}
	
	protected String 		mID;							// android ID of view (either hardcoded hex number or fully qualified resource id)
	protected String 		mClass;							// view class (fully qualified)
	protected String		mInternalClass;					// for Class.forName() internal class (fully qualified)
	protected String		mText;							// hardcoded quoted string or fully qualified resource id
	protected int 			mIndex;							// class index (if id != null, relative to ancestor, not to entire view hierarchy)
	protected ReferenceType	mType;							// describes how to decompose the reference
	protected TextType		mTextType;						// string or resource iff mType == TEXT_ID
	protected IDType		mIDType;						// hardcoded or resource if mType == TEXT_ID, ID or CLASS_INDEX_ID
	
	// class_index,class android.widget.ListView,1
	// id,com.example.android.apis.R$id.radio_button
	
	public ReferenceParser(List<String> parts, int startIndex) {
		mType = ReferenceType.UNKNOWN;
		mID = null;
		mClass = null;
		mIndex = -1;
		mTextType = TextType.HARDCODED;
		mIDType = IDType.HARDCODED;
		if (parts.get(startIndex).equals(Constants.CLASS_INDEX)) {
			mType = ReferenceType.CLASS_INDEX;
			mClass = parts.get(startIndex + 1);
			mIndex = Integer.parseInt(parts.get(startIndex + 2));	
		} else if (parts.get(startIndex).equals(Constants.INTERNAL_CLASS_INDEX)) {
			mType = ReferenceType.INTERNAL_CLASS_INDEX;
			mClass = parts.get(startIndex + 2);
			mInternalClass = parts.get(startIndex + 1);
			mIndex = Integer.parseInt(parts.get(startIndex + 3));
		} else if (parts.get(startIndex).equals(Constants.CLASS_INDEX_ID)) {
			mType = ReferenceType.CLASS_INDEX_ID;
		} else if (parts.get(startIndex).equals(Constants.ID)) {
			mType = ReferenceType.ID;
			mID = parts.get(startIndex + 1);
			mClass = parts.get(startIndex + 2);
			if (StringUtils.isHexNumber(mID)) {
				mIDType = IDType.HARDCODED;
			} else {
				mIDType = IDType.RESOURCE;
				mID = mID.replace('$', '.');
			}
		} else if (parts.get(startIndex).equals(Constants.TEXT_ID)) {
			mType = ReferenceType.TEXT_ID;
			mID = parts.get(startIndex + 1);
			if (StringUtils.isHexNumber(mID)) {
				mIDType = IDType.HARDCODED;
			} else {
				mID = mID.replace('$', '.');
				mIDType = IDType.RESOURCE;
			}
			mText = parts.get(startIndex + 2);
			if (StringUtils.isQuotedString(mText)) {
				mTextType = TextType.HARDCODED;
			} else {
				mTextType = TextType.RESOURCE;
			}
		} else {
			mType = ReferenceType.UNKNOWN;
		}	
	}
	
	// accessors
	public ReferenceType getReferenceType() {
		return mType;
	}
	
	public String getID() {
		return mID;
	}
	
	public IDType getIDType() {
		return mIDType;
	}
	
	public String getText() {
		return mText;
	}
	
	public TextType getTextType() {
		return mTextType;
	}
	
	public int getIndex() {
		return mIndex;
	}
	
	public String getClassName() {
		return mClass;
	}
	
	public String getInternalClassName() {
		return mInternalClass;
	}
	
	/**
	 * test for equivalent references.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ReferenceParser)) {
			return false;
		}
		ReferenceParser b = (ReferenceParser) obj;
		if (mType != b.mType) {
			return false;
		}
		switch (mType) {
		case ID:
			if ((mID == null) || (b.mID == null)) {
				return false;
			}
			return mID.equals(b.mID);
		case CLASS_INDEX:
			if ((mClass == null) || (b.mClass == null)) {
				return false;
			}
			if ((mIndex == -1) || (b.mIndex == -1)) {
				return false;
			}
			return mClass.equals(b.mClass) && (mIndex == b.mIndex);
		case CLASS_INDEX_ID:
			if ((mClass == null) || (b.mClass == null)) {
				return false;
			}
			if ((mIndex == -1) || (b.mIndex == -1)) {
				return false;
			}
			if ((mID == null) || (b.mID == null)) {
				return false;
			}			
			return mID.equals(b.mID) && mClass.equals(b.mClass) && (mIndex == b.mIndex);
		case TEXT_ID:
			if ((mID == null) || (b.mID == null)) {
				return false;
			}			
			if ((mText == null) || (b.mID == null)) {
				return false;
			}	
			return mID.equals(b.mID) && mText.equals(b.mText); 
		case UNKNOWN:
			return false;
		default:
			return false;
		}
	}
}
