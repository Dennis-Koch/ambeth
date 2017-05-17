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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DataSourceConnectionFactory extends AbstractConnectionFactory {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DataSourceName, mandatory = false)
	protected String dataSourceName;

	@Autowired(optional = true)
	protected DataSource datasource;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		if (datasource == null) {
			ParamChecker.assertNotNull(dataSourceName, "dataSourceName");
			lookupDataSource();
		}

		ParamChecker.assertNotNull(datasource, "datasource");
	}

	protected void lookupDataSource() {
		InitialContext ic;
		try {
			ic = new InitialContext();
			datasource = (DataSource) ic.lookup(dataSourceName);
		}
		catch (NamingException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected Connection createIntern() throws Exception {
		return datasource.getConnection();
	}
}
