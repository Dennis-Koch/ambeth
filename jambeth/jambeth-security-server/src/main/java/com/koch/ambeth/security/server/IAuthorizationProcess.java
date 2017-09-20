package com.koch.ambeth.security.server;

import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.exceptions.AuthenticationMissingException;
import com.koch.ambeth.security.exceptions.InvalidUserException;

public interface IAuthorizationProcess {

	/**
	 * @exception RuntimeException
	 *              if a explicit {@link IAuthorizationExceptionFactory}-bean is configured to deal
	 *              with exceptions.
	 * @exception AuthenticationMissingException
	 *              if the IAuthorizationExceptionFactory is not defined and no authentication handle
	 *              is available to work with ( {@link ISecurityContext#getAuthentication()})
	 * @exception InvalidUserException
	 *              if the IAuthorizationExceptionFactory is not defined and the verification of the
	 *              authentication handle failed. That is: If no {@link IAuthorization}-handle could
	 *              be created
	 */
	void ensureAuthorization();
}