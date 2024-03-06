package com.behl.overseer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.NonNull;

public class InvalidPlanException extends ResponseStatusException {

	private static final long serialVersionUID = 4506094675559975006L;

	public InvalidPlanException(@NonNull final String reason) {
		super(HttpStatus.NOT_FOUND, reason);
	}

}
