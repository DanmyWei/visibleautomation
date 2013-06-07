package com.androidApp.emitter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.androidApp.util.StringUtils;

/** table used to generate unique identifiers on a per-class basis, and the import list, so we don't have to give the
 * entire class name when declaring the variable.
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */

public class ClassTable {
	protected class ClassData {
		public int			mVariableIndex;				// unique index used when no naming information is available
		public List<String>	mVariableNames;				// list of variable names to check for uniqueness.
		
		public ClassData() {
			mVariableIndex = 0;
			mVariableNames = new ArrayList<String>();
		}
	}
	
	protected Hashtable<String, ClassData> mClassTable;
	
	public ClassTable() {
		mClassTable = new Hashtable<String, ClassData>();
	}
	
	// add a class to the hashtable.
	public boolean addClass(String className) {
		ClassData classData = mClassTable.get(className);
		if (classData == null) {
			classData = new ClassData();
			mClassTable.put(className, classData);
			return true;
		} else {
			classData.mVariableIndex++;
			return false;
		}
	}
	
	/**
	 * is this a unique variable name for this class in the hash table
	 * @param className name of class to search the hashtable
	 * @param variableName variable name to test against.
	 * @return true if the variable name hasn't been used yet.  false if it's been used before.
	 */
	public boolean isUniqueVariableName(String className, String variableName) {
		ClassData classData = mClassTable.get(className);
		for (Iterator<String> iterVar = classData.mVariableNames.iterator(); iterVar.hasNext(); ) {
			String variableNameCand = iterVar.next();
			if (variableNameCand.equals(variableName)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * given a fully qualified path name, like android.view.TextView, return a variable name with the appropriate index
	 * for that class, like TextView14. NOTE: this functon is NOT idempotent, since it increments the variable index
	 * for that class.
	 * @param className
	 * @return
	 */
	public String getVariableName(String className) {
		ClassData classData = mClassTable.get(className);
		if (classData == null) {
			addClass(className);
			classData = mClassTable.get(className);
		}
		String varname = StringUtils.getNameFromClassPath(className) + Integer.toString(classData.mVariableIndex);
		classData.mVariableIndex++;
		return varname;
	}
	
	/**
	 * return the list of package names for use in import statements in the output code.  We don't prefix the "import" or suffix the ";",
	 * because hashtables for local functions have to be merged.
	 * @return
	 */
	public List<String> generateImports() {
		List<String> importNames = new ArrayList<String>();
		for (Enumeration<String> enumeration = mClassTable.keys(); enumeration.hasMoreElements(); ) {
			String className = enumeration.nextElement();
			String packageName = StringUtils.getPackageFromClassPath(className);
			importNames.add(packageName);
				
		}
		return importNames;
	}
	
	/**
	 * merge the import lists from the class tables into a unique set of imported packages.
	 * @param importListList list of list of packages extracted from the class tables
	 * @return unique set of package names.
	 */
	public static String[] mergeImports(List<List<String>> importListList) {
		Set<String> importSet = new HashSet<String>();
		for (Iterator<List<String>> iterList = importListList.iterator(); iterList.hasNext(); ) {
			List<String> importList = iterList.next();
			for (Iterator<String> iter = importList.iterator(); iter.hasNext(); ) {
				String packageName = iter.next();
				importSet.add(packageName);
			}
		}
		return (String []) importSet.toArray();
	}
}
