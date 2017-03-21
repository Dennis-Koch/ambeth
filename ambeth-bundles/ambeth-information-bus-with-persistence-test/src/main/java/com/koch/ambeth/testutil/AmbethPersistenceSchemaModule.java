package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.PersistenceHelper;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.connection.ConnectionFactory;
import com.koch.ambeth.persistence.jdbc.testconnector.DialectSelectorSchemaModule;
import com.koch.ambeth.persistence.orm.IOrmPatternMatcher;
import com.koch.ambeth.persistence.orm.OrmPatternMatcher;
import com.koch.ambeth.persistence.sql.ISqlKeywordRegistry;
import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.persistence.util.IPersistenceExceptionUtil;
import com.koch.ambeth.persistence.util.PersistenceExceptionUtil;

public class AmbethPersistenceSchemaModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(final IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ConnectionFactory.class).autowireable(IConnectionFactory.class);
		beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
		beanContextFactory.registerBean(OrmPatternMatcher.class).autowireable(IOrmPatternMatcher.class);
		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class, ISqlKeywordRegistry.class);
		beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);

		beanContextFactory.registerBean(IocModule.class);
		beanContextFactory.registerBean(DialectSelectorSchemaModule.class);
	}
}
