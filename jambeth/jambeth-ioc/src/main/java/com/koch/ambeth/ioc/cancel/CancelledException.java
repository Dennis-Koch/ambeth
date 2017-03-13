package com.koch.ambeth.ioc.cancel;

/**
 * Thrown by {@link ICancellation#ensureNotCancelled()} if {@link ICancellation#isCancelled()} returns true
 */
public class CancelledException extends RuntimeException
{
	private static final long serialVersionUID = -3110298100220185217L;

	public CancelledException()
	{
	}

	public CancelledException(String message)
	{
		super(message);
	}

	public CancelledException(Throwable cause)
	{
		super(cause);
	}

	public CancelledException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CancelledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
