package com.koch.ambeth.security.server.exceptions;

import java.lang.reflect.Method;

public class AuthenticationMissingException extends SecurityException
{
	private static final long serialVersionUID = -1163535828252169857L;

	public AuthenticationMissingException(Method method)
	{
		super("No authentication handle found - Requested to access '" + method.toGenericString() + "'");
	}
}
