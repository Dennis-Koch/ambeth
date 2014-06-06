package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehaviour;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class SecurityFilterInterceptor extends CascadedInterceptor
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	@LogInstance
	private ILogger log;

	@Autowired
	protected IMethodLevelBehaviour<SecurityContextType> methodLevelBehaviour;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityManager securityManager;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired(optional = true)
	protected ISidHelper sidHelper;

	@Autowired
	protected IUserHandleFactory userHandleFactory;

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		if (!securityActivation.isSecured() || method.getDeclaringClass().equals(Object.class))
		{
			return invokeTarget(obj, method, args, proxy);
		}
		SecurityContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		ISecurityContext currentSecurityContext = SecurityContextHolder.getContext();
		IAuthentication authentication = currentSecurityContext != null ? currentSecurityContext.getAuthentication() : null;
		IUserHandle oldUserHandle = securityScopeProvider.getUserHandle();
		IUserHandle userHandle = null;
		if (oldUserHandle == null)
		{
			String userName = null, sid = null;
			final String databaseSid;
			if (authentication != null)
			{
				userName = authentication.getUserName();
				// TODO: where to get windows sid?
				sid = authentication.getUserName();

				databaseSid = sidHelper != null ? sidHelper.convertWindowsSidToDatabaseSid(sid) : sid;

				userHandle = securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<IUserHandle>()
				{
					@Override
					public IUserHandle invoke()
					{
						return userHandleFactory.createUserHandle(databaseSid, securityScopeProvider.getSecurityScopes());
					}
				});
			}
			else
			{
				databaseSid = null;
			}
			if (userHandle == null || !userHandle.isValid())
			{
				if (!SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod))
				{
					throw new SecurityException("User is not a valid user. '" + userName + "' with SID '" + databaseSid + "'");
				}
			}
		}
		else
		{
			userHandle = oldUserHandle;
		}
		ISecurityScope[] oldSecurityScopes = securityScopeProvider.getSecurityScopes();
		securityScopeProvider.setUserHandle(userHandle);
		try
		{
			// Check for authorized access if requested
			if (SecurityContextType.AUTHORIZED.equals(behaviourOfMethod))
			{
				securityManager.checkServiceAccess(method, args, behaviourOfMethod, userHandle);
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
			securityScopeProvider.setUserHandle(oldUserHandle);
		}
	}
}
