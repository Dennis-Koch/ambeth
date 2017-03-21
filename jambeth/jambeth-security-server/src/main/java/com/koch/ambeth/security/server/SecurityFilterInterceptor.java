package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationManager;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.ISecurityManager;
import com.koch.ambeth.security.ISidHelper;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.security.server.exceptions.AuthenticationMissingException;
import com.koch.ambeth.security.server.exceptions.InvalidUserException;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.proxy.CascadedInterceptor;

import net.sf.cglib.proxy.MethodProxy;

public class SecurityFilterInterceptor extends CascadedInterceptor {
	public static class SecurityMethodMode {
		public final SecurityContextType securityContextType;

		public final int userNameIndex;

		public final int userPasswordIndex;

		public final PasswordType passwordType;

		public final ISecurityScope securityScope;

		public final int securityScopeIndex;

		public SecurityMethodMode(SecurityContextType securityContextType) {
			this.securityContextType = securityContextType;
			userNameIndex = -1;
			userPasswordIndex = -1;
			passwordType = null;
			securityScopeIndex = -1;
			securityScope = StringSecurityScope.DEFAULT_SCOPE;
		}

		public SecurityMethodMode(SecurityContextType securityContextType, int userNameIndex,
				int userPasswordIndex, PasswordType passwordType, int securityScopeIndex,
				ISecurityScope securityScope) {
			this.securityContextType = securityContextType;
			this.userNameIndex = userNameIndex;
			this.userPasswordIndex = userPasswordIndex;
			this.passwordType = passwordType;
			this.securityScopeIndex = securityScopeIndex;
			this.securityScope = securityScope;
		}
	}

	public static final String PROP_CHECK_METHOD_ACCESS = "CheckMethodAccess";

	private static final ThreadLocal<Boolean> ignoreInvalidUserTL = new ThreadLocal<Boolean>();

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
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IMethodLevelBehavior<SecurityMethodMode> methodLevelBehaviour;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityManager securityManager;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired(optional = true)
	protected ISidHelper sidHelper;

	@Autowired
	protected IAuthorizationManager authorizationManager;

	@Property(defaultValue = "true")
	protected boolean checkMethodAccess;

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (finalizeMethod.equals(method)) {
			return null;
		}
		if (method.getDeclaringClass().equals(Object.class) || !securityActivation.isSecured()) {
			return invokeTarget(obj, method, args, proxy);
		}
		SecurityMethodMode securityMethodMode = methodLevelBehaviour.getBehaviourOfMethod(method);
		SecurityContextType behaviourOfMethod = securityMethodMode.securityContextType;

