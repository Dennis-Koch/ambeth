package com.koch.ambeth.util.exception;

public class MaskingRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1999600062155801713L;

	public MaskingRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MaskingRuntimeException(Throwable cause)
	{
		super(null, cause);
	}
}
