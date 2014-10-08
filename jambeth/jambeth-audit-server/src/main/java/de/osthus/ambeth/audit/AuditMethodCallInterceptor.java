package de.osthus.ambeth.audit;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.IDatabase;
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
	protected IMethodLevelBehavior<Audited> methodLevelBehaviour;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		Audited auditMethod = methodLevelBehaviour.getBehaviourOfMethod(method);
		if (auditMethod == null || !auditMethod.value())
		{
			return invokeTarget(obj, method, args, proxy);
		}
		if (transactionState.isTransactionActive())
		{
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
		return transaction.processAndCommit(new ResultingDatabaseCallback<Object>()
		{
			@Override
			public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
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
		});
	}
}
