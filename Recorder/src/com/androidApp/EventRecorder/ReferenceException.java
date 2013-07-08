package com.androidApp.EventRecorder;

/**
 *  Exception thrown by the UserDefinedViewReference code (currently used for listening to motion events)
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 * @author matt2
 *
 */
public class ReferenceException extends Exception {
	static final long serialVersionUID = 0;
	
	public ReferenceException(String s) {
		super(s);
	}
}
