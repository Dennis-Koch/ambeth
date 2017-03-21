package com.koch.ambeth.query.jdbc.sql;

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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyConstructor;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderExtension;
import com.koch.ambeth.query.IQueryBuilderExtensionExtendable;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

public class SqlQueryBuilderFactory
		implements IQueryBuilderFactory, IQueryBuilderExtensionExtendable, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	@Autowired
	protected ILightweightTransaction transaction;

	protected final DefaultExtendableContainer<IQueryBuilderExtension> queryBuilderExtensions =
			new DefaultExtendableContainer<>(IQueryBuilderExtension.class,
					"queryBuilderExtension");

	@SuppressWarnings("rawtypes")
	protected IGarbageProxyConstructor<IQueryBuilder> queryBuilderGPC;

	protected final Lock writeLock = new ReentrantLock();

	protected volatile boolean firstQueryBuilder = true;

	@Override
	public void afterPropertiesSet() throws Throwable {
		queryBuilderGPC = garbageProxyFactory.createGarbageProxyConstructor(IQueryBuilder.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType) {
		if (firstQueryBuilder) {
			writeLock.lock();
			try {
				if (firstQueryBuilder) {
					transaction.runInTransaction(new IBackgroundWorkerDelegate() {
						@Override
						public void invoke() throws Throwable {
							// intended blank
						}
					});
					firstQueryBuilder = false;
				}
			}
			finally {
				writeLock.unlock();
			}
		}
		Class<?> realEntityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		IQueryBuilderExtension[] queryBuilderExtensions = this.queryBuilderExtensions.getExtensions();
		IQueryBuilder<T> sqlQueryBuilder = beanContext.registerBean(SqlQueryBuilder.class)//
				.propertyValue("EntityType", realEntityType)//
				.propertyValue("DisposeContextOnDispose", Boolean.FALSE)//
				.propertyValue("QueryBuilderExtensions", queryBuilderExtensions)//
				.finish();

		return queryBuilderGPC.createInstance(sqlQueryBuilder);
	}

	@Override
	public void registerQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension) {
		queryBuilderExtensions.register(queryBuilderExtension);
	}

	@Override
	public void unregisterQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension) {
		queryBuilderExtensions.unregister(queryBuilderExtension);
	}
}
