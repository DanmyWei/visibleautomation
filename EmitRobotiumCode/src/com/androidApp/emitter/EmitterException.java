package com.androidApp.emitter;

/**
 * so we can have a custom exception for emitter errors
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class EmitterException extends Exception {
	static final long serialVersionUID = 0;
	
	public EmitterException(String s) {
		super(s);
	}
}
