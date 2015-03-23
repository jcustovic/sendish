package com.sendish.api.exception;

public class ResizeFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ResizeFailedException(Exception e) {
		super(e);
	}

}
