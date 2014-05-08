package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map.Entry;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.database.TransactionBeginEvent;
import de.osthus.ambeth.util.ParamChecker;

public class OracleDatabaseChangeRegistration implements IInitializingBean, IStartingBean, IDisposableBean, IEventListener
{
	@LogInstance
	private ILogger log;

	protected IConnectionFactory connectionFactory;

	protected IDatabase database;

	protected ITransaction transaction;

	protected DatabaseChangeListener databaseChangeListener;

	protected DatabaseChangeRegistration databaseChangeRegistration;

	protected IEventListenerExtendable eventListenerExtendable;

	protected boolean firstMapping;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(connectionFactory, "ConnectionFactory");
		ParamChecker.assertNotNull(database, "Database");
		ParamChecker.assertNotNull(eventListenerExtendable, "eventListenerExtendable");
		ParamChecker.assertNotNull(transaction, "Transaction");
	}

	@Override
	public void afterStarted() throws Throwable
	{
	}

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
			((OracleConnection) connection).unregisterDatabaseChangeNotification(databaseChangeRegistration);
		}
		finally
		{
			JdbcUtil.close(connection);
		}
	}

	public void setConnectionFactory(IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setDatabaseChangeListener(DatabaseChangeListener databaseChangeListener)
	{
		this.databaseChangeListener = databaseChangeListener;
	}

	public void setEventListenerExtendable(IEventListenerExtendable eventListenerExtendable)
	{
		this.eventListenerExtendable = eventListenerExtendable;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
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
			OracleConnection connection = entry.getValue().getAutowiredBeanInContext(OracleConnection.class);
			databaseChangeRegistration = connection.registerDatabaseChangeNotification(prop);
			databaseChangeRegistration.addListener(databaseChangeListener);

			Statement stm = connection.createStatement();
			try
			{
				((OracleStatement) stm).setDatabaseChangeRegistration(databaseChangeRegistration);

				for (ITable table : database.getTables())
				{
					String tableName = table.getName();

					stm.execute("SELECT \"" + table.getIdField().getName() + "\" FROM \"" + tableName + "\"");
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
