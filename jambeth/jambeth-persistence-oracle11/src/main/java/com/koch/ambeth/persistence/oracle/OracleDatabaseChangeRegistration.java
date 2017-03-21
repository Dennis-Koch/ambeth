package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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
import java.sql.Statement;
import java.util.Map.Entry;

import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.database.TransactionBeginEvent;
import com.koch.ambeth.util.collections.ILinkedMap;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;

public class OracleDatabaseChangeRegistration implements IDisposableBean, IEventListener
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionFactory connectionFactory;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected ITransaction transaction;

	@Autowired(optional = true)
	protected DatabaseChangeListener databaseChangeListener;

	@Autowired(optional = true)
	protected DatabaseChangeRegistration databaseChangeRegistration;

	@Autowired
	protected IEventListenerExtendable eventListenerExtendable;

	protected boolean firstMapping;

	@Override
	public void destroy() throws Throwable
	{
		if (databaseChangeRegistration == null)
		{
			return;
		}
		Connection connection = connectionFactory.create();
		try
		{
			connection.unwrap(OracleConnection.class).unregisterDatabaseChangeNotification(databaseChangeRegistration);
		}
		finally
		{
			JdbcUtil.close(connection);
		}
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (!(eventObject instanceof IEntityMetaDataEvent))
		{
			return;
		}
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				if (persistenceUnitToDatabaseMap == null)
				{
					final Thread currentThread = Thread.currentThread();
					// This may happen if the event is handled while building up the very first transaction
					// So we register for the TransactionBeginEvent and execute then
					eventListenerExtendable.registerEventListener(new IEventListener()
					{
						@Override
						public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
						{
							if (Thread.currentThread() != currentThread)
							{
								// A concurrent thread has been faster than our own transaction event
								return;
							}
							eventListenerExtendable.unregisterEventListener(this, TransactionBeginEvent.class);
							handleEventIntern(((TransactionBeginEvent) eventObject).getPersistenceUnitToDatabaseMap());
						}
					}, TransactionBeginEvent.class);
					return;
				}
				handleEventIntern(persistenceUnitToDatabaseMap);
			}
		});
	}

	protected void handleEventIntern(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
	{
		java.util.Properties prop = new java.util.Properties();
		prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
		// prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");

		for (Entry<Object, IDatabase> entry : persistenceUnitToDatabaseMap)
		{
			OracleConnection connection = entry.getValue().getAutowiredBeanInContext(Connection.class).unwrap(OracleConnection.class);
			databaseChangeRegistration = connection.registerDatabaseChangeNotification(prop);
			databaseChangeRegistration.addListener(databaseChangeListener);

			Statement stm = connection.createStatement();
			try
			{
				((OracleStatement) stm).setDatabaseChangeRegistration(databaseChangeRegistration);

				for (ITableMetaData table : databaseMetaData.getTables())
				{
					String tableName = table.getFullqualifiedEscapedName();

					stm.execute("SELECT \"" + table.getIdField().getName() + "\" FROM " + tableName + "");
				}
				if (log.isDebugEnabled())
				{
					String[] tableNames = databaseChangeRegistration.getTables();
					for (int i = 0; i < tableNames.length; i++)
					{
						log.debug(tableNames[i] + " is part of the registration.");
					}
				}
			}
			finally
			{
				JdbcUtil.close(stm);
			}
		}
	}
}
