package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehaviour;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;

public class SecurityFilterInterceptor extends CascadedInterceptor implements IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected IMethodLevelBehaviour<SecurityContextType> methodLevelBehaviour;

	protected ISecurityActivation securityActivation;

	protected ISecurityManager securityManager;

	protected ISecurityScopeProvider securityScopeProvider;

	protected ISidHelper sidHelper;

	protected IUserHandleFactory userHandleFactory;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(methodLevelBehaviour, "methodLevelBehaviour");
		ParamChecker.assertNotNull(securityActivation, "securityActivation");
		ParamChecker.assertNotNull(securityManager, "securityManager");
		ParamChecker.assertNotNull(securityScopeProvider, "securityScopeProvider");
		ParamChecker.assertNotNull(userHandleFactory, "userHandleFactory");
	}

	public void setMethodLevelBehaviour(IMethodLevelBehaviour<SecurityContextType> methodLevelBehaviour)
	{
		this.methodLevelBehaviour = methodLevelBehaviour;
	}

	public void setSecurityActivation(ISecurityActivation securityActivation)
	{
		this.securityActivation = securityActivation;
	}

	public void setSecurityManager(ISecurityManager securityManager)
	{
		this.securityManager = securityManager;
	}

	public void setSecurityScopeProvider(ISecurityScopeProvider securityScopeProvider)
	{
		this.securityScopeProvider = securityScopeProvider;
	}

	public void setSidHelper(ISidHelper sidHelper)
	{
		this.sidHelper = sidHelper;
	}

	public void setUserHandleFactory(IUserHandleFactory userHandleFactory)
	{
		this.userHandleFactory = userHandleFactory;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
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
					// It is not expected for this method, that the user is be valid
				}
				else
				{
					if (log.isInfoEnabled())
					{
						log.info("User is not a registered user. '" + userName + "' with SID '" + databaseSid + "'");
					}
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
			securityManager.checkServiceAccess(method, args, behaviourOfMethod, userHandle);
			Object unfilteredResult = invokeTarget(obj, method, args, proxy);
			if (!securityActivation.isFilterActivated())
			{
				return unfilteredResult;
			}
			// Filter result
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
