package com.koch.ambeth.security.server;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationManager;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.ISidHelper;
import com.koch.ambeth.security.events.AuthorizationMissingEvent;
import com.koch.ambeth.security.exceptions.PasswordChangeRequiredException;
import com.koch.ambeth.security.server.exceptions.AuthenticationMissingException;
import com.koch.ambeth.security.server.exceptions.InvalidUserException;
import com.koch.ambeth.service.model.ISecurityScope;

public class AuthorizationProcess implements IAuthorizationProcess {
	private static final ThreadLocal<Boolean> ignoreInvalidUserTL = new ThreadLocal<>();

	public static final String HANDLE_AUTHORIZATION_MSSING = "handleAuthorizationMissing";

	public static boolean setIgnoreInvalidUser(boolean value) {
		Boolean oldValue = ignoreInvalidUserTL.get();
		if (value) {
			ignoreInvalidUserTL.set(Boolean.TRUE);
		}
		else {
			ignoreInvalidUserTL.set(null);
		}
		return oldValue != null ? oldValue.booleanValue() : false;
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuthenticationManager authenticationManager;

	@Autowired(optional = true)
	protected IAuthorizationExceptionFactory authorizationExceptionFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired(optional = true)
	protected ISidHelper sidHelper;

	@Autowired
	protected IAuthorizationManager authorizationManager;

	@Override
	public void ensureAuthorization() {
		if (!securityActivation.isSecured()) {
			return;
		}
		ISecurityContext securityContext = securityContextHolder.getContext();
		if (securityContext == null) {
			throw new AuthenticationMissingException();
		}
		IAuthentication authentication = securityContext.getAuthentication();
		IAuthorization previousAuthorization = securityContext.getAuthorization();
		ISecurityScope[] previousSecurityScopes = null;
		boolean success = false;
		try {
			IAuthorization authorization = previousAuthorization != null ? previousAuthorization
					: createAuthorization(authentication);
			if (authorization == null
					|| (!Boolean.TRUE.equals(ignoreInvalidUserTL.get()) && !authorization.isValid())) {
				if (authorizationExceptionFactory != null) {
					RuntimeException authorizationException = authorizationExceptionFactory
							.createAuthorizationException(authentication, authorization);
					if (authorizationException != null) {
						throw authorizationException;
					}
				}
				String userName = authentication != null ? authentication.getUserName() : null;
				String sid = authorization != null ? authorization.getSID() : null;

				if (userName == null && sid == null) {
					throw new AuthenticationMissingException();
				}
				throw new InvalidUserException(sid != null ? sid : userName);
			}
			securityContext.setAuthorization(authorization);
			success = true;
		}
		finally {
			if (!success) {
				if (securityContext != null) {
					securityContext.setAuthorization(previousAuthorization);
				}
				if (previousSecurityScopes != null) {
					securityScopeProvider.setSecurityScopes(previousSecurityScopes);
				}
			}
		}
	}

	protected IAuthorization createAuthorization(IAuthentication authentication) {
		IAuthorization authorization = null;

		String sid = null;
		final String databaseSid;
		if (authentication != null) {
			IAuthenticationResult authenticationResult = authenticationManager
					.authenticate(authentication);

			if (authenticationResult.isChangePasswordRequired()) {
				throw new PasswordChangeRequiredException();
			}
			sid = authenticationResult.getSID();
			databaseSid = sidHelper != null ? sidHelper.convertOperatingSystemSidToFrameworkSid(sid)
					: sid;

			authorization = authorizationManager.authorize(databaseSid,
					securityScopeProvider.getSecurityScopes(), authenticationResult);
		}
		else {
			databaseSid = null;
		}
		return authorization;
	}

	public void handleAuthorizationMissing(AuthorizationMissingEvent evnt) {
		ensureAuthorization();
	}
}
