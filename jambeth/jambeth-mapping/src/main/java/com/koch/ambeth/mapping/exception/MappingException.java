package com.koch.ambeth.mapping.exception;

public class MappingException extends RuntimeException
{
	private static final long serialVersionUID = -3291616900591611906L;

	public MappingException()
	{
		// Intended blank
	}

	public MappingException(String message)
	{
		super(message);
	}

	public MappingException(Throwable cause)
	{
		super(cause);
	}

	public MappingException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
