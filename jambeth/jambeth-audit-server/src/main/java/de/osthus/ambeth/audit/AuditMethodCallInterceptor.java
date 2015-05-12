package de.osthus.ambeth.audit;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.audit.model.AuditedArg;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

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
	protected ILightweightTransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Property(name = AuditConfigurationConstants.AuditedServiceArgDefaultModeActive, defaultValue = "false")
	protected boolean auditedServiceArgDefaultModeActive;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		final AuditInfo auditInfo = methodLevelBehaviour.getBehaviourOfMethod(method);
		if ((auditInfo == null && !auditedServiceDefaultModeActive) || (auditInfo != null && !auditInfo.getAudited().value()))
		{
			return invokeTarget(obj, method, args, proxy);
		}

		// filter the args by audit configuration
		AuditedArg[] auditedArgs = auditInfo.getAuditedArgs();
		final Object[] filteredArgs = new Object[args.length];
		for (int i = 0; i < filteredArgs.length; i++)
		{
			AuditedArg auditedArg = auditedArgs != null ? auditedArgs[i] : null;
			if ((auditedArg == null && auditedServiceArgDefaultModeActive) || (auditedArg != null && auditedArg.value()))
			{
				filteredArgs[i] = args[i];
			}
			else
			{
				filteredArgs[i] = "n/a";
			}
		}
		if (transactionState.isTransactionActive())
		{
			IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method, filteredArgs);
			try
			{
				return invokeTarget(obj, method, args, proxy);
			}
			finally
			{
				methodCallLogger.logMethodCallFinish(methodCallHandle);
			}
		}
		return transaction.runInTransaction(new IResultingBackgroundWorkerDelegate<Object>()
		{
			@Override
			public Object invoke() throws Throwable
			{
				IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method, filteredArgs);
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
