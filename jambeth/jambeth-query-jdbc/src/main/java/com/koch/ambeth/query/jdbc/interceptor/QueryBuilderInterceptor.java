package com.koch.ambeth.query.jdbc.interceptor;

/*-
 * #%L
 * jambeth-query-jdbc
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

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;

import com.koch.ambeth.util.proxy.MethodProxy;

public class QueryBuilderInterceptor extends AbstractSimpleInterceptor {
	protected static final Method disposeMethod;

	protected static final HashSet<Method> cleanupMethods = new HashSet<>();

	static {
		try {
			disposeMethod = IQueryBuilder.class.getMethod("dispose");
			cleanupMethods.add(IQueryBuilder.class.getMethod("build"));
			cleanupMethods.add(IQueryBuilder.class.getMethod("build", IOperand.class));
			cleanupMethods.add(IQueryBuilder.class.getMethod("build", IOperand.class, ISqlJoin[].class));
			cleanupMethods.add(disposeMethod);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IQueryBuilder<?> queryBuilder;

	protected boolean finalized = false;

	public QueryBuilderInterceptor(IQueryBuilder<?> queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	@Override
	protected void finalize() throws Throwable {
		if (queryBuilder != null) {
			queryBuilder.dispose();
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (finalized) {
			if (disposeMethod.equals(method)) {
				return null;
			}
			throw new IllegalStateException("This query builder already is finalized!");
		}
		try {
			Object result = proxy.invoke(queryBuilder, args);
			if (result == queryBuilder) {
				return proxy;
			}
			return result;
		}
		finally {
			if (cleanupMethods.contains(method)) {
				finalized = true;
				IQueryBuilder<?> queryBuilder = this.queryBuilder;
				if (queryBuilder != null) {
					this.queryBuilder = null;
					queryBuilder.dispose();
				}
			}
		}
	}
}
