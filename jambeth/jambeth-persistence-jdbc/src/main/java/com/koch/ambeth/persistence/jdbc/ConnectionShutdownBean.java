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

import java.sql.Connection;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

public class ConnectionShutdownBean implements IInitializingBean, IDisposableBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected Connection connection;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(connection, "connection");
	}

	@Override
	public void destroy() throws Throwable {
		if (connection != null) {
			JdbcUtil.close(connection);
			connection = null;
		}
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}
