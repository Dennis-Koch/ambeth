package com.koch.ambeth.persistence.jdbc.connection;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DataSourceConnectionFactory extends AbstractConnectionFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String dataSourceName;

	protected DataSource datasource;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		if (this.datasource == null)
		{
			ParamChecker.assertNotNull(this.dataSourceName, "dataSourceName");
			lookupDataSource();
		}

		ParamChecker.assertNotNull(this.datasource, "datasource");
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DataSourceName, mandatory = false)
	public void setDatasource(String dataSourceName)
	{
		this.dataSourceName = dataSourceName;
	}

	protected void lookupDataSource()
	{
		InitialContext ic;
		try
		{
			ic = new InitialContext();
			this.datasource = (DataSource) ic.lookup(this.dataSourceName);
		}
		catch (NamingException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected Connection createIntern() throws Exception
	{
		return datasource.getConnection();
	}
}
