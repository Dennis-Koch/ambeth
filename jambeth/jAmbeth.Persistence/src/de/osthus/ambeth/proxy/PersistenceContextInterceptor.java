package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.database.IDatabaseProviderRegistry;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class PersistenceContextInterceptor extends CascadedInterceptor implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IDatabaseProviderRegistry databaseProviderRegistry;

	protected ITransaction transaction;

	protected IMethodLevelBehaviour<PersistenceContextType> methodLevelBehaviour;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(databaseProviderRegistry, "DatabaseProviderRegistry");
		ParamChecker.assertNotNull(methodLevelBehaviour, "MethodLevelBehaviour");
		ParamChecker.assertNotNull(transaction, "Transaction");
	}

	public void setDatabaseProviderRegistry(IDatabaseProviderRegistry databaseProviderRegistry)
	{
		this.databaseProviderRegistry = databaseProviderRegistry;
	}

	public void setMethodLevelBehaviour(IMethodLevelBehaviour<PersistenceContextType> methodLevelBehaviour)
	{
		this.methodLevelBehaviour = methodLevelBehaviour;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	@Override
	public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		Class<?> declaringClass = method.getDeclaringClass();
		if (declaringClass.equals(Object.class) || declaringClass.equals(IDisposable.class))
		{
			return invokeTarget(obj, method, args, proxy);
		}
		PersistenceContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		if (PersistenceContextType.FORBIDDEN.equals(behaviourOfMethod))
		{
			ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
			for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap)
			{
				IDatabaseProvider databaseProvider = entry.getValue();
				if (databaseProvider.tryGetInstance() != null)
				{
					throw new UnsupportedOperationException("It is not allowed to call " + method + " while a database context is active");
				}
			}
			return invokeTarget(obj, method, args, proxy);
		}
		if (!PersistenceContextType.REQUIRED.equals(behaviourOfMethod) && !PersistenceContextType.REQUIRED_READ_ONLY.equals(behaviourOfMethod))
		{
			// Do nothing if there is no transaction explicitly required for this method
			return invokeTarget(obj, method, args, proxy);
		}
		boolean readOnly = PersistenceContextType.REQUIRED_READ_ONLY.equals(behaviourOfMethod);
		return transaction.processAndCommit(new ResultingDatabaseCallback<Object>()
		{
			@Override
			public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				try
				{
					return invokeTarget(obj, method, args, proxy);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}, false, readOnly);
	}
}
