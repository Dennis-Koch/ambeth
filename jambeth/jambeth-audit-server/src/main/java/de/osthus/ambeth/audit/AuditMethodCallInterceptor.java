package de.osthus.ambeth.audit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
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
	protected IMethodLevelBehavior<AuditInfo> methodLevelBehaviour;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		final AuditInfo auditInfo = methodLevelBehaviour.getBehaviourOfMethod(method);
		if ((auditInfo == null && !auditedServiceDefaultModeActive) || (auditInfo != null && !auditInfo.getAudited().value()))
		{
			return invokeTarget(obj, method, args, proxy);
		}

		// filter the args by audit configuration
		final List<Object> auditedArgs = new ArrayList<Object>();
		if (auditInfo.getAuditedArgs() != null)
		{
			for (int i = 0; i < auditInfo.getAuditedArgs().length; i++)
			{
				AuditedArg auditedArg = auditInfo.auditedArgs[i];
				if ((auditedArg == null && auditedServiceDefaultModeActive) || (auditedArg != null && auditedArg.value()))
				{
					auditedArgs.add(args[i]);
				}
			}
		}

		if (transactionState.isTransactionActive())
		{

			IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method, auditedArgs.toArray());
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
				IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method, auditedArgs.toArray());
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
