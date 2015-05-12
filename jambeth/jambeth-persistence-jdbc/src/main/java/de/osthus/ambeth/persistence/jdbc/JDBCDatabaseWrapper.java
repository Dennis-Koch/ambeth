package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.Database;
import de.osthus.ambeth.persistence.DirectedLink;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IDirectedLinkMetaData;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.persistence.ILinkMetaData;
import de.osthus.ambeth.persistence.ISavepoint;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.util.IAlreadyLinkedCache;

public class JDBCDatabaseWrapper extends Database
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired
	protected IConnectionDialect connectionDialect;

	protected long lastTestTime = System.currentTimeMillis(), trustTime = 10000;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		IdentityHashMap<ILinkMetaData, ILink> alreadyCreatedLinkMap = new IdentityHashMap<ILinkMetaData, ILink>();
		IdentityHashMap<IDirectedLinkMetaData, IDirectedLink> alreadyCreatedDirectedLinkMap = new IdentityHashMap<IDirectedLinkMetaData, IDirectedLink>();
		IdentityHashMap<ITableMetaData, ITable> alreadyCreatedTableMap = new IdentityHashMap<ITableMetaData, ITable>();

		for (ITableMetaData tableMD : metaData.getTables())
		{
			JdbcTable table = new JdbcTable();
			tables.add(table);
			alreadyCreatedTableMap.put(tableMD, table);
		}
		for (ILinkMetaData linkMD : metaData.getLinks())
		{
			JdbcLink link = new JdbcLink();
			links.add(link);
			alreadyCreatedLinkMap.put(linkMD, link);

			alreadyCreatedDirectedLinkMap.put(linkMD.getDirectedLink(), new DirectedLink());
			alreadyCreatedDirectedLinkMap.put(linkMD.getReverseDirectedLink(), new DirectedLink());
		}
		for (Entry<ITableMetaData, ITable> entry : alreadyCreatedTableMap)
		{
			ITableMetaData tableMD = entry.getKey();
			JdbcTable table = (JdbcTable) entry.getValue();

			table.init(tableMD, alreadyCreatedDirectedLinkMap);

			table = serviceContext.registerWithLifecycle(table)//
					.propertyValue("MetaData", tableMD)//
					.finish();
		}
		for (Entry<IDirectedLinkMetaData, IDirectedLink> entry : alreadyCreatedDirectedLinkMap)
		{
			IDirectedLinkMetaData directedLinkMD = entry.getKey();
			DirectedLink directedLink = (DirectedLink) entry.getValue();

			directedLink = serviceContext.registerWithLifecycle(directedLink)//
					.propertyValue("MetaData", directedLinkMD)//
					.propertyValue("FromTable", getExistingValue(alreadyCreatedTableMap, directedLinkMD.getFromTable()))//
					.propertyValue("ToTable", getExistingValue(alreadyCreatedTableMap, directedLinkMD.getToTable()))//
					.propertyValue("Link", getExistingValue(alreadyCreatedLinkMap, directedLinkMD.getLink()))//
					.propertyValue("Reverse", getExistingValue(alreadyCreatedDirectedLinkMap, directedLinkMD.getReverseLink()))//
					.finish();
		}
		for (Entry<ILinkMetaData, ILink> entry : alreadyCreatedLinkMap)
		{
			ILinkMetaData linkMD = entry.getKey();
			JdbcLink link = (JdbcLink) entry.getValue();

			link = serviceContext.registerWithLifecycle(link)//
					.propertyValue("MetaData", linkMD)//
					.propertyValue("FromTable", getExistingValue(alreadyCreatedTableMap, linkMD.getFromTable()))//
					.propertyValue("ToTable", getExistingValue(alreadyCreatedTableMap, linkMD.getToTable()))//
					.propertyValue("DirectedLink", getExistingValue(alreadyCreatedDirectedLinkMap, linkMD.getDirectedLink()))//
					.propertyValue("ReverseDirectedLink", getExistingValue(alreadyCreatedDirectedLinkMap, linkMD.getReverseDirectedLink()))//
					.finish();
		}
		for (ITable table : tables)
		{
			Class<?> entityType = table.getMetaData().getEntityType();
			nameToTableDict.put(table.getMetaData().getName(), table);

			if (entityType == null)
			{
				continue;
			}
			if (table.getMetaData().isArchive())
			{
				typeToArchiveTableDict.put(entityType, table);
			}
			else
			{
				typeToTableDict.put(entityType, table);
			}
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		connection = null;
		super.destroy();
	}

	protected <K, V> V getExistingValue(IdentityHashMap<K, V> map, K key)
	{
		if (key == null)
		{
			return null;
		}
		V value = map.get(key);
		if (value != null)
		{
			return value;
		}
		throw new IllegalStateException("No value for key: " + key);
	}

	@Override
	public void flush()
	{
		try
		{
			connectionDialect.commit(connection);
		}
		catch (SQLException e)
		{
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public void revert()
	{
		alreadyLinkedCache.clear();
		try
		{
			connectionDialect.rollback(connection);
		}
		catch (SQLException e)
		{
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public void revert(ISavepoint savepoint)
	{
		alreadyLinkedCache.clear();
		try
		{
			rollback(savepoint);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean test()
	{
		if (System.currentTimeMillis() - lastTestTime <= trustTime)
		{
			return true;
		}
		try
		{
			try
			{
				return connection.isValid(0);
			}
			catch (AbstractMethodError e)
			{
				// Oracle driver does not support this operation
				return !connection.isClosed();
			}
		}
		catch (SQLException e)
		{
			return false;
		}
		finally
		{
			lastTestTime = System.currentTimeMillis();
		}
	}

	@Override
	public ISavepoint setSavepoint()
	{
		try
		{
			return new JdbcSavepoint(connection.setSavepoint());
		}
		catch (SQLException e)
		{
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint)
	{
		try
		{
			connectionDialect.releaseSavepoint(((JdbcSavepoint) savepoint).getSavepoint(), connection);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void rollback(ISavepoint savepoint)
	{
		try
		{
			connection.rollback(((JdbcSavepoint) savepoint).getSavepoint());
		}
		catch (SQLException e)
		{
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public IList<String> disableConstraints()
	{
		return connectionDialect.disableConstraints(connection);
	}

	@Override
	public void enableConstraints(IList<String> disabled)
	{
		connectionDialect.enableConstraints(connection, disabled);
	}
}
