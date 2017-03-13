package com.koch.ambeth.cache.exception;

public class LazyInitializationException extends RuntimeException
{
	private static final long serialVersionUID = 5454657483598635935L;

	public LazyInitializationException()
	{
		// Intended blank
	}

	public LazyInitializationException(String message)
	{
		super(message);
		// Intended blank
	}

	public LazyInitializationException(Throwable cause)
	{
		super(cause);
		// Intended blank
	}

	public LazyInitializationException(String message, Throwable cause)
	{
		super(message, cause);
		// Intended blank
	}
}
