package de.osthus.ambeth.exceptions;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ServiceCallForbiddenException extends RuntimeException
{
	private static final long serialVersionUID = -5303216306654899560L;

	@SuppressWarnings("unused")
	@LogInstance(ServiceCallForbiddenException.class)
	private ILogger log;

	public ServiceCallForbiddenException(String message)
	{
		super(message);
	}
}
