package com.koch.ambeth.ioc.exception;

public class ExtendableException extends RuntimeException
{
	private static final long serialVersionUID = 6667080317792337677L;

	public ExtendableException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ExtendableException(String message)
	{
		super(message);
	}
}
