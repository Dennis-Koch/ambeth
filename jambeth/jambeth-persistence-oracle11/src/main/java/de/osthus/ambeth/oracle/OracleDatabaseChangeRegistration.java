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
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.database.TransactionBeginEvent;

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
