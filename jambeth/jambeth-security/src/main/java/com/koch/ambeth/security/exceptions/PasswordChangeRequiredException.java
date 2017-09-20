package com.koch.ambeth.security.exceptions;

public class PasswordChangeRequiredException extends SecurityException {
	private static final long serialVersionUID = 3994482018658533249L;

	public PasswordChangeRequiredException() {
		super();
	}

	public PasswordChangeRequiredException(String s) {
		super(s);
	}
}
