package de.osthus.ambeth.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sql.IPrimaryKeyProvider;
import de.osthus.ambeth.sql.ISqlConnection;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class H2SequencePrimaryKeyProvider implements IInitializingBean, IPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IThreadLocalObjectCollector objectCollector;

	protected ISqlConnection sqlConnection;

	protected IConversionHelper conversionHelper;

	protected Connection connection;

	protected int prefetchIdAmount;

	protected final HashMap<String, ArrayList<Object>> seqToCachedIdsMap = new HashMap<String, ArrayList<Object>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(sqlConnection, "sqlConnection");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setSqlConnection(ISqlConnection sqlConnection)
	{
		this.sqlConnection = sqlConnection;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	@Property(name = PersistenceConfigurationConstants.SequencePrefetchSize, defaultValue = "200")
	public void setPrefetchIdAmount(int prefetchIdAmount)
	{
		this.prefetchIdAmount = prefetchIdAmount;
	}

	@Override
	public IList<Object> acquireIds(ITable table, int count)
	{
		if (count == 0)
		{
			return EmptyList.getInstance();
		}
		String sequenceName = table.getSequenceName();

		ArrayList<Object> ids = new ArrayList<Object>(count);
		int prefetchIdAmount = this.prefetchIdAmount;

		Lock writeLock = this.writeLock;
		ArrayList<Object> cachedIds = null;
		if (prefetchIdAmount > 0)
		{
			writeLock.lock();
			try
			{
				cachedIds = seqToCachedIdsMap.get(sequenceName);
				if (cachedIds == null)
				{
					cachedIds = new ArrayList<Object>(prefetchIdAmount + count);
					seqToCachedIdsMap.put(sequenceName, cachedIds);
				}
				while (count > 0 && cachedIds.size() >= count)
				{
					Object cachedId = cachedIds.remove(cachedIds.size() - 1);
					ids.add(cachedId);
					count--;
				}
			}
			finally
			{
				writeLock.unlock();
			}
			if (count == 0)
			{
				// ids could be fully satisfied by the cache
				return ids;
			}
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		Class<?> idType = table.getIdField().getMember().getRealType();
		ArrayList<Object> cacheableIds = cachedIds != null ? new ArrayList<Object>(prefetchIdAmount) : null;
		String sql = StringBuilderUtil.concat(objectCollector.getCurrent(), "SELECT ", sequenceName, ".nextval FROM DUAL CONNECT BY level<=?");
		PreparedStatement pstm = null;
		try
		{
			pstm = connection.prepareStatement(sql);
			pstm.setInt(1, count + prefetchIdAmount); // Make sure after the request are still enough ids cached
			ResultSet rs = pstm.executeQuery();
			while (rs.next())
			{
				Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to retrieve it by the unique id
				id = conversionHelper.convertValueToType(idType, id);
				if (count > 0)
				{
					count--;
					ids.add(id);
				}
				else
				{
					cacheableIds.add(id);
				}
			}
			JdbcUtil.close(rs);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(pstm);
			pstm = null;
		}
		if (cacheableIds != null)
		{
			// put ids to cache for later requests
			writeLock.lock();
			try
			{
				cachedIds.addAll(cacheableIds);
			}
			finally
			{
				writeLock.unlock();
			}
		}
		return ids;
	}
}
