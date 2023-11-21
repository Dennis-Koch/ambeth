package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class ConnectionHolderInterceptor extends AbstractSimpleInterceptor
		implements IConnectionHolder, IThreadLocalCleanupBean {
	public static final String P_CONNECTION = "Connection";

	@Forkable
	protected final ThreadLocal<Connection> connectionTL = new SensitiveThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		if (connectionTL.get() != null) {
			throw new IllegalStateException(
					"At this point the thread-local connection has to be already cleaned up gracefully");
		}
	}

	@Override
	public void setConnection(Connection connection) {
		Connection oldConnection = connectionTL.get();
		if (oldConnection != null && connection != null && oldConnection != connection) {
			throw new IllegalStateException(
					"Thread-local connection instance already applied!. This is a fatal state");
		}
		if (connection == null) {
			connectionTL.remove();
		}
		else {
			connectionTL.set(connection);
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		try {
			Connection connection = getConnection();
			if (connection == null) {
				throw new IllegalStateException("No connection currently applied. This often occurs if a "
						+ Connection.class.getName() + "-bean is used without scoping the call through the "
						+ ILightweightTransaction.class.getName() + "-bean");
			}
			return proxy.invoke(connection, args);
		}
		catch (InvocationTargetException e) {
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}

	@Override
	public Connection getConnection() {
		return connectionTL.get();
	}
}
