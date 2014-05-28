package de.osthus.ambeth.ioc.exception;

public class BeanContextDeclarationException extends RuntimeException
{
	private static final long serialVersionUID = -7902605225571344449L;

	public BeanContextDeclarationException(StackTraceElement[] stackTrace)
	{
		this(stackTrace, null);
	}

	public BeanContextDeclarationException(StackTraceElement[] stackTrace, Throwable cause)
	{
		super("Declaration at", cause);
		setStackTrace(stackTrace);
	}
}
