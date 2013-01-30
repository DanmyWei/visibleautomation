package com.androidApp.emitter;

import java.util.List;

import com.androidApp.util.Constants;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

public class ReferenceParser {
	public enum ReferenceType {
		ID,					// resource id (better be unique)
		CLASS_INDEX,		// class index
		CLASS_INDEX_ID,		// class index relative to ancestor ID
		TEXT_ID,			// text and ancestor ID
		UNKNOWN				// couldn't parse expression
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
		mTextType = TextType.HARDCODED;
		mIDType = IDType.HARDCODED;
		if (parts.get(startIndex).equals(Constants.CLASS_INDEX)) {
			mType = ReferenceType.CLASS_INDEX;
			mClass = parts.get(startIndex + 1);
			mIndex = Integer.parseInt(parts.get(startIndex + 2));	
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
}
