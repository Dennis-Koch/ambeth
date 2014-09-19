package de.osthus.ambeth.exceptions;


public class ServiceCallForbiddenException extends RuntimeException
{
	private static final long serialVersionUID = -5303216306654899560L;

	public ServiceCallForbiddenException(String message)
	{
		super(message);
	}
}
