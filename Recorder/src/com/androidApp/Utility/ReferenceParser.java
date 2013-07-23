package com.androidApp.Utility;

import java.util.ArrayList;
import java.util.List;

// using reflection, parse a syntax, and return the specified field
// Reference = [FieldReference class].Reference
// FieldReference = <identifier | identifier[index]> where index is an array reference

public class ReferenceParser {
	
	// [split][a][string [of nested bracket references]][into][their top][level reference]
	// like split,a,string [of nested bracket references],into,their top,level reference
	protected static List<String> splitReferences(String reference) {
		int nestLevel = 0;
		ArrayList<String> referenceList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int ich = 0; ich < reference.length(); ich++) {
			char ch = reference.charAt(ich);
			if (ch == '[') {
				nestLevel++;
			} else if (ch == ']') {
				nestLevel--;
				if (nestLevel == 0) {
					referenceList.add(sb.toString());
				}
			} else {
				if (nestLevel > 0) {
					sb.append(ch);
				}
			}
		}
		return referenceList;
	}
	
	// is s of the form x[0-9]?
	protected static boolean isArrayRef(String s) {
		int ichOpen = s.lastIndexOf('[');
		int ichClose = s.lastIndexOf(']');
		if ((ichOpen != -1) && (ichClose != -1)) {
			for (int ichNum = 0; ichNum < ichClose; ichNum++) {
				if (!Character.isDigit(ichNum)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	protected static int getArrayIndex(String s) {
		int ichOpen = s.lastIndexOf('[');
		int ichClose = s.lastIndexOf(']');
		return Integer.parseInt(s.substring(ichOpen + 1, ichClose - 1));
	}
	
	protected String getArrayName(String s) {
		int ichOpen = s.lastIndexOf('[');
		return s.substring(0, ichOpen - 1);
	}
	
	public Object getReference(Object obj, String reference)  throws Exception, NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
		List<String> references = splitReferences(reference);
		Object objRef = null;
		for (String fieldRef : references) {
			String[] refPair = fieldRef.split(" ");
			Class cls = Class.forName(refPair[1]);
			if (isArrayRef(refPair[0])) {
				int index = getArrayIndex(refPair[0]);
				String fieldName = getArrayName(refPair[0]);
				objRef = ReflectionUtils.getFieldValue(obj, cls, fieldName);
				if (objRef instanceof List) {
					objRef = ((List) objRef).get(index);
				} else if (objRef instanceof Object[]) {
					objRef = ((Object[]) objRef)[index];				
				} else {
					throw new Exception("not an array or list reference");
				}
				obj = objRef;
				if (obj == null) {
					return obj;
				}
			} else {
				String fieldName = refPair[0];
				obj = ReflectionUtils.getFieldValue(objRef,  cls, fieldName);
			}
			
		}
		return obj;
	}
}
