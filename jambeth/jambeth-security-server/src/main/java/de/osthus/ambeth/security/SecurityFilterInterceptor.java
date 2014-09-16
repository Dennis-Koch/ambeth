package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;

public class SecurityFilterInterceptor extends CascadedInterceptor
{
	public static final String PROP_CHECK_METHOD_ACCESS = "CheckMethodAccess";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuthenticationManager authenticationManager;

	@Autowired
	protected IMethodLevelBehavior<SecurityContextType> methodLevelBehaviour;

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

	@Property
	protected boolean checkMethodAccess = true;

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (method.getDeclaringClass().equals(Object.class) || !securityActivation.isSecured())
		{
			return invokeTarget(obj, method, args, proxy);
		}
		SecurityContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		IAuthorization oldAuthorization = securityContextHolder.getCreateContext().getAuthorization();
		IAuthorization authorization = null;
		if (oldAuthorization == null && !SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod))
		{
			authorization = createAuthorization();
		}
		else
		{
			authorization = oldAuthorization;
		}
		if (authorization == null || !authorization.isValid())
		{
			if (!SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod))
			{
				IAuthentication authentication = getAuthentication();
				String userName = authentication != null ? authentication.getUserName() : null;
				String sid = authorization != null ? authorization.getSID() : null;
				throw new SecurityException("User is not a valid user. '" + userName + "' with SID '" + sid + "'");
			}
		}
		ISecurityScope[] oldSecurityScopes = securityScopeProvider.getSecurityScopes();
		if (oldAuthorization != authorization)
		{
			securityContextHolder.getCreateContext().setAuthorization(authorization);
		}
		try
		{
			// Check for authorized access if requested
			if (checkMethodAccess && SecurityContextType.AUTHORIZED.equals(behaviourOfMethod))
			{
				securityManager.checkMethodAccess(method, args, behaviourOfMethod, authorization);
			}
			Object unfilteredResult = invokeTarget(obj, method, args, proxy);
			if (!SecurityContextType.AUTHORIZED.equals(behaviourOfMethod) || !securityActivation.isFilterActivated())
			{
				return unfilteredResult;
			}
			return securityManager.filterValue(unfilteredResult);
		}
		finally
		{
			// Important to restore the old security scopes again because within InvokeTarget it may have been modified
			securityScopeProvider.setSecurityScopes(oldSecurityScopes);
			if (oldAuthorization != authorization)
			{
				securityContextHolder.getCreateContext().setAuthorization(oldAuthorization);
			}
		}
	}

	protected IAuthentication getAuthentication()
	{
		ISecurityContext currentSecurityContext = securityContextHolder.getContext();
		return currentSecurityContext != null ? currentSecurityContext.getAuthentication() : null;
	}

	protected IAuthorization createAuthorization() throws Throwable
	{
		IAuthentication authentication = getAuthentication();
		IAuthorization authorization = null;

		String sid = null;
		final String databaseSid;
		if (authentication != null)
		{
			if (securityActivation.isSecured())
			{
				IAuthenticationResult authenticationResult = authenticationManager.authenticate(authentication);
				sid = authenticationResult.getUserName();
			}
			databaseSid = sidHelper != null ? sidHelper.convertWindowsSidToDatabaseSid(sid) : sid;

			authorization = authorizationManager.authorize(databaseSid, securityScopeProvider.getSecurityScopes());
		}
		else
		{
			databaseSid = null;
		}
		return authorization;
	}
}
