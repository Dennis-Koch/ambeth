package com.koch.ambeth.persistence.jdbc.connection;

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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DataSourceConnectionFactory extends AbstractConnectionFactory {
	@Property(name = PersistenceJdbcConfigurationConstants.DataSourceInstance, mandatory = false)
	protected DataSource dataSource;

	@Property(name = PersistenceJdbcConfigurationConstants.DataSourceName, mandatory = false)
	protected String dataSourceName;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		if (dataSource == null) {
			ParamChecker.assertNotNull(dataSourceName, "dataSourceName");
			try {
				InitialContext ic = new InitialContext();
				dataSource = (DataSource) ic.lookup(dataSourceName);
			}
			catch (NamingException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}

		ParamChecker.assertNotNull(dataSource, "datasource");
	}

	@Override
	protected Connection createIntern() throws Exception {
		return dataSource.getConnection();
	}
}
