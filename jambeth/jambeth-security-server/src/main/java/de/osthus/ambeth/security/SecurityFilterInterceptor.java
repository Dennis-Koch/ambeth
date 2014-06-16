package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.config.Property;
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
	public static final String PROP_CHECK_METHOD_ACCESS = "CheckMethodAccess";

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

	@Property
	protected boolean checkMethodAccess = true;

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		if (method.getDeclaringClass().equals(Object.class) || !securityActivation.isSecured())
		{
			return invokeTarget(obj, method, args, proxy);
		}
		SecurityContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		IUserHandle oldUserHandle = securityScopeProvider.getUserHandle();
		IUserHandle userHandle = null;
		if (oldUserHandle == null)
		{
			userHandle = createUserHandle();
		}
		else
		{
			userHandle = oldUserHandle;
		}
		if (userHandle == null || !userHandle.isValid())
		{
			if (!SecurityContextType.NOT_REQUIRED.equals(behaviourOfMethod))
			{
				IAuthentication authentication = getAuthentication();
				String userName = authentication != null ? authentication.getUserName() : null;
				String sid = userHandle != null ? userHandle.getSID() : null;
				throw new SecurityException("User is not a valid user. '" + userName + "' with SID '" + sid + "'");
			}
		}
		ISecurityScope[] oldSecurityScopes = securityScopeProvider.getSecurityScopes();
		if (oldUserHandle != userHandle)
		{
			securityScopeProvider.setUserHandle(userHandle);
		}
		try
		{
			// Check for authorized access if requested
			if (checkMethodAccess && SecurityContextType.AUTHORIZED.equals(behaviourOfMethod))
			{
				securityManager.checkMethodAccess(method, args, behaviourOfMethod, userHandle);
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
			if (oldUserHandle != userHandle)
			{
				securityScopeProvider.setUserHandle(oldUserHandle);
			}
		}
	}

	protected IAuthentication getAuthentication()
	{
		ISecurityContext currentSecurityContext = SecurityContextHolder.getContext();
		return currentSecurityContext != null ? currentSecurityContext.getAuthentication() : null;
	}

	protected IUserHandle createUserHandle() throws Throwable
	{
		IAuthentication authentication = getAuthentication();
		IUserHandle userHandle = null;

		String sid = null;
		final String databaseSid;
		if (authentication != null)
		{
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
		return userHandle;
	}
}
