package com.koch.ambeth.service.exceptions;

public class ServiceCallForbiddenException extends SecurityException
{
	private static final long serialVersionUID = -5303216306654899560L;

	public ServiceCallForbiddenException(String message)
	{
		super(message);
	}
}
