package com.koch.ambeth.audit.server.exceptions;

public class AuditReasonMissingException extends RuntimeException
{
	private static final long serialVersionUID = -8001601509238027891L;

	public AuditReasonMissingException(String message)
	{
		super(message);
	}
}
