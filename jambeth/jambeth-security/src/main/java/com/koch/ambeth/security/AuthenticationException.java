package com.koch.ambeth.security;

public class AuthenticationException extends RuntimeException
{
	private static final long serialVersionUID = 2211300283992879898L;

	public AuthenticationException()
	{
		super();
	}

	public AuthenticationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public AuthenticationException(String message)
	{
		super(message);
	}

	public AuthenticationException(Throwable cause)
	{
		super(cause);
	}

}
