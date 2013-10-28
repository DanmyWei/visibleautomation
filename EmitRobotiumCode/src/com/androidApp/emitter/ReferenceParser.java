package com.androidApp.emitter;

import java.util.List;

import com.androidApp.util.Constants;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

/**
 * parse a reference to an android view, either by unique id, which can be a hexadecimal, or a resource id, 
 * the class of the view, and its index relative to the root view, or a parent with a unique id, or for
 * text views, a string, and the unique id of its ancestor.
 * TODO: turn this into a class hierarchy based on the reference type.
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
	protected int 			mIndexShown;					// shown class index (if id != null, relative to ancestor, not to entire view hierarchy)
	protected int			mIndexReal;						// class index in the view hierarch (includes non-shown views)
	protected ReferenceType	mType;							// describes how to decompose the reference
	protected TextType		mTextType;						// string or resource iff mType == TEXT_ID
	protected IDType		mIDType;						// hardcoded or resource if mType == TEXT_ID, ID or CLASS_INDEX_ID
	protected int			mTokensConsumed;				// how many tokens were consumed to parse this reference?
	// class_index,class android.widget.ListView,1
	// id,com.example.android.apis.R$id.radio_button
	
	public ReferenceParser(List<String> parts, int startIndex) {
		mType = ReferenceType.UNKNOWN;
		mID = null;
		mClass = null;
		mIndexShown = -1;
		mIndexReal = -1;
		mTextType = TextType.HARDCODED;
		mIDType = IDType.HARDCODED;
		if (parts.get(startIndex).equals(Constants.CLASS_INDEX)) {
			mType = ReferenceType.CLASS_INDEX;
			mClass = parts.get(startIndex + 1);
			mIndexShown = Integer.parseInt(parts.get(startIndex + 2));	
			mIndexReal = Integer.parseInt(parts.get(startIndex + 3));	
			mTokensConsumed = 4;
		} else if (parts.get(startIndex).equals(Constants.INTERNAL_CLASS_INDEX)) {
			mType = ReferenceType.INTERNAL_CLASS_INDEX;
			mClass = parts.get(startIndex + 2);
			mInternalClass = parts.get(startIndex + 1);
			mIndexShown = Integer.parseInt(parts.get(startIndex + 3));
			mIndexReal = Integer.parseInt(parts.get(startIndex + 4));	
			mTokensConsumed = 4;
		} else if (parts.get(startIndex).equals(Constants.CLASS_INDEX_ID)) {
			mType = ReferenceType.CLASS_INDEX_ID;
			mTokensConsumed = 1;
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
			mTokensConsumed = 3;
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
			mTokensConsumed = 3;
		} else {
			mType = ReferenceType.UNKNOWN;
			mTokensConsumed = 0;
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
	
	public int getShownIndex() {
		return mIndexShown;
	}
	
	public int getRealIndex() {
		return mIndexReal;
	}
	
	public String getClassName() {
		return mClass;
	}
	
	public String getInternalClassName() {
		return mInternalClass;
	}
	
	public int getTokensConsumed() {
		return mTokensConsumed;
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
		case INTERNAL_CLASS_INDEX:
			if ((mClass == null) || (b.mClass == null)) {
				return false;
			}
			if ((mIndexShown == -1) || (b.mIndexShown == -1)) {
				return false;
			}
			if ((mIndexReal == -1) || (b.mIndexReal == -1)) {
				return false;
			}
			return mClass.equals(b.mClass) && (mIndexShown == b.mIndexShown) && (mIndexReal == b.mIndexReal);
		case CLASS_INDEX_ID:
			if ((mClass == null) || (b.mClass == null)) {
				return false;
			}
			if ((mIndexShown == -1) || (b.mIndexShown == -1)) {
				return false;
			}
			if ((mIndexReal == -1) || (b.mIndexReal == -1)) {
				return false;
			}
			if ((mID == null) || (b.mID == null)) {
				return false;
			}			
			return mClass.equals(b.mClass) && (mIndexShown == b.mIndexShown) && (mIndexReal == b.mIndexReal);
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
