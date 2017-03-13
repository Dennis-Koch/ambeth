package com.koch.ambeth.ioc.exception;

import com.koch.ambeth.util.exception.MaskingRuntimeException;

public class BeanContextInitException extends RuntimeException
{
	private static final long serialVersionUID = 7291486344156039797L;

	protected static Throwable extractUnmaskedThrowable(Throwable cause)
	{
		if (cause instanceof MaskingRuntimeException)
		{
			if (((MaskingRuntimeException) cause).getMessage() == null)
			{
				return extractUnmaskedThrowable(cause.getCause());
			}
		}
		return cause;
	}

	public BeanContextInitException(String message, Throwable cause)
	{
		super(message, extractUnmaskedThrowable(cause));
	}

	public BeanContextInitException(String message)
	{
		super(message);
	}
}
