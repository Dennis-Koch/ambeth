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
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.persistence.database.IDatabaseProviderRegistry;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class PersistenceContextInterceptor extends CascadedInterceptor {
	public static final String P_METHOD_LEVEL_BEHAVIOUR = "MethodLevelBehaviour";

	@Autowired
	protected IDatabaseProviderRegistry databaseProviderRegistry;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Autowired
	protected IMethodLevelBehavior<PersistenceContextType> methodLevelBehaviour;

	protected HashMap<PersistenceContextType, MethodInterceptor> typeToLogicMap;

	protected MethodInterceptor resolveLogic(PersistenceContextType behaviourOfMethod) {
		if (typeToLogicMap != null) {
			return typeToLogicMap.get(behaviourOfMethod);
		}
		HashMap<PersistenceContextType, MethodInterceptor> typeToLogicMap = new HashMap<>(0.5f);
		typeToLogicMap.put(PersistenceContextType.FORBIDDEN, new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
					throws Throwable {
				ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap =
						databaseProviderRegistry
								.getPersistenceUnitToDatabaseProviderMap();
				for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
					IDatabaseProvider databaseProvider = entry.getValue();
					if (databaseProvider.tryGetInstance() != null) {
						throw new UnsupportedOperationException(
								"It is not allowed to call " + method + " while a database context is active");
					}
				}
				return invokeTarget(obj, method, args, proxy);
			}
		});
		typeToLogicMap.put(PersistenceContextType.EXPECTED, new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
					throws Throwable {
				ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap =
						databaseProviderRegistry
								.getPersistenceUnitToDatabaseProviderMap();
				for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
					IDatabaseProvider databaseProvider = entry.getValue();
					if (databaseProvider.tryGetInstance() == null) {
						throw new UnsupportedOperationException("It is not allowed to call " + method
								+ " without an already active database context");
					}
				}
				return invokeTarget(obj, method, args, proxy);
			}
		});
		typeToLogicMap.put(PersistenceContextType.REQUIRED_LAZY, new MethodInterceptor() {
			@Override
			public Object intercept(final Object obj, final Method method, final Object[] args,
					final MethodProxy proxy) throws Throwable {
				if (transactionState.isTransactionActive()) {
					return invokeTarget(obj, method, args, proxy);
				}
				return transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<Object>() {
					@Override
					public Object invoke() throws Exception {
						try {
							return invokeTarget(obj, method, args, proxy);
						}
						catch (Error e) {
							throw e;
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
			}
		});
		typeToLogicMap.put(PersistenceContextType.NOT_REQUIRED, new MethodInterceptor() {
			@Override
			public Object intercept(final Object obj, final Method method, final Object[] args,
					final MethodProxy proxy) throws Throwable {
				return invokeTarget(obj, method, args, proxy);
			}
		});
		typeToLogicMap.put(PersistenceContextType.REQUIRED, new MethodInterceptor() {
			@Override
			public Object intercept(final Object obj, final Method method, final Object[] args,
					final MethodProxy proxy) throws Throwable {
				if (transactionState.isTransactionActive()) {
					return invokeTarget(obj, method, args, proxy);
				}
				return transaction.processAndCommit(new ResultingDatabaseCallback<Object>() {
					@Override
					public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
							throws Exception {
						try {
							return invokeTarget(obj, method, args, proxy);
						}
						catch (Error e) {
							throw e;
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				}, false, false);
			}
		});
		typeToLogicMap.put(PersistenceContextType.REQUIRED_READ_ONLY, new MethodInterceptor() {
			@Override
			public Object intercept(final Object obj, final Method method, final Object[] args,
					final MethodProxy proxy) throws Throwable {
				if (transactionState.isTransactionActive()) {
					return invokeTarget(obj, method, args, proxy);
				}
				return transaction.processAndCommit(new ResultingDatabaseCallback<Object>() {
					@Override
					public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
							throws Exception {
						try {
							return invokeTarget(obj, method, args, proxy);
						}
						catch (Error e) {
							throw e;
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				}, false, true);
			}
		});
		this.typeToLogicMap = typeToLogicMap;
		return typeToLogicMap.get(behaviourOfMethod);
	}

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args,
			final MethodProxy proxy) throws Throwable {
		Class<?> declaringClass = method.getDeclaringClass();
		if (declaringClass.equals(Object.class) || declaringClass.equals(IDisposable.class)) {
			return invokeTarget(obj, method, args, proxy);
		}
		PersistenceContextType behaviourOfMethod = methodLevelBehaviour.getBehaviourOfMethod(method);

		MethodInterceptor logic = resolveLogic(behaviourOfMethod);
		if (logic != null) {
			return logic.intercept(obj, method, args, proxy);
		}
		throw RuntimeExceptionUtil.createEnumNotSupportedException(behaviourOfMethod);
	}
}
