package com.androidApp.util;

/**
 * test exception, so we have our own exception type
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class TestException extends Exception {
	static final long serialVersionUID = 0;
	
	public TestException(String s) {
		super(s);
	}

}