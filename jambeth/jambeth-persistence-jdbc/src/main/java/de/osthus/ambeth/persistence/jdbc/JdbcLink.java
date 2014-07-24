package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.sql.SqlLink;

public class JdbcLink extends SqlLink
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected Connection connection;

	protected ILinkedMap<String, PreparedStatement> namesToPstmMap;

	@Override
	public void startBatch()
	{
		if (namesToPstmMap != null)
		{
			throw new IllegalStateException("Must never happen");
		}
		namesToPstmMap = new LinkedHashMap<String, PreparedStatement>();
		super.startBatch();
	}

	@Override
	public int[] finishBatch()
	{
		for (Entry<String, PreparedStatement> entry : namesToPstmMap)
		{
			PreparedStatement pstm = entry.getValue();
			try
			{
				pstm.executeBatch();
			}
			catch (SQLException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return super.finishBatch();
	}

	@Override
	public void clearBatch()
	{
		for (Entry<String, PreparedStatement> entry : namesToPstmMap)
		{
			PreparedStatement pstm = entry.getValue();
			try
			{
				pstm.close();
			}
			catch (SQLException e)
			{
				// Intended blank
			}
		}
		namesToPstmMap = null;
		super.clearBatch();
	}

	@Override
	protected void linkIdsIntern(String names, Object fromId, Class<?> toIdType, List<Object> toIds)
	{
		try
		{
			PreparedStatement pstm = namesToPstmMap.get(names);
			if (pstm == null)
			{
				IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				try
				{
					sb.append("INSERT INTO ");
					sqlBuilder.appendName(getName(), sb);
					sb.append(" (").append(names).append(") VALUES (?,?)");
					pstm = connection.prepareStatement(sb.toString());
					namesToPstmMap.put(names, pstm);
				}
				finally
				{
					tlObjectCollector.dispose(sb);
				}
			}
			pstm.setObject(1, fromId);
			for (int a = 0, size = toIds.size(); a < size; a++)
			{
				pstm.setObject(2, toIds.get(a));
				pstm.addBatch();
			}
			pstm.clearParameters();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void unlinkIdsIntern(String whereSQL, Class<?> toIdType, IMap<Integer, Object> params)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		try
		{
			PreparedStatement pstm = namesToPstmMap.get(whereSQL);
			if (pstm == null)
			{
				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				try
				{
					sb.append("DELETE FROM ");
					sqlBuilder.appendName(getName(), sb);
					sb.append(" WHERE ").append(whereSQL);

					pstm = connection.prepareStatement(sb.toString());
					namesToPstmMap.put(whereSQL, pstm);
				}
				finally
				{
					tlObjectCollector.dispose(sb);
				}
			}
			for (Entry<Integer, Object> entry : params)
			{
				pstm.setObject(entry.getKey(), entry.getValue());
			}
			pstm.addBatch();
			pstm.clearParameters();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
