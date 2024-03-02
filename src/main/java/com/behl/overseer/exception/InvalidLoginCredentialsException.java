package com.behl.overseer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidLoginCredentialsException extends ResponseStatusException {

	private static final long serialVersionUID = 7439642984069939024L;
	private static final String DEFAULT_MESSAGE = "Invalid login credentials provided";

	public InvalidLoginCredentialsException() {
		super(HttpStatus.UNAUTHORIZED, DEFAULT_MESSAGE);
	}

}