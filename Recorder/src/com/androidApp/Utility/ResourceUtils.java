package com.androidApp.Utility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;

/**
 * utility functions to get resource names for ID's and strings, so our output isn't getView(0xdeadbeef)
 * @author matt2
 *
 */
public class ResourceUtils {

	/**
	 * given a string (probably yanked from a TextView), return the matching R.string references (there may be more than one)
	 * @param res - application resources
	 * @param rdotstring - R.string class
	 * @param s string to compare against
	 * @return List<String> list of matching strings.
	 * @throws IllegalAccessException
	 */
	public static List<String> getIdForString(Resources res, Object rdotstring, String s) throws IllegalAccessException {
		List<String> resultList = new ArrayList<String>();
		Class cls = rdotstring.getClass();
		Field[] fieldList = cls.getDeclaredFields();
		for (Field field : fieldList) {
			int fieldValue = field.getInt(rdotstring);
			String candString = res.getString(fieldValue);
			if (s.equals(candString)) {
				String reference = cls.getName() + "." + field.getName();
				resultList.add(reference);
			}
		}
		return resultList;
	}

	/**
	 * Given a R.id class which is generated from the XML files for Android, and a value for an id from a view, return the fully qualified
	 * field name and class name, for example: com.example.R.id.cancel_button
	 * @param r R.id class to search
	 * @param idValue value to search for in fields
	 * @return R.id.foo or null if not found.
	 */
	public static String getIdForValue(Object rdotid, int idValue) throws IllegalAccessException {
		Class cls = rdotid.getClass();
		Field[] fieldList = cls.getDeclaredFields();
		for (Field field : fieldList) {
			int fieldValue = field.getInt(rdotid);
			if (fieldValue == idValue) {
				return cls.getName() + "." + field.getName();
			}
		}
		return null;
	}

	/**
	 * same thing, except iterate over the list of R.string classes.
	 * @param res
	 * @param rdotstringlist
	 * @param s
	 * @return
	 * @throws IllegalAccessException
	 */
	public static List<String> getIdForString(Resources res, List<Object> rdotstringlist, String s) throws IllegalAccessException {
		List<String> resultList = new ArrayList<String>();
		for (Object rdotstring : rdotstringlist) {
			List<String> someResults = getIdForString(res, rdotstring, s);
			resultList.addAll(someResults);
		}
		return resultList;
	}

	/**
	 * iterate over the list of Android-generated "R.id" classes, see if idValue occurs in any of them
	 * @param rdotidlist list of com.myexample.R classes
	 * @param idValue id to search for
	 * @return class/field reference for id, or null.
	 * @throws IllegalAccessException
	 */
	public static String getIdForValue(List<Object> rdotidlist, int idValue) throws IllegalAccessException {
		for (Object rdotid : rdotidlist) {
			String id = getIdForValue(rdotid, idValue);
			if (id != null) {
				return id;
			}
		}
		return "0x" + Integer.toHexString(idValue);
	}

}