		ISecurityContext securityContext = null;
		IAuthentication previousAuthentication = null;
		IAuthorization previousAuthorization = null;
		if (securityMethodMode.userNameIndex != -1) {
			securityContext = securityContextHolder.getCreateContext();
			String userName =
					conversionHelper.convertValueToType(String.class, args[securityMethodMode.userNameIndex]);
			char[] userPass = securityMethodMode.userPasswordIndex != -1 ? conversionHelper
					.convertValueToType(char[].class, args[securityMethodMode.userPasswordIndex]) : null;
			previousAuthentication = securityContext.getAuthentication();
			previousAuthorization = securityContext.getAuthorization();
			securityContext.setAuthentication(
					new DefaultAuthentication(userName, userPass, securityMethodMode.passwordType));
			securityContext.setAuthorization(null);
		}
		else {
			securityContext = securityContextHolder.getContext();
			previousAuthentication = securityContext != null ? securityContext.getAuthentication() : null;
			previousAuthorization = securityContext != null ? securityContext.getAuthorization() : null;
		}
		ISecurityScope[] previousSecurityScopes = null;
		if (securityMethodMode.securityScopeIndex != -1) {
			final String securityScopeName = conversionHelper.convertValueToType(String.class,
					args[securityMethodMode.securityScopeIndex]);
			previousSecurityScopes = securityScopeProvider.getSecurityScopes();
			if (previousSecurityScopes.length == 1
					&& previousSecurityScopes[0].getName().equals(securityScopeName)) {
				previousSecurityScopes = null;
			}
			else {
				ISecurityScope securityScope =
						securityScopeName.equals(StringSecurityScope.DEFAULT_SCOPE_NAME)
								? StringSecurityScope.DEFAULT_SCOPE : new StringSecurityScope(securityScopeName);
				securityScopeProvider.setSecurityScopes(new ISecurityScope[] {securityScope});
			}
		}
		else if (securityMethodMode.securityScope != null) {
			previousSecurityScopes = securityScopeProvider.getSecurityScopes();
			if (previousSecurityScopes.length == 1 && previousSecurityScopes[0].getName()
					.equals(securityMethodMode.securityScope.getName())) {
				previousSecurityScopes = null;
			}
			else {
				securityScopeProvider
						.setSecurityScopes(new ISecurityScope[] {securityMethodMode.securityScope});
			}
		}
		try {
			if (securityContext == null) {
				securityContext = securityContextHolder.getContext();
			}
			IAuthorization oldAuthorization =
					securityContext != null ? securityContext.getAuthorization() : null;
			IAuthorization authorization = null;
			if (oldAuthorization == null && !SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod)) {
				if (securityContext == null) {
					securityContext = securityContextHolder.getCreateContext();
				}
				authorization = createAuthorization();
			}
			else {
				authorization = oldAuthorization;
			}
			if (authorization == null
					|| (!Boolean.TRUE.equals(ignoreInvalidUserTL.get()) && !authorization.isValid())) {
				if (!SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod)) {
					IAuthentication authentication = getAuthentication();

					if (authorizationExceptionFactory != null) {
						Throwable authorizationException = authorizationExceptionFactory
								.createAuthorizationException(authentication, authorization);
						if (authorizationException != null) {
							throw authorizationException;
						}
					}
					String userName = authentication != null ? authentication.getUserName() : null;
					String sid = authorization != null ? authorization.getSID() : null;

					if (userName == null && sid == null) {
						throw new AuthenticationMissingException(method);
					}
					throw new InvalidUserException(sid != null ? sid : userName);
				}
			}
			ISecurityScope[] oldSecurityScopes = securityScopeProvider.getSecurityScopes();
			if (oldAuthorization != authorization) {
				securityContext.setAuthorization(authorization);
			}
			try {
				// Check for authorized access if requested
				if (checkMethodAccess && securityActivation.isServiceSecurityEnabled()
						&& SecurityContextType.AUTHORIZED.equals(behaviourOfMethod)) {
					securityManager.checkMethodAccess(method, args, behaviourOfMethod, authorization);
				}
				Object unfilteredResult = invokeTarget(obj, method, args, proxy);
				if (!SecurityContextType.AUTHORIZED.equals(behaviourOfMethod)
						|| !securityActivation.isFilterActivated()) {
					return unfilteredResult;
				}
				return securityManager.filterValue(unfilteredResult);
			}
			finally {
				// Important to restore the old security scopes again because within InvokeTarget it may
				// have been modified
				securityScopeProvider.setSecurityScopes(oldSecurityScopes);
				if (oldAuthorization != authorization) {
					securityContext.setAuthorization(oldAuthorization);
				}
			}
		}
		finally {
			if (securityContext != null) {
				securityContext.setAuthentication(previousAuthentication);
				securityContext.setAuthorization(previousAuthorization);
			}
			if (previousSecurityScopes != null) {
				securityScopeProvider.setSecurityScopes(previousSecurityScopes);
			}
		}
	}

	protected IAuthentication getAuthentication() {
		ISecurityContext currentSecurityContext = securityContextHolder.getContext();
		return currentSecurityContext != null ? currentSecurityContext.getAuthentication() : null;
	}

	protected IAuthorization createAuthorization() throws Throwable {
		IAuthentication authentication = getAuthentication();
		IAuthorization authorization = null;

		String sid = null;
		final String databaseSid;
		if (authentication != null) {
			IAuthenticationResult authenticationResult =
					authenticationManager.authenticate(authentication);
			sid = authenticationResult.getSID();
			databaseSid = sidHelper != null ? sidHelper.convertWindowsSidToDatabaseSid(sid) : sid;

			authorization = authorizationManager.authorize(databaseSid,
					securityScopeProvider.getSecurityScopes(), authenticationResult);
		}
		else {
			databaseSid = null;
		}
		return authorization;
	}
}
