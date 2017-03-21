package com.koch.ambeth.persistence.proxy;

/*-
 * #%L
 * jambeth-persistence
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
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.persistence.database.IDatabaseProviderRegistry;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.proxy.CascadedInterceptor;

import net.sf.cglib.proxy.MethodProxy;

public class PersistenceContextInterceptor extends CascadedInterceptor {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabaseProviderRegistry databaseProviderRegistry;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Autowired
	protected IMethodLevelBehavior<PersistenceContextType> methodLevelBehaviour;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args,
			final MethodProxy proxy) throws Throwable {
		Class<?> declaringClass = method.getDeclaringClass();
		if (declaringClass.equals(Object.class) || declaringClass.equals(IDisposable.class)) {
			return invokeTarget(obj, method, args, proxy);
		}
		PersistenceContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		if (PersistenceContextType.FORBIDDEN.equals(behaviourOfMethod)) {
			ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap =
					databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
			for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
				IDatabaseProvider databaseProvider = entry.getValue();
				if (databaseProvider.tryGetInstance() != null) {
					throw new UnsupportedOperationException(
							"It is not allowed to call " + method + " while a database context is active");
				}
			}
			return invokeTarget(obj, method, args, proxy);
		}
		if (PersistenceContextType.EXPECTED.equals(behaviourOfMethod)) {
			ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap =
					databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
			for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
				IDatabaseProvider databaseProvider = entry.getValue();
				if (databaseProvider.tryGetInstance() == null) {
					throw new UnsupportedOperationException("It is not allowed to call " + method
							+ " without an already active database context");
				}
			}
			return invokeTarget(obj, method, args, proxy);
		}
		if (!PersistenceContextType.REQUIRED.equals(behaviourOfMethod)
				&& !PersistenceContextType.REQUIRED_READ_ONLY.equals(behaviourOfMethod)) {
			// Do nothing if there is no transaction explicitly required for this method
			return invokeTarget(obj, method, args, proxy);
		}
		if (transactionState.isTransactionActive()) {
			return invokeTarget(obj, method, args, proxy);
		}
		boolean readOnly = PersistenceContextType.REQUIRED_READ_ONLY.equals(behaviourOfMethod);
		return transaction.processAndCommit(new ResultingDatabaseCallback<Object>() {
			@Override
			public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Throwable {
				return invokeTarget(obj, method, args, proxy);
			}
		}, false, readOnly);
	}
}
