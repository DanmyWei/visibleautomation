package com.androidApp.savestate;

/**
 * specialized exception for when grabbing the save state files fails
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved
 * @author matt2
 *
 */
public class SaveStateException extends Exception {
	static final long serialVersionUID = 0;
	
	public SaveStateException(String s) {
		super(s);
	}
}
