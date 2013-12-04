package com.androidApp.EventRecorder;

import java.util.ArrayList;
import java.util.List;

import com.androidApp.Intercept.MagicFrame;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * for a fully-qualified hierarchical reference to a view. NOTE: For the child index, we DO NOT take the
 * actual child index, but rather the index of the items with a matching class.  This makes it a bit more
 * robust when elements are inserted and removed from the tree.  If we do pure-child, then any child inserted
 * before the referred child in the "thread" down the window tree will interfere with the reference, but
 * with class filtering, we can at least be stable unless inserted child is of the same type, which is
 * less likely
 * References look like this:
 * FrameLayout.LinearLayout[0].RelativeLayout[2].ImageView[0]
 * The first reference is neccessary for the content view (the child of the root), so it doesn't need
 * an index (it defaults to -1).  We could do it by default, but the syntax is a bit nicer, plus it retains
 * the root classname.
 * @author matt2
 *
 */

public class HierarchyRef {
	public String mClassName;			// simple name of the class. There may be ambiguity, but the references would just be too long
	public int mChildIndex;				// index of the child (filtered by the child's class), or -1 for the top view
	
	public HierarchyRef(String className, int childIndex) {
		mClassName = className;
		mChildIndex = childIndex;
	}
		
	// parse a hierarchy reference from the expression Class[index] or Class
	public HierarchyRef(String ref) {
		int ichOpen = ref.indexOf('[');
		int ichClose = ref.indexOf(']');
		if (ichOpen != -1 && ichClose != -1) {
			mClassName = ref.substring(0, ichOpen);
			String indexString = ref.substring(ichOpen + 1, ichClose);
			mChildIndex = Integer.parseInt(indexString);
		} else {
			mClassName = ref;
			mChildIndex = -1;
		}
	}
	
	// get the hierarchy reference for a parent-child reference
	public HierarchyRef(View v, ViewGroup vParent) {
		int classCount = 0;
		for (int iChild = 0; iChild < vParent.getChildCount(); iChild++) {
			View vChild = vParent.getChildAt(iChild);
			if (vChild == v) {
				mClassName = v.getClass().getSimpleName();
				mChildIndex = classCount;
				break;
			}
			if (vChild.getClass() == v.getClass()) {
				classCount++;
			}
		}
	}
	
	// simply the root class name
	public HierarchyRef(View v) {
		mClassName = v.getClass().getSimpleName();
		mChildIndex = -1;
	}
	
	// get the child specified from the hierarchy reference
	public View getChild(ViewGroup vParent) {
		int classCount = 0;
		for (int iChild = 0; iChild < vParent.getChildCount(); iChild++) {
			View vChild = vParent.getChildAt(iChild);
			if (vChild.getClass().getSimpleName().equals(mClassName)) {
				if (classCount == mChildIndex) {
					return vChild;
				}
				classCount++;
			}
		}
		return null;
	}
	
	// return className for root, className[childIndex] for child
	public String toString() {
		if (mChildIndex == -1) {
			return mClassName;
		} else {
			return mClassName + '[' + Integer.toString(mChildIndex) + ']';
		}
	}
	
	// split.a[1].hierarchy[0].reference[2].list[0] into tokens and construct a reference for each one
	public static HierarchyRef[] hierarchyReference(String expr) {
		String[] refs = expr.split("\\.");
		HierarchyRef[] refExprs = new HierarchyRef[refs.length];
		for (int iRef = 0; iRef < refs.length; iRef++) {
			refExprs[iRef] = new HierarchyRef(refs[iRef]);
		}
		return refExprs;
	}
	
	public static String referenceToString(HierarchyRef[] refExprs) {
		StringBuffer sb = new StringBuffer();
		for (int iRef = 0; iRef < refExprs.length; iRef++) {
			sb.append(refExprs[iRef]);
			if (iRef < refExprs.length - 1) {
				sb.append('.');
			}
		}
		return sb.toString();
	}
	
	// get the hierarchy reference for a view
	public static HierarchyRef[] hierarchyReference(View v) {
		ArrayList<HierarchyRef> refList = new ArrayList<HierarchyRef>();
		hierarchyReference(v, v.getRootView(), refList);
		HierarchyRef[] refArray = new HierarchyRef[refList.size()];
		return refList.toArray(refArray);
	}
	
	// NOTE: the magic frame has to be removed, because it's reparenting the content on record,
	// but not on playback.
	public static void hierarchyReference(View v, View vRoot, List<HierarchyRef> refList) {
		while ((v != vRoot) && (v.getParent() != vRoot) && !(v.getParent() instanceof MagicFrame)) {
			ViewParent vParent = v.getParent();
			if (vParent instanceof ViewGroup) {
				HierarchyRef ref = new HierarchyRef(v, (ViewGroup) vParent);
				refList.add(0, ref);
			}
			v = (View) v.getParent();
		}
		HierarchyRef rootRef = new HierarchyRef(v);
		refList.add(0, rootRef);
	}
	
	// get the view for a reference (skip the root, but we should check it)
	
	public static View getView(View view, HierarchyRef[] reference) {
		// bypass the phoneDecorView, and MagicFrame if it's been inserted. (not on playback)
		View rootView = view.getRootView();
		View contentView = ((ViewGroup) rootView).getChildAt(0);
		if (contentView instanceof MagicFrame) {
			contentView = ((ViewGroup) contentView).getChildAt(0);
		}
		for (int iRef = 1; iRef < reference.length; iRef++) {
			contentView = reference[iRef].getChild((ViewGroup) contentView);
			if (contentView == null) {
				return null;
			}
		}
		return contentView;
	}
	
}
