package de.osthus.ambeth.audit;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;

public class AuditMethodCallInterceptor extends CascadedInterceptor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMethodCallLogger methodCallLogger;

	@Autowired
	protected IMethodLevelBehavior<AuditAccess> methodLevelBehaviour;

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		AuditAccess auditMethod = methodLevelBehaviour.getBehaviourOfMethod(method);
		if (auditMethod == null || !auditMethod.value())
		{
			return invokeTarget(obj, method, args, proxy);
		}
		IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method);
		try
		{
			return invokeTarget(obj, method, args, proxy);
		}
		finally
		{
			methodCallLogger.logMethodCallFinish(methodCallHandle);
		}
	}
}
