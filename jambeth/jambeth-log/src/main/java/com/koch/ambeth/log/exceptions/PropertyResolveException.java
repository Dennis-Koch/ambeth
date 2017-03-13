package com.koch.ambeth.log.exceptions;

public class PropertyResolveException extends RuntimeException
{
	private static final long serialVersionUID = -7149333583589679782L;

	public PropertyResolveException()
	{
		super();
	}

	protected PropertyResolveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PropertyResolveException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PropertyResolveException(String message)
	{
		super(message);
	}

	public PropertyResolveException(Throwable cause)
	{
		super(cause);
	}

}
